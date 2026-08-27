package com.example.slmplay.data.model

data class UserProfile(
    val isLoggedIn: Boolean = false,
    val username: String = "Utilisateur SLM",
    val emailOrId: String = "",
    val avatarUri: String? = null,
    val bio: String = "Passionné de musique & créations SLM Studio",
    val joinedDate: Long = System.currentTimeMillis(),
    val totalTracksPlayed: Int = 0,
    val lastCloudBackupDate: Long? = null
)
