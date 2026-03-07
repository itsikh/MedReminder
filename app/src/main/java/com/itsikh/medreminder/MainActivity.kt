package com.itsikh.medreminder

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.itsikh.medreminder.bugreport.CrashAutoReporter
import com.itsikh.medreminder.ui.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The single Activity for the app.
 *
 * This template uses a single-Activity architecture with Jetpack Compose and
 * Compose Navigation. All screens are composables mounted inside [AppNavHost].
 *
 * ## Why FragmentActivity (not ComponentActivity)?
 * [androidx.biometric.BiometricPrompt] requires a [FragmentActivity] as its host.
 * [security.BiometricHelper] and [security.ClearDataConfirmationDialog] both need it,
 * so this must stay as [FragmentActivity] for biometric auth to work.
 *
 * ## Hilt
 * [@AndroidEntryPoint] enables field injection into this Activity and into any
 * composable that uses `hiltViewModel()` within [setContent].
 *
 * ## Crash auto-reporting
 * On every launch, [CrashAutoReporter.checkAndReport] is called. It no-ops if there
 * is no pending crash log. If a crash log exists from a previous session, it automatically
 * files a GitHub issue with device info and log content, then clears the log.
 *
 * ## Edge-to-edge
 * [enableEdgeToEdge] is called before [setContent] so the app draws behind the system
 * bars. Each screen is responsible for consuming the window insets via [Scaffold] or
 * explicit padding modifiers.
 *
 * ## Replacing the UI
 * To change the navigation structure, edit [AppNavHost].
 * To change the entry screen, change `startDestination` in [AppNavHost].
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var crashAutoReporter: CrashAutoReporter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkTheme = isSystemInDarkTheme()
            val colorScheme = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                    if (darkTheme) dynamicDarkColorScheme(this) else dynamicLightColorScheme(this)
                darkTheme -> darkColorScheme()
                else -> lightColorScheme()
            }
            MaterialTheme(colorScheme = colorScheme) {
                AppNavHost()
            }
        }
        lifecycleScope.launch { crashAutoReporter.checkAndReport() }
    }
}
