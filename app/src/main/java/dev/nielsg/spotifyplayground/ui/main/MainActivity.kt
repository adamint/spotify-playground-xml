package dev.nielsg.spotifyplayground.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import dev.nielsg.spotifyplayground.R
import dev.nielsg.spotifyplayground.extensions.toast
import dev.nielsg.spotifyplayground.ui.auth.SpotifyAuthActivity
import dev.nielsg.spotifyplayground.ui.common.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    private lateinit var trackAdapter: TrackAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (model.preferences.spotifyAccessToken.isEmpty()) {
            startActivity(Intent(this, SpotifyAuthActivity::class.java))
            finish()
            return
        }

        trackAdapter = TrackAdapter(model) { track ->
            toast("${track.name}: ${track.artists.joinToString(", ") { it.name }}")
        }

        tracks_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        tracks_list.adapter = trackAdapter

        model.spotify.loadSpotifyApi(
            success = { searchTracks() },
            failed = { toast(R.string.spotify_api_load_failed) }
        )

        spotify_logout_action.setOnClickListener {
            if (model.preferences.clear()) {
                startActivity(Intent(this, SpotifyAuthActivity::class.java))
                finish()
            } else {
                toast(R.string.logout_failed)
            }
        }
    }

    private fun searchTracks() {
        model.spotify.searchTracks("Avicii") { tracks ->
            tracks?.let {
                runOnUiThread {
                    trackAdapter.tracks = tracks
                }
            } ?: run {
                toast(getString(R.string.tracks_search_failed))
            }
        }
    }
}