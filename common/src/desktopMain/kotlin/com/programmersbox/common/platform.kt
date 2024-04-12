package com.programmersbox.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

public actual fun getPlatformName(): String {
    return "Desktop"
}

@Composable
public fun UIShow() {
    CompositionLocalProvider(
        LocalCardColor provides ComposeCardColor(
            black = MaterialTheme.colorScheme.onBackground,
            red = Color.Red
        )
    ) {
        App(
            settings = remember { Settings { Settings.DATA_STORE_FILE_NAME } }
        )
    }
}

public actual fun hasDisplayGsl(): Boolean = true