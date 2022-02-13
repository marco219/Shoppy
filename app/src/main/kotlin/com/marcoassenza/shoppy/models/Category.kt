package com.marcoassenza.shoppy.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Category(
    @PrimaryKey val categoryId: Int,
    val categoryName: String,
    val color: Int = -1
) : Serializable {

    val displayName: String
        get() {
            return categoryName.replaceFirstChar {
                it.uppercaseChar()
            }
        }
}

