package it.mobileflow.mfcovaxt.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.mobileflow.mfcovaxt.database.VaxInjectionsStatsDatabase
import it.mobileflow.mfcovaxt.entity.*

class VaxDataViewModel(private val db : VaxInjectionsStatsDatabase) : ViewModel() {
    enum class VaxDataType {
        LAST_UPDATE_DATASETS,
        PARTS_OF_VAXABLE_POPULATION,
        PHYSICAL_INJECTION_LOCATIONS,
        VAX_DELIVERIES,
        VAX_INJECTIONS,
        VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE,
        VAX_INJECTIONS_SUMMARIES_BY_DAY_AND_AREA,
        VAX_STATS_SUMMARIES_BY_AREA
    }

    private val urls = mapOf(
        VaxDataType.LAST_UPDATE_DATASETS to
                "https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/last-update-dataset.json",
        VaxDataType.PARTS_OF_VAXABLE_POPULATION to
                "https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/platea.csv",
        VaxDataType.PHYSICAL_INJECTION_LOCATIONS to
                "https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/punti-somministrazione-tipologia.csv",
        VaxDataType.VAX_DELIVERIES to
                "https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/consegne-vaccini-latest.csv",
        VaxDataType.VAX_INJECTIONS to
                "https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/somministrazioni-vaccini-latest.csv",
        VaxDataType.VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE to
                "https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/anagrafica-vaccini-summary-latest.csv",
        VaxDataType.VAX_INJECTIONS_SUMMARIES_BY_DAY_AND_AREA to
                "https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/somministrazioni-vaccini-summary-latest.csv",
        VaxDataType.VAX_STATS_SUMMARIES_BY_AREA to
                "https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/vaccini-summary-latest.csv"
    )

    val lastUpdateDatasets = MutableLiveData<Array<LastUpdateDataset>>()
    val partsOfVaxablePopulation = MutableLiveData<Array<PartOfVaxablePopulation>>()
    val physicalInjectionLocations = MutableLiveData<Array<PhysicalInjectionLocation>>()
    val vaxDeliveries = MutableLiveData<Array<VaxDelivery>>()
    val vaxInjections = MutableLiveData<Array<VaxInjection>>()
    val vaxInjectionsSummariesByAgeRange =
        MutableLiveData<Array<VaxInjectionsSummaryByAgeRange>>()
    val vaxInjectionsSummariesByDayAndArea =
        MutableLiveData<Array<VaxInjectionsSummaryByDayAndArea>>()
    val vaxStatsSummariesByArea = MutableLiveData<Array<VaxStatsSummaryByArea>>()

    fun updateData() {

    }
}