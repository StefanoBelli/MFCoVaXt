package it.mobileflow.mfcovaxt.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.mobileflow.mfcovaxt.entity.VaxStatsSummaryByArea

@Dao
interface VaxStatsSummaryByAreaDao {
    @Insert
    fun insert(item: VaxStatsSummaryByArea): Long

    @Query("SELECT * FROM VaxStatsSummaryByArea")
    fun getVaxStatsSummariesByArea(): Array<VaxStatsSummaryByArea>

    @Query("DELETE FROM VaxStatsSummaryByArea")
    fun deleteTable(): Int
}