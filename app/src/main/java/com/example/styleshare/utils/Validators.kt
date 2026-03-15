/**
 * מטרת הקובץ:
 * בדיקות בסיסיות לטפסים.
 */
package com.example.styleshare.utils

object Validators {

    /** בדיקת אימייל בסיסית */
    fun isEmailValid(email: String): Boolean {
        return email.contains("@") && email.contains(".") && email.length >= 5
    }

    /** בדיקת סיסמה בסיסית */
    fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    /** בדיקה אם שדה לא ריק */
    fun isNotBlank(text: String): Boolean {
        return text.trim().isNotEmpty()
    }
}
