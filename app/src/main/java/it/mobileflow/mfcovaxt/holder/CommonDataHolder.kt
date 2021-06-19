package it.mobileflow.mfcovaxt.holder

import it.mobileflow.mfcovaxt.entity.PhysicalInjectionLocation
import it.mobileflow.mfcovaxt.entity.VaxDelivery
import it.mobileflow.mfcovaxt.entity.VaxInjectionsSummaryByAgeRange
import it.mobileflow.mfcovaxt.entity.VaxInjectionsSummaryByDayAndArea

object CommonDataHolder {
    lateinit var vaxInjectionsSummaryByAgeRanges : Array<VaxInjectionsSummaryByAgeRange>
    lateinit var physicalInjectionLocations: Array<PhysicalInjectionLocation>
    lateinit var vaxDeliveries: Array<VaxDelivery>
    lateinit var ageRanges: Set<String>
    lateinit var vaxes: Set<String>
}