package top.role.android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.staticCompositionLocalOf

val LocalTopRoleDarkTheme = staticCompositionLocalOf { false }

private val DarkColorScheme = darkColorScheme(
    primary = TopRolePrimaryDark,
    onPrimary = Color(0xFF003919),
    primaryContainer = TopRolePrimarySoftDark,
    onPrimaryContainer = Color(0xFFB7F5CC),
    secondary = TopRoleSecondaryDark,
    tertiary = TopRoleTertiaryDark,
    background = Color(0xFF111612),
    surface = Color(0xFF111612),
    onBackground = Color(0xFFE3E9E3),
    onSurface = Color(0xFFE3E9E3),
    surfaceVariant = Color(0xFF414A43),
    onSurfaceVariant = Color(0xFFC1CAC2),
    outline = Color(0xFF8B958D),
)

private val LightColorScheme = lightColorScheme(
    primary = TopRolePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDFF5E6),
    onPrimaryContainer = Color(0xFF00210B),
    secondaryContainer = Color(0xFFDFF5E6),
    onSecondaryContainer = Color(0xFF003919),
    secondary = TopRoleSecondary,
    tertiary = TopRoleTertiary,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

@Composable
fun TopRoleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor &&
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S -> {

            val context = LocalContext.current

            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> { DarkColorScheme }

        else -> { LightColorScheme }
    }


    androidx.compose.runtime.CompositionLocalProvider(
        LocalTopRoleDarkTheme provides darkTheme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}