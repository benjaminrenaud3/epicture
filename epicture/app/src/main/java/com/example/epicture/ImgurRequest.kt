package com.example.epicture

import android.util.Log
import okhttp3.*
import java.io.IOException
import okhttp3.FormBody
import okhttp3.OkHttpClient


class ImgurRequest
{
    private var httpClient = OkHttpClient()

    class Photo {
        internal var id: String? = null
        internal var title: String? = null
    }

    fun getRequest(url: String, clientId: String, appName: String, callback : (response : Response) -> Unit) {

        httpClient = OkHttpClient.Builder().build()
            val request = Request.Builder()
            .url(url)
            .header("Authorization", clientId)
            .header("User-Agent", appName)
            .build()
        httpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("GET REQUEST", "An error has occurred $e")
            }
            @Override
            override fun onResponse(call: Call, response: Response) {
                callback(response)
            }
        })
    }

    fun postRequest(url: String, clientId: String, appName: String, callback : (response : Response) -> Unit)
    {
        val formBody = FormBody.Builder()
            .add("message", "Your message")
            .build()
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .header("Authorization", clientId)
            .header("User-Agent", appName)
            .build()

        httpClient = OkHttpClient.Builder().build()
        httpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("POST REQUEST", "An error has occurred $e")
            } @Override

            override fun onResponse(call: Call, response: Response) {
                Log.e("POST REQUEST", " dans OnResponse")
                callback(response)
            }
        })
    }

    fun imagePostRequest(url: String, clientId: String, callback: (response : Response) -> Unit)
    {

        val formBody = FormBody.Builder()
            .build()
            //hashMap
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .header("Authorization", clientId)
            .build()

        httpClient = OkHttpClient.Builder().build()
        httpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("POST REQUEST", "An error has occurred $e")
            } @Override

            override fun onResponse(call: Call, response: Response) {
                Log.e("POST REQUEST", " dans OnResponse")
                callback(response)
            }
        })
    }
}
