package it.mobileflow.mfcovaxt.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class EzNetwork {
    companion object {
        fun connected(context: Context) : Boolean {
            var result = false
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                result = hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            }

            return result
        }
    }
}