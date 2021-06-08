package it.mobileflow.mfcovaxt.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.mobileflow.mfcovaxt.entity.VaxInjectionsSummaryByAgeRange

@Dao
interface VaxInjectionsSummaryByAgeRangeDao {
    @Insert
    fun insert(item: VaxInjectionsSummaryByAgeRange): Long

    @Query("SELECT * FROM VaxInjectionsSummaryByAgeRange")
    fun getVaxInjectionsSummariesByAgeRange(): Array<VaxInjectionsSummaryByAgeRange>
}