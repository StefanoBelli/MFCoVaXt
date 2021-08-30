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
import it.mobileflow.mfcovaxt.util.EzAppDataUpdateTracker
import it.mobileflow.mfcovaxt.util.EzDateParser
import it.mobileflow.mfcovaxt.util.EzNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.csv.CSVRecord
import java.util.*


class VaxDataViewModel : ViewModel() {
    enum class VaxData {
        PARTS_OF_VAXABLE_POPULATION,
        PHYSICAL_INJECTION_LOCATIONS,
        VAX_DELIVERIES,
        VAX_INJECTIONS,
        VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE,
        VAX_INJECTIONS_SUMMARIES_BY_DAY_AND_AREA,
        VAX_STATS_SUMMARIES_BY_AREA
    }

    enum class LudError {
        OK,
        UPDATE_IN_PROGRESS,
        NO_CONNECTIVITY
    }

    lateinit var db : VaxInjectionsStatsDatabase

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

    val lastUpdateDatasetDate = MutableLiveData<Date>()
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
            doneListener: ((Boolean, Boolean)->Unit),
            errorListener: ((VolleyError)->Unit)
    ) : LudError {
        if(!isUpdatingLastUpdateDataset && EzNetwork.connected(appContext)) {
            isUpdatingLastUpdateDataset = true // other call attempt are locked out [main thread]
            Http.getInstance(appContext).addToRequestQueue(
                    JsonObjectRequest(Request.Method.GET, LAST_UPDATE_DATASET_URL, null,
                            { response ->
                                viewModelScope.launch(Dispatchers.Default) {
                                    updateLastUpdateDataset(
                                        response.getString("ultimo_aggiornamento"),
                                        doneListener, appContext)
                                }
                            },
                            { error ->
                                isUpdatingLastUpdateDataset = false // unlocked from another thread
                                errorListener.invoke(error)
                            }))

            return LudError.OK
        }

        return if(isUpdatingLastUpdateDataset)
            LudError.UPDATE_IN_PROGRESS
        else {
            viewModelScope.launch(Dispatchers.IO) {
                val lud = db.getLastUpdateDatasetDao().getLastUpdateDataset()
                if(lud.size == 1) {
                    withContext(Dispatchers.Main) {
                        lastUpdateDatasetDate.value = lud[0].lastUpdate
                    }
                }
            }
            LudError.NO_CONNECTIVITY
        }
    }

    private suspend fun updateLastUpdateDataset(
        lastUpdateIso8601: String,
        doneListener: ((Boolean, Boolean)->Unit),
        appContext: Context
    ) {
        val lastUpdate = EzDateParser.parseIso8601TzUTC(lastUpdateIso8601, appContext)

        val lastUpdateDatasetDao = db.getLastUpdateDatasetDao()
        var lastUpdateDataset : Array<LastUpdateDataset>

        withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
            lastUpdateDataset = lastUpdateDatasetDao.getLastUpdateDataset()
        }

        var lsuSync = true
        var dataSync = true
        if(lastUpdateDataset.isEmpty()) {
            lastUpdateDatasetDao.insert(LastUpdateDataset(0, lastUpdate))
            shouldUpdateEveryVaxData()
            lsuSync = false
            dataSync = false
        } else {
            val localLastUpdateDate = lastUpdateDataset[0].lastUpdate
            if (lastUpdate.after(localLastUpdateDate)) {
                lastUpdateDatasetDao.update(LastUpdateDataset(0, lastUpdate))
                shouldUpdateEveryVaxData()
                lsuSync = false
                dataSync = false
            } else {
                for(key in shouldUpdateVaxData.keys) {
                    if(EzAppDataUpdateTracker
                                    .getLastUpdate(key, appContext).before(localLastUpdateDate)) {
                        shouldUpdateVaxData[key] = true // potentially unlock populateVaxData
                        dataSync = false
                    }
                }
            }
        }

        withContext(Dispatchers.Main) {
            lastUpdateDatasetDate.value = lastUpdate
        }

        isUpdatingLastUpdateDataset = false // unlocked from another thread
        withContext(Dispatchers.Main) {
            doneListener.invoke(lsuSync, dataSync)
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
            errorListener: ((VolleyError)->Unit)
    ) {
        if(shouldUpdateVaxData[vaxData]!! && EzNetwork.connected(appContext) &&
                !isUpdatingLastUpdateDataset) {
            shouldUpdateVaxData[vaxData] = false // immediate locking from [main] thread
            Http.getInstance(appContext).addToRequestQueue(CsvRequest(urls[vaxData],
                    { response ->
                        viewModelScope.launch(Dispatchers.Default) {
                            updateVaxData(vaxData, response, appContext)
                        }
                    },
                    { error ->
                        errorListener.invoke(error)
                        shouldUpdateVaxData[vaxData] = true // unlocking from another thread
                    }))
        } else if(shouldReloadVaxData[vaxData]!! && !shouldUpdateVaxData[vaxData]!!) {
            shouldReloadVaxData[vaxData] = false // immediate locking from [main] thread
            viewModelScope.launch(Dispatchers.Default) {
                loadVaxDataFromLocalDb(vaxData)
            }
        }
    }

    /**
     * UPDATE VAX DATA FROM EXTERNAL SOURCE
     */
    private suspend fun updateVaxData(vaxData: VaxData, resp: List<CSVRecord>, context: Context) {
        when (vaxData) {
            VaxData.PARTS_OF_VAXABLE_POPULATION ->
                updatePartsOfVaxablePopulation(resp, context)
            VaxData.PHYSICAL_INJECTION_LOCATIONS ->
                updatePhysicalInjectionLocations(resp, context)
            VaxData.VAX_DELIVERIES ->
                updateVaxDeliveries(resp, context)
            VaxData.VAX_INJECTIONS ->
                updateVaxInjections(resp, context)
            VaxData.VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE ->
                updateVaxInjectionSummariesByAgeRange(resp, context)
            VaxData.VAX_INJECTIONS_SUMMARIES_BY_DAY_AND_AREA ->
                updateVaxInjectionSummariesByDayAndArea(resp, context)
            VaxData.VAX_STATS_SUMMARIES_BY_AREA ->
                updateVaxStatsSummariesByArea(resp, context)
        }

        //will not load data if connection disappears,
        // force-loading after update from external source
        shouldReloadVaxData[vaxData] = false
        loadVaxDataFromLocalDb(vaxData)
    }

    private suspend fun updateVaxStatsSummariesByArea(resp: List<CSVRecord>, context: Context) {
        db.getVaxStatsSummaryByAreaDao().apply {
            withContext(Dispatchers.IO) {
                db.runInTransaction {
                    deleteTable()
                    for (i in 1 until resp.size) {
                        insert(
                                VaxStatsSummaryByArea(
                                        resp[i].get(0),
                                        Integer.parseInt(resp[i].get(1)),
                                        Integer.parseInt(resp[i].get(2)),
                                        resp[i].get(3).toFloat(),
                                        resp[i].get(5),
                                        resp[i].get(6),
                                        Integer.parseInt(resp[i].get(7)),
                                        resp[i].get(8)
                                )
                        )
                    }
                }
            }
        }

        EzAppDataUpdateTracker.putLastUpdate(VaxData.VAX_STATS_SUMMARIES_BY_AREA, context)
    }

    private suspend fun updateVaxInjectionSummariesByDayAndArea(
            resp: List<CSVRecord>,
            context: Context
    ) {
        db.getVaxInjectionsSummaryByDayAndAreaDao().apply {
            withContext(Dispatchers.IO) {
                db.runInTransaction {
                    deleteTable()
                    for (i in 1 until resp.size) {
                        insert(
                                VaxInjectionsSummaryByDayAndArea(
                                        resp[i].get(1),
                                        EzDateParser.parseDateOnly(resp[i].get(0), context),
                                        Integer.parseInt(resp[i].get(2)),
                                        Integer.parseInt(resp[i].get(3)),
                                        Integer.parseInt(resp[i].get(4)),
                                        Integer.parseInt(resp[i].get(5)),
                                        Integer.parseInt(resp[i].get(6)),
                                        resp[i].get(7),
                                        resp[i].get(8),
                                        Integer.parseInt(resp[i].get(9)),
                                        resp[i].get(10)
                                )
                        )
                    }
                }
            }
        }

        EzAppDataUpdateTracker
                .putLastUpdate(VaxData.VAX_INJECTIONS_SUMMARIES_BY_DAY_AND_AREA, context)
    }

    private suspend fun updateVaxInjectionSummariesByAgeRange(
            resp: List<CSVRecord>,
            context: Context
    ) {
        db.getVaxInjectionsSummaryByAgeRangeDao().apply {
            withContext(Dispatchers.IO) {
                db.runInTransaction {
                    deleteTable()
                    for (i in 1 until resp.size) {
                        insert(
                                VaxInjectionsSummaryByAgeRange(
                                        resp[i].get(0),
                                        Integer.parseInt(resp[i].get(1)),
                                        Integer.parseInt(resp[i].get(2)),
                                        Integer.parseInt(resp[i].get(3)),
                                        Integer.parseInt(resp[i].get(4)),
                                        Integer.parseInt(resp[i].get(5))
                                )
                        )
                    }
                }
            }
        }

        EzAppDataUpdateTracker.putLastUpdate(VaxData.VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE, context)
    }

    private suspend fun updateVaxInjections(resp: List<CSVRecord>, context: Context) {
        db.getVaxInjectionDao().apply {
            withContext(Dispatchers.IO) {
                db.runInTransaction {
                    deleteTable()
                    for (i in 1 until resp.size) {
                        insert(
                                VaxInjection(
                                        0,
                                        resp[i].get(2),
                                        resp[i].get(1),
                                        EzDateParser.parseDateOnly(resp[i].get(0), context),
                                        resp[i].get(3),
                                        Integer.parseInt(resp[i].get(4)),
                                        Integer.parseInt(resp[i].get(5)),
                                        Integer.parseInt(resp[i].get(6)),
                                        Integer.parseInt(resp[i].get(7)),
                                        Integer.parseInt(resp[i].get(8)),
                                        resp[i].get(9),
                                        resp[i].get(10),
                                        Integer.parseInt(resp[i].get(11)),
                                        resp[i].get(12)
                                )
                        )
                    }
                }
            }
        }

        EzAppDataUpdateTracker.putLastUpdate(VaxData.VAX_INJECTIONS, context)
    }


    private suspend fun updateVaxDeliveries(resp: List<CSVRecord>, context: Context) {
        db.getVaxDeliveryDao().apply {
            withContext(Dispatchers.IO) {
                db.runInTransaction {
                    deleteTable()
                    for (i in 1 until resp.size) {
                        insert(
                                VaxDelivery(
                                        0,
                                        resp[i].get(0),
                                        resp[i].get(1),
                                        EzDateParser.parseDateOnly(resp[i].get(3), context),
                                        Integer.parseInt(resp[i].get(2)),
                                        resp[i].get(4),
                                        resp[i].get(5),
                                        Integer.parseInt(resp[i].get(6)),
                                        resp[i].get(7)
                                )
                        )
                    }
                }
            }
        }

        EzAppDataUpdateTracker.putLastUpdate(VaxData.VAX_DELIVERIES, context)
    }

    private suspend fun updatePhysicalInjectionLocations(resp: List<CSVRecord>, context: Context) {
        db.getPhysicalInjectionLocationDao().apply {
            withContext(Dispatchers.IO) {
                db.runInTransaction {
                    deleteTable()
                    for (i in 1 until resp.size) {
                        insert(
                                PhysicalInjectionLocation(0,
                                        resp[i].get(0),
                                        resp[i].get(1),
                                        resp[i].get(2),
                                        resp[i].get(3),
                                        resp[i].get(4),
                                        Integer.parseInt(resp[i].get(5)),
                                        resp[i].get(6)
                                )
                        )
                    }
                }
            }
        }

        EzAppDataUpdateTracker.putLastUpdate(VaxData.PHYSICAL_INJECTION_LOCATIONS, context)
    }

    private suspend fun updatePartsOfVaxablePopulation(resp: List<CSVRecord>, context: Context) {
        db.getPartOfVaxablePopulationDao().apply {
            withContext(Dispatchers.IO) {
                db.runInTransaction {
                    deleteTable()
                    for (i in 1 until resp.size) {
                        insert(
                                PartOfVaxablePopulation(
                                        resp[i].get(0),
                                        resp[i].get(1),
                                        resp[i].get(2),
                                        Integer.parseInt(resp[i].get(3))
                                )
                        )
                    }
                }
            }
        }

        EzAppDataUpdateTracker.putLastUpdate(VaxData.PARTS_OF_VAXABLE_POPULATION, context)
    }

    /**
     * LOAD VAX DATA FROM LOCAL SOURCE
     */
    private suspend fun loadVaxDataFromLocalDb(vaxData: VaxData) {
        when (vaxData) {
            VaxData.PARTS_OF_VAXABLE_POPULATION ->
                withContext(Dispatchers.IO) {
                    val v = db.getPartOfVaxablePopulationDao().getPartsOfVaxablePopulation()
                    withContext(Dispatchers.Main) {
                        partsOfVaxablePopulation.value = v
                    }
                }
            VaxData.PHYSICAL_INJECTION_LOCATIONS ->
                withContext(Dispatchers.IO) {
                    val v = db.getPhysicalInjectionLocationDao().getPhysicalInjectionLocations()
                    withContext(Dispatchers.Main) {
                        physicalInjectionLocations.value = v
                    }
                }
            VaxData.VAX_DELIVERIES ->
                withContext(Dispatchers.IO) {
                    val v = db.getVaxDeliveryDao().getVaxDeliveries()
                    withContext(Dispatchers.Main) {
                        vaxDeliveries.value = v
                    }
                }
            VaxData.VAX_INJECTIONS ->
                withContext(Dispatchers.IO) {
                    val v = db.getVaxInjectionDao().getVaxInjections()
                    withContext(Dispatchers.Main) {
                        vaxInjections.value = v
                    }
                }
            VaxData.VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE ->
                withContext(Dispatchers.IO) {
                    val v = db.getVaxInjectionsSummaryByAgeRangeDao()
                        .getVaxInjectionsSummariesByAgeRange()
                    withContext(Dispatchers.Main) {
                        vaxInjectionsSummariesByAgeRange.value = v
                    }
                }
            VaxData.VAX_INJECTIONS_SUMMARIES_BY_DAY_AND_AREA ->
                withContext(Dispatchers.IO) {
                    val v = db.getVaxInjectionsSummaryByDayAndAreaDao()
                        .getVaxInjectionsSummariesByDayAndArea()
                    withContext(Dispatchers.Main) {
                        vaxInjectionsSummariesByDayAndArea.value = v
                    }
                }
            VaxData.VAX_STATS_SUMMARIES_BY_AREA ->
                withContext(Dispatchers.IO) {
                    val v = db.getVaxStatsSummaryByAreaDao().getVaxStatsSummariesByArea()
                    withContext(Dispatchers.Main) {
                        vaxStatsSummariesByArea.value = v
                    }
                }
        }
    }
}