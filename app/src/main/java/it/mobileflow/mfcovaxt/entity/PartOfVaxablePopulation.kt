package it.mobileflow.mfcovaxt.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Schema:
 * https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/platea.csv
 */
@Entity
data class PartOfVaxablePopulation (
    @PrimaryKey val area : String,
    val areaName : String,
    @PrimaryKey val ageRange : String,
    val totalPopulation : Int)
