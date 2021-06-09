package it.mobileflow.mfcovaxt.entity

import androidx.room.Entity
import java.sql.Timestamp

/**
 * Schema
 * https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/consegne-vaccini-latest.csv
 */
@Entity
data class VaxDelivery (
    val area : String,
    val vaxName : String,
    val deliveryDate : Timestamp,
    val numOfVaxes : Int,
    val nuts1Code : String,
    val nuts2Code : String,
    val istatAreaCode : Int,
    val areaName : String)
