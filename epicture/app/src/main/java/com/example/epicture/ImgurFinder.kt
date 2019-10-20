package com.example.epicture

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ImgurFinder {
    fun SearchImage(mainActivityInstance: MainActivity, callback: (response: Response) -> Unit) {
        val searchUrl =
            "https://api.imgur.com/3/gallery/search/${mainActivityInstance.page}/${mainActivityInstance.time}/?q=" + mainActivityInstance.toSearch + "&q_all"
        //val searchUrl = "https://api.imgur.com/3/gallery/search/${mainActivityInstance.time}/${mainActivityInstance.date}/${mainActivityInstance.page}?q=cats"

        mainActivityInstance.request.getRequest(
            searchUrl,
            mainActivityInstance.clientId,
            mainActivityInstance.appName,
            callback
        )
    }

    /*fun SearchFav(mainActivityInstance: MainActivity, callback: (response: Response) -> Unit) {
        val url = "https://api.imgur.com/3/account/${mainActivityInstance.Username}/favorites/1"
        var httpClient = OkHttpClient()

        httpClient = OkHttpClient.Builder().build()
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer ${mainActivityInstance.accessToken}")
            .header("User-Agent", "epicture")
            .build()
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("GET REQUEST", "An error has occurred $e")
            }

            @Override
            override fun onResponse(call: Call, response: Response) {
                callback(response)
            }
        })
    }*/
}