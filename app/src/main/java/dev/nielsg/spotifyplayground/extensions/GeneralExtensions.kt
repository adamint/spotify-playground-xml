package dev.nielsg.spotifyplayground.extensions

import android.content.Intent
import android.content.SharedPreferences
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.StringRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import dev.nielsg.spotifyplayground.model.Constants
import dev.nielsg.spotifyplayground.ui.common.BaseActivity
import dev.nielsg.spotifyplayground.utils.Utils

// safeLet retrieved from: https://stackoverflow.com/a/35522422/6422820
fun <T1 : Any, T2 : Any, R : Any> Any.safeLet(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? =
    if (p1 != null && p2 != null) block(p1, p2) else null

// safeLet retrieved from: https://stackoverflow.com/a/35522422/6422820
fun <T1 : Any, T2 : Any, T3 : Any, R : Any> Any.safeLet(p1: T1?, p2: T2?, p3: T3?, block: (T1, T2, T3) -> R?): R? =
    if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null

fun Intent?.isSpotifyAuthIntent() : Boolean = (this != null && this.dataString?.startsWith("${Constants.SPOTIFY_REDIRECT_URI}/?code=") == true)

fun SharedPreferences.saveString(key: String, value: String) = this.edit().putString(key, value).apply()

fun SharedPreferences.saveBoolean(key: String, value: Boolean) = this.edit().putBoolean(key, value).apply()

fun SharedPreferences.getStringNotNull(key: String, defaultValue: String? = ""): String = this.getString(key, defaultValue) ?: defaultValue ?: ""

fun BaseActivity.toast(message: String, duration: Int = Toast.LENGTH_SHORT) = Utils.toast(this, message, duration)

fun BaseActivity.toast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) = Utils.toast(this, getString(message), duration)

fun ImageView.loadTrackImage(image: Any?) {
    safeLoadImage { Glide.with(this).load(image).transform(RoundedCorners(8)).into(this) }
}

private fun safeLoadImage(action: () -> Unit) {
    try {
        action.invoke()
    } catch (e: IllegalArgumentException) {
        // Possible error: You cannot start a load for a destroyed activity
    }
}
