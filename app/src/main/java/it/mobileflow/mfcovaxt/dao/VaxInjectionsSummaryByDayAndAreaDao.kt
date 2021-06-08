package it.mobileflow.mfcovaxt.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.mobileflow.mfcovaxt.entity.VaxInjectionsSummaryByDayAndArea

@Dao
interface VaxInjectionsSummaryByDayAndAreaDao {
    @Insert
    fun insert(item: VaxInjectionsSummaryByDayAndArea): Long

    @Query("SELECT * FROM VaxInjectionsSummaryByDayAndArea")
    fun getVaxInjectionsSummariesByDayAndArea(): Array<VaxInjectionsSummaryByDayAndArea>
}