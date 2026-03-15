package com.example.styleshare.utils

import com.example.styleshare.data.repository.LooksRepository

object DemoDataInjector {

    suspend fun injectDemoData(repository: LooksRepository) {
        val demoUserId = "demo_user_123"

        // 1. Summer look
        val id1 = repository.createLook(
            title = "לוק קייצי קליל",
            description = "שמלה קיצית מושלמת לים או ליציאה בצהריים. נוחה ואוורירית.",
            imagePath = "https://images.unsplash.com/photo-1523359346063-d87ce0af37f9?q=80&w=800&auto=format&fit=crop", // Casual summer shirt/dress vibe
            createdByUid = demoUserId,
            tags = listOf("קיץ", "שמלה", "ים")
        )
        repository.addComment(id1, "איזה יופי של שמלה!", "הדר")
        repository.addComment(id1, "מאיפה קנית?", "נועה")
        repository.incrementLike(id1)
        repository.incrementLike(id1)

        // 2. Winter sweater
        val id2 = repository.createLook(
            title = "סוודר חורפי מחמם",
            description = "סוודר אוברסייז נעים, מושלם לימים קרים במיוחד עם כוס קפה.",
            imagePath = "https://images.unsplash.com/photo-1614032049021-9be9bc0d27c6?q=80&w=800&auto=format&fit=crop", // Cozy winter sweater
            createdByUid = demoUserId,
            tags = listOf("חורף", "סוודר", "קר")
        )
        repository.addComment(id2, "נראה כל כך נעים ומחמם!", "דנה")
        repository.incrementLike(id2)

        // 3. Evening elegance
        val id3 = repository.createLook(
            title = "אלגנטי לערב",
            description = "חליפה מחייטת או שמלת ערב. לוק שמשדר יוקרה וביטחון.",
            imagePath = "https://images.unsplash.com/photo-1566206091558-7f218b696731?q=80&w=800&auto=format&fit=crop", // Elegant look
            createdByUid = demoUserId,
            tags = listOf("ערב", "אלגנטי", "יציאה")
        )
        repository.addComment(id3, "מושלם, אין דברים כאלה!", "שרון")
        repository.addComment(id3, "אני חייבת כזה ארון.", "יעל")
        repository.incrementLike(id3)
        repository.incrementLike(id3)
        repository.incrementLike(id3)

        // 4. Sporty look
        val id4 = repository.createLook(
            title = "ספורטיבי ונוח",
            description = "סט ספורט מושלם לאימון או סתם לסידורים בעיר.",
            imagePath = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?q=80&w=800&auto=format&fit=crop", // Sporty fitness clothing
            createdByUid = demoUserId,
            tags = listOf("ספורט", "נוח", "אימון")
        )
        repository.addComment(id4, "הצבעים מושלמים", "רוני")

        // 5. Casual streetwear
        val id5 = repository.createLook(
            title = "סטריט קז'ואל",
            description = "ג'ינס קרוע וטישרט בייסיק שתמיד עובד, עם סניקרס מגניבות.",
            imagePath = "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?q=80&w=800&auto=format&fit=crop", // Casual street style
            createdByUid = demoUserId,
            tags = listOf("קז'ואל", "סטריט", "ג'ינס")
        )
        repository.incrementLike(id5)

        // 6. Winter coat
        val id6 = repository.createLook(
            title = "מעיל ארוך קלאסי",
            description = "מעיל שישדרג כל הופעה חורפית וייתן מראה אלגנטי גם כשקפוא בחוץ.",
            imagePath = "https://images.unsplash.com/photo-1539533113208-f6df8cc8b543?q=80&w=800&auto=format&fit=crop", // Classic winter coat
            createdByUid = demoUserId,
            tags = listOf("חורף", "מעיל", "קפוא", "שלג")
        )
        repository.addComment(id6, "ואוו המעיל הזה פשוט חלום רטוב", "מיכל")
        repository.incrementLike(id6)
        repository.incrementLike(id6)
    }
}
