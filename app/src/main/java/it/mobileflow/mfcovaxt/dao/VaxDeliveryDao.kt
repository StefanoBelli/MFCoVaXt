package it.mobileflow.mfcovaxt.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.mobileflow.mfcovaxt.entity.VaxDelivery

@Dao
interface VaxDeliveryDao {
    @Insert
    fun insert(item: VaxDelivery): Long

    @Query("SELECT * FROM VaxDelivery")
    fun getVaxDeliveries(): Array<VaxDelivery>
}