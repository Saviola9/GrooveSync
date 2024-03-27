package com.example.kotlinproject1.data.remote

import android.util.Log
import com.example.kotlinproject1.data.entities.Song
import com.example.kotlinproject1.other.Constants
import com.example.kotlinproject1.other.Constants.SONG_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MusicDatabase {

    private val firestore = FirebaseFirestore.getInstance()
    private val songCollection = firestore.collection(SONG_COLLECTION)

    suspend fun getAllSongs() : List<Song>
    {
        Log.d(Constants.TAG, "MUSIC DATABASE GETALLSONGS STARTED")
        return try {
         songCollection.get().await().toObjects(Song::class.java)
        } catch (e : Exception)
        {
            emptyList()
        }

    }
}