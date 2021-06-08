package it.mobileflow.mfcovaxt.http

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class Http constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: Http? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Http(context).also {
                    INSTANCE = it
                }
            }
    }

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }

    fun addToRequestQueue(req: CsvRequest) {
        requestQueue.add(req)
    }

    fun addToRequestQueue(req: JsonObjectRequest) {
        requestQueue.add(req)
    }
}
