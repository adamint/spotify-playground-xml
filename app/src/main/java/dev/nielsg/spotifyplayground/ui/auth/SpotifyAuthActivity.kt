package dev.nielsg.spotifyplayground.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import com.adamratzman.spotify.SpotifyScope
import com.adamratzman.spotify.getPkceAuthorizationUrl
import com.adamratzman.spotify.getSpotifyPkceCodeChallenge
import com.spotify.sdk.android.auth.AuthorizationResponse
import dev.nielsg.spotifyplayground.BuildConfig
import dev.nielsg.spotifyplayground.R
import dev.nielsg.spotifyplayground.extensions.isSpotifyAuthIntent
import dev.nielsg.spotifyplayground.extensions.logError
import dev.nielsg.spotifyplayground.extensions.toast
import dev.nielsg.spotifyplayground.model.Constants
import dev.nielsg.spotifyplayground.ui.common.BaseActivity
import dev.nielsg.spotifyplayground.ui.main.MainActivity
import kotlinx.android.synthetic.main.activity_spotify_auth.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random

class SpotifyAuthActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spotify_auth)

        spotify_user_login_action.setOnClickListener {
            loginSpotify()
        }

        spotify_anonymous_login_action.setOnClickListener {
            loginAnonymousSpotify()
        }
    }

    /**
     * Login with a Spotify account using the PKCE flow
     */
    private fun loginSpotify() {
        val scopes = arrayOf(
            SpotifyScope.STREAMING,
            SpotifyScope.APP_REMOTE_CONTROL,
            SpotifyScope.USER_MODIFY_PLAYBACK_STATE,
            SpotifyScope.USER_READ_CURRENTLY_PLAYING,
            SpotifyScope.USER_READ_PLAYBACK_STATE
        )

        GlobalScope.launch {
            model.preferences.spotifyCodeVerifier = (0..96).map { Random.nextInt(0, 9) }.joinToString(separator = "")
            model.preferences.spotifyCodeState = Random.nextLong().toString()

            val authUrl = getPkceAuthorizationUrl(
                scopes = scopes,
                clientId = BuildConfig.SPOTIFY_CLIENT_ID,
                redirectUri = Constants.SPOTIFY_REDIRECT_URI,
                codeChallenge = getSpotifyPkceCodeChallenge(model.preferences.spotifyCodeVerifier),
                state = model.preferences.spotifyCodeState
            ).toUri()

            val authIntent = Intent(Intent.ACTION_VIEW, authUrl)
            startActivity(authIntent)
        }
    }

    /**
     * Login without a Spotify account using the client credentials flow
     */
    private fun loginAnonymousSpotify() {
        GlobalScope.launch {
            model.spotify.requestClientCredentialsToken(
                success = { token ->
                    model.spotify.loadAnonymousSpotify(
                        token = token.asToken(),
                        success = {
                            startActivity(Intent(this@SpotifyAuthActivity, MainActivity::class.java))
                            finish()
                        },
                        failed = {
                            logError("Failed to load Spotify API")
                            toast(R.string.spotify_authentication_failed)
                        }
                    )
                }, failed = {
                    toast(R.string.spotify_authentication_failed)
                }
            )
        }
    }

    /**
     * User has accepted Spotify permissions at the website and has been redirected to the app, though the app was not open
     */
    override fun onResume() {
        super.onResume()
        if (intent?.isSpotifyAuthIntent() == true) {
            handleSpotifyAuthenticationResponse(AuthorizationResponse.fromUri(intent?.data))
        }
    }

    /**
     * User accepted Spotify permissions at the website and has been redirected to the app
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.data != null) setIntent(intent)
    }

    /**
     * Handle the authentication response, only allowing a "code" as response type
     */
    private fun handleSpotifyAuthenticationResponse(response: AuthorizationResponse) {
        if (response.type != AuthorizationResponse.Type.CODE) {
            if (response.type == AuthorizationResponse.Type.TOKEN || response.type == AuthorizationResponse.Type.ERROR ||
                response.type == AuthorizationResponse.Type.EMPTY || response.type == AuthorizationResponse.Type.UNKNOWN
            ) {
                toast(getString(R.string.spotify_authentication_failed))
            }

            return
        }

        GlobalScope.launch {
            val authorizationCode = response.code
            if (authorizationCode.isNullOrBlank()) return@launch toast(R.string.spotify_authentication_failed)

            model.spotify.exchangeAuthorizationCodeForToken(
                authorizationCode = authorizationCode,
                success = { token ->
                    token.refreshToken?.let { refreshToken -> model.preferences.spotifyRefreshToken = refreshToken }
                    model.preferences.spotifyAccessToken = token.accessToken

                    model.spotify.loadSpotifyApi({
                        startActivity(Intent(this@SpotifyAuthActivity, MainActivity::class.java))
                        finish()
                    }, {
                        logError("Failed to load Spotify API")
                        toast(R.string.spotify_authentication_failed)
                    })
                },
                failed = {
                    toast(R.string.spotify_authentication_failed)
                }
            )
        }
    }
}