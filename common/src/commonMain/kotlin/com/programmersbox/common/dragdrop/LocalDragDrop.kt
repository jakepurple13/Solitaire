package com.programmersbox.common.dragdrop

import androidx.compose.runtime.compositionLocalOf

val LocalDragDrop = compositionLocalOf<DragDropState> { error("LocalDragDrop not present") }
