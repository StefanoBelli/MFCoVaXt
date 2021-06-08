package it.mobileflow.mfcovaxt.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Schema
 * https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/punti-somministrazione-tipologia.csv
 */
@Entity
data class PhysicalInjectionLocation (
    @PrimaryKey val area : String,
    @PrimaryKey val locationName : String,
    val type : String,
    val nuts1Code : String,
    val nuts2Code : String,
    val istatAreaCode : Int,
    val areaName : String)