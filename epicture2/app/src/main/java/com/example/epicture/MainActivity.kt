package com.example.epicture

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import okhttp3.OkHttpClient
import org.json.JSONObject
import org.json.JSONArray
import android.widget.TextView
import android.view.View
import android.widget.ImageView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.squareup.picasso.Picasso
import android.view.ViewGroup




class MainActivity : AppCompatActivity() {

    private var httpClient = OkHttpClient()
    private var TAG = "MainActivity"

    private class Photo {
        internal var id: String? = null
        internal var title: String? = null
    }

    private class PhotoVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var photo: ImageView? = null
        internal var title: TextView? = null
        internal var photos_list = null
    }

    private fun fetchData()
    {
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
                val data = JSONObject(response.body()?.string())
                val items = data.getJSONArray("data")
                val photos = ArrayList<Photo>()

                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val photo = Photo()
                    if (item.getBoolean("is_album")) {
                        photo.id = item.getString("cover")
                    } else {
                        photo.id = item.getString("id")
                    }
                    photo.title = item.getString("title")

                    photos.add(photo)
                }
                runOnUiThread{render(photos)}
            }
        })
    }

    private fun render(photos: List<Photo>) {
        val rv = findViewById(R.id.rv_of_photos) as RecyclerView
        rv.layoutManager = LinearLayoutManager(this)

        val adapter = object:RecyclerView.Adapter<PhotoVH>() {
            override fun onCreateViewHolder(parent:ViewGroup, viewType:Int):PhotoVH {
                val vh = PhotoVH(layoutInflater.inflate(R.layout.item, null))
                vh.photo = vh.itemView.findViewById(R.id.photo) as ImageView
                vh.title = vh.itemView.findViewById(R.id.title) as TextView
                return vh
            }

            override fun onBindViewHolder(holder:PhotoVH, position:Int) {
                Picasso.get().load(
                    "https://i.imgur.com/" +
                            photos.get(position).id + ".jpg"
                ).into(holder.photo)
                holder.title?.setText(photos.get(position).title)
            }
            override fun getItemCount():Int {
                return photos.size
            }
        };
        rv.setAdapter(adapter);
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        fetchData()

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
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
