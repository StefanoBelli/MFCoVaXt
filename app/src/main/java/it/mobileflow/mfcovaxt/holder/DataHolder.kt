package it.mobileflow.mfcovaxt.holder

import it.mobileflow.mfcovaxt.entity.PhysicalInjectionLocation
import it.mobileflow.mfcovaxt.entity.VaxInjectionsSummaryByAgeRange
import it.mobileflow.mfcovaxt.entity.VaxInjectionsSummaryByDayAndArea

object DataHolder {
    lateinit var vaxInjectionsSummaryByAgeRanges : Array<VaxInjectionsSummaryByAgeRange>
    lateinit var physicalInjectionLocations: Array<PhysicalInjectionLocation>
    lateinit var vaxInjectionsSummaryByDayAndAreas: Array<VaxInjectionsSummaryByDayAndArea>
}