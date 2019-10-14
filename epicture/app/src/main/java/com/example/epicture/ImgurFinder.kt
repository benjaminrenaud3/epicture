package com.example.epicture

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ImgurFinder
{
    fun SearchImage(mainActivityInstance: MainActivity, callback : (response : Response) -> Unit)
    {
        val searchUrl = "https://api.imgur.com/3/gallery/search/?q=" + mainActivityInstance.toSearch + "&q_all"

        mainActivityInstance.request.getRequest(
            searchUrl,
            mainActivityInstance.clientId,
            mainActivityInstance.appName,
            callback
        )
    }

    fun SortPicture(mainActivityInstance : MainActivity, callback : (response : Response) -> Unit)
    {
        val searchUrl = "https://api.imgur.com/3/gallery/search/${mainActivityInstance.time}/${mainActivityInstance.date}/${mainActivityInstance.page}?q=cats"

        mainActivityInstance.request.getRequest(
            searchUrl,
            mainActivityInstance.clientId,
            mainActivityInstance.appName,
            callback
        )
    }
}