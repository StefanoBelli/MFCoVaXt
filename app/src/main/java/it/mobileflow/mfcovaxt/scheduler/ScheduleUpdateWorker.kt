package it.mobileflow.mfcovaxt.scheduler

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ScheduleUpdateWorker(ac: Context, wp: WorkerParameters): Worker(ac, wp) {
    override fun doWork(): Result {
        LudScheduler.canUpdateBecauseOnlineYouShouldNotChangeThis = true
        LudScheduler.scheduleUpdate()
        return Result.success()
    }
}
