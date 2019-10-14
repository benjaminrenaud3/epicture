package com.example.epicture

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient


class Webview : AppCompatActivity() {

    fun getAccessToken(string: String): String {
        val words = string.split("=", "&")
        return words[2]
    }

    fun getRefreshToken(string: String): String {
        val words = string.split("=", "&")
        return words[8]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        val monIntent = Intent(this, MainActivity::class.java)
        var mywebview: WebView? = null
        var access_token: String
        var refresh_token: String
        mywebview = findViewById<WebView>(R.id.WebView)
        mywebview!!.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != "https://api.imgur.com/oauth2/authorize?client_id=3b2d4847669fb6c&response_type=token&state=valide\n") {
                    access_token = getAccessToken(url.toString())
                    refresh_token = getRefreshToken(url.toString())
                    monIntent.putExtra("AccessToken", access_token)
                    monIntent.putExtra("RefreshToken", refresh_token)
                    startActivity(monIntent)
                }
                view?.loadUrl(url)
                return true
            }
        }
        mywebview.loadUrl("https://api.imgur.com/oauth2/authorize?client_id=3b2d4847669fb6c&response_type=token&state=valide\n")
    }
}
