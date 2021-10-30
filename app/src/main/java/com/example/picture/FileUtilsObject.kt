package com.example.picture

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

//for this code to work we have to add provider in manifest (check manifest)
//and provider paths xml file (check res->xml->provider_paths)
object FileUtilsObject {
    fun getTmpFileUri(context: Context): Uri {
        val tmpFile =
            File.createTempFile("tmp_image_file", ".png", context.cacheDir).apply {
                createNewFile()
                deleteOnExit()
            }

        return FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.provider",
            tmpFile
        )
    }
}