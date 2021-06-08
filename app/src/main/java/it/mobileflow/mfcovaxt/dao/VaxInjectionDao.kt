package it.mobileflow.mfcovaxt.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.mobileflow.mfcovaxt.entity.VaxInjection

@Dao
interface VaxInjectionDao {
    @Insert
    fun insert(item: VaxInjection): Long

    @Query("SELECT * FROM VaxInjection")
    fun getVaxInjections(): Array<VaxInjection>
}