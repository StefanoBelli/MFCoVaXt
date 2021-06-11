package it.mobileflow.mfcovaxt.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import it.mobileflow.mfcovaxt.viewmodel.VaxDataViewModel

class VaxDataViewModelFactory private constructor() : ViewModelProvider.Factory {
    companion object {
        private var instance: VaxDataViewModelFactory? = null
        fun getInstance(): VaxDataViewModelFactory {
            if (instance == null) {
                instance = VaxDataViewModelFactory()
            }

            return instance!!
        }
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VaxDataViewModel::class.java)) {
            return modelClass.newInstance() as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}