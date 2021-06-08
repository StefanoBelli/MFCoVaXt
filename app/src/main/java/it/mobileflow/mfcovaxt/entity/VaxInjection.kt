package it.mobileflow.mfcovaxt.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

/**
 * Schema:
 * https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/somministrazioni-vaccini-latest.csv
 */
@Entity
data class VaxInjection (
    @PrimaryKey val index : Int,
    val area : String,
    val vaxName : String,
    val injDate : Timestamp,
    val ageRange : String,
    val male : Boolean,
    val female : Boolean,
    val firstInj : Boolean,
    val secondInj : Boolean,
    val nuts1Code : String,
    val nuts2Code : String,
    val istatAreaCode : Int,
    val areaName : String)