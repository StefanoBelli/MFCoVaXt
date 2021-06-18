package it.mobileflow.mfcovaxt.scheduler

import android.content.Context
import androidx.work.*
import it.mobileflow.mfcovaxt.util.volleyErrorHandler
import it.mobileflow.mfcovaxt.viewmodel.VaxDataViewModel
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit


object LudScheduler {
    private const val UNIQUE_WORK_NAME = "auto_ludsched"
    private const val VOLLEY_ERROR_MYMSG = "LudScheduler.scheduleUpdate()"

    private var ludSchedulerScope = CoroutineScope(Dispatchers.Default)

    /*
     * properties that must be changed explicitly from client
     */
    lateinit var viewModel : VaxDataViewModel
    lateinit var appContext : Context
    lateinit var subscriber : LudSchedulerSubscriber

    /*
     * !!! public property that shall *not* be changed !!! (see ScheduleUpdateWorker)
     */
    var canUpdateBecauseOnlineYouShouldNotChangeThis = true

    fun scheduleUpdate() {
        ludSchedulerScope.launch {
            if (canUpdateBecauseOnlineYouShouldNotChangeThis) {
                when(viewModel.lastUpdateDataset(appContext,
                    { ls: Boolean, ds: Boolean -> subscriber.onLsuUpdateOk(ls, ds) }, //Dispatched using Main thread pool
                    { volleyErrorHandler(appContext, it, VOLLEY_ERROR_MYMSG) })) {

                    VaxDataViewModel.LudError.OK -> {
                        enqueueOtwrByTimeConstraint(true)
                    }
                    VaxDataViewModel.LudError.UPDATE_IN_PROGRESS -> {
                        enqueueOtwrByTimeConstraint(false)
                        withContext(Dispatchers.Main) {
                            subscriber.onLsuUpdateInProgress()
                        }
                    }
                    VaxDataViewModel.LudError.NO_CONNECTIVITY ->  {
                        enqueueOtwrByConnectivityChangeConstraint()
                        canUpdateBecauseOnlineYouShouldNotChangeThis = false
                        withContext(Dispatchers.Main) {
                            subscriber.onLsuUpdateNoConnectivity()
                        }
                    }
                }
            }
        }
    }

    private fun enqueueWorkRequest(oneTimeWorkRequest: OneTimeWorkRequest) {
        WorkManager.getInstance(appContext)
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
}