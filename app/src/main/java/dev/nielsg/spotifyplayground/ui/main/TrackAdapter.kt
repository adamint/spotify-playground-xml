package dev.nielsg.spotifyplayground.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.adamratzman.spotify.models.Track
import dev.nielsg.spotifyplayground.R
import dev.nielsg.spotifyplayground.extensions.loadTrackImage
import dev.nielsg.spotifyplayground.model.Model
import kotlinx.android.synthetic.main.item_track.view.*

class TrackAdapter(val model: Model, private val onClick: (track: Track) -> Unit) : RecyclerView.Adapter<TrackAdapter.ViewHolder>() {
    var tracks: List<Track> = arrayListOf()
        set(tracks) {
            field = tracks
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false))

    override fun getItemCount(): Int = tracks.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        tracks.getOrNull(holder.adapterPosition)?.let { song ->
            holder.image.loadTrackImage(song.album.images.getOrNull(0)?.url)
            holder.title.text = song.name
            holder.artists.text = song.artists.joinToString(", ") { it.name }
            holder.container.setOnClickListener {
                onClick(song)
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: AppCompatImageView = view.album_cover
        val title: AppCompatTextView = view.title
        val artists: AppCompatTextView = view.artist
        val container: ConstraintLayout = view.container
    }
}