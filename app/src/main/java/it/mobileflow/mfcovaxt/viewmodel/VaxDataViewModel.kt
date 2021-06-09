package it.mobileflow.mfcovaxt.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import it.mobileflow.mfcovaxt.database.VaxInjectionsStatsDatabase
import it.mobileflow.mfcovaxt.entity.*
import it.mobileflow.mfcovaxt.http.CsvRequest
import it.mobileflow.mfcovaxt.http.Http
import it.mobileflow.mfcovaxt.listener.OnGenericListener
import it.mobileflow.mfcovaxt.util.EzDateParser
import it.mobileflow.mfcovaxt.util.EzNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.csv.CSVRecord
import java.sql.Timestamp
import java.util.*


class VaxDataViewModel(private val db: VaxInjectionsStatsDatabase) : ViewModel() {
    enum class VaxData {
        PARTS_OF_VAXABLE_POPULATION,
        PHYSICAL_INJECTION_LOCATIONS,
        VAX_DELIVERIES,
        VAX_INJECTIONS,
        VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE,
        VAX_INJECTIONS_SUMMARIES_BY_DAY_AND_AREA,
        VAX_STATS_SUMMARIES_BY_AREA
    }

    companion object {
        private val urls = mapOf(
                VaxData.PARTS_OF_VAXABLE_POPULATION to
                        "https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/platea.csv",
                VaxData.PHYSICAL_INJECTION_LOCATIONS to
                        "https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/punti-somministrazione-tipologia.csv",
                VaxData.VAX_DELIVERIES to
                        "https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/consegne-vaccini-latest.csv",
                VaxData.VAX_INJECTIONS to
                        "https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/somministrazioni-vaccini-latest.csv",
                VaxData.VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE to
                        "https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/anagrafica-vaccini-summary-latest.csv",
                VaxData.VAX_INJECTIONS_SUMMARIES_BY_DAY_AND_AREA to
                        "https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/somministrazioni-vaccini-summary-latest.csv",
                VaxData.VAX_STATS_SUMMARIES_BY_AREA to
                        "https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/vaccini-summary-latest.csv")

        private const val LAST_UPDATE_DATASET_URL =
                "https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/last-update-dataset.json"
    }

    private val shouldUpdateVaxData = mutableMapOf(
            VaxData.PARTS_OF_VAXABLE_POPULATION to false,
            VaxData.PHYSICAL_INJECTION_LOCATIONS to false,
            VaxData.VAX_DELIVERIES to false,
            VaxData.VAX_INJECTIONS to false,
            VaxData.VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE to false,
            VaxData.VAX_INJECTIONS_SUMMARIES_BY_DAY_AND_AREA to false,
            VaxData.VAX_STATS_SUMMARIES_BY_AREA to false)

    private val shouldReloadVaxData = mutableMapOf(
            VaxData.PARTS_OF_VAXABLE_POPULATION to true,
            VaxData.PHYSICAL_INJECTION_LOCATIONS to true,
            VaxData.VAX_DELIVERIES to true,
            VaxData.VAX_INJECTIONS to true,
            VaxData.VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE to true,
            VaxData.VAX_INJECTIONS_SUMMARIES_BY_DAY_AND_AREA to true,
            VaxData.VAX_STATS_SUMMARIES_BY_AREA to true)

    private var isUpdatingLastUpdateDataset = false

    val partsOfVaxablePopulation = MutableLiveData<Array<PartOfVaxablePopulation>>()
    val physicalInjectionLocations = MutableLiveData<Array<PhysicalInjectionLocation>>()
    val vaxDeliveries = MutableLiveData<Array<VaxDelivery>>()
    val vaxInjections = MutableLiveData<Array<VaxInjection>>()
    val vaxInjectionsSummariesByAgeRange =
        MutableLiveData<Array<VaxInjectionsSummaryByAgeRange>>()
    val vaxInjectionsSummariesByDayAndArea =
        MutableLiveData<Array<VaxInjectionsSummaryByDayAndArea>>()
    val vaxStatsSummariesByArea = MutableLiveData<Array<VaxStatsSummaryByArea>>()

    /**
     * (assume) called *only* from main thread
     */
    fun lastUpdateDataset(
            appContext: Context,
            doneListener: OnGenericListener<Boolean>,
            errorListener: OnGenericListener<VolleyError>) {
        if(!isUpdatingLastUpdateDataset) {
            isUpdatingLastUpdateDataset = true // other call attempt are locked out [main thread]
            Http.getInstance(appContext).addToRequestQueue(
                    JsonObjectRequest(Request.Method.GET, LAST_UPDATE_DATASET_URL, null,
                            { response ->
                                updateLastUpdateDataset(
                                        response.getString("ultimo_aggiornamento"))
                                isUpdatingLastUpdateDataset = false // unlocked from another thread
                                doneListener.onEvent(true)
                            },
                            { error ->
                                isUpdatingLastUpdateDataset = false // unlocked from another thread
                                errorListener.onEvent(error)
                            }))
        }
    }

    private fun updateLastUpdateDataset(lastUpdateIso8601: String) {
        val lastUpdate = EzDateParser.parse(lastUpdateIso8601)

        val lastUpdateDatasetDao = db.getLastUpdateDatasetDao()
        val lastUpdateDataset = lastUpdateDatasetDao.getLastUpdateDataset()

        if(lastUpdateDataset.isEmpty()) {
            lastUpdateDatasetDao.insert(
                    LastUpdateDataset(0,
                            Timestamp(lastUpdate.time)))
            shouldUpdateEveryVaxData()
        } else {
            val localLastUpdateDate = Date(lastUpdateDataset[0].lastUpdate.time)
            if (lastUpdate.after(localLastUpdateDate)) {
                lastUpdateDatasetDao.update(
                        LastUpdateDataset(0,
                                Timestamp(lastUpdate.time)))
                shouldUpdateEveryVaxData()
            }
        }
    }

    private fun shouldUpdateEveryVaxData() {
        for(key in shouldUpdateVaxData.keys) {
            shouldUpdateVaxData[key] = true // potentially unlock populateVaxData
        }
    }

    /**
     * (assume) called *only* from main thread
     */
    fun populateVaxData(
            vaxData: VaxData,
            appContext: Context,
            errorListener: OnGenericListener<VolleyError>
    ) {
        if(EzNetwork.connected(appContext) && !isUpdatingLastUpdateDataset &&
                shouldUpdateVaxData[vaxData]!!) {
            shouldUpdateVaxData[vaxData] = false // immediate locking from [main] thread
            Http.getInstance(appContext).addToRequestQueue(CsvRequest(urls[vaxData],
                    { response -> updateVaxData(vaxData, response, appContext) },
                    { error ->
                        errorListener.onEvent(error)
                        shouldUpdateVaxData[vaxData] = true // unlocking from another thread
                    }))
        } else if(shouldReloadVaxData[vaxData]!!) {
            shouldReloadVaxData[vaxData] = false // immediate locking from [main] thread
            viewModelScope.launch(Dispatchers.IO) {
                loadVaxDataFromLocalDb(vaxData)
            }
        }
    }

    /**
     * UPDATE VAX DATA FROM EXTERNAL SOURCE
     */
    private fun updateVaxData(vaxData: VaxData, resp: List<CSVRecord>, context: Context) {
        when (vaxData) {
            VaxData.PARTS_OF_VAXABLE_POPULATION ->
                updatePartsOfVaxablePopulation(resp)
            VaxData.PHYSICAL_INJECTION_LOCATIONS ->
                updatePhysicalInjectionLocations(resp)
            VaxData.VAX_DELIVERIES ->
                updateVaxDeliveries(resp, context)
            VaxData.VAX_INJECTIONS ->
                updateVaxInjections(resp, context)
            VaxData.VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE ->
                updateVaxInjectionSummariesByAgeRange(resp)
            VaxData.VAX_INJECTIONS_SUMMARIES_BY_DAY_AND_AREA ->
                updateVaxInjectionSummariesByDayAndArea(resp, context)
            VaxData.VAX_STATS_SUMMARIES_BY_AREA ->
                updateVaxStatsSummariesByArea(resp)
        }

        //will not load data if connection disappears,
        // force-loading after update from external source
        shouldReloadVaxData[vaxData] = false
        loadVaxDataFromLocalDb(vaxData)
    }

    private fun updateVaxStatsSummariesByArea(resp: List<CSVRecord>) {
        db.getVaxStatsSummaryByAreaDao().apply {
            deleteTable()
            for(i in 1 until resp.size) {
                insert(VaxStatsSummaryByArea(
                        resp[i].get(0),
                        Integer.parseInt(resp[i].get(1)),
                        Integer.parseInt(resp[i].get(2)),
                        Integer.parseInt(resp[i].get(3)),
                        resp[i].get(5),
                        resp[i].get(6),
                        Integer.parseInt(resp[i].get(7)),
                        resp[i].get(8)))
            }
        }
    }

    private fun updateVaxInjectionSummariesByDayAndArea(resp: List<CSVRecord>, context: Context) {
        db.getVaxInjectionsSummaryByDayAndAreaDao().apply {
            deleteTable()
            for(i in 1 until resp.size) {
                insert(VaxInjectionsSummaryByDayAndArea(
                        resp[i].get(1),
                        Timestamp(EzDateParser.parseDateOnlyGetTime(resp[i].get(0), context)),
                        Integer.parseInt(resp[i].get(2)),
                        Integer.parseInt(resp[i].get(3)),
                        Integer.parseInt(resp[i].get(4)),
                        Integer.parseInt(resp[i].get(5)),
                        Integer.parseInt(resp[i].get(7)),
                        resp[i].get(8),
                        resp[i].get(9),
                        Integer.parseInt(resp[i].get(10)),
                        resp[i].get(11)))
            }
        }
    }

    private fun updateVaxInjectionSummariesByAgeRange(resp: List<CSVRecord>) {
        db.getVaxInjectionsSummaryByAgeRangeDao().apply {
            deleteTable()
            for(i in 1 until resp.size) {
                insert(VaxInjectionsSummaryByAgeRange(
                        resp[i].get(0),
                        Integer.parseInt(resp[i].get(1)),
                        Integer.parseInt(resp[i].get(2)),
                        Integer.parseInt(resp[i].get(3)),
                        Integer.parseInt(resp[i].get(4)),
                        Integer.parseInt(resp[i].get(5))))
            }
        }
    }

    private fun updateVaxInjections(resp: List<CSVRecord>, context: Context) {
        db.getVaxInjectionDao().apply {
            deleteTable()
            for(i in 1 until resp.size) {
                insert(VaxInjection(
                        resp[i].get(2),
                        resp[i].get(1),
                        Timestamp(EzDateParser.parseDateOnlyGetTime(resp[i].get(0), context)),
                        resp[i].get(3),
                        Integer.parseInt(resp[i].get(4)),
                        Integer.parseInt(resp[i].get(5)),
                        Integer.parseInt(resp[i].get(6)),
                        Integer.parseInt(resp[i].get(7)),
                        resp[i].get(8),
                        resp[i].get(9),
                        Integer.parseInt(resp[i].get(10)),
                        resp[i].get(11)))
            }
        }
    }

    private fun updateVaxDeliveries(resp: List<CSVRecord>, context: Context) {
        db.getVaxDeliveryDao().apply {
            deleteTable()
            for (i in 1 until resp.size) {
                insert(VaxDelivery(
                        resp[i].get(0),
                        resp[i].get(1),
                        Timestamp(EzDateParser.parseDateOnlyGetTime(resp[i].get(3), context)),
                        Integer.parseInt(resp[i].get(2)),
                        resp[i].get(4),
                        resp[i].get(5),
                        Integer.parseInt(resp[i].get(6)),
                        resp[i].get(7)))
            }
        }
    }

    private fun updatePhysicalInjectionLocations(resp: List<CSVRecord>) {
        db.getPhysicalInjectionLocationDao().apply {
            deleteTable()
            for (i in 1 until resp.size) {
                insert(PhysicalInjectionLocation(
                        resp[i].get(0),
                        resp[i].get(1),
                        resp[i].get(2),
                        resp[i].get(3),
                        resp[i].get(4),
                        Integer.parseInt(resp[i].get(5)),
                        resp[i].get(6)))
            }
        }
    }

    private fun updatePartsOfVaxablePopulation(resp: List<CSVRecord>) {
        db.getPartOfVaxablePopulationDao().apply {
            deleteTable()
            for (i in 1 until resp.size) {
                insert(PartOfVaxablePopulation(
                        resp[i].get(0),
                        resp[i].get(1),
                        resp[i].get(2),
                        Integer.parseInt(resp[i].get(3))))
            }
        }
    }

    /**
     * LOAD VAX DATA FROM LOCAL SOURCE
     */
    private fun loadVaxDataFromLocalDb(vaxData: VaxData) {
        when (vaxData) {
            VaxData.PARTS_OF_VAXABLE_POPULATION ->
                partsOfVaxablePopulation.value =
                        db.getPartOfVaxablePopulationDao().getPartsOfVaxablePopulation()
            VaxData.PHYSICAL_INJECTION_LOCATIONS ->
                physicalInjectionLocations.value =
                        db.getPhysicalInjectionLocationDao().getPhysicalInjectionLocations()
            VaxData.VAX_DELIVERIES ->
                vaxDeliveries.value = db.getVaxDeliveryDao().getVaxDeliveries()
            VaxData.VAX_INJECTIONS ->
                vaxInjections.value = db.getVaxInjectionDao().getVaxInjections()
            VaxData.VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE ->
                vaxInjectionsSummariesByAgeRange.value =
                        db.getVaxInjectionsSummaryByAgeRangeDao()
                                .getVaxInjectionsSummariesByAgeRange()
            VaxData.VAX_INJECTIONS_SUMMARIES_BY_DAY_AND_AREA ->
                vaxInjectionsSummariesByDayAndArea.value =
                        db.getVaxInjectionsSummaryByDayAndAreaDao()
                                .getVaxInjectionsSummariesByDayAndArea()
            VaxData.VAX_STATS_SUMMARIES_BY_AREA ->
                vaxStatsSummariesByArea.value =
                        db.getVaxStatsSummaryByAreaDao().getVaxStatsSummariesByArea()
        }
    }
}