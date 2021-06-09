package it.mobileflow.mfcovaxt.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Schema
 * https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/vaccini-summary-latest.csv
 */
@Entity
data class VaxStatsSummaryByArea (
    @PrimaryKey val area : String,
    val totalInjs : Int,
    val totalDelivVaxes : Int,
    val percInjs : Float,
    val nuts1Code : String,
    val nuts2Code : String,
    val istatAreaCode : Int,
    val areaName : String)
