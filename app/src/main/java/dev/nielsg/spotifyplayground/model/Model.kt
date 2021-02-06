package dev.nielsg.spotifyplayground.model

import dev.nielsg.spotifyplayground.SpotifyPlayground
import dev.nielsg.spotifyplayground.network.Spotify

data class Model(val app: SpotifyPlayground) {
    val preferences = Preferences(app)
    var spotify = Spotify(this)
}