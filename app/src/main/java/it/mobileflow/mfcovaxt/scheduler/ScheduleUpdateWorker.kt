package it.mobileflow.mfcovaxt.scheduler

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ScheduleUpdateWorker(ac: Context, wp: WorkerParameters): Worker(ac, wp) {
    override fun doWork(): Result {
        val ms = inputData.getLong(LudScheduler.WAIT_ONCE_KEY_FOR_WORKER, 0)

        if(ms == 0L) {
            LudScheduler.alterConnectionStateDoNotCallThisFromAnywhere()
        } else {
            Thread.sleep(ms)
        }

        LudScheduler.scheduleUpdate()
        return Result.success()
    }
}
