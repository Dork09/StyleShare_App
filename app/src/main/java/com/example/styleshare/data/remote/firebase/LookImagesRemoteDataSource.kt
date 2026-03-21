package com.example.styleshare.data.remote.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

class LookImagesRemoteDataSource(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    suspend fun uploadLookImage(localImagePath: String, userUid: String): String {
        val imageFile = File(localImagePath)
        if (!imageFile.exists()) return localImagePath

        val imageRef = storage.reference
            .child("looks")
            .child(userUid)
            .child("${UUID.randomUUID()}.jpg")

        imageRef.putFile(Uri.fromFile(imageFile)).await()
        return imageRef.downloadUrl.await().toString()
    }
}
