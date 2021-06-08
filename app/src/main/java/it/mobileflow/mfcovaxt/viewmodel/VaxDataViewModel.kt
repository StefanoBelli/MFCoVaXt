package it.mobileflow.mfcovaxt.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import it.mobileflow.mfcovaxt.database.VaxInjectionsStatsDatabase
import it.mobileflow.mfcovaxt.entity.*
import it.mobileflow.mfcovaxt.http.Http
import it.mobileflow.mfcovaxt.listener.OnGenericListener
import it.mobileflow.mfcovaxt.util.EzDateParser
import org.json.JSONObject
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*

class VaxDataViewModel(private val db : VaxInjectionsStatsDatabase) : ViewModel() {
    enum class VaxData {
        PARTS_OF_VAXABLE_POPULATION,
        PHYSICAL_INJECTION_LOCATIONS,
        VAX_DELIVERIES,
        VAX_INJECTIONS,
        VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE,
        VAX_INJECTIONS_SUMMARIES_BY_DAY_AND_AREA,
        VAX_STATS_SUMMARIES_BY_AREA
    }

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
                "https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/vaccini-summary-latest.csv"
    )

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
     * algorithm:
     *  (1) fetch "latest_update_dataset.json" from remote source
     *  (2) fetch latest_update from local source
     *  (2.1) if it is not available then store it and
     *      enable this algorithm to fetch updates for any vax data
     *  (2.2) if it is available from local source, then remote.data > local.data enables this
     *        algorithm to fetch updates for any vax data, store remote.data
     *  (3) check which type_of_data user wants to fetch
     *  (4) check if local.type_of_data.latest_update is available
     *  (4.1) if available then,
     *        if local.type_of_data.latest_update < local.latest_update then
     *          fetch from remote source
     *          clear db table
     *          populate table with new records
     *          local.type_of_data.latest_update := now
     *  (4.2) otherwise,
     *          fetch from remote source
     *          clear db table
     *          populate table with new records
     *  (5) TERMINATE ALGORITHM BY UPDATING MutableLiveData, any Observer attached will be notified
     */
    fun updateData(vaxData: VaxData, context: Context, errorListener: OnGenericListener<VolleyError>) {
        Http.getInstance(context).addToRequestQueue(
                JsonObjectRequest(Request.Method.GET, urls[vaxData], null,
                        { response -> actualUpdateData(vaxData, response) },
                        { error -> errorListener.onEvent(error) }))
    }

    private fun actualUpdateData(vaxData: VaxData, remoteLatestUpdate: JSONObject) {
        val remoteLastUpdateDate =
            EzDateParser.parse(remoteLatestUpdate.getString("ultimo_aggiornamento"))

        val lastUpdateDatasetDao = db.getLastUpdateDatasetDao()
        val lastUpdateDataset = lastUpdateDatasetDao.getLastUpdateDataset()
        var mustFetchFromRemote = false

        if(lastUpdateDataset.isEmpty()) {
            lastUpdateDatasetDao.insert(
                    LastUpdateDataset(0,
                            Timestamp(remoteLastUpdateDate.time)))
            mustFetchFromRemote = true
        } else {
            val localLastUpdateDate = Date(lastUpdateDataset[0].lastUpdate.time)
            if(remoteLastUpdateDate.after(localLastUpdateDate)) {
                lastUpdateDatasetDao.update(
                        LastUpdateDataset(0,
                                Timestamp(remoteLastUpdateDate.time)))
                mustFetchFromRemote = true
            }
        }

        if(!mustFetchFromRemote &&
                queryVaxDataAppLastUpdate(vaxData).before(remoteLastUpdateDate)) {
            mustFetchFromRemote = true
        }

        fetchData(vaxData, mustFetchFromRemote)
    }

    private fun fetchData(vaxData: VaxData, fetchRemote: Boolean) {
        if(fetchRemote) {

        } else {

        }
    }

    private fun queryVaxDataAppLastUpdate(vaxData : VaxData) : Date {
        val appOwnLastUpdate = db.getAppOwnLastUpdateDao().getAppOwnLastUpdate()
        if(appOwnLastUpdate.isEmpty()) {
            return Date(0)
        }

        val ts = when(vaxData) {
            VaxData.PARTS_OF_VAXABLE_POPULATION ->
                appOwnLastUpdate[0].partOfVaxablePopulation
            VaxData.PHYSICAL_INJECTION_LOCATIONS ->
                appOwnLastUpdate[0].physicalInjectionLocation
            VaxData.VAX_DELIVERIES ->
                appOwnLastUpdate[0].vaxDelivery
            VaxData.VAX_INJECTIONS ->
                appOwnLastUpdate[0].vaxInjection
            VaxData.VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE ->
                appOwnLastUpdate[0].vaxInjectionsSummaryByAgeRange
            VaxData.VAX_INJECTIONS_SUMMARIES_BY_DAY_AND_AREA ->
                appOwnLastUpdate[0].vaxInjectionsSummaryByDayAndArea
            VaxData.VAX_STATS_SUMMARIES_BY_AREA ->
                appOwnLastUpdate[0].vaxStatsSummaryByArea
        }

        return Date(ts.time)
    }

    private fun updateOrInsertVaxDataAppLastUpdate(vaxData: VaxData, datetime: Timestamp) {
        val dao = db.getAppOwnLastUpdateDao()
        val appOwnLastUpdates = dao.getAppOwnLastUpdate()
        if(appOwnLastUpdates.isEmpty()) {
            dao.insert(assignAppOwnLastUpdate(vaxData, datetime, buildEmptyAppOwnLastUpdate()))
        } else {
            dao.update(assignAppOwnLastUpdate(vaxData, datetime, appOwnLastUpdates[0]))
        }
    }

    private fun assignAppOwnLastUpdate(
            vaxData: VaxData,
            datetime: Timestamp,
            o: AppOwnLastUpdate)
    : AppOwnLastUpdate{

        when(vaxData) {
            VaxData.PARTS_OF_VAXABLE_POPULATION ->
                o.partOfVaxablePopulation = datetime
            VaxData.PHYSICAL_INJECTION_LOCATIONS ->
                o.physicalInjectionLocation = datetime
            VaxData.VAX_DELIVERIES ->
                o.vaxDelivery = datetime
            VaxData.VAX_INJECTIONS ->
                o.vaxInjection = datetime
            VaxData.VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE ->
                o.vaxInjectionsSummaryByAgeRange = datetime
            VaxData.VAX_INJECTIONS_SUMMARIES_BY_DAY_AND_AREA ->
                o.vaxInjectionsSummaryByDayAndArea = datetime
            VaxData.VAX_STATS_SUMMARIES_BY_AREA ->
                o.vaxStatsSummaryByArea = datetime
        }

        return o
    }

    private fun buildEmptyAppOwnLastUpdate() : AppOwnLastUpdate {
        return AppOwnLastUpdate(
                0, Timestamp(0), Timestamp(0),Timestamp(0),
                Timestamp(0), Timestamp(0), Timestamp(0), Timestamp(0))
    }
}