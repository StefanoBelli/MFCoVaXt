package it.mobileflow.mfcovaxt.scheduler

import android.content.Context
import androidx.work.*
import it.mobileflow.mfcovaxt.util.volleyErrorHandler
import it.mobileflow.mfcovaxt.viewmodel.VaxDataViewModel
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit


object LudScheduler {
    private const val UNIQUE_WORK_NAME = "auto_ludsched"
    private var ludSchedulerScope = CoroutineScope(Dispatchers.Default)
    //
    // TODO SINGLE SUBSCRIBER
    //
    private var subscribers = mutableListOf<LudSchedulerSubscriber>()

    /*
     * properties that must be changed explicitly from client
     */
    lateinit var viewModel : VaxDataViewModel
    lateinit var appContext : Context

    /*
     * !!! public property that shall *not* be changed !!! (see ScheduleUpdateWorker)
     */
    var canUpdateBecauseOnlineYouShouldNotChangeThis = true

    fun scheduleUpdate() {
        ludSchedulerScope.launch {
            if (canUpdateBecauseOnlineYouShouldNotChangeThis) {
                when(
                    viewModel.lastUpdateDataset(appContext,
                    { notifyAllSubscribers(true, VaxDataViewModel.LudError.OK) },
                    { volleyErrorHandler(appContext, it, "LudScheduler.scheduleUpdate()") })
                ) {
                    VaxDataViewModel.LudError.OK -> {
                        enqueueOtwrByTimeConstraint(true)
                    }
                    VaxDataViewModel.LudError.UPDATE_IN_PROGRESS -> {
                        enqueueOtwrByTimeConstraint(false)
                        notifyAllSubscribers(true, VaxDataViewModel.LudError.UPDATE_IN_PROGRESS)
                    }
                    VaxDataViewModel.LudError.NO_CONNECTIVITY ->  {
                        enqueueOtwrByConnectivityChangeConstraint()
                        canUpdateBecauseOnlineYouShouldNotChangeThis = false
                        notifyAllSubscribers(true, VaxDataViewModel.LudError.NO_CONNECTIVITY)
                    }
                }
            } else {
                notifyAllSubscribers(false, VaxDataViewModel.LudError.NO_CONNECTIVITY)
            }
        }
    }

    fun subscribe(subscriber : LudSchedulerSubscriber) {
        subscribers.add(subscriber)
    }

    fun unsubscribe(subscriber : LudSchedulerSubscriber) {
        subscribers.remove(subscriber)
    }

    fun cancelCoros() {
        ludSchedulerScope.cancel()
    }

    private fun enqueueWorkRequest(oneTimeWorkRequest: OneTimeWorkRequest) {
        WorkManager
            .getInstance(appContext)
            .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, oneTimeWorkRequest)
    }

    private fun enqueueOtwrByTimeConstraint(isOkUpdate : Boolean) {
        if(isOkUpdate) {
            enqueueWorkRequest(OneTimeWorkRequestBuilder<ScheduleUpdateWorker>()
                .setInitialDelay(300, TimeUnit.SECONDS).build())
        } else {
            enqueueWorkRequest(OneTimeWorkRequestBuilder<ScheduleUpdateWorker>()
                .setInitialDelay(15, TimeUnit.SECONDS).build())
        }
    }

    private fun enqueueOtwrByConnectivityChangeConstraint() {
        enqueueWorkRequest(OneTimeWorkRequestBuilder<ScheduleUpdateWorker>().setConstraints(
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build())
    }

    private fun notifyAllSubscribers(ps: Boolean, le: VaxDataViewModel.LudError) {
        for(subscriber in subscribers) {
            subscriber.onSchedulingResult(ps, le)
        }
    }
}