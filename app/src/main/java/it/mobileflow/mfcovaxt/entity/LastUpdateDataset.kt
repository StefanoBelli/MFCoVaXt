package it.mobileflow.mfcovaxt.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

/**
 * Schema:
 * https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/last-update-dataset.json
 */
@Entity
data class LastUpdateDataset (
    @PrimaryKey val index : Int,
    val lastUpdate : Timestamp)