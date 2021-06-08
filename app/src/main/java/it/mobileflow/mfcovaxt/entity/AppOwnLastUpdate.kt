package it.mobileflow.mfcovaxt.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity
data class AppOwnLastUpdate(
    @PrimaryKey val index : Int,
    val partOfVaxablePopulation : Timestamp,
    val physicalInjectionLocation : Timestamp,
    val vaxDelivery: Timestamp,
    val vaxInjection: Timestamp,
    val vaxInjectionsSummaryByAgeRange: Timestamp,
    val vaxInjectionsSummaryByDayAndArea: Timestamp,
    val vaxStatsSummaryByArea: Timestamp)
