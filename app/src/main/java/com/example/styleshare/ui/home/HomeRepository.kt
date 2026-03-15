/**
 * מטרת הקובץ:
 * Repository של Home שמאגד:
 * - Feed מתוך Room
 * - Weather מתוך REST
 */
package com.example.styleshare.ui.home

import android.content.Context
import com.example.styleshare.data.repository.LooksRepository
import com.example.styleshare.data.repository.WeatherRepository
import com.example.styleshare.model.Look

class HomeRepository(context: Context) {

    private val looksRepo = LooksRepository(context)
    private val weatherRepo = WeatherRepository()

    /** מחזיר פיד לוקים (ומחדש את נתוני הדמו אם אין לוקים או כדי לרענן תמונות) */
    suspend fun getFeed(currentUid: String): List<Look> {
        // Only inject demo data if there are absolutely no looks in the database
        // This preserves comments and favorites across app restarts.
        var looks = looksRepo.getFeed(currentUid)
        if (looks.isEmpty()) {
            com.example.styleshare.utils.DemoDataInjector.injectDemoData(looksRepo)
            looks = looksRepo.getFeed(currentUid)
        }
        return looks
    }

    /** משנה מועדפים */
    suspend fun toggleFavorite(lookId: String, currentUid: String) = looksRepo.toggleFavorite(lookId, currentUid)

    /** העלאת לייקים */
    suspend fun incrementLike(lookId: String) = looksRepo.incrementLike(lookId)

    /** מחזיר טמפרטורה */
    suspend fun getTemperature(lat: Double, lon: Double): Double = weatherRepo.getTemperature(lat, lon)
}
