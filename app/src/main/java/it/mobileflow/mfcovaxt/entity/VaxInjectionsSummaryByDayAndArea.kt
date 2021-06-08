package it.mobileflow.mfcovaxt.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

/**
 * Schema
 * https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/somministrazioni-vaccini-summary-latest.csv
 */
@Entity
data class VaxInjectionsSummaryByDayAndArea (
    @PrimaryKey val area : String,
    @PrimaryKey val injDate : Timestamp,
    val totalInj : Int,
    val maleInjs : Int,
    val femaleInjs : Int,
    val firstInjs : Int,
    val secondInjs : Int,
    val nuts1Code : String,
    val nuts2Code : String,
    val istatAreaCode : Int,
    val areaName : String)