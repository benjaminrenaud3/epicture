package com.example.epicture

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val request = ImgurRequest()
    private val lambdaMainActivity : MainActivity = this

    private class PhotoVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var photo: ImageView? = null
        internal var title: TextView? = null
        internal var photos_list = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        request.fetchData(lambdaMainActivity)
        Log.e("MainActivity", "finit")

        fab.setOnClickListener { view ->
            Snackbar.make(view, "mange ta mere", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    fun display(photos: ArrayList<ImgurRequest.Photo>) {
        runOnUiThread { render(photos) }
    }

    fun render(photos: ArrayList<ImgurRequest.Photo>) {
        val rv = findViewById<RecyclerView>(R.id.rv_of_photos)
        rv.layoutManager = LinearLayoutManager(this)

        val adapter = object: RecyclerView.Adapter<PhotoVH>() {
            @SuppressLint("InflateParams")
            override fun onCreateViewHolder(parent: ViewGroup, viewType:Int): PhotoVH {
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

            override fun onBindViewHolder(holder: PhotoVH, position:Int) {
                Picasso.get().load(
                    "https://i.imgur.com/" +
                            photos[position].id + ".jpg"
                ).into(holder.photo)
                holder.title?.text = photos[position].title
            }
            override fun getItemCount():Int {
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
