package it.mobileflow.mfcovaxt.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp
import java.util.*

/**
 * Schema
 * https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/somministrazioni-vaccini-summary-latest.csv
 */
@Entity(primaryKeys = ["area", "injDate"])
data class VaxInjectionsSummaryByDayAndArea (
    val area : String,
    val injDate : Date,
    val totalInj : Int,
    val maleInjs : Int,
    val femaleInjs : Int,
    val firstInjs : Int,
    val secondInjs : Int,
    val nuts1Code : String,
    val nuts2Code : String,
    val istatAreaCode : Int,
    val areaName : String)