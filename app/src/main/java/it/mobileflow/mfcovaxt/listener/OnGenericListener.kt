package it.mobileflow.mfcovaxt.listener

interface OnGenericListener<T> {
    fun onEvent(arg: T)
}