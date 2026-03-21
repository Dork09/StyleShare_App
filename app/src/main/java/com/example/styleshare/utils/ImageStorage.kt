/**
 * מטרת הקובץ:
 * שמירת תמונות בצורה מקומית (ללא Firebase Storage!)
 * - מעתיק תמונה מ-URI לקובץ פנימי באפליקציה
 * - מחזיר נתיב מלא לקובץ
 */
package com.example.styleshare.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageStorage {
    private const val IMAGE_CACHE_DIR = "image_cache"

    /**
     * שומר תמונה מקומית ומחזיר נתיב.
     */
    fun saveImageToInternalStorage(
        context: Context,
        uri: Uri,
        directoryName: String = "images",
        filePrefix: String = "image"
    ): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val directory = File(context.filesDir, directoryName).apply { mkdirs() }
            val fileName = "${filePrefix}_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
            val file = File(directory, fileName)

            FileOutputStream(file).use { output ->
                inputStream.copyTo(output)
            }

            syncImageToCache(context, file.absolutePath)
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun resolveImagePathForDisplay(context: Context, imagePath: String?): String? {
        if (imagePath.isNullOrBlank()) return null
        if (imagePath.startsWith("http")) return imagePath

        val original = File(imagePath)
        if (!original.exists()) return imagePath

        return syncImageToCache(context, original.absolutePath) ?: original.absolutePath
    }

    fun deleteImageIfInternal(context: Context, imagePath: String?) {
        if (imagePath.isNullOrBlank()) return

        val internalRoot = context.filesDir.absolutePath
        if (!imagePath.startsWith(internalRoot)) return

        runCatching {
            File(imagePath).takeIf { it.exists() }?.delete()
        }
        cacheFileForPath(context, imagePath).takeIf { it.exists() }?.delete()
    }

    private fun syncImageToCache(context: Context, imagePath: String): String? {
        return runCatching {
            val original = File(imagePath)
            if (!original.exists()) return null

            val cacheFile = cacheFileForPath(context, imagePath)
            val shouldRefresh = !cacheFile.exists() ||
                cacheFile.length() != original.length() ||
                cacheFile.lastModified() < original.lastModified()

            if (shouldRefresh) {
                cacheFile.parentFile?.mkdirs()
                original.copyTo(cacheFile, overwrite = true)
            }

            cacheFile.absolutePath
        }.getOrNull()
    }

    private fun cacheFileForPath(context: Context, imagePath: String): File {
        val cacheDir = File(context.cacheDir, IMAGE_CACHE_DIR).apply { mkdirs() }
        val originalName = File(imagePath).name.ifBlank { "image.jpg" }
        val cacheName = "${imagePath.hashCode()}_$originalName"
        return File(cacheDir, cacheName)
    }
}
