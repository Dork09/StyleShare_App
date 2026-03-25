package com.example.styleshare

import android.app.Application
import com.example.styleshare.utils.CloudinaryManager

class StyleShareApp : Application() {

    override fun onCreate() {
        super.onCreate()
        CloudinaryManager.init(this)
    }
}
