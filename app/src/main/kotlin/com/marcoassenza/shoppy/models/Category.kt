package com.marcoassenza.shoppy.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Category(
    @PrimaryKey val id: Int,
    val categoryName: String,
    val color: Int = -1):Serializable{

    val displayName: String
        get() {
            return categoryName.replaceFirstChar {
                it.uppercaseChar() }
        }

    val hexColor: String?
        get() {
            if (color == -1) return null
            return Integer.toHexString(color).substring(2)
        }
}

