package dev.nielsg.spotifyplayground.model

import android.util.Base64
import dev.nielsg.spotifyplayground.BuildConfig

object Constants {
    const val SPOTIFY_TOKEN_URL = "https://accounts.spotify.com/api/token/"
    const val SPOTIFY_ACCESS_TOKEN = "spotifyAccessToken"
    const val SPOTIFY_REFRESH_TOKEN = "spotifyRefreshToken"
    const val SPOTIFY_REDIRECT_URI = "spotifyplayground://spotify"
    const val IS_USER_WITHOUT_SPOTIFY_ACCOUNT = "isUserWithoutSpotifyAccount"

    const val SPOTIFY_USER_PKCE_AUTH_CODE = "spotifyAuthCode"
    const val SPOTIFY_USER_PKCE_CODE_VERIFIER = "spotifyCodeVerifier"
    const val SPOTIFY_USER_PKCE_CODE_STATE = "spotifyCodeState"

    fun spotifyAuthorizationHeader() = "Basic ${Base64.encodeToString("${BuildConfig.SPOTIFY_CLIENT_ID}:${BuildConfig.SPOTIFY_CLIENT_SECRET}".toByteArray(), Base64.NO_WRAP)}"
}