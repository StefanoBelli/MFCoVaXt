package it.mobileflow.mfcovaxt.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.mobileflow.mfcovaxt.entity.LastUpdateDataset

@Dao
interface LastUpdateDatasetDao {
    @Insert
    fun insert(item: LastUpdateDataset): Long

    @Query("SELECT lastUpdate FROM LastUpdateDataset LIMIT 1")
    fun getLastUpdateDataset(): Array<LastUpdateDataset>
}