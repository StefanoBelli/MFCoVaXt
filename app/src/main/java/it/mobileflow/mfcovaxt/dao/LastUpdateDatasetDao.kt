package it.mobileflow.mfcovaxt.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import it.mobileflow.mfcovaxt.entity.LastUpdateDataset

@Dao
interface LastUpdateDatasetDao {
    @Insert
    fun insert(item: LastUpdateDataset): Long

    @Update
    fun update(item: LastUpdateDataset): Long

    @Query("SELECT lastUpdate FROM LastUpdateDataset LIMIT 1")
    fun getLastUpdateDataset(): Array<LastUpdateDataset>
}