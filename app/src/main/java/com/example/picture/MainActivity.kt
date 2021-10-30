package com.example.picture

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.picture.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream
import java.util.*

private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var imageUri: Uri?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted())
            requestPermissions()

       lifecycleScope.launchWhenStarted {
           addClickListeners()
       }

    }

    private val multiPermissionCallBack =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            if (map.entries.isEmpty())
                Toast.makeText(this, "Please accept all permissions.", Toast.LENGTH_SHORT).show()
        }

    private val getPicturesFromGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                binding.ivPicture.load(it)
            }
        }

    private val takePictureWithCamera =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if(it) {
                imageUri?.let {
                    binding.ivPicture.load(imageUri)
                }
            }
        }

    private fun requestPermissions() {
        multiPermissionCallBack.launch(
            REQUIRED_PERMISSIONS
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            this, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun addClickListeners() {
        binding.apply {
            btnConvert.setOnClickListener {
                if(imageUri == null)
                    Toast.makeText(this@MainActivity,"Select Image First",Toast.LENGTH_SHORT).show()
                else
                    convertToBase64(imageUri!!)
            }

            ibGallery.setOnClickListener {
                selectorLayout.isVisible = false
                getPicturesFromGallery.launch("image/*")
            }

            ibCamera.setOnClickListener {
                selectorLayout.isVisible = false
                FileUtilsObject.getTmpFileUri(this@MainActivity).let{ uri->
                    imageUri = uri
                    takePictureWithCamera.launch(uri)
                }
            }

            ibCancel.setOnClickListener {
                selectorLayout.isVisible = false
            }

            btnGetPicture.setOnClickListener {
                selectorLayout.isVisible = true
            }
        }
    }

    private fun convertToBase64(imageUri:Uri) {
        val stream =  contentResolver.openInputStream(imageUri)
        val bitmap =  BitmapFactory.decodeStream(stream)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val byteArray = baos.toByteArray()
        val base64 = Base64.encodeToString(byteArray,Base64.DEFAULT)

        binding.tvBase64.text = base64
    }
}