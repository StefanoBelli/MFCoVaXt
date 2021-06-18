package it.mobileflow.mfcovaxt.scheduler

interface LudSchedulerSubscriber {
    fun onLsuUpdateOk(lsuSync: Boolean, dataSync: Boolean)
    fun onLsuUpdateInProgress()
    fun onLsuUpdateNoConnectivity()
}