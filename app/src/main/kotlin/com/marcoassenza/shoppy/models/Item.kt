package com.marcoassenza.shoppy.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude
import java.io.Serializable

@Entity
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    @Embedded var category: Category = Category(),
    @field:JvmField val isInGroceryList: Boolean = true,
    val stockQuantity: Int = 0
) : Serializable {

    @get:Exclude
    val displayName: String
        get() {
            return name.replaceFirstChar {
                it.uppercaseChar()
            }
        }
}

fun Item.isInStorage(): Boolean = !isInGroceryList && (stockQuantity > 0)

fun Item.areIdEqual(other: Item): Boolean {
    return id == other.id
}