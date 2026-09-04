package com.example.domain.utils

import android.content.Context
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageHelper {
    /**
     * Copies an image from a source URI (like Gallery) to the app's internal storage.
     * This ensures the app always has access to the photo even if permissions expire.
     */
    fun saveImageToInternalStorage(context: Context, sourceUri: Uri, profileId: Long): String? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(sourceUri) ?: return null
            
            val directory = File(context.filesDir, "profile_photos").apply { mkdirs() }
            val fileName = "profile_${profileId}_${System.currentTimeMillis()}.jpg"
            val destFile = File(directory, fileName)
            
            FileOutputStream(destFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            
            Uri.fromFile(destFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Deletes a specific photo file from internal storage.
     */
    fun deleteSpecificPhoto(uriString: String?) {
        if (uriString == null) return
        try {
            val file = File(Uri.parse(uriString).path ?: return)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Converts an image file (from internal storage URI) to a Base64 string for export.
     */
    fun getBase64FromInternalUri(context: Context, uriString: String?): String? {
        if (uriString == null) return null
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bytes = inputStream.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves a Base64 string as an image file in internal storage and returns the URI.
     */
    fun saveBase64ToInternalStorage(context: Context, base64String: String?, profileId: Long): String? {
        if (base64String.isNullOrBlank()) return null
        return try {
            val bytes = Base64.decode(base64String, Base64.DEFAULT)
            
            val directory = File(context.filesDir, "profile_photos").apply { mkdirs() }
            val fileName = "profile_${profileId}_imported_${System.currentTimeMillis()}.jpg"
            val destFile = File(directory, fileName)
            
            FileOutputStream(destFile).use { outputStream ->
                outputStream.write(bytes)
            }
            
            Uri.fromFile(destFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
