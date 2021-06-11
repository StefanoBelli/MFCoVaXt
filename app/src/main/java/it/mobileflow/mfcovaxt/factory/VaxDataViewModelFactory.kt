package it.mobileflow.mfcovaxt.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import it.mobileflow.mfcovaxt.viewmodel.VaxDataViewModel
import java.lang.IllegalArgumentException

class VaxDataViewModelFactory private constructor() : ViewModelProvider.Factory {
    companion object {
        private var instance: VaxDataViewModelFactory? = null
        fun getInstance(): VaxDataViewModelFactory {
            if (instance == null) {
                instance = VaxDataViewModelFactory()
            }

            return instance as VaxDataViewModelFactory
        }
    }

    private var viewModelInstance : VaxDataViewModel? = null

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(VaxDataViewModel::class.java)) {
            if(viewModelInstance == null) {
                viewModelInstance = modelClass.newInstance() as VaxDataViewModel
            }

            return viewModelInstance as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}