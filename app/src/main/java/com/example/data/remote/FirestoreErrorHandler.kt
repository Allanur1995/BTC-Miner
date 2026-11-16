package com.example.data.remote

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType {
    CREATE, UPDATE, DELETE, LIST, GET, WRITE
}

fun unwrapFirestoreException(throwable: Throwable?): FirebaseFirestoreException? {
    var current: Throwable? = throwable
    while (current != null) {
        if (current is FirebaseFirestoreException) return current
        current = current.cause
    }
    return null
}

fun handleFirestoreError(
    exception: Throwable,
    operationType: OperationType,
    path: String
): Nothing {
    if (exception is CancellationException && unwrapFirestoreException(exception) == null) {
        throw exception
    }

    val currentUser = try {
        Firebase.auth.currentUser
    } catch (_: Throwable) {
        null
    }

    val providerArray = JSONArray()
    currentUser?.providerData?.forEach { profile ->
        providerArray.put(
            JSONObject().apply {
                put("providerId", profile.providerId)
                put("displayName", profile.displayName ?: JSONObject.NULL)
                put("email", profile.email ?: JSONObject.NULL)
            }
        )
    }

    val firestoreException = unwrapFirestoreException(exception)
    val errorMsg = firestoreException?.message ?: exception.message ?: exception.toString()

    val payload = JSONObject().apply {
        put("error", errorMsg)
        put("operationType", operationType.name.lowercase())
        put("path", path)
        put("authInfo", JSONObject().apply {
            put("userId", currentUser?.uid ?: JSONObject.NULL)
            put("email", currentUser?.email ?: JSONObject.NULL)
            put("emailVerified", currentUser?.isEmailVerified ?: false)
            put("isAnonymous", currentUser?.isAnonymous ?: false)
            put("providerInfo", providerArray)
        })
    }

    val formattedJson = payload.toString(2)
    Log.e("FirestoreError", "Firestore Error (${operationType.name.lowercase()} on $path): $formattedJson", exception)
    throw exception
}

fun formatUserFacingFirestoreError(throwable: Throwable): String {
    val firestoreException = unwrapFirestoreException(throwable)
    return when (firestoreException?.code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "Permission denied. Please sign in with Google to access the global leaderboard."
        FirebaseFirestoreException.Code.UNAVAILABLE ->
            "Leaderboard temporarily unreachable. Check your internet connection and retry."
        FirebaseFirestoreException.Code.UNAUTHENTICATED ->
            "Authentication required. Please sign in with Google."
        else -> firestoreException?.localizedMessage ?: throwable.localizedMessage ?: "Failed to load leaderboard."
    }
}
