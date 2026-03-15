/**
 * מטרת הקובץ:
 * Converters ל-Room:
 * מאפשר לשמור List<String> (למשל תגיות של לוק) בתוך SQLite.
 *
 * איך זה עובד?
 * - Room לא יודע לשמור רשימה ישירות
 * - אז אנחנו הופכים את הרשימה למחרוזת: "summer,winter,red"
 * - וכשקוראים מה-DB מחזירים אותה שוב לרשימה
 */
package com.example.styleshare.data.local.db

import androidx.room.TypeConverter

class Converters {

    /** ממיר רשימת תגיות למחרוזת אחת לשמירה ב-DB */
    @TypeConverter
    fun fromList(list: List<String>?): String {
        return list?.joinToString(separator = ",") ?: ""
    }

    /** ממיר מחרוזת מה-DB חזרה לרשימת תגיות */
    @TypeConverter
    fun toList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}
