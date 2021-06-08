package it.mobileflow.mfcovaxt.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import it.mobileflow.mfcovaxt.entity.AppOwnLastUpdate

@Dao
interface AppOwnLastUpdateDao {
    @Insert
    fun insert(item: AppOwnLastUpdate): Long

    @Update
    fun update(item: AppOwnLastUpdate): Long

    @Query("SELECT * FROM AppOwnLastUpdate LIMIT 1")
    fun getAppOwnLastUpdate(): Array<AppOwnLastUpdate>
}