package com.example.slmplay.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String = "",
    val gradientIndex: Int = 0,
    val createdDate: Long = System.currentTimeMillis(),
    val isSystem: Boolean = false // e.g. "Vos musiques", "Coups de cœur"
)
