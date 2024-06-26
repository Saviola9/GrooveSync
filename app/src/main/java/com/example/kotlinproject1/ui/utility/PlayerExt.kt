package com.example.kotlinproject1.ui.utility

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.kotlinproject1.ui.state.PlayerState


internal val Player.currentMediaItems: List<MediaItem> get() {
    return List(mediaItemCount, ::getMediaItemAt)
}

fun Player.updatePlaylist(incoming: List<MediaItem>) {
    val oldMediaIds = currentMediaItems.map { it.mediaId }.toSet()
    val itemsToAdd = incoming.filterNot { item -> item.mediaId in oldMediaIds }
    Log.d("PlayerExt", "updatePlaylist: itemsToAdd: $itemsToAdd")
    addMediaItems(itemsToAdd)
}


fun Player.playMediaAt(index: Int) {
    if (currentMediaItemIndex == index)
        return
    seekToDefaultPosition(index)
    playWhenReady = true
    // Recover from any errors that may have happened at previous media positions
    prepare()
}

val PlayerState.isBuffering get() = playbackState == Player.STATE_BUFFERING