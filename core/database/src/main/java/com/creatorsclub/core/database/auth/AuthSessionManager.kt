package com.creatorsclub.core.database.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

class AuthSessionManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val accountRegistryPrefs: SharedPreferences =
        context.getSharedPreferences(ACCOUNTS_REGISTRY_PREFS, Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow(loadStoredUser())
    val currentUser: StateFlow<AuthUser> = _currentUser.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(true)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isGuestMode = MutableStateFlow(true)
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(prefs.getBoolean(KEY_COMPLETED_ONBOARDING, false))
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    private val _hasCompletedPermissions = MutableStateFlow(prefs.getBoolean(KEY_COMPLETED_PERMISSIONS, false))
    val hasCompletedPermissions: StateFlow<Boolean> = _hasCompletedPermissions.asStateFlow()

    private fun loadStoredUser(): AuthUser {
        val userId = prefs.getString(KEY_USER_ID, null) ?: "creator_local"
        val email = prefs.getString(KEY_EMAIL, null)
        val name = prefs.getString(KEY_DISPLAY_NAME, "Creator") ?: "Creator"
        val provider = prefs.getString(KEY_PROVIDER, "local") ?: "local"
        val isAnon = prefs.getBoolean(KEY_IS_ANONYMOUS, true)
        val createdAt = prefs.getLong(KEY_CREATED_AT, System.currentTimeMillis())
        val avatarUrl = prefs.getString(KEY_AVATAR_URL, null)

        return AuthUser(
            id = userId,
            email = email,
            displayName = name,
            avatarUrl = avatarUrl,
            isAnonymous = isAnon,
            provider = provider,
            createdAt = createdAt
        )
    }

    private fun persistUser(user: AuthUser) {
        prefs.edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_DISPLAY_NAME, user.displayName)
            .putString(KEY_PROVIDER, user.provider)
            .putString(KEY_AVATAR_URL, user.avatarUrl)
            .putBoolean(KEY_IS_ANONYMOUS, user.isAnonymous)
            .putLong(KEY_CREATED_AT, user.createdAt)
            .putBoolean(KEY_COMPLETED_ONBOARDING, true)
            .apply()

        _currentUser.value = user
        _isAuthenticated.value = !user.isAnonymous
        _isGuestMode.value = user.isAnonymous
        _hasCompletedOnboarding.value = true
    }

    fun setPermissionsCompleted(completed: Boolean = true) {
        prefs.edit().putBoolean(KEY_COMPLETED_PERMISSIONS, completed).apply()
        _hasCompletedPermissions.value = completed
    }

    fun setOnboardingCompleted(completed: Boolean = true) {
        prefs.edit().putBoolean(KEY_COMPLETED_ONBOARDING, completed).apply()
        _hasCompletedOnboarding.value = completed
    }

    private fun hashPassword(password: CharSequence): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(("kreavilo_salt_" + password.toString()).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun signUpWithEmail(email: String, password: CharSequence, displayName: String): Result<AuthUser> {
        val trimmedEmail = email.trim().lowercase()
        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }
        if (displayName.trim().isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your name."))
        }

        // Duplicate account detection in local registry
        val existingAccountJson = accountRegistryPrefs.getString(trimmedEmail, null)
        if (existingAccountJson != null) {
            return Result.failure(IllegalArgumentException("An account with $trimmedEmail already exists. Please Sign In."))
        }

        val userId = "usr_${UUID.nameUUIDFromBytes(trimmedEmail.toByteArray())}"
        val user = AuthUser(
            id = userId,
            email = trimmedEmail,
            displayName = displayName.trim(),
            isAnonymous = false,
            provider = "email"
        )

        // Store account credentials securely in registry
        val accountObj = JSONObject().apply {
            put("id", userId)
            put("email", trimmedEmail)
            put("displayName", displayName.trim())
            put("passwordHash", hashPassword(password))
            put("createdAt", user.createdAt)
            put("provider", "email")
        }
        accountRegistryPrefs.edit().putString(trimmedEmail, accountObj.toString()).apply()

        persistUser(user)
        return Result.success(user)
    }

    fun signInWithEmail(email: String, password: CharSequence): Result<AuthUser> {
        val trimmedEmail = email.trim().lowercase()
        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }

        val accountJsonStr = accountRegistryPrefs.getString(trimmedEmail, null)
        if (accountJsonStr == null) {
            return Result.failure(IllegalArgumentException("No account found for $trimmedEmail. Please Create an Account."))
        }

        val accountObj = JSONObject(accountJsonStr)
        val storedHash = accountObj.optString("passwordHash")
        val inputHash = hashPassword(password)

        if (storedHash != inputHash) {
            return Result.failure(IllegalArgumentException("Incorrect password. Please try again or use Forgot Password."))
        }

        val user = AuthUser(
            id = accountObj.optString("id", "usr_${UUID.randomUUID()}"),
            email = trimmedEmail,
            displayName = accountObj.optString("displayName", trimmedEmail.substringBefore("@")),
            isAnonymous = false,
            provider = "email",
            createdAt = accountObj.optLong("createdAt", System.currentTimeMillis())
        )
        persistUser(user)
        return Result.success(user)
    }

    fun signInWithGoogle(idToken: String? = null, email: String? = null, displayName: String? = null): Result<AuthUser> {
        val googleEmail = email ?: "creator@gmail.com"
        val googleName = displayName ?: "Google Creator"
        val userId = "goog_${UUID.nameUUIDFromBytes(googleEmail.toByteArray())}"

        val user = AuthUser(
            id = userId,
            email = googleEmail,
            displayName = googleName,
            isAnonymous = false,
            provider = "google"
        )

        // Cache Google account
        val accountObj = JSONObject().apply {
            put("id", userId)
            put("email", googleEmail)
            put("displayName", googleName)
            put("passwordHash", "google_oauth")
            put("createdAt", user.createdAt)
            put("provider", "google")
        }
        accountRegistryPrefs.edit().putString(googleEmail, accountObj.toString()).apply()

        persistUser(user)
        return Result.success(user)
    }

    fun continueAsGuest(): AuthUser {
        val guestId = "guest_${UUID.randomUUID().toString().take(8)}"
        val guest = AuthUser(
            id = guestId,
            displayName = "Guest Creator",
            isAnonymous = true,
            provider = "local"
        )
        persistUser(guest)
        return guest
    }

    fun resetPassword(email: String, newPassword: CharSequence): Result<Unit> {
        val trimmedEmail = email.trim().lowercase()
        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("New password must be at least 6 characters."))
        }

        val accountJsonStr = accountRegistryPrefs.getString(trimmedEmail, null)
            ?: return Result.failure(IllegalArgumentException("No account registered with $trimmedEmail."))

        val accountObj = JSONObject(accountJsonStr)
        accountObj.put("passwordHash", hashPassword(newPassword))
        accountRegistryPrefs.edit().putString(trimmedEmail, accountObj.toString()).apply()

        return Result.success(Unit)
    }

    fun updateProfileName(newName: String): Result<AuthUser> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("No active session"))
        val updated = current.copy(displayName = newName.trim())
        persistUser(updated)

        // Update in registry if email account
        current.email?.let { email ->
            val accountJsonStr = accountRegistryPrefs.getString(email, null)
            if (accountJsonStr != null) {
                val obj = JSONObject(accountJsonStr)
                obj.put("displayName", newName.trim())
                accountRegistryPrefs.edit().putString(email, obj.toString()).apply()
            }
        }
        return Result.success(updated)
    }

    fun deleteAccount(): Result<Unit> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("No active session"))
        current.email?.let { email ->
            accountRegistryPrefs.edit().remove(email).apply()
        }
        signOut()
        prefs.edit()
            .putBoolean(KEY_COMPLETED_ONBOARDING, false)
            .putBoolean(KEY_COMPLETED_PERMISSIONS, false)
            .apply()
        _hasCompletedOnboarding.value = false
        _hasCompletedPermissions.value = false
        return Result.success(Unit)
    }

    fun signOut() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_EMAIL)
            .remove(KEY_DISPLAY_NAME)
            .remove(KEY_PROVIDER)
            .remove(KEY_AVATAR_URL)
            .remove(KEY_IS_ANONYMOUS)
            .remove(KEY_COMPLETED_ONBOARDING)
            .apply()

        _currentUser.value = loadStoredUser()
        _isAuthenticated.value = true
        _isGuestMode.value = true
        _hasCompletedOnboarding.value = true
    }

    companion object {
        private const val PREFS_NAME = "kreavilo_auth_session"
        private const val ACCOUNTS_REGISTRY_PREFS = "kreavilo_accounts_registry"
        private const val KEY_USER_ID = "auth_user_id"
        private const val KEY_EMAIL = "auth_email"
        private const val KEY_DISPLAY_NAME = "auth_display_name"
        private const val KEY_PROVIDER = "auth_provider"
        private const val KEY_AVATAR_URL = "auth_avatar_url"
        private const val KEY_IS_ANONYMOUS = "auth_is_anonymous"
        private const val KEY_CREATED_AT = "auth_created_at"
        private const val KEY_COMPLETED_ONBOARDING = "auth_completed_onboarding"
        private const val KEY_COMPLETED_PERMISSIONS = "auth_completed_permissions"

        @Volatile
        private var instance: AuthSessionManager? = null

        fun getInstance(context: Context): AuthSessionManager {
            return instance ?: synchronized(this) {
                instance ?: AuthSessionManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
