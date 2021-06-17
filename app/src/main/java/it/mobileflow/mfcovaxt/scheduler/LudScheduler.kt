package it.mobileflow.mfcovaxt.scheduler

import android.content.Context
import androidx.work.*
import it.mobileflow.mfcovaxt.util.volleyErrorHandler
import it.mobileflow.mfcovaxt.viewmodel.VaxDataViewModel
import kotlinx.coroutines.*


object LudScheduler {
    const val WAIT_ONCE_KEY_FOR_WORKER = "wait_once"

    private const val UNIQUE_WORK_NAME = "auto_ludsched"
    private var canUpdateBecauseOnline = true
    private var ludSchedulerScope = CoroutineScope(Dispatchers.Default)
    private var subscribers = mutableListOf<LudSchedulerSubscriber>()

    /*
     * properties that must be changed explicitly from client
     */
    lateinit var viewModel : VaxDataViewModel
    lateinit var appContext : Context
    var okUpdateEveryMs : Long = 1800000 // 30 mins
    var updateInProgressUpdateEveryMs : Long = 30000 // 30 secs

    fun scheduleUpdate() {
        ludSchedulerScope.launch {
            if (canUpdateBecauseOnline) {
                when(
                    viewModel.lastUpdateDataset(appContext,
                    { notifyAllSubscribers(true, VaxDataViewModel.LudError.OK, it) },
                    { volleyErrorHandler(appContext,it, "LudScheduler.scheduleUpdate()") })
                ) {
                    VaxDataViewModel.LudError.OK -> {
                        enqueueOtwrByTimeConstraint(okUpdateEveryMs)
                    }
                    VaxDataViewModel.LudError.UPDATE_IN_PROGRESS -> {
                        enqueueOtwrByTimeConstraint(updateInProgressUpdateEveryMs)
                        notifyAllSubscribers(true, VaxDataViewModel.LudError.UPDATE_IN_PROGRESS,
                            false)
                    }
                    VaxDataViewModel.LudError.NO_CONNECTIVITY ->  {
                        enqueueOtwrByConnectivityChangeConstraint()
                        canUpdateBecauseOnline = false
                        notifyAllSubscribers(true, VaxDataViewModel.LudError.NO_CONNECTIVITY,
                            false)
                    }
                }
            } else {
                notifyAllSubscribers(false, VaxDataViewModel.LudError.NO_CONNECTIVITY,
                    false)
            }
        }
    }

    fun addSubscriber(subscriber : LudSchedulerSubscriber) {
        subscribers.add(subscriber)
    }

    fun delSubscriber(subscriber : LudSchedulerSubscriber) {
        subscribers.remove(subscriber)
    }

    fun cancelCoros() {
        ludSchedulerScope.cancel()
    }

    fun alterConnectionStateDoNotCallThisFromAnywhere() {
        canUpdateBecauseOnline = true
    }

    private fun enqueueWorkRequest(oneTimeWorkRequest: OneTimeWorkRequest) {
        WorkManager
            .getInstance(appContext)
            .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, oneTimeWorkRequest)
    }

    private fun enqueueOtwrByTimeConstraint(tmMillis : Long) {
        val data = Data.Builder().putLong(WAIT_ONCE_KEY_FOR_WORKER, tmMillis).build()
        val otwr = OneTimeWorkRequestBuilder<ScheduleUpdateWorker>().setInputData(data).build()
        enqueueWorkRequest(otwr)
    }

    private fun enqueueOtwrByConnectivityChangeConstraint() {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val otwr = OneTimeWorkRequestBuilder<ScheduleUpdateWorker>().setConstraints(constraints)
            .build()
        enqueueWorkRequest(otwr)
    }

    private fun notifyAllSubscribers(ps: Boolean, le: VaxDataViewModel.LudError, ins: Boolean) {
        for(subscriber in subscribers) {
            subscriber.onSchedulingResult(ps, le, ins)
        }
    }
}