package it.mobileflow.mfcovaxt.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp
import java.util.*

/**
 * Schema:
 * https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/somministrazioni-vaccini-latest.csv
 */
@Entity
data class VaxInjection (
    @PrimaryKey(autoGenerate = true) val index : Int = 0,
    val area : String,
    val vaxName : String,
    val injDate : Date,
    val ageRange : String,
    val male : Int,
    val female : Int,
    val firstInj : Int,
    val secondInj : Int,
    val nuts1Code : String,
    val nuts2Code : String,
    val istatAreaCode : Int,
    val areaName : String)