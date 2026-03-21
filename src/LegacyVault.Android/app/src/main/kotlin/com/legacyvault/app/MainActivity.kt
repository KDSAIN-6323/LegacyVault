package com.legacyvault.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.legacyvault.app.data.local.preferences.UserPreferencesDataStore
import com.legacyvault.app.ui.auth.InactivityManager
import com.legacyvault.app.ui.navigation.AppNavGraph
import com.legacyvault.app.ui.theme.LegacyVaultTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var inactivityManager: InactivityManager
    @Inject lateinit var userPreferences: UserPreferencesDataStore

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        inactivityManager.start()

        setContent {
            val theme    by userPreferences.theme.collectAsStateWithLifecycle(
                initialValue = UserPreferencesDataStore.DEFAULT_THEME
            )
            val fontSize by userPreferences.fontSize.collectAsStateWithLifecycle(
                initialValue = UserPreferencesDataStore.DEFAULT_FONT_SIZE
            )

            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (theme) {
                "dark"  -> true
                "light" -> false
                else    -> systemDark
            }
            val fontScale = when (fontSize) {
                "small" -> 0.85f
                "large" -> 1.15f
                else    -> 1.0f
            }

            LegacyVaultTheme(darkTheme = darkTheme, fontScale = fontScale) {
                // Reset inactivity timer on any pointer event anywhere in the app
                AppNavGraph(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInteropFilter { _ ->
                            inactivityManager.onUserActivity()
                            false   // don't consume — let the event propagate normally
                        }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        inactivityManager.stop()
    }
}
