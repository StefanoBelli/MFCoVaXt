package it.mobileflow.mfcovaxt.holder

import it.mobileflow.mfcovaxt.entity.*

object CommonDataHolder {
    lateinit var vaxInjectionsSummaryByAgeRanges: Array<VaxInjectionsSummaryByAgeRange>
    lateinit var vaxInjections : Array<VaxInjection>
    lateinit var physicalInjectionLocations: Array<PhysicalInjectionLocation>
    lateinit var vaxDeliveries: Array<VaxDelivery>
    lateinit var ageRanges: Set<String>
    lateinit var vaxes: Set<String>
}