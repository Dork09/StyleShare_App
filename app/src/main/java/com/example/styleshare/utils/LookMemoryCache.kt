package com.example.styleshare.utils

import android.util.LruCache
import com.example.styleshare.model.Look

object LookMemoryCache {
    private const val MAX_LOOKS = 200

    private val lookCache = LruCache<String, Look>(MAX_LOOKS)
    private val listCache = mutableMapOf<String, List<Look>>()

    fun getLook(lookId: String): Look? = synchronized(this) {
        lookCache.get(lookId)
    }

    fun putLook(look: Look) = synchronized(this) {
        lookCache.put(look.id, look)
    }

    fun getList(key: String): List<Look>? = synchronized(this) {
        listCache[key]
    }

    fun putList(key: String, looks: List<Look>) = synchronized(this) {
        listCache[key] = looks
        looks.forEach { lookCache.put(it.id, it) }
    }

    fun invalidateAll() = synchronized(this) {
        lookCache.evictAll()
        listCache.clear()
    }
}
