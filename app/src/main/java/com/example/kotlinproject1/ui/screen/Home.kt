package com.example.kotlinproject1.ui.screen

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.kotlinproject1.R
import com.example.kotlinproject1.data.entities.Song
import com.example.kotlinproject1.other.Constants
import com.example.kotlinproject1.ui.theme.KotlinProject1Theme
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    progress : Float,
    onProgress : (Float) -> Unit,
    isAudioPlaying : Boolean,
    currentPlayingAudio : Song,
    audioList : List<Song>,
    onStart : () -> Unit,
    onItemClick : (Int) -> Unit,
    onNext: ()->Unit
) {
    Scaffold(
        bottomBar = {
            BottomBarPlayer(
                progress = progress,
                onProgress = onProgress,
                audio = currentPlayingAudio,
                isAudioPlaying = isAudioPlaying,
                onStart = onStart,
                onNext = onNext)
        }
    ) {
        LazyColumn(contentPadding = it) {

            itemsIndexed(audioList) { index, audio ->

                AudioItem(audio = audio, //onItemClick = {onItemClick(index)},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color.White)
                        .padding(16.dp)
                        .clickable {
                            onItemClick(index)
                        }
                )

            }

        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AudioItem(
    audio : Song,
  //  onItemClick : () -> Unit,
    modifier : Modifier
) {
    Log.d(Constants.TAG, "AUDIO ITEM ${audio.title} ")

    Row(modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment =Alignment.CenterVertically
    ) {


       /* Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
              //  .clickable { onItemClick() }
        ) {*/
            Row(
                modifier = Modifier.padding(end = 8.dp)
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {


                PlayerArtworkView(
                    modifier = Modifier
                        .padding(4.dp),
                    artworkUri = audio.imageUrl.toUri()
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Center
                ) {

                    Spacer(modifier = Modifier.size(4.dp))

                    Text(
                        text = audio.title,
                        style = MaterialTheme.typography.titleLarge,
                        overflow = TextOverflow.Clip,
                        maxLines = 1,

                        )

                    Spacer(modifier = Modifier.size(4.dp))

                    Text(
                        text = audio.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        fontSize = 13.sp
                    )


                }

                Text(text = timeStampToDuration(audio.duration.toLong()))

                Spacer(modifier = Modifier.size(8.dp))


            }


        //}
    }

}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PlayerArtworkView(
    modifier: Modifier = Modifier,
    artworkUri: Uri?
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp)
    ) {
        GlideImage(model = artworkUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(50.dp)
                .clip(shape = MaterialTheme.shapes.medium)
               // .padding(8.dp)
            )
    }
}


private fun timeStampToDuration(position : Long) : String {
    val totalSecond = floor(position / 1E3).toInt()
    val minutes = totalSecond / 60
    val remainingSeconds = totalSecond - (minutes * 60)
    return if(position < 0) "--:--"
    else "%d:%02d".format(minutes,remainingSeconds) 
}


@Composable
fun BottomBarPlayer(
    progress: Float,
    onProgress: (Float) -> Unit,
    audio : Song,
    isAudioPlaying: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit
) {
    BottomAppBar (
        content = {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    ArtisInfo(
                        audio = audio,
                        modifier = Modifier.weight(1f),
                    )
                    MediaPlayerController(
                        isAudioPlaying = isAudioPlaying,
                        onStart = onStart,
                        onNext = onNext
                    )

                    Slider(
                        value = progress, onValueChange = { onProgress(it) },
                        valueRange = 0f..100f
                    )
                }
            }

        }
    )

}

@Composable
fun MediaPlayerController(
    isAudioPlaying: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        modifier = Modifier
            .height(56.dp)
            .padding(4.dp)
    )  {
        PlayerIconItem(icon = if (isAudioPlaying) ImageVector.vectorResource(id = R.drawable.baseline_pause_circle_24)
                         else Icons.Default.PlayArrow,
            onClick = onStart,
            modifier = Modifier.size(8.dp))

        Icon(painter = painterResource(id = R.drawable.baseline_skip_next_24), contentDescription = null,
            modifier = Modifier.clickable {
                onNext()
            }
            )
    }
}

@Composable
fun ArtisInfo(
    modifier : Modifier = Modifier,
    audio : Song
) {
    Row(
        modifier = modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerIconItem(icon = ImageVector.vectorResource(id = R.drawable.baseline_music_note_24),
            borderStroke = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface
            )

        ) {}

        Spacer(modifier = Modifier.size(4.dp))

        Column {

            Text(text = audio.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                overflow = TextOverflow.Clip,
                modifier = Modifier.weight(1f),
                maxLines = 1
                )

            Spacer(modifier = Modifier.size(4.dp))

            Text(text = audio.subtitle,
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Clip,
                maxLines = 1
            )
        }

    }
}

@Composable
fun PlayerIconItem(
    modifier : Modifier = Modifier,
    icon:ImageVector,
    borderStroke: BorderStroke? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        border = borderStroke,
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() },
        contentColor = color,
        color = backgroundColor
    ) {
        
        Box(modifier = Modifier.padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null)
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun HomeScreenPrev() {

    KotlinProject1Theme {
        HomeScreen(
            progress = 50f,
            onProgress = {},
            isAudioPlaying = true,
            audioList = listOf(
                Song(songUrl = "",
                    title = "Title 1",
                    subtitle = "Artist 1",
                    mediaId = "1",
                    duration = "235000",
                    imageUrl = ""),
                Song(songUrl = "",
                    title = "Title 2",
                    subtitle = "Artist 2",
                    mediaId = "2",
                    duration = "345000",
                    imageUrl = "https://firebasestorage.googleapis.com/v0/b/kotlin-project1-ad2d0.appspot.com/o/photo.png?alt=media&token=b185673f-8d17-42a5-a5c9-61af26949f30"),
                ),
            currentPlayingAudio = Song(songUrl = "",
                title = "Title 1",
                subtitle = "Artist 1",
                mediaId = "1",
                duration = "235000",
                imageUrl = "https://firebasestorage.googleapis.com/v0/b/kotlin-project1-ad2d0.appspot.com/o/photo.png?alt=media&token=b185673f-8d17-42a5-a5c9-61af26949f30"),
            onStart = {},
            onItemClick = {},
            onNext = {}
            )

    }

}