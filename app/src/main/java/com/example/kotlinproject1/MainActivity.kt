package com.example.kotlinproject1

 import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
 import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
 import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
 import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
 import androidx.compose.runtime.getValue
 import androidx.compose.runtime.mutableFloatStateOf
 import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
 import androidx.compose.runtime.setValue
 import androidx.compose.ui.Modifier
 import androidx.lifecycle.compose.collectAsStateWithLifecycle
 import com.example.kotlinproject1.data.entities.Song
 import com.example.kotlinproject1.exoplayer.rememberManagedMediaController
import com.example.kotlinproject1.other.Constants
import com.example.kotlinproject1.other.Constants.TAG
import com.example.kotlinproject1.ui.screen.HomeScreen
 import com.example.kotlinproject1.ui.viewmodel.MusicViewModel
 import com.example.kotlinproject1.ui.state.PlayerState
 import com.example.kotlinproject1.ui.state.state
 import com.example.kotlinproject1.ui.theme.KotlinProject1Theme
 import com.example.kotlinproject1.ui.utility.playMediaAt
 import dagger.hilt.android.AndroidEntryPoint
 import kotlinx.coroutines.delay


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

   private val viewModel by viewModels<MusicViewModel>()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            KotlinProject1Theme {


                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {


                    val isPlayerSetUp by viewModel.isPlay.collectAsStateWithLifecycle()


                    val mediaController by rememberManagedMediaController()

                    LaunchedEffect(key1 = isPlayerSetUp) {

                        if(isPlayerSetUp) {
                            Log.d(Constants.TAG, "ISPLAYER SETUP TRUE LAUNCHED EFFECT ")

                            viewModel.setMediaItems(mediaController!!)
                        }
                    }



                    var playerState : PlayerState? by remember {
                        mutableStateOf( mediaController?.state() )
                    }




                    DisposableEffect(key1 = mediaController) {

                        mediaController?.run {
                            Log.d(Constants.TAG, "MUSICSERVICE HANDLER DISPOSABLE EFFECT STARTING ")
                                playerState = state()
                        }
                        onDispose {
                            Log.d(Constants.TAG, "MUSICSERVICE HANDLER DISPOSE STARTING ")
                            playerState?.dispose()
                        }

                    }

                    var progress by remember {
                        mutableFloatStateOf(0f)
                    }
                    LaunchedEffect(key1 = mediaController?.isPlaying) {
                        while(mediaController?.isPlaying == true){
                                progress = mediaController?.currentPosition!!.toFloat() / mediaController?.duration!! * 100
                            delay(200)
                            }

                    }
                  //  var progress = playerState?.currentPosition!!.toFloat() / playerState?.duration!!

                    val musicList by viewModel.musicList.collectAsStateWithLifecycle()

                    HomeScreen(
                     //    progress = viewModel.progress,
                      //  progress = playerState!!.duration.toFloat() / playerState!!.currentPosition,
                        progress = progress,
                        onProgress = {
                                     // viewModel.onUiEvents(UIEvents.SeekTo(it))
                                    val position = it * playerState?.duration!! / 100
                                     mediaController?.seekTo(position.toLong())
                                     Log.d("SPOTIFY", "$it  ${playerState?.duration!!}  ${playerState?.currentPosition!!}   $position")
                                     },
                    //    isAudioPlaying = viewModel.isPlaying,
                        isAudioPlaying = (playerState?.isPlaying == true),
                       currentPlayingAudio = Song(mediaId = "1", imageUrl = "https://firebasestorage.googleapis.com/v0/b/kotlin-project1-ad2d0.appspot.com/o/photo.png?alt=media&token=b185673f-8d17-42a5-a5c9-61af26949f30", songUrl = "https://firebasestorage.googleapis.com/v0/b/kotlin-project1-ad2d0.appspot.com/o/I%20-%20Can.mp3?alt=media&token=5d14be4f-0cc6-4ce0-9aef-469ce23bc094", title = "I CAN", subtitle = "FINGAZZ", duration = "162000" ),
                     //   currentPlayingAudio = viewModel.currentSelectedAudio,
                        audioList = musicList,
                        onStart = {
                            Log.d(TAG, "PLAY OR PAUSE MAIN ACTIVITY ")

                          //  viewModel.onUiEvents(UIEvents.PlayPause)
                            if(playerState?.isPlaying == true)
                                mediaController?.pause()
                            else
                                mediaController?.play()

                            }
                            ,
                        onItemClick = {index ->

                            Log.d(TAG, "M.A ITEM CLICKED ")
                            mediaController?.playMediaAt(index)
                        },
                        onNext = {
                           // viewModel.onUiEvents((UIEvents.SeekToNext))
                            Log.d(TAG, "M.A ON NEXT ")
                            mediaController?.seekToNext()
                        }
                    )
                }
            }
        }
    }

    override fun onStop() {
        Log.d(TAG, "ONSTOP MAIN ACTIVITY")
        super.onStop()
    }




}



