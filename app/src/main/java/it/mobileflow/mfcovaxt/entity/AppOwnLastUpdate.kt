package it.mobileflow.mfcovaxt.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity
data class AppOwnLastUpdate(
    @PrimaryKey val index : Int,
    var partOfVaxablePopulation : Timestamp,
    var physicalInjectionLocation : Timestamp,
    var vaxDelivery: Timestamp,
    var vaxInjection: Timestamp,
    var vaxInjectionsSummaryByAgeRange: Timestamp,
    var vaxInjectionsSummaryByDayAndArea: Timestamp,
    var vaxStatsSummaryByArea: Timestamp)
