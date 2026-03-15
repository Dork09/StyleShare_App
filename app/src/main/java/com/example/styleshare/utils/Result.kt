/**
 * מטרת הקובץ:
 * מחלקת תוצאה אחידה כדי שה-UI ידע להציג Loading / Success / Error.
 */
package com.example.styleshare.utils

sealed class Result<out T> {

    /** מצב טעינה */
    object Loading : Result<Nothing>()

    /** מצב הצלחה */
    data class Success<T>(val data: T) : Result<T>()

    /** מצב שגיאה */
    data class Error(val message: String) : Result<Nothing>()
}
