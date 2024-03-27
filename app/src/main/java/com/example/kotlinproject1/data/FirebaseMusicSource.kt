package com.example.kotlinproject1.data

import android.util.Log
import com.example.kotlinproject1.data.entities.Song
import com.example.kotlinproject1.data.remote.MusicDatabase
import com.example.kotlinproject1.other.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseMusicSource @Inject constructor(
    private val musicDatabase: MusicDatabase
) {

    suspend fun getMusicData() : List<Song> = withContext(Dispatchers.IO) {
        Log.d(Constants.TAG, "FB MUSIC SOURCE GETMUSICDATA STARTED ")
        musicDatabase.getAllSongs()
    }


}