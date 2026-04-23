package com.izmir.avmmap.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    secondary = BlueGrey80,
    tertiary = Teal80,
    background = DarkCardBackgroundColor,
    surface = DarkCardBackgroundColor,
    onPrimary = Blue40,
    onSecondary = BlueGrey40,
    onTertiary = Teal40
)

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    secondary = BlueGrey40,
    tertiary = Teal40,
    background = CardBackgroundColor,
    surface = CardBackgroundColor,
    onPrimary = Blue80,
    onSecondary = BlueGrey80,
    onTertiary = Teal80
)

/**
 * İzmir AVM Map uygulama teması.
 * Koyu ve açık tema desteği sağlar.
 */
@Composable
fun IzmirAVMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
