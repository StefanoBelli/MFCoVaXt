package it.mobileflow.mfcovaxt.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.mobileflow.mfcovaxt.entity.PartOfVaxablePopulation

@Dao
interface PartOfVaxablePopulationDao {
    @Insert
    fun insert(item: PartOfVaxablePopulation): Long

    @Query("SELECT * FROM PartOfVaxablePopulation")
    fun getWholeVaxablePopulation(): Array<PartOfVaxablePopulation>

    @Query("DELETE FROM PartOfVaxablePopulation")
    fun deleteTable(): Int
}