package com.pm.ridepay.data.local

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class ImageSaver(private val context: Context) {

    suspend fun savePng(bytes: ByteArray, displayName: String): Result<Unit> = withContext(
        Dispatchers.IO){
        try {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/RidePay"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("Could not create the file")
            resolver.openOutputStream(uri)?.use { it.write(bytes) }
                ?: error("Could not open the file")

            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            Result.success(Unit)
        }
        catch (e: CancellationException){
            throw e
        }
        catch (e: Exception){
            Result.failure(e)
        }
    }
}