package com.example.kotlinproject1.di

import android.app.Application
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.kotlinproject1.R
import com.example.kotlinproject1.data.FirebaseMusicSource
import com.example.kotlinproject1.data.remote.MusicDatabase
import com.example.kotlinproject1.exoplayer.MusicServiceHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions().placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
    )

    @Singleton
    @Provides
    fun providesMusicDatabase() : MusicDatabase = MusicDatabase()

    @DefaultDispatcher
    @Provides
    fun providesDefaultDispatcher() : CoroutineDispatcher = Dispatchers.Default

    @MainDispatcher
    @Provides
    fun providesMainDispatcher() : CoroutineDispatcher = Dispatchers.Main

    @IoDispatcher
    @Provides
    fun provideIoDispatcher() : CoroutineDispatcher = Dispatchers.IO

    @Singleton
    @Provides
    fun provideCoroutineScope(
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
    ) : CoroutineScope = CoroutineScope(SupervisorJob() + defaultDispatcher)


    @Singleton
    @Provides
    fun provideAudioAttributes() =  AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @OptIn(UnstableApi::class)
    @Singleton
    @Provides
    fun provideExoPlayer(
        @ApplicationContext context : Context,
        audioAttributes: AudioAttributes
    ) : ExoPlayer = ExoPlayer.Builder(context)
        .setTrackSelector(DefaultTrackSelector(context))
        .setAudioAttributes(audioAttributes, true)
        .setHandleAudioBecomingNoisy(true)
        .build()

  /*  @Provides
    fun provideMediaServiceHandler(
        exoPlayer: ExoPlayer,
        coroutineScope: CoroutineScope
    ) : MusicServiceHandler = MusicServiceHandler(exoPlayer,coroutineScope)
*/

}