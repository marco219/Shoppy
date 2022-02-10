package com.marcoassenza.shoppy.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Item(@PrimaryKey val name: String,
                val category: String,
                val stockQuantity: Int = 0
):Serializable