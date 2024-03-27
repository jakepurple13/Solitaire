package com.programmersbox.common

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ComposeCardColor(
    val black: Color = Color.Unspecified,
    val red: Color = Color.Unspecified,
)

val LocalCardColor = staticCompositionLocalOf { ComposeCardColor() }