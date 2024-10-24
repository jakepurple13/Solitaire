package com.programmersbox.common.dragdrop

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private const val TAG = "DragTarget"

data class DragTargetState<T>(
    val dataToDrop: T?,
    val dragType: DragType,
)

@Composable
fun <T> DragTarget(
    dataToDrop: T?,
    modifier: Modifier = Modifier,
    enable: Boolean = true,
    uniqueKey: (() -> Any)? = null,
    dragType: DragType = LocalDragDrop.current.dragType,
    hiddenOnDragging: Boolean = false,
    customDragContent: (@Composable () -> Unit)? = null,
    onDoubleTap: ((T?) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .dragTarget(
                dataToDrop = dataToDrop,
                draggableContent = customDragContent ?: content,
                enable = enable,
                uniqueKey = uniqueKey,
                dragType = dragType,
                onDoubleTap = onDoubleTap,
            )
    ) {
        val state = LocalDragDrop.current
        when {
            hiddenOnDragging && state.isDragging -> {
                if (uniqueKey == null || uniqueKey.invoke() != state.targetKey) {
                    content()
                }
            }

            else -> content()
        }
    }
}