package com.example.styleshare.utils

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Uploads an image [Uri] to Cloudinary and returns the secure HTTPS URL.
 *
 * Must be called from a coroutine (e.g. inside viewModelScope.launch).
 * CloudinaryManager.init() must have been called before the first upload
 * (it is called automatically in StyleShareApp.onCreate).
 */
suspend fun uploadImageToCloudinary(context: Context, uri: Uri): String =
    suspendCancellableCoroutine { cont ->

        val requestId = MediaManager.get()
            .upload(uri)
            .callback(object : UploadCallback {

                override fun onStart(requestId: String) {}

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    if (url != null) {
                        cont.resume(url)
                    } else {
                        cont.resumeWithException(Exception("Cloudinary response missing secure_url"))
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    cont.resumeWithException(Exception("Cloudinary upload failed: ${error.description}"))
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    cont.resumeWithException(Exception("Cloudinary upload rescheduled: ${error.description}"))
                }
            })
            .dispatch(context)

        cont.invokeOnCancellation {
            MediaManager.get().cancelRequest(requestId)
        }
    }
