package it.mobileflow.mfcovaxt.http

import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.BufferedReader
import java.io.InputStreamReader

class CsvRequest(
        url: String?,
        private val listener: Response.Listener<List<CSVRecord>>,
        errorListener: Response.ErrorListener
) : Request<List<CSVRecord>>(Method.GET, url, errorListener) {
    override fun parseNetworkResponse(response: NetworkResponse?): Response<List<CSVRecord>> {
        return Response.success(
            CSVFormat.DEFAULT.parse(
                BufferedReader(
                    InputStreamReader(
                        response?.data?.inputStream()))).records,
                HttpHeaderParser.parseCacheHeaders(response))
    }

    override fun deliverResponse(response: List<CSVRecord>) = listener.onResponse(response)
}