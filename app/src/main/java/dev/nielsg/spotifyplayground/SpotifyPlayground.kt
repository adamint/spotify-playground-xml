package dev.nielsg.spotifyplayground


import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import dev.nielsg.spotifyplayground.model.Model

class SpotifyPlayground : Application() {
    lateinit var model: Model

    override fun onCreate() {
        super.onCreate()
        model = Model(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}