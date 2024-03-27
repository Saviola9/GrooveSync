package com.example.kotlinproject1.exoplayer

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.kotlinproject1.other.Constants
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


@Composable
fun rememberManagedMediaController(
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle
) : State<MediaController?> {

    val appContext = LocalContext.current.applicationContext
    val controllerManager = remember {MediaControllerManager.getInstance(appContext)}

    DisposableEffect(key1 = lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when(event) {
                Lifecycle.Event.ON_START -> {
                    Log.d(Constants.TAG, "LIFECYCLE ON START")
                    controllerManager.initialize()}

                Lifecycle.Event.ON_STOP ->{
                    Log.d(Constants.TAG, "LIFECYCLE ON STOP")
                    controllerManager.release()
                }
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer)}

    }
    return controllerManager.controller
}





@Stable
internal class MediaControllerManager private constructor(context : Context) : RememberObserver {

   private val appContext = context.applicationContext
    private var factory : ListenableFuture<MediaController>? = null

    var controller = mutableStateOf<MediaController?>(null)
        private set

    init {
        initialize()
    }

     fun initialize() {
         Log.d(Constants.TAG, "MEDIACONTROLLER MANAGER INITIALIZE")

        if(factory == null || factory?.isDone == true) {
            factory = MediaController.Builder(appContext,
                SessionToken(appContext, ComponentName(appContext, MusicService::class.java))).buildAsync()
        }

        factory?.addListener(
            {
                controller.value = factory?.let {
                    if(it.isDone)
                        it.get()
                    else
                        null
                }
            },
            MoreExecutors.directExecutor()
            //ContextCompat.getMainExecutor(appContext)
        )
    }

    internal fun release() {

        Log.d(Constants.TAG, "RELEASING MEDIACONTROLLER ")

        factory?.let {
            MediaController.releaseFuture(it)
            controller.value = null
        }
        factory = null

    }

    override fun onAbandoned() {
        release()
    }

    override fun onForgotten() {
        release()
    }

    override fun onRemembered() {
    }




    companion object {
        @Volatile
        private var instance : MediaControllerManager? = null

        fun getInstance(context: Context) : MediaControllerManager {

            return instance?: synchronized(this) {
                instance?: MediaControllerManager(context).also { instance = it }
            }
        }
    }
}