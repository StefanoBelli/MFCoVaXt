package it.mobileflow.mfcovaxt.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.mobileflow.mfcovaxt.database.VaxInjectionsStatsDatabase
import it.mobileflow.mfcovaxt.entity.*

class VaxDataViewModel(private val db : VaxInjectionsStatsDatabase) : ViewModel() {
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