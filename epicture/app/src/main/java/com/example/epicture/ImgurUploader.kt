package com.example.epicture

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.PermissionChecker.checkSelfPermission
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.upload.*
import okhttp3.*
import android.Manifest
import android.provider.DocumentsContract
import java.io.File
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ImgurUploader : AppCompatActivity()
{
    private val IMAGE_REQUEST_CODE = 1
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            IMAGE_REQUEST_CODE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload)
        initView()
    }

    private fun Int.pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(
            Intent.createChooser(
                intent,
                "Select file to upload "
            ), this
        )
    }

    private fun initView() {
        IMAGE_REQUEST_CODE.pickImage()
        imageToUpload.setOnClickListener {
            Log.e("appel pickimage", "appel pickimage")
            IMAGE_REQUEST_CODE.pickImage()
        }
        uploadButton.setOnClickListener {
            Log.e("appel uploadImage", "appel uploadImage")
            uploadImage()
            finish()
        }
    }

    private fun uploadImage()
    {
        if (!::imageUri.isInitialized) {
            return
        }
        val accessToken = intent.getStringExtra("accessToken")
        val imgurService = Retrofit.Builder()
            .baseUrl("https://api.imgur.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ImgurService::class.java)
        val image = File(getPathFromUri(imageUri))
        val requestFile =
            RequestBody.create(MediaType.parse(contentResolver.getType(imageUri)), image)
        val imageBody = MultipartBody.Part.createFormData("image", image.name, requestFile)
        val titleBody = RequestBody.create(MultipartBody.FORM, imageTitle.text.toString())
        val descriptionBody =
            RequestBody.create(MultipartBody.FORM, imageDescription.text.toString())
        val optionalBody = mapOf("title" to titleBody, "description" to descriptionBody)
        val call = imgurService.uploadImage("Bearer $accessToken", imageBody, optionalBody)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                Log.e("token", accessToken)
                Log.v("Upload", "response:")
                Log.v("test", response.toString())
                Log.v("Upload", "success")
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("Upload error:", t.message)
            }
        })
    }

    private fun getPathFromUri(contentUri: Uri): String? {
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
            finish()
        }
        return ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                imageUri = data?.data!!
                val inputStream = applicationContext.contentResolver.openInputStream(imageUri)
                imageToUpload.setImageBitmap(BitmapFactory.decodeStream(inputStream))
            }
        }
    }

interface ImgurService {
    @GET("/3/account/me/images")
    fun listImages(@Header("Authorization") authorization: String)

    @Multipart
    @POST("/3/image")
    fun uploadImage(@Header("Authorization") authorization: String,
                    @Part image: MultipartBody.Part,
                    @PartMap queries: Map<String, @JvmSuppressWildcards RequestBody>): Call<ResponseBody>
    }
}