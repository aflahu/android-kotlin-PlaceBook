package com.example.placebook.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,

    var placeId: String? = null,
    var name: String = "",
    var address: String = "",
    var latitude: Double = 0.0,
    var longtitude: Double = 0.0,
    var phone: String = ""
)
