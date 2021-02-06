package dev.nielsg.spotifyplayground.network

import com.adamratzman.spotify.*
import com.adamratzman.spotify.models.Token
import com.adamratzman.spotify.models.Track
import com.google.gson.Gson
import dev.nielsg.spotifyplayground.BuildConfig
import dev.nielsg.spotifyplayground.extensions.log
import dev.nielsg.spotifyplayground.model.Constants
import dev.nielsg.spotifyplayground.model.Model
import dev.nielsg.spotifyplayground.model.objects.SpotifyToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await

class Spotify(val model: Model) {
    private var spotifyClientApi: SpotifyClientApi? = null
    private var spotifyAppApi: SpotifyAppApi? = null

    private fun handler(action: suspend () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                action.invoke()
            } catch (e: Exception) {
                // Catch unforeseen crashes
                e.printStackTrace()
            }
        }
    }

    private fun getApi(): GenericSpotifyApi? {
        return if (model.preferences.isUserWithoutSpotifyAccount) spotifyAppApi
        else spotifyClientApi
    }

    fun loadSpotifyApi(success: (() -> Unit)? = null, failed: ((error: Exception) -> Unit)? = null) {
        if (model.preferences.isUserWithoutSpotifyAccount) {
            model.spotify.loadAnonymousSpotify(null, success, failed)
        } else {
            model.spotify.loadUserSpotify(success, failed)
        }
    }

    fun loadAnonymousSpotify(
        token: Token? = Token(
            accessToken = model.preferences.spotifyAccessToken,
            tokenType = "Bearer",
            refreshToken = model.preferences.spotifyRefreshToken,
            expiresIn = 0
        ),
        success: (() -> Unit)? = null,
        failed: ((error: Exception) -> Unit)? = null
    ) {
        log("Loading Spotify api for client")

        GlobalScope.launch {
            try {
                spotifyAppApi = spotifyAppApi {
                    credentials {
                        clientId = BuildConfig.SPOTIFY_CLIENT_ID
                        clientSecret = BuildConfig.SPOTIFY_CLIENT_SECRET
                        redirectUri = Constants.SPOTIFY_REDIRECT_URI
                    }
                    authorization {
                        this.token = token
                    }
                    options {
                        this.enableLogger = true
                        this.allowBulkRequests = true
                        this.useCache = true
                        this.automaticRefresh = true
                    }
                }.build()

                spotifyAppApi?.token?.let { newToken ->
                    model.preferences.spotifyAccessToken = newToken.accessToken
                }

                log("Spotify Api created!")
                success?.invoke()
            } catch (e: Exception) {
                e.printStackTrace()
                failed?.invoke(e)
            }
        }
    }

    private fun loadUserSpotify(success: (() -> Unit)? = null, failed: ((error: Exception) -> Unit)? = null) {
        log("Loading Spotify Api for user")

        GlobalScope.launch {
            try {
                spotifyClientApi = spotifyClientApi {
                    credentials {
                        this.clientId = BuildConfig.SPOTIFY_CLIENT_ID
                        this.redirectUri = Constants.SPOTIFY_REDIRECT_URI
                    }
                    authentication {
                        this.token = Token(model.preferences.spotifyAccessToken, "Bearer", 0, model.preferences.spotifyRefreshToken)
                        this.pkceCodeVerifier = model.preferences.spotifyCodeVerifier
                    }
                    options {
                        this.enableLogger = true
                        this.allowBulkRequests = true
                        this.useCache = true
                        this.automaticRefresh = true
                        this.testTokenValidity = true
                        this.onTokenRefresh = {
                            it.token.refreshToken?.let { refreshToken -> model.preferences.spotifyRefreshToken = refreshToken }
                            model.preferences.spotifyAccessToken = it.token.accessToken
                        }
                    }
                }.build()

                log("Spotify Api loaded!")

                spotifyClientApi?.let {
                    it.token.refreshToken?.let { refreshToken -> model.preferences.spotifyRefreshToken = refreshToken }
                    model.preferences.spotifyAccessToken = it.token.accessToken
                    success?.invoke()
                }
            } catch (error: Exception) {
                error.printStackTrace()
                if (error is SpotifyException.AuthenticationException && error.message?.contains("temporarily_unavailable") == true) {
                    error("Spotify temporarily unavailable")
                } else if (error is SpotifyException.AuthenticationException && error.message?.contains("Refresh token revoked") == true) {
                    // Some kind of bug at Spotify's side regarding PKCE auth
                    error("Spotify refresh token revoked")
                } else {
                    failed?.invoke(error)
                }
            }
        }
    }

    fun searchTracks(query: String, callback: (tracks: List<Track>?) -> Unit) {
        handler {
            getApi()?.search?.searchTrack(query)?.let { result ->
                callback.invoke(result.items)
            } ?: kotlin.run {
                callback.invoke(null)
            }
        }
    }

    suspend fun requestClientCredentialsToken(success: (token: SpotifyToken) -> Unit, failed: () -> Unit) {
        val request = Request.Builder().url(Constants.SPOTIFY_TOKEN_URL).post(
            body = FormBody.Builder().add("grant_type", "client_credentials").build()
        ).apply {
            this.addHeader("Authorization", Constants.spotifyAuthorizationHeader())
        }

        OkHttpClient().newCall(request.build()).await().use { response ->
            if (!response.isSuccessful) failed()

            try {
                success(Gson().fromJson(response.body?.string(), SpotifyToken::class.java))
            } catch (e: Exception) {
                e.printStackTrace()
                failed()
            }
        }
    }

    fun exchangeAuthorizationCodeForToken(authorizationCode: String, success: (token: Token) -> Unit, failed: () -> Unit) {
        GlobalScope.launch {
            val api = spotifyClientPkceApi(
                clientId = BuildConfig.SPOTIFY_CLIENT_ID,
                redirectUri = Constants.SPOTIFY_REDIRECT_URI,
                authorization = SpotifyUserAuthorization(
                    authorizationCode = authorizationCode,
                    pkceCodeVerifier = model.preferences.spotifyCodeVerifier
                )
            ) {
                this.enableLogger = true
                this.allowBulkRequests = true
                this.useCache = true
                this.automaticRefresh = true
            }.build()

            if (api.token.accessToken.isNotBlank()) {
                log("Spotify Api created from authorization code!")
                model.preferences.spotifyAccessToken = api.token.accessToken
                api.token.refreshToken?.let { refreshToken -> model.preferences.spotifyRefreshToken = refreshToken }
                model.preferences.spotifyAuthCode = authorizationCode
                success(api.token)
            } else {
                log("Failed to create Spotify Api from authorization code!")
                failed()
            }
        }
    }
}