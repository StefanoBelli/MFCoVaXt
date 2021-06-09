package it.mobileflow.mfcovaxt.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Schema:
 * https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/platea.csv
 */
@Entity(primaryKeys = ["area", "ageRange"])
data class PartOfVaxablePopulation (
    val area : String,
    val areaName : String,
    val ageRange : String,
    val totalPopulation : Int)
