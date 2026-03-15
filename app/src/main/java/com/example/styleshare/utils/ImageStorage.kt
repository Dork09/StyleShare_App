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

object ImageStorage {

    /**
     * שומר תמונה מקומית ומחזיר נתיב.
     */
    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            val fileName = "profile_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)

            FileOutputStream(file).use { output ->
                inputStream.copyTo(output)
            }

            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
