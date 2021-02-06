package dev.nielsg.spotifyplayground.ui.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.nielsg.spotifyplayground.SpotifyPlayground
import dev.nielsg.spotifyplayground.model.Model

abstract class BaseActivity : AppCompatActivity() {
    protected lateinit var model: Model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = (application as SpotifyPlayground).model
    }
}
