package com.example.slmplay.data.db

import androidx.room.Entity

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistId", "trackId"]
)
data class PlaylistTrackCrossRef(
    val playlistId: String,
    val trackId: String,
    val position: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)
