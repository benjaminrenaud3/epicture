package com.example.epicture

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ImgurRequest
{
    private var httpClient = OkHttpClient()
    private var TAG = "Request"

    class Photo {
        internal var id: String? = null
        internal var title: String? = null
    }

    fun fetchData(mainActivity: MainActivity) {

        httpClient = OkHttpClient.Builder().build()
            val request = Request.Builder()
            .url("https://api.imgur.com/3/gallery/user/rising/0.json")
            .header("Authorization", "Client-ID e7296bdc089bf0e")
            .header("User-Agent", "epicture")
            .build()
        httpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "An error has occurred $e")
            }
            @Override
            override fun onResponse(call: Call, response: Response) {
                val photos = ArrayList<Photo>()
                val data = JSONObject(response.body()?.string())
                val items = data.getJSONArray("data")

                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val photo = Photo()
                    if (item.getBoolean("is_album"))
                        photo.id = item.getString("cover")
                    else
                        photo.id = item.getString("id")
                    photo.title = item.getString("title")
                    photos.add(photo)
                }
                mainActivity.display(photos)
            }
        })
    }
}
