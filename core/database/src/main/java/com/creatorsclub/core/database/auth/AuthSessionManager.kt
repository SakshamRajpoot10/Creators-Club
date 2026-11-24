package com.creatorsclub.core.database.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AuthSessionManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow(loadStoredUser())
    val currentUser: StateFlow<AuthUser> = _currentUser.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(!_currentUser.value.isAnonymous)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private fun loadStoredUser(): AuthUser {
        val userId = prefs.getString(KEY_USER_ID, null)
        val email = prefs.getString(KEY_EMAIL, null)
        val name = prefs.getString(KEY_DISPLAY_NAME, null)
        val provider = prefs.getString(KEY_PROVIDER, "local") ?: "local"
        val isAnon = prefs.getBoolean(KEY_IS_ANONYMOUS, true)
        val createdAt = prefs.getLong(KEY_CREATED_AT, System.currentTimeMillis())

        return if (userId != null) {
            AuthUser(
                id = userId,
                email = email,
                displayName = name,
                avatarUrl = null,
                isAnonymous = isAnon,
                provider = provider,
                createdAt = createdAt
            )
        } else {
            // Default: 100% free Guest Creator with local storage
            val guestId = "guest_${UUID.randomUUID().toString().take(8)}"
            val guest = AuthUser(
                id = guestId,
                displayName = "Guest Creator",
                isAnonymous = true,
                provider = "local"
            )
            persistUser(guest)
            guest
        }
    }

    private fun persistUser(user: AuthUser) {
        prefs.edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_DISPLAY_NAME, user.displayName)
            .putString(KEY_PROVIDER, user.provider)
            .putBoolean(KEY_IS_ANONYMOUS, user.isAnonymous)
            .putLong(KEY_CREATED_AT, user.createdAt)
            .apply()

        _currentUser.value = user
        _isAuthenticated.value = !user.isAnonymous
    }

    fun signInWithEmail(email: String, password: CharSequence): Result<AuthUser> {
        if (!email.contains("@") || !email.contains(".")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }

        // In production, exchange with Supabase Auth GoTrue endpoint
        val userId = "usr_${UUID.nameUUIDFromBytes(email.toByteArray())}"
        val displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
        val user = AuthUser(
            id = userId,
            email = email,
            displayName = displayName,
            isAnonymous = false,
            provider = "email"
        )
        persistUser(user)
        return Result.success(user)
    }

    fun signUpWithEmail(email: String, password: CharSequence, displayName: String): Result<AuthUser> {
        if (!email.contains("@") || !email.contains(".")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }

        val userId = "usr_${UUID.nameUUIDFromBytes(email.toByteArray())}"
        val user = AuthUser(
            id = userId,
            email = email,
            displayName = displayName.ifBlank { email.substringBefore("@") },
            isAnonymous = false,
            provider = "email"
        )
        persistUser(user)
        return Result.success(user)
    }

    fun signInWithGoogle(idToken: String, email: String?, displayName: String?): Result<AuthUser> {
        val userId = "goog_${UUID.randomUUID().toString().take(12)}"
        val user = AuthUser(
            id = userId,
            email = email,
            displayName = displayName ?: "Google Creator",
            isAnonymous = false,
            provider = "google"
        )
        persistUser(user)
        return Result.success(user)
    }

    fun continueAsGuest(): AuthUser {
        val guest = AuthUser(
            id = "guest_${UUID.randomUUID().toString().take(8)}",
            displayName = "Guest Creator",
            isAnonymous = true,
            provider = "local"
        )
        persistUser(guest)
        return guest
    }

    fun signOut() {
        prefs.edit().clear().apply()
        continueAsGuest()
    }

    companion object {
        private const val PREFS_NAME = "kreavilo_auth_session"
        private const val KEY_USER_ID = "auth_user_id"
        private const val KEY_EMAIL = "auth_email"
        private const val KEY_DISPLAY_NAME = "auth_display_name"
        private const val KEY_PROVIDER = "auth_provider"
        private const val KEY_IS_ANONYMOUS = "auth_is_anonymous"
        private const val KEY_CREATED_AT = "auth_created_at"

        @Volatile
        private var instance: AuthSessionManager? = null

        fun getInstance(context: Context): AuthSessionManager {
            return instance ?: synchronized(this) {
                instance ?: AuthSessionManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
