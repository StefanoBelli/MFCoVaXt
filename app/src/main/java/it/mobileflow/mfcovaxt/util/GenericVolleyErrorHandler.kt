package it.mobileflow.mfcovaxt.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.VolleyError
import it.mobileflow.mfcovaxt.R

fun volleyErrorHandler(context: Context, volleyError: VolleyError, myMsg: String) {
    Toast
        .makeText(context, R.string.volley_error_check_logcat,
            Toast.LENGTH_LONG)
        .show()
    Log.e(context.getString(R.string.app_name),
        context.getString(R.string.volley_error_msg_logcat).format(myMsg), volleyError)
}