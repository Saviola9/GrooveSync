package com.example.kotlinproject1.exoplayer

import android.provider.MediaStore.Audio.Media
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.kotlinproject1.other.Constants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import javax.inject.Inject

class MusicServiceHandler //@Inject constructor
 (
         private val exoPlayer: Player
   // private val exoPlayer: ExoPlayer,
    //private val coroutineScope: CoroutineScope,
) : Player.Listener
{
private val _audioState : MutableStateFlow<AudioState> = MutableStateFlow(AudioState.Initial)
val audioState : StateFlow<AudioState> = _audioState.asStateFlow()

//Experimenting it with changing value to null from coroutineScope.coroutineContext.job
private var job : Job? = null

    init {
        Log.d(Constants.TAG, "MUSIC HANDLER STARTING")
        exoPlayer.addListener(this)

    }

fun addMediaItem(mediaItem: MediaItem) {
    exoPlayer.setMediaItem(mediaItem)
    exoPlayer.prepare()
}

    fun addMediaItemList(mediaItems: List<MediaItem>){
        Log.d(Constants.TAG, "ADD MEDIA ITEMS LIST")
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
    }

    suspend fun onPlayerEvents(
        playerEvent : PlayerEvent,
        selectedAudioIndex: Int = -1,
        seekPosition:Long = 0
    ){
        Log.d(Constants.TAG, "OnPlayer Events Called")
        when(playerEvent) {

            PlayerEvent.Backward -> {
                exoPlayer.seekBack()
            }
            PlayerEvent.Forward -> {
                exoPlayer.seekForward()
            }
            PlayerEvent.SeekToNext -> {
                exoPlayer.seekToNext()
            }
            PlayerEvent.PlayPause -> {
                Log.d(Constants.TAG, "POP MUSIC SERVICE HANDLER")
                playOrPause()
            }
            PlayerEvent.SeekTo -> {
                exoPlayer.seekTo(seekPosition)
            }
            PlayerEvent.SelectAudioChange -> {
                when(selectedAudioIndex) {
                    exoPlayer.currentMediaItemIndex -> {
                        playOrPause()
                    }
                    else -> {
                        exoPlayer.seekToDefaultPosition(selectedAudioIndex)
                        _audioState.value = AudioState.Playing(isPlaying = true)
                        exoPlayer.playWhenReady = true
                        startProgressUpdate()
                    }
                }
            }

            PlayerEvent.Stop -> {
                stopProgressUpdate()
            }
            is PlayerEvent.UpdateProgress -> {
                exoPlayer.seekTo(
                    (exoPlayer.duration * playerEvent.newProgress).toLong()
                )
            }

        }

    }

    override fun onPlayerError(error: PlaybackException) {


        Log.d(Constants.TAG, "${error.errorCode}")
        Log.d(Constants.TAG, "${error.cause}")
        Log.d(Constants.TAG, "${error.stackTrace}")
        super.onPlayerError(error)
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        Log.d(Constants.TAG, "ON PLAYBACK STATE CHANGED")
        when(playbackState) {
            ExoPlayer.STATE_BUFFERING -> _audioState.value = AudioState.Buffering(exoPlayer.duration)
            ExoPlayer.STATE_READY ->_audioState.value = AudioState.Ready(exoPlayer.duration)
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        Log.d(Constants.TAG, "ONISPLAYING CHANGED STARTING")

        _audioState.value = AudioState.Playing(isPlaying = true)
        _audioState.value = AudioState.CurrentPlaying(exoPlayer.currentMediaItemIndex)
        if(isPlaying) {
            Log.d(Constants.TAG, "ONISPLAYING's isPlaying true ")
            GlobalScope.launch(Dispatchers.Main) {
                startProgressUpdate()
            }
        }
        else
        {
            Log.d(Constants.TAG, "ONISPLAYING's isPlaying false ")
                stopProgressUpdate()
        }
    }

    private suspend fun playOrPause(){
        Log.d(Constants.TAG, "PLAY OR PAUSE FUNCTION STARTING ")
        if(exoPlayer.isPlaying) {
            Log.d(Constants.TAG, "POP FUNCTION ISPLAYING IS TRUE ")
            exoPlayer.pause()

        }
        else
        {
            Log.d(Constants.TAG, "POP FUNCTION ISPLAYING IS FALSE ")
            exoPlayer.play()
            _audioState.value = AudioState.Playing(isPlaying = true)

        }
    }

    private suspend fun startProgressUpdate() = job.run {
        Log.d(Constants.TAG, "START PROGRESS UPDATE ")
        while(true){
            delay(500)
            _audioState.value = AudioState.Progress(exoPlayer.currentPosition)
        }
    }

    private fun stopProgressUpdate()
    {
        Log.d(Constants.TAG, "STOP PROGRESS UPDATE")
        job?.cancel()
        _audioState.value = AudioState.Playing(isPlaying = false)
    }


    fun dispose() {
        Log.d(Constants.TAG, "DISPOSE LISTENER ")
        exoPlayer.removeListener(this)
    }

}
sealed class PlayerEvent {

    object PlayPause : PlayerEvent()
    object SelectAudioChange : PlayerEvent()
    object Backward : PlayerEvent()
    object SeekToNext : PlayerEvent()
    object Forward : PlayerEvent()
    object SeekTo : PlayerEvent()
    object Stop : PlayerEvent()
    data class UpdateProgress(val newProgress : Float) : PlayerEvent()
}


sealed class AudioState {
    object Initial:AudioState()
    data class Ready(val duration: Long) : AudioState()
    data class Progress(val progress : Long) : AudioState()
    data class Buffering(val progress : Long) : AudioState()
    data class Playing(val isPlaying : Boolean) : AudioState()
    data class CurrentPlaying(val mediaItemIndex : Int) : AudioState()
}