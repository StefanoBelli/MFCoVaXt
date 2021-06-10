package it.mobileflow.mfcovaxt.util

import android.content.Context
import it.mobileflow.mfcovaxt.viewmodel.VaxDataViewModel
import org.json.JSONObject
import java.io.File
import java.util.*

class EzAppDataUpdateTracker {
    companion object {
        private const val TRACKER_NAME = "/app_own_update.json"

        fun getLastUpdate(v: VaxDataViewModel.VaxData, ctx: Context) : Date {
            val target = File(ctx.filesDir.absolutePath + TRACKER_NAME)
            if(target.exists()) {
                val bufreader = target.bufferedReader()
                val rootJsonObject = JSONObject(bufreader.readText())
                bufreader.close()

                return Date(rootJsonObject.getLong(vaxDataToKeyString(v)))
            }

            return Date(0)
        }

        fun putLastUpdate(v: VaxDataViewModel.VaxData, ctx: Context) {
            val target = File(ctx.filesDir.absolutePath + TRACKER_NAME)
            val rootJsonObject : JSONObject
            if(!target.exists()) {
                target.createNewFile()
                rootJsonObject = createAppOwnUpdateJsonObject()
            } else {
                val bufreader = target.bufferedReader()
                rootJsonObject = JSONObject(bufreader.readText())
                bufreader.close()
            }

            rootJsonObject.put(vaxDataToKeyString(v), Date().time)

            val bufwriter = target.bufferedWriter()
            bufwriter.write(rootJsonObject.toString())
            bufwriter.close()
        }

        private fun vaxDataToKeyString(v: VaxDataViewModel.VaxData) : String {
            return when(v) {
                VaxDataViewModel.VaxData.PARTS_OF_VAXABLE_POPULATION -> "partsOfVaxablePopulation"
                VaxDataViewModel.VaxData.PHYSICAL_INJECTION_LOCATIONS -> "physicalInjectionLocations"
                VaxDataViewModel.VaxData.VAX_DELIVERIES -> "vaxDeliveries"
                VaxDataViewModel.VaxData.VAX_INJECTIONS -> "vaxInjections"
                VaxDataViewModel.VaxData.VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE -> "vaxInjectionsSummariesByAgeRange"
                VaxDataViewModel.VaxData.VAX_INJECTIONS_SUMMARIES_BY_DAY_AND_AREA -> "vaxInjectionsSummariesByDayAndArea"
                VaxDataViewModel.VaxData.VAX_STATS_SUMMARIES_BY_AREA -> "vaxStatsSummariesByArea"
            }
        }

        private fun createAppOwnUpdateJsonObject() : JSONObject {
            val rootJsonObject = JSONObject()
            rootJsonObject.put(
                    vaxDataToKeyString(
                            VaxDataViewModel.VaxData.PARTS_OF_VAXABLE_POPULATION), 0)

            rootJsonObject.put(
                    vaxDataToKeyString(
                            VaxDataViewModel.VaxData.PHYSICAL_INJECTION_LOCATIONS), 0)

            rootJsonObject.put(
                    vaxDataToKeyString(
                            VaxDataViewModel.VaxData.VAX_DELIVERIES), 0)

            rootJsonObject.put(
                    vaxDataToKeyString(
                            VaxDataViewModel.VaxData.VAX_INJECTIONS), 0)

            rootJsonObject.put(
                    vaxDataToKeyString(
                            VaxDataViewModel.VaxData.VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE), 0)

            rootJsonObject.put(
                    vaxDataToKeyString(
                            VaxDataViewModel.VaxData.VAX_INJECTIONS_SUMMARIES_BY_DAY_AND_AREA), 0)

            rootJsonObject.put(
                    vaxDataToKeyString(
                            VaxDataViewModel.VaxData.VAX_STATS_SUMMARIES_BY_AREA), 0)

            return rootJsonObject
        }
    }
}