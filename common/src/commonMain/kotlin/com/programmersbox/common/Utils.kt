package com.programmersbox.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.materialkolor.rememberDynamicColorScheme

data class ComposeCardColor(
    val black: Color = Color.Unspecified,
    val red: Color = Color.Unspecified,
)

val LocalCardColor = staticCompositionLocalOf { ComposeCardColor() }

fun Modifier.animatedBorder(
    borderColors: List<Color>,
    backgroundColor: Color,
    shape: Shape = RectangleShape,
    borderWidth: Dp = 1.dp,
    animationDurationInMillis: Int = 1000,
    easing: Easing = LinearEasing,
): Modifier = composed {
    val brush = Brush.sweepGradient(borderColors)
    val infiniteTransition = rememberInfiniteTransition(label = "animatedBorder")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDurationInMillis, easing = easing),
            repeatMode = RepeatMode.Restart
        ), label = "angleAnimation"
    )

    this
        .clip(shape)
        .padding(borderWidth)
        .drawWithContent {
            rotate(angle) {
                drawCircle(
                    brush = brush,
                    radius = size.width,
                    blendMode = BlendMode.SrcIn,
                )
            }
            drawContent()
        }
        .background(color = backgroundColor, shape = shape)
}

enum class ThemeColor(
    val seedColor: Color,
) {
    Dynamic(Color.Transparent),
    Blue(Color.Blue),
    Red(Color.Red),
    Green(Color.Green),
    Yellow(Color.Yellow),
    Cyan(Color.Cyan),
    Magenta(Color.Magenta),
    Custom(Color.Transparent),
}

@Composable
fun buildColorScheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
): ColorScheme {
    val themeColor by rememberThemeColor()
    val isAmoled by rememberIsAmoled()

    val animationSpec = spring<Color>(stiffness = Spring.StiffnessLow)

    return when (themeColor) {
        ThemeColor.Dynamic -> colorSchemeSetup(darkTheme, dynamicColor).let {
            if (isAmoled && darkTheme) {
                it.copy(
                    surface = Color.Black,
                    onSurface = Color.White,
                    background = Color.Black,
                    onBackground = Color.White
                )
            } else {
                it
            }
        }

        ThemeColor.Custom -> {
            rememberDynamicColorScheme(
                seedColor = rememberCustomColor().value,
                isDark = darkTheme,
                isAmoled = isAmoled
            )
        }

        else -> rememberDynamicColorScheme(
            seedColor = themeColor.seedColor,
            isDark = darkTheme,
            isAmoled = isAmoled
        )
    }.let { colorScheme ->
        colorScheme.copy(
            primary = colorScheme.primary.animate(animationSpec),
            primaryContainer = colorScheme.primaryContainer.animate(animationSpec),
            secondary = colorScheme.secondary.animate(animationSpec),
            secondaryContainer = colorScheme.secondaryContainer.animate(animationSpec),
            tertiary = colorScheme.tertiary.animate(animationSpec),
            tertiaryContainer = colorScheme.tertiaryContainer.animate(animationSpec),
            background = colorScheme.background.animate(animationSpec),
            surface = colorScheme.surface.animate(animationSpec),
            surfaceTint = colorScheme.surfaceTint.animate(animationSpec),
            surfaceBright = colorScheme.surfaceBright.animate(animationSpec),
            surfaceDim = colorScheme.surfaceDim.animate(animationSpec),
            surfaceContainer = colorScheme.surfaceContainer.animate(animationSpec),
            surfaceContainerHigh = colorScheme.surfaceContainerHigh.animate(animationSpec),
            surfaceContainerHighest = colorScheme.surfaceContainerHighest.animate(animationSpec),
            surfaceContainerLow = colorScheme.surfaceContainerLow.animate(animationSpec),
            surfaceContainerLowest = colorScheme.surfaceContainerLowest.animate(animationSpec),
            surfaceVariant = colorScheme.surfaceVariant.animate(animationSpec),
            error = colorScheme.error.animate(animationSpec),
            errorContainer = colorScheme.errorContainer.animate(animationSpec),
            onPrimary = colorScheme.onPrimary.animate(animationSpec),
            onPrimaryContainer = colorScheme.onPrimaryContainer.animate(animationSpec),
            onSecondary = colorScheme.onSecondary.animate(animationSpec),
            onSecondaryContainer = colorScheme.onSecondaryContainer.animate(animationSpec),
            onTertiary = colorScheme.onTertiary.animate(animationSpec),
            onTertiaryContainer = colorScheme.onTertiaryContainer.animate(animationSpec),
            onBackground = colorScheme.onBackground.animate(animationSpec),
            onSurface = colorScheme.onSurface.animate(animationSpec),
            onSurfaceVariant = colorScheme.onSurfaceVariant.animate(animationSpec),
            onError = colorScheme.onError.animate(animationSpec),
            onErrorContainer = colorScheme.onErrorContainer.animate(animationSpec),
            inversePrimary = colorScheme.inversePrimary.animate(animationSpec),
            inverseSurface = colorScheme.inverseSurface.animate(animationSpec),
            inverseOnSurface = colorScheme.inverseOnSurface.animate(animationSpec),
            outline = colorScheme.outline.animate(animationSpec),
            outlineVariant = colorScheme.outlineVariant.animate(animationSpec),
            scrim = colorScheme.scrim.animate(animationSpec),
        )
    }
}

@Composable
private fun Color.animate(animationSpec: AnimationSpec<Color>): Color {
    return animateColorAsState(this, animationSpec).value
}