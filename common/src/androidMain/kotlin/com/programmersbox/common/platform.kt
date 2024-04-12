package com.programmersbox.common

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

public actual fun getPlatformName(): String {
    return "Android"
}

@Composable
public fun UIShow() {
    val context = LocalContext.current
    App(
        settings = remember { Settings { context.filesDir.resolve(Settings.DATA_STORE_FILE_NAME).absolutePath } }
    )
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
public actual fun hasDisplayGsl(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
