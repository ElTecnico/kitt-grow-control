package com.example.kittgrowcontrol.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = InkPrimary,
    secondary = InkViolet,
    tertiary = InkCyan,
    background = InkBg,
    surface = InkCardBg,
    onPrimary = InkDarkText,
    onSecondary = InkDarkText,
    onTertiary = InkDarkText,
    onBackground = InkTextPrimary,
    onSurface = InkTextPrimary,
    onSurfaceVariant = InkTextSecondary,
    outline = InkBorder,
    primaryContainer = InkAmber,
    error = InkRed,
    onError = InkBg
)

private val DarkColorScheme = darkColorScheme(
    primary = CyberDarkPrimary,
    secondary = CyberDarkViolet,
    tertiary = CyberDarkCyan,
    background = CyberDarkBg,
    surface = CyberDarkCardBg,
    onPrimary = CyberDarkButtonText,
    onSecondary = CyberDarkButtonText,
    onTertiary = CyberDarkButtonText,
    onBackground = CyberDarkTextPrimary,
    onSurface = CyberDarkTextPrimary,
    onSurfaceVariant = CyberDarkTextSecondary,
    outline = CyberDarkBorder,
    primaryContainer = CyberDarkAmber,
    error = CyberDarkRed,
    onError = CyberDarkBg
)

object CyberThemeColors {
  val bg: Color @Composable get() = MaterialTheme.colorScheme.background
  val cardBg: Color @Composable get() = MaterialTheme.colorScheme.surface
  val primary: Color @Composable get() = MaterialTheme.colorScheme.primary
  val onPrimary: Color @Composable get() = MaterialTheme.colorScheme.onPrimary
  val secondary: Color @Composable get() = MaterialTheme.colorScheme.secondary
  val tertiary: Color @Composable get() = MaterialTheme.colorScheme.tertiary
  val error: Color @Composable get() = MaterialTheme.colorScheme.error
  val textPrimary: Color @Composable get() = MaterialTheme.colorScheme.onBackground
  val textSecondary: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
  val border: Color @Composable get() = MaterialTheme.colorScheme.outline
  val amber: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer
}

@Composable
fun KITTGrowControlTheme(
  isDarkMode: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (isDarkMode) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
