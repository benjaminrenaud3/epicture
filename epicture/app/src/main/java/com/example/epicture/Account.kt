package com.example.epicture

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import org.json.JSONObject
import okhttp3.*
import kotlinx.android.synthetic.main.activity_account.*

class Account : AppCompatActivity() {

    private val accountInstance = this
    val request = ImgurRequest()
    val finder = ImgurFinder()
    var accessToken = ""
    var Username = ""

    private class PhotoVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var photo: ImageView? = null
        internal var title: TextView? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        val intent = intent
        if (intent != null) {
            if (intent.hasExtra("AccessToken"))
                accessToken = intent.getStringExtra("AccessToken")
            if (intent.hasExtra("Username")) {
                Username = intent.getStringExtra("Username")
                //finder.SearchFav(AccountInstance, this::AssignFav)
            }
            name.text = Username
        }
        imageButton.setOnClickListener {
            request.getAccountImage(accountInstance, "https://api.imgur.com/3/account/me/images", this::displayPicture)
        }
        favButton.setOnClickListener {
            request.getAccountImage(accountInstance, "https://api.imgur.com/3/account/$Username/favorites", this::displayPicture)
        }
    }

    fun displayPicture(response: Response) {
        println("REPONSE : $response")
        val photos = ArrayList<ImgurRequest.Photo>()
        val data = JSONObject(response.body()?.string())
        val items = data.getJSONArray("data")

        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val photo = ImgurRequest.Photo()
            photo.id = item.getString("id")
            photo.title = item.getString("title")
            photos.add(photo)
        }
        runOnUiThread { render(photos) }
    }

    fun render(photos: ArrayList<ImgurRequest.Photo>) {
        val rv = findViewById<RecyclerView>(R.id.rv_of_photos)
        rv.layoutManager = LinearLayoutManager(this) as RecyclerView.LayoutManager?

        val adapter = object : RecyclerView.Adapter<PhotoVH>() {
            @SuppressLint("InflateParams")
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoVH {
                val vh = PhotoVH(
                    layoutInflater.inflate(
                        R.layout.item,
                        null
                    )
                )
                vh.photo = vh.itemView.findViewById(R.id.photo) as ImageView
                vh.title = vh.itemView.findViewById(R.id.title) as TextView
                return vh
            }

            override fun onBindViewHolder(holder: PhotoVH, position: Int) {
                Picasso.get().load(
                    "https://i.imgur.com/" +
                            photos[position].id + ".jpg"
                ).into(holder.photo)
                holder.title?.text = photos[position].title
            }

            override fun getItemCount(): Int {
                return photos.size
            }
        }
        rv.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
