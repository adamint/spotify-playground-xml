package dev.nielsg.spotifyplayground.model

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dev.nielsg.spotifyplayground.SpotifyPlayground
import dev.nielsg.spotifyplayground.extensions.getStringNotNull
import dev.nielsg.spotifyplayground.extensions.saveBoolean
import dev.nielsg.spotifyplayground.extensions.saveString

data class Preferences(val app: SpotifyPlayground) {
    private val encryptedPreferences = EncryptedSharedPreferences
        .create(
            "preferences",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            app,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    var spotifyAccessToken: String
        get() = encryptedPreferences.getStringNotNull(Constants.SPOTIFY_ACCESS_TOKEN)
        set(token) = encryptedPreferences.saveString(Constants.SPOTIFY_ACCESS_TOKEN, token)

    var spotifyRefreshToken: String
        get() = encryptedPreferences.getStringNotNull(Constants.SPOTIFY_REFRESH_TOKEN)
        set(token) = encryptedPreferences.saveString(Constants.SPOTIFY_REFRESH_TOKEN, token)

    var spotifyAuthCode: String
        get() = encryptedPreferences.getStringNotNull(Constants.SPOTIFY_USER_PKCE_AUTH_CODE)
        set(token) = encryptedPreferences.saveString(Constants.SPOTIFY_USER_PKCE_AUTH_CODE, token)

    var spotifyCodeVerifier: String
        get() = encryptedPreferences.getStringNotNull(Constants.SPOTIFY_USER_PKCE_CODE_VERIFIER)
        set(token) = encryptedPreferences.saveString(Constants.SPOTIFY_USER_PKCE_CODE_VERIFIER, token)

    var spotifyCodeState: String
        get() = encryptedPreferences.getStringNotNull(Constants.SPOTIFY_USER_PKCE_CODE_STATE)
        set(token) = encryptedPreferences.saveString(Constants.SPOTIFY_USER_PKCE_CODE_STATE, token)

    var isUserWithoutSpotifyAccount: Boolean
        get() = encryptedPreferences.getBoolean(Constants.IS_USER_WITHOUT_SPOTIFY_ACCOUNT, false)
        set(value) = encryptedPreferences.saveBoolean(Constants.IS_USER_WITHOUT_SPOTIFY_ACCOUNT, value)

    fun clear(): Boolean = try {
        encryptedPreferences.edit().clear().commit()
    } catch (e: Exception) {
        // This might crash, encrypted preferences is still alpha...
        false
    }
}