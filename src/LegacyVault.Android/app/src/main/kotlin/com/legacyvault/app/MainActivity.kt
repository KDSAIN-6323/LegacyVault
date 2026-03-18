package com.legacyvault.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import com.legacyvault.app.ui.auth.InactivityManager
import com.legacyvault.app.ui.navigation.AppNavGraph
import com.legacyvault.app.ui.theme.LegacyVaultTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var inactivityManager: InactivityManager

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        inactivityManager.start()

        setContent {
            LegacyVaultTheme {
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
