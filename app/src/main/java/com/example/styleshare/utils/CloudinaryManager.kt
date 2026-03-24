package com.example.styleshare.utils

import android.content.Context
import com.cloudinary.android.MediaManager

/**
 * Initializes Cloudinary once at app startup.
 *
 * ⚠️  Security note: embedding API_SECRET in the APK is acceptable for development /
 *     personal projects, but in production you should generate upload signatures on a
 *     backend server and remove API_SECRET from the client.
 *
 * Replace the three placeholder values below with your real Cloudinary credentials
 * (Dashboard → Settings → Access Keys).
 */
object CloudinaryManager {

    const val CLOUD_NAME = "drduckphh"
    const val API_KEY    = "658623415983627"
    const val API_SECRET = "B1EL4EwO5B8PpZ04m1jPygVay8U"

    fun init(context: Context) {
        val config = hashMapOf<String, Any>(
            "cloud_name" to CLOUD_NAME,
            "api_key"    to API_KEY,
            "api_secret" to API_SECRET
        )
        MediaManager.init(context.applicationContext, config)
    }
}
