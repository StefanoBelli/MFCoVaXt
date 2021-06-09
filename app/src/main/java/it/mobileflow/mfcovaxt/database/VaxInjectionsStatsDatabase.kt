package it.mobileflow.mfcovaxt.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import it.mobileflow.mfcovaxt.dao.*
import it.mobileflow.mfcovaxt.entity.*

@Database(exportSchema = false, version = 1, entities = [
    VaxInjection::class,
    VaxInjectionsSummaryByDayAndArea::class,
    VaxStatsSummaryByArea::class,
    VaxInjectionsSummaryByAgeRange::class,
    VaxDelivery::class,
    LastUpdateDataset::class,
    PartOfVaxablePopulation::class,
    PhysicalInjectionLocation::class])
@TypeConverters(Converters::class)
abstract class VaxInjectionsStatsDatabase : RoomDatabase() {
    abstract fun getLastUpdateDatasetDao() : LastUpdateDatasetDao
    abstract fun getPartOfVaxablePopulationDao() : PartOfVaxablePopulationDao
    abstract fun getPhysicalInjectionLocationDao() : PhysicalInjectionLocationDao
    abstract fun getVaxDeliveryDao() : VaxDeliveryDao
    abstract fun getVaxInjectionDao() : VaxInjectionDao
    abstract fun getVaxInjectionsSummaryByAgeRangeDao() : VaxInjectionsSummaryByAgeRangeDao
    abstract fun getVaxInjectionsSummaryByDayAndAreaDao() : VaxInjectionsSummaryByDayAndAreaDao
    abstract fun getVaxStatsSummaryByAreaDao() : VaxStatsSummaryByAreaDao
}
