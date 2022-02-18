package com.marcoassenza.shoppy.data.local.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.marcoassenza.shoppy.models.Item
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import javax.inject.Inject

class FirebaseDatabaseDao @Inject constructor(
    private val firebaseDB: DatabaseReference
) {

    var userName: String? = null


    fun updateRemoteItems(items: List<Item>) {
        userName?.let { firebaseDB.child(it).setValue(items) }
    }

    @ExperimentalCoroutinesApi
    val remoteDataListenerCallBackFlow = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                this@callbackFlow.trySend(Result.failure(error.toException()))
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val items = dataSnapshot.children.map { ds ->
                    ds.getValue(Item::class.java)
                }
                this@callbackFlow.trySend(Result.success(items.filterNotNull()))
            }
        }

        userName?.let {
            firebaseDB.child(it).addValueEventListener(listener)
            Timber.d("Subscribe user: $it")
        }

        awaitClose {
            userName?.let {
                Timber.d("Unsubscribe user: $it")
                firebaseDB.child(it).removeEventListener(listener)
            }
        }
    }
}