package com.example.kotlinproject1.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import com.example.kotlinproject1.data.FirebaseMusicSource
import com.example.kotlinproject1.data.entities.Song
import com.example.kotlinproject1.other.Constants
import com.example.kotlinproject1.ui.utility.updatePlaylist
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private val songDummy = Song(mediaId = "0", imageUrl = "", songUrl = "", title = "", subtitle = "", duration = "" )

@HiltViewModel
class MusicViewModel @Inject constructor(

  // private val musicServiceHandler: MusicServiceHandler,
    private val firebaseMusicSource: FirebaseMusicSource,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    /*var duration by savedStateHandle.saveable{ mutableStateOf(0L) }
    var progress by savedStateHandle.saveable{ mutableStateOf(0f) }
    var progressString by savedStateHandle.saveable{ mutableStateOf("00:00") }
    var isPlaying by savedStateHandle.saveable{ mutableStateOf(false) }
    var currentSelectedAudio by savedStateHandle.saveable{ mutableStateOf(songDummy) }


   private var _musicList by savedStateHandle.saveable{ mutableStateOf(listOf<Song>()) }
        val musicList : List<Song>
        get() {
            Log.d(Constants.TAG, "Accessing MUSICLIST ")
            return _musicList
        }
*/

    private val _isPlay = MutableStateFlow(false)
    val isPlay = _isPlay.asStateFlow()

    private var _musicList : MutableStateFlow<List<Song>> = MutableStateFlow(emptyList())
    val musicList : StateFlow<List<Song>> = _musicList.asStateFlow()

  /*  private val _uiState: MutableStateFlow<UIState> = MutableStateFlow(UIState.Initial)
    val uiState : StateFlow<UIState> = _uiState.asStateFlow()*/

    @ApplicationContext
    @Inject
    lateinit var context : Context



    init {

        loadAudioData()

    }

    fun readyPlayer() {
        _isPlay.update {
            true
        }
    }
   /* init {
        viewModelScope.launch {
            musicServiceHandler.audioState.collectLatest{mediaState ->
                when(mediaState) {
                    AudioState.Initial -> _uiState.value = UIState.Initial
                  //  is AudioState.Buffering -> calculateProgressValue(mediaState.progress)
                    is AudioState.Playing -> isPlaying = mediaState.isPlaying
                 //   is AudioState.Progress -> calculateProgressValue(mediaState.progress)
                    is AudioState.CurrentPlaying -> {
                        currentSelectedAudio = musicList.value[mediaState.mediaItemIndex]
                    }
                    is AudioState.Ready -> {
                        duration = mediaState.duration
                        _uiState.value = UIState.Ready

                    }
                    else -> {}
                }

            }
        }
    }*/

   /* fun startMusicServiceHandler(handler: MusicServiceHandler) {
        Log.d(Constants.TAG, "START MUSIC SERVICE HANDLER ")
        musicServiceHandler = handler
        viewModelScope.launch {
            musicServiceHandler!!.audioState.collectLatest{mediaState ->
                when(mediaState) {
                    AudioState.Initial -> _uiState.value = UIState.Initial
                      is AudioState.Buffering -> calculateProgressValue(mediaState.progress)
                    is AudioState.Playing -> isPlaying = mediaState.isPlaying
                       is AudioState.Progress -> calculateProgressValue(mediaState.progress)
                    is AudioState.CurrentPlaying -> {
                        currentSelectedAudio = musicList[mediaState.mediaItemIndex]
                    }
                    is AudioState.Ready -> {
                        duration = mediaState.duration
                        _uiState.value = UIState.Ready

                    }
                   // else -> {}
                }

            }
        }

    }*/

    private fun loadAudioData() {
        Log.d(Constants.TAG, "VIEWMODEL LOAD AUDIO DATA STARTED ")
         viewModelScope.launch {
            Log.d(Constants.TAG,"LOADING DATA")
            _musicList.value = firebaseMusicSource.getMusicData()
           _musicList.value.forEach(){song->
               Log.d(Constants.TAG,"${song.mediaId} ${song.title}")
           }

         //   setMediaItems()
             readyPlayer()
            Log.d(Constants.TAG,"END")
        }
    }



   fun setMediaItems(mediaController: MediaController) {
        Log.d(Constants.TAG, "SET MEDIA ITEMS ")
        _musicList.value.map {song ->

            MediaItem.Builder()
                .setMediaId(song.mediaId)
                .setUri(song.songUrl)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setArtist(song.subtitle)
                        .setAlbumArtist(song.subtitle)
                        .setDisplayTitle(song.title)
                        .setTitle(song.title)
                        .setArtworkUri(song.imageUrl.toUri())
                        .build()
                )
                .build()

        }.also {
            mediaController.updatePlaylist(it)
        }
    }

    /*private fun calculateProgressValue(currentProgress: Long){
       // Log.d(Constants.TAG, "CALCULATE PROGRESS VALUE ")
        progress = if (currentProgress > 0) ( (currentProgress.toFloat()) / duration.toFloat()  * 100f )
        else 0f

        progressString = formatDuration(currentProgress)
    }*/

   /* fun onUiEvents(uiEvents : UIEvents) = viewModelScope.launch {
        Log.d(Constants.TAG, "VIEWMODEL ON UI EVENTS  ")
        when(uiEvents) {
            UIEvents.Backward -> musicServiceHandler?.onPlayerEvents(PlayerEvent.Backward)
            UIEvents.Forward -> musicServiceHandler?.onPlayerEvents(PlayerEvent.Forward)
            UIEvents.SeekToNext -> musicServiceHandler?.onPlayerEvents(PlayerEvent.SeekToNext)
            is UIEvents.PlayPause -> {
                Log.d(Constants.TAG, "POP VIEWMODEL ")
                musicServiceHandler?.onPlayerEvents(PlayerEvent.PlayPause)
            }
            is UIEvents.SeekTo -> {
                musicServiceHandler?.onPlayerEvents(
                    PlayerEvent.SeekTo ,
                    seekPosition = ((duration * uiEvents.position) / 100f).toLong()
                )
            }
            is UIEvents.SelectedAudioChange -> {
                musicServiceHandler?.onPlayerEvents(
                    PlayerEvent.SelectAudioChange,
                    selectedAudioIndex = uiEvents.index
                )
            }
         *//*   else -> {}*//*
            is UIEvents.UpdateProgress -> {
                musicServiceHandler?.onPlayerEvents(
                    PlayerEvent.UpdateProgress(
                        uiEvents.newProgress
                    )
                )
                progress = uiEvents.newProgress
            }
        }
    }*/

   private fun formatDuration(duration: Long) : String {
      // Log.d(Constants.TAG, "FORMAT DURATION ")
        val minute = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
        val seconds = (minute) - minute * TimeUnit.SECONDS.convert(1,TimeUnit.MINUTES)
        return String.format("%02d:%02d", minute, seconds)
    }



    override fun onCleared() {
        Log.d(Constants.TAG, "VIEWMODEL ONCLEARED")
       /* viewModelScope.launch {
            musicServiceHandler?.onPlayerEvents(PlayerEvent.Stop)
        }*/

        super.onCleared()
    }


}

sealed class UIEvents {
    object PlayPause: UIEvents()
    data class SelectedAudioChange(val index : Int) : UIEvents()
    data class SeekTo(val position : Float) : UIEvents()
    object SeekToNext : UIEvents()
    object Backward : UIEvents()
    object Forward : UIEvents()
    data class UpdateProgress(val newProgress: Float) : UIEvents()

}

sealed class UIState {
    object Initial: UIState()
    object Ready: UIState()
}