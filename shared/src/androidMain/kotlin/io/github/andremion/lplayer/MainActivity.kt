package io.github.andremion.lplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.andremion.lplayer.domain.AudioPlayer
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val player by inject<AudioPlayer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { LPlayer() }
    }

    override fun onResume() {
        super.onResume()
        player.setMediaUri("https://cdns-preview-d.dzcdn.net/stream/c-ddf3ecfe031b0e38be1f7cef597d6af1-7.mp3")
//        player.play()
    }

    override fun onPause() {
        player.release()
        super.onPause()
    }
}
