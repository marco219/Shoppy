package com.marcoassenza.shoppy.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Item(@PrimaryKey val name: String,
                @Embedded val category: Category,
                val stockQuantity: Int = 0
):Serializable{
    val displayName: String
        get() {
            return name.replaceFirstChar {
                it.uppercaseChar() }
        }
}
