package com.example.epicture

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker.checkSelfPermission
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.upload.*
import okhttp3.*
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.provider.DocumentsContract
import com.google.gson.annotations.SerializedName
import java.io.File
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.Serializable


class ImgurUploader : AppCompatActivity()
{
    private val IMAGE_REQUEST_CODE = 1
    protected lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload)
        setSupportActionBar(toolbar)
        Log.e("on create", "on create")
        initView()
    }

    private fun pickImage(req_code: Int) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select file to upload "), req_code)
    }

    private fun initView() {
        pickImage(IMAGE_REQUEST_CODE)
        imageToUpload.setOnClickListener { _ ->
            Log.e("appel pickimage", "appel pickimage")
            pickImage(IMAGE_REQUEST_CODE)
        }
        uploadButton.setOnClickListener { _ ->
            Log.e("appel uploadImage", "appel uploadImage")
            uploadImage()
            finish()
        }
    }

    fun uploadImage() {
        val imgurService = Retrofit.Builder()
            .baseUrl("https://api.imgur.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ImgurService::class.java)

        val imageFile = File(getRealPathFromUri(imageUri))

        val requestFile = RequestBody.create(MediaType.parse(contentResolver.getType(imageUri)), imageFile)
        val imageBody = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
        val titleBody = RequestBody.create(okhttp3.MultipartBody.FORM, imageTitle.text.toString())
        val descriptionBody = RequestBody.create(okhttp3.MultipartBody.FORM, imageDescription.text.toString())
        val optinalBodyMap = mapOf("title" to titleBody, "description" to descriptionBody)

        val call = imgurService.uploadImage("Bearer " + ImgurAuthorization.instance.getAccessToken(applicationContext), imageBody, optinalBodyMap)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>,
                                    response: Response<ResponseBody>) {
                Log.v("Upload", "success")
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("Upload error:", t.message)
            }
        })
    }

    protected fun getRealPathFromUri(contentUri: Uri): String? {
        val wholeID = DocumentsContract.getDocumentId(contentUri)
        val id = wholeID.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        val column = arrayOf(MediaStore.Images.Media.DATA)
        val sel = MediaStore.Images.Media._ID + "=?"
        if (checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, arrayOf(id), null)
            var filePath: String? = null
            val columnIndex = cursor!!.getColumnIndex(column[0])
            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex)
            }
            cursor.close()
            return filePath
        } else {
            Log.e("getRealUri", "error")
            finish()
        }
        return ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IMAGE_REQUEST_CODE) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                imageUri = data?.data!!
                val inputStream = applicationContext.contentResolver.openInputStream(imageUri)
                imageToUpload.setImageBitmap(BitmapFactory.decodeStream(inputStream))
            }
        }
    }
}

interface ImgurService {
    @GET("/3/account/me/images")
    fun listImages(@Header("Authorization") authorization: String): Call<ImgurImageList>

    @Multipart
    @POST("/3/image")
    fun uploadImage(@Header("Authorization") authorization: String,
                    @Part image: MultipartBody.Part,
                    @PartMap queries: Map<String, @JvmSuppressWildcards RequestBody>): Call<ResponseBody>
}


class ImgurImageList : ImageList {
    @SerializedName("data")
    override lateinit var data: List<ImgurImage>
}

interface ImageUrl : Serializable {
    val from: String
    fun getImageUrl(): String
}

interface ImageList {
    val data: List<ImageUrl>
}

class ImgurImage : ImageUrl {

    override val from: String
        get() = "imgur"

    override fun getImageUrl(): String {
        return when {
            link != null -> link!!
            else -> ""
        }
    }

    var id: String = ""
    var title: String = ""
    var description: String = ""
    var datetime: Int = 0
    var type: String = ""
    var animated: Boolean = false
    var width: Int = 0
    var height: Int = 0
    var size: Int = 0
    var views: Int = 0
    var bandwidth: Int = 0
    var vote: String? = null
    var favorite: Boolean = false
    var nsfw: String? = null
    var section: String? = null
    var account_url: String? = null
    var account_id: Int = 0
    var is_ad: Boolean = false
    var in_most_viral: Boolean = false
    var has_sound: Boolean = false
    var tags: Array<String>? = null
    var ad_type: Int = 0
    var ad_url: String? = null
    var in_gallery: Boolean = false
    var deletehash: String? = null
    var name: String? = null
    var link: String? = null
}

class ImgurAuthorization private constructor() {

    private object Holder { val INSTANCE = ImgurAuthorization() }

    companion object {
        val instance: ImgurAuthorization by lazy { Holder.INSTANCE }
    }

    @SuppressLint("ApplySharedPref")
    fun saveRefreshToken(context: Context, refreshToken: String?, accessToken: String?, expiresIn: Long) {
        context.getSharedPreferences("imgur_auth", 0)
            .edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .putLong("expires_in", expiresIn)
            .commit()
    }

    fun getAccessToken(context: Context): String {
        return context.getSharedPreferences("imgur_auth", 0)
            .getString("access_token", "0")
    }

    @SuppressLint("ApplySharedPref")
    fun deleteAuthorization(context: Context) {
        context.getSharedPreferences("imgur_auth", 0)
            .edit()
            .clear()
            .commit()

    }
}