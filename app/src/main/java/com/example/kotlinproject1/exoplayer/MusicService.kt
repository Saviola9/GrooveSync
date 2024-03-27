package com.example.kotlinproject1.exoplayer


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.app.TaskStackBuilder
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.kotlinproject1.MainActivity
import com.example.kotlinproject1.R
import com.example.kotlinproject1.other.Constants
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val NOTIFICATION_ID = 101
private const val NOTIFICATION_CHANNEL_NAME = "notification channel 1"
private const val NOTIFICATION_CHANNEL_ID = "notification channel id 1"

@AndroidEntryPoint
class MusicService : MediaSessionService() {


  // private var mediaLibrarySession: MediaLibrarySession? = null
     private var mediaLibrarySession: MediaSession? = null
    @ApplicationContext
    @Inject
    lateinit var context:Context

    @Inject
    lateinit var exoPlayer : ExoPlayer

  /*  @Inject
    lateinit var audioAttributes : AudioAttributes*/

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        Log.d(Constants.TAG, "CREATING MEDIA LIBRARY SERVICE")


      /*  mediaLibrarySession = MediaLibrarySession.Builder(this,exoPlayer,MyCallback()
        ).also { builder ->
        getSingleTopActivity()?.let { builder.setSessionActivity(it) }

        }.build()
*/
      /*  exoPlayer = ExoPlayer.Builder(context)
            .setTrackSelector(DefaultTrackSelector(context))
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
*/
        mediaLibrarySession = MediaSession.Builder(this,exoPlayer).also { builder ->  getSingleTopActivity()?.let { builder.setSessionActivity(it) } }.build()
        setListener(MediaLibrarySessionListener())

     //   setMediaNotificationProvider(DefaultMediaNotificationProvider(context))




      /*  setMediaNotificationProvider(object : MediaNotification.Provider{
            override fun createNotification(
                mediaSession: MediaSession,
                customLayout: ImmutableList<CommandButton>,
                actionFactory: MediaNotification.ActionFactory,
                onNotificationChangedCallback: MediaNotification.Provider.Callback
            ): MediaNotification {

                    return updateNotification(mediaSession)

            }

            override fun handleCustomCommand(
                session: MediaSession,
                action: String,
                extras: Bundle
            ): Boolean {
                return false
            }

        })
*/



    }


    /*override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }*/

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaLibrarySession
    }

    private fun getSingleTopActivity() : PendingIntent? {
        return PendingIntent.getActivity(
            this,0,
            Intent(this,MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getBackStackedActivity() : PendingIntent? {
        return TaskStackBuilder.create(this).run {
            addNextIntent(Intent(this@MusicService, MainActivity::class.java))
            getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT )
        }
    }


    fun updateNotification(mediaSession: MediaSession) : MediaNotification
    {
        val notificationManager : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Log.d(Constants.TAG, "Creating Notification Manager")


        val bitmap = Glide.with(context).asBitmap().
        load(mediaSession.player.mediaMetadata.artworkUri)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .submit()
            .get()


        @androidx.annotation.OptIn(UnstableApi::class)
        val notify = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(mediaSession.player.mediaMetadata.albumTitle)
            .setContentText(mediaSession.player.mediaMetadata.displayTitle)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
           // .setContentIntent(mediaSession.sessionActivity)
            .build()

       this.startForeground(NOTIFICATION_ID,notify)
        return MediaNotification(NOTIFICATION_ID,notify)


    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        //super.onTaskRemoved(rootIntent)
        Log.d(Constants.TAG, "ONTASKREMOVED MEDIA LIBRARY SERVICE STARTING")
        val player = mediaLibrarySession!!.player
        if(!player.playWhenReady || player.mediaItemCount == 0)
            stopSelf()

    }

    @OptIn(UnstableApi::class)
    override fun onDestroy() {

        Log.d(Constants.TAG, "DESTROYING MEDIA LIBRARY SERVICE")
       mediaLibrarySession?.run {

           getBackStackedActivity()?.let {
                setSessionActivity(it)
           }

            if(player.playbackState != Player.STATE_IDLE)
            {
                player.seekTo(0)
                player.playWhenReady = false
                player.stop()
            }
                player.release()

                release()
                mediaLibrarySession = null

        }
        clearListener()
        super.onDestroy()
    }

    @OptIn(UnstableApi::class)
    private inner class MediaLibrarySessionListener : Listener {
        override fun onForegroundServiceStartNotAllowedException() {
            Log.d(Constants.TAG, "MEDIASERVICE LISTENER ONFOREGROUNDSERVICESTARTNOTALLOWEDEXCEPTION")
            val notificationManager = NotificationManagerCompat.from(this@MusicService)
            createNotificationChannel(notificationManager)

            val builder = Notification.Builder(this@MusicService, NOTIFICATION_CHANNEL_ID)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setAutoCancel(true)
                .also { builder -> getBackStackedActivity()?.let { builder.setContentIntent(it) }
                }

        //    notificationManager.notify(NOTIFICATION_ID, builder.build())
            startForeground(1,builder.build())



        }

        private fun createNotificationChannel(notificationManager: NotificationManagerCompat) {
            if(Build.VERSION.SDK_INT < 26 ||
                notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) != null)
            {
                return
            }

            notificationManager.createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME,NotificationManager.IMPORTANCE_LOW)
            )
        }

    }

/*

     inner class MyCallback() : MediaLibrarySession.Callback {
          override fun onGetLibraryRoot(
              session: MediaLibrarySession,
              browser: MediaSession.ControllerInfo,
              params: LibraryParams?
          ): ListenableFuture<LibraryResult<MediaItem>> {
              return super.onGetLibraryRoot(session, browser, params)
          }

          override fun onGetItem(
              session: MediaLibrarySession,
              browser: MediaSession.ControllerInfo,
              mediaId: String
          ): ListenableFuture<LibraryResult<MediaItem>> {
              return super.onGetItem(session, browser, mediaId)
          }

          override fun onGetChildren(
              session: MediaLibrarySession,
              browser: MediaSession.ControllerInfo,
              parentId: String,
              page: Int,
              pageSize: Int,
              params: LibraryParams?
          ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {

              return super.onGetChildren(session, browser, parentId, page, pageSize, params)
          }
      }
*/



}