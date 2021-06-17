package it.mobileflow.mfcovaxt.scheduler

import it.mobileflow.mfcovaxt.viewmodel.VaxDataViewModel

interface LudSchedulerSubscriber {
    fun onSchedulingResult(
        performedScheduling: Boolean,
        ludErr: VaxDataViewModel.LudError,
        inSync: Boolean)
}