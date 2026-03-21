package com.example.styleshare.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LooksRemoteDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val looksCollection = firestore.collection("looks")

    suspend fun getAllLooks(): List<LookRemoteDto> {
        return looksCollection.get().await().documents.mapNotNull { document ->
            document.toObject(LookRemoteDto::class.java)?.copy(
                id = document.getString("id").orEmpty().ifBlank { document.id }
            )
        }
    }

    suspend fun upsertLook(look: LookRemoteDto) {
        looksCollection.document(look.id).set(look).await()
    }

    suspend fun deleteLook(lookId: String) {
        looksCollection.document(lookId).delete().await()
    }
}
