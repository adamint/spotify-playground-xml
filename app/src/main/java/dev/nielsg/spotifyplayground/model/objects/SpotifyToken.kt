package dev.nielsg.spotifyplayground.model.objects

import com.adamratzman.spotify.models.Token
import com.google.gson.annotations.SerializedName

class SpotifyToken(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("scope") val scope: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("refresh_token") val refreshToken: String
) {
    fun asToken(): Token = Token(accessToken = this.accessToken, refreshToken = refreshToken, tokenType = this.tokenType, scopeString = this.scope, expiresIn = this.expiresIn)
}