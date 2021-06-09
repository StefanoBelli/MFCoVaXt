package it.mobileflow.mfcovaxt.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
            context: Context,
            doneListener: OnGenericListener<Boolean>,
            errorListener: OnGenericListener<VolleyError>) {
        if(!isUpdatingLastUpdateDataset) {
            isUpdatingLastUpdateDataset = true // other call attempt are locked out [main thread]
            Http.getInstance(context).addToRequestQueue(
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
            context: Context,
            errorListener: OnGenericListener<VolleyError>
    ) {
        if(isNetworkConnected(context) && !isUpdatingLastUpdateDataset &&
                shouldUpdateVaxData[vaxData]!!) {
            shouldUpdateVaxData[vaxData] = false // immediate locking from [main] thread
            Http.getInstance(context).addToRequestQueue(CsvRequest(urls[vaxData],
                    { response -> updateVaxData(vaxData, response) },
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
    private fun updateVaxData(vaxData: VaxData, resp: List<CSVRecord>) {
        when (vaxData) {
            VaxData.PARTS_OF_VAXABLE_POPULATION ->
                updatePartsOfVaxablePopulation(resp)
            VaxData.PHYSICAL_INJECTION_LOCATIONS ->
                updatePhysicalInjectionLocations(resp)
            VaxData.VAX_DELIVERIES ->
                updateVaxDeliveries(resp)
            VaxData.VAX_INJECTIONS ->
                updateVaxInjections(resp)
            VaxData.VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE ->
                updateVaxInjectionSummariesByAgeRange(resp)
            VaxData.VAX_INJECTIONS_SUMMARIES_BY_DAY_AND_AREA ->
                updateVaxInjectionSummariesByDayAndArea(resp)
            VaxData.VAX_STATS_SUMMARIES_BY_AREA ->
                updateVaxStatsSummariesByArea(resp)
        }

        shouldReloadVaxData[vaxData] = true // unlocks call to populateVaxData

        loadVaxDataFromLocalDb(vaxData)
    }

    private fun updateVaxStatsSummariesByArea(resp: List<CSVRecord>) {

    }

    private fun updateVaxInjectionSummariesByDayAndArea(resp: List<CSVRecord>) {

    }

    private fun updateVaxInjectionSummariesByAgeRange(resp: List<CSVRecord>) {

    }

    private fun updateVaxInjections( resp: List<CSVRecord>) {

    }

    private fun updateVaxDeliveries(resp: List<CSVRecord>) {

    }

    private fun updatePhysicalInjectionLocations(resp: List<CSVRecord>) {

    }

    private fun updatePartsOfVaxablePopulation(resp: List<CSVRecord>) {

    }

    /**
     * LOAD VAX DATA FROM LOCAL SOURCE
     */
    private fun loadVaxDataFromLocalDb(vaxData: VaxData) {
        when (vaxData) {
            VaxData.PARTS_OF_VAXABLE_POPULATION ->
                loadPartsOfVaxablePopulation()
            VaxData.PHYSICAL_INJECTION_LOCATIONS ->
                loadPhysicalInjectionLocations()
            VaxData.VAX_DELIVERIES ->
                loadVaxDeliveries()
            VaxData.VAX_INJECTIONS ->
                loadVaxInjections()
            VaxData.VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE ->
                loadVaxInjectionSummariesByAgeRange()
            VaxData.VAX_INJECTIONS_SUMMARIES_BY_DAY_AND_AREA ->
                loadVaxInjectionSummariesByDayAndArea()
            VaxData.VAX_STATS_SUMMARIES_BY_AREA ->
                loadVaxStatsSummariesByArea()
        }
    }

    private fun loadVaxStatsSummariesByArea() {

    }

    private fun loadVaxInjectionSummariesByDayAndArea() {

    }

    private fun loadVaxInjectionSummariesByAgeRange() {

    }

    private fun loadVaxInjections() {

    }

    private fun loadVaxDeliveries() {

    }

    private fun loadPhysicalInjectionLocations() {

    }

    private fun loadPartsOfVaxablePopulation() {

    }

    /**
     * util function
     */
    private fun isNetworkConnected(context: Context) : Boolean {
        var result = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        cm.getNetworkCapabilities(cm.activeNetwork)?.run {
            result = hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        }

        return result
    }
}