package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val LightColorScheme =
  lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
  )

private val CatppuccinDarkColorScheme = darkColorScheme(
    primary = CatMochaPrimary,
    secondary = CatMochaSecondary,
    tertiary = CatMochaTertiary,
    background = CatMochaBase,
    surface = CatMochaSurface,
    onPrimary = CatMochaOnPrimary,
    onSecondary = CatMochaOnSecondary,
    onBackground = CatMochaOnBackground,
    onSurface = CatMochaOnSurface,
    outline = CatMochaOutline
)

private val CatppuccinLightColorScheme = lightColorScheme(
    primary = CatLattePrimary,
    secondary = CatLatteSecondary,
    tertiary = CatLatteTertiary,
    background = CatLatteBase,
    surface = CatLatteSurface,
    onPrimary = CatLatteOnPrimary,
    onSecondary = CatLatteOnSecondary,
    onBackground = CatLatteOnBackground,
    onSurface = CatLatteOnSurface,
    outline = CatLatteOutline
)

private val SimpleDarkColorScheme = darkColorScheme(
    primary = SimpleDarkPrimary,
    secondary = SimpleDarkSecondary,
    tertiary = SimpleDarkTertiary,
    background = SimpleDarkBase,
    surface = SimpleDarkSurface,
    onPrimary = SimpleDarkOnPrimary,
    onSecondary = SimpleDarkOnSecondary,
    onBackground = SimpleDarkOnBackground,
    onSurface = SimpleDarkOnSurface,
    outline = SimpleDarkOutline
)

private val SimpleLightColorScheme = lightColorScheme(
    primary = SimpleLightPrimary,
    secondary = SimpleLightSecondary,
    tertiary = SimpleLightTertiary,
    background = SimpleLightBase,
    surface = SimpleLightSurface,
    onPrimary = SimpleLightOnPrimary,
    onSecondary = SimpleLightOnSecondary,
    onBackground = SimpleLightOnBackground,
    onSurface = SimpleLightOnSurface,
    outline = SimpleLightOutline
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  colorSchemeName: String = "MATERIAL_3",
  content: @Composable () -> Unit,
) {
  val context = LocalContext.current
  val colorScheme = when (colorSchemeName) {
    "CATPPUCCIN" -> {
      if (darkTheme) CatppuccinDarkColorScheme else CatppuccinLightColorScheme
    }
    "SIMPLE" -> {
      if (darkTheme) SimpleDarkColorScheme else SimpleLightColorScheme
    }
    else -> { // MATERIAL_3
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      } else {
        if (darkTheme) DarkColorScheme else LightColorScheme
      }
    }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
