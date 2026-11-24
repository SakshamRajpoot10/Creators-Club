package com.creatorsclub.core.database.auth

data class AuthUser(
    val id: String,
    val email: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val isAnonymous: Boolean = false,
    val provider: String = "local",
    val createdAt: Long = System.currentTimeMillis()
) {
    val displayTitle: String
        get() = displayName?.ifBlank { null }
            ?: email?.substringBefore("@")
            ?: if (isAnonymous) "Guest Creator" else "Creator"
}
