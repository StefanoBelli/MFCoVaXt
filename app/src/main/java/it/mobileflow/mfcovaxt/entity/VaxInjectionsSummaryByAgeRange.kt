package it.mobileflow.mfcovaxt.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Schema
 * https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/anagrafica-vaccini-summary-latest.csv
 */
@Entity
data class VaxInjectionsSummaryByAgeRange (
    @PrimaryKey val ageRange : String,
    val totalInj : Int,
    val maleInjs : Int,
    val femaleInjs : Int,
    val firstInjs : Int,
    val secondInjs : Int)