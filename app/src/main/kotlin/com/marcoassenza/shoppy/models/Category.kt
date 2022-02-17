package com.marcoassenza.shoppy.models

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude
import java.io.Serializable

@Keep
@Entity
data class Category(
    @PrimaryKey val categoryId: Int = 0,
    @get:Exclude val categoryName: String = "",
    @get:Exclude val color: Int = -1
) : Serializable {

    @get:Exclude
    val categoryDisplayName: String
        get() {
            return categoryName.replaceFirstChar {
                it.uppercaseChar()
            }
        }
}

fun Category.areIdEqual(other: Category): Boolean {
    return categoryId == other.categoryId
}

