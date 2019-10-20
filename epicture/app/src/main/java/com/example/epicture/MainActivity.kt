package com.example.epicture

import android.annotation.SuppressLint
import android.content.Intent
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
import org.json.JSONObject
import okhttp3.*
import android.widget.CheckBox

class MainActivity : AppCompatActivity() {

    val mainActivityInstance = this
    val request = ImgurRequest()
    val finder = ImgurFinder()
    val galeryUrl = "https://api.imgur.com/3/gallery/user/rising/0.json"
    val clientId = "Client-ID e7296bdc089bf0e"
    var accessToken = ""
    var Username = ""
    var fav = ArrayList<String>()
    val appName = "epicture"
    var time = ""
    var page = ""
    var toSearch = ""
    var previous = ""

    private class PhotoVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var photo: ImageView? = null
        internal var title: TextView? = null
    }

    class CheckFav {
        internal var box: CheckBox? = null
        internal var id: String? = null
    }

    var allCheckBox = ArrayList<CheckFav>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        SearchSort.minValue = 0
        SearchSort.maxValue = 1
        SearchSort.displayedValues = arrayOf("time", "viral")
        Log.e("acess token in onCreate", accessToken)

        finder.SearchImage(mainActivityInstance, this::displayPicture)

        recupToken()

        //Connection bouton
        loginButton.setOnClickListener {
            if (Username == "") {
                val monIntent: Intent = Intent(this, Webview::class.java)
                startActivity(monIntent)
            }
            val view = Intent(this, Webview::class.java)
            startActivity(view)
        }

        val upload_intent = Intent(this, ImgurUploader::class.java)
        upload_intent.putExtra("accessToken", accessToken)


        fab.setOnClickListener { view ->
            Snackbar.make(view, "Gallery", Snackbar.LENGTH_LONG)
            startActivity(upload_intent)
        }

        SearchButton.setOnClickListener {
            val tonTexte = SearchPage.text.toString()
            toSearch = Search.text.toString()
            page =  SearchPage.text.toString()
            time = SearchSort.value.toString()
            finder.SearchImage(mainActivityInstance, this::displayPicture)
        }
    }

    @SuppressLint("SetTextI18n")
    fun recupToken() {
        //Recuperation du token du a la page de connexion imgur
        val intent = intent
        if (intent != null) {
            if (intent.hasExtra("AccessToken"))
                accessToken = intent.getStringExtra("AccessToken")
            if (intent.hasExtra("Username")) {
                Username = intent.getStringExtra("Username")
                //finder.SearchFav(mainActivityInstance, this::AssignFav)
                loginButton.text = "Account"
            }
        }
    }

    fun drawGallery() {
        request.getRequest(galeryUrl, clientId, appName, this::displayPicture)
    }

    fun AssignFav (response: Response) {
        val data = JSONObject(response.body()?.string())
        val items = data.getJSONArray("data")

        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            if (item.getBoolean("is_album"))
                fav.add(item.getString("cover"))
            else
                fav.add(item.getString("id"))
        }
    }

    fun displayPicture(response: Response) {
        val photos = ArrayList<ImgurRequest.Photo>()
        val data = JSONObject(response.body()?.string())
        val items = data.getJSONArray("data")

        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val photo = ImgurRequest.Photo()
            if (item.getBoolean("is_album"))
                photo.id = item.getString("cover")
            else
                photo.id = item.getString("id")
            photo.title = item.getString("title")
            photos.add(photo)
        }
        runOnUiThread { render(photos) }
    }

    private fun favHandler(id: String)
    {
        val tmp = CheckFav()
        /*for (i in 0 until fav.size) {
            if (id == fav[i]) {
                this.findViewById<CheckBox>(R.id.favBox)?.isChecked
                return
            }
        }*/
        if (previous == "")
            previous = id
        else {
            tmp.box =  this.findViewById<CheckBox>(R.id.favBox)
            tmp.id = previous
            previous = id
            allCheckBox.add(tmp)
        }

        for (i in 0 until allCheckBox.size) {
            allCheckBox[i].box?.setOnClickListener(View.OnClickListener {
                request.AddToMyFav(mainActivityInstance, allCheckBox[i].id, this::printerror)
            })
        }
    }

    fun printerror(response: Response)
    {
        println("AND THE REPONSE IS = $response")
    }


    fun render(photos: ArrayList<ImgurRequest.Photo>) {
        val rv = findViewById<RecyclerView>(R.id.rv_of_photos)
        rv.layoutManager = LinearLayoutManager(this) as RecyclerView.LayoutManager?

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

                if (Username != "") {
                    findViewById<CheckBox>(R.id.favBox)?.visibility = View.VISIBLE
                    photos[position].id?.let { favHandler(it) }
                }
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
