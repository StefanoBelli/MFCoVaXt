package it.mobileflow.mfcovaxt.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.mobileflow.mfcovaxt.entity.PhysicalInjectionLocation

@Dao
interface PhysicalInjectionLocationDao {
    @Insert
    fun insert(item: PhysicalInjectionLocation): Long

    @Query("SELECT * FROM PhysicalInjectionLocation")
    fun getPhysicalInjectionLocations(): Array<PhysicalInjectionLocation>

    @Query("DELETE FROM PhysicalInjectionLocation")
    fun deleteTable(): Int
}