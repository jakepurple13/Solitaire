package com.programmersbox.common

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter

fun Modifier.attachAsContainer() = composed {
    val state = LocalDragDrop.current
    this.onGloballyPositioned { state.attach(it) }
}

@Suppress("UNCHECKED_CAST")
fun <T> Modifier.dropTarget(
    state: DropTargetState<T>, enabled: Boolean = true, onDrop: (T?) -> Unit
) = composed {
    val isEnabled by rememberUpdatedState(newValue = enabled)
    val boundInBox = remember {
        mutableStateOf<Rect?>(null)
    }

    if (isEnabled) {
        RegisterDropTarget(boundInBox, onDrop)
    }

    val dragDropState = LocalDragDrop.current
    LaunchedEffect(isEnabled) {
        if (isEnabled) {
            snapshotFlow {
                val isInBound =
                    boundInBox.value?.contains(dragDropState.calculateDragPosition()) ?: false
                isInBound to (if (isInBound) dragDropState.dataToDrop as? T else null)
            }.collectLatest { (isInBound, dataToDrop) ->
                state.apply {
                    this.isInBound = isInBound
                    this.dataToDrop = dataToDrop
                }
            }
        } else {
            state.apply {
                isInBound = false
                dataToDrop = null
            }
        }
    }
    onGloballyPositioned {
        dragDropState.boundInBox(it).let { rect ->
            boundInBox.value = rect
        }
    }
}

fun <T> Modifier.dragTarget(
    enable: Boolean,
    dataToDrop: T?,
    dragType: DragType,
    draggableComposable: @Composable () -> Unit,
) = composed {
    val currentState = LocalDragDrop.current
    val dragTargetState = rememberUpdatedState(newValue = DragTargetState(dataToDrop, dragType))
    var currentOffsetInBox by remember {
        mutableStateOf(Offset.Zero)
    }
    var currentSizePx by remember {
        mutableStateOf(IntSize.Zero)
    }
    if (!enable) {
        Modifier
    } else {
        this
            .onGloballyPositioned {
                currentOffsetInBox = currentState.positionInBox(it)
                currentSizePx = it.size
            }
            .pointerInput(currentState, dragTargetState, currentSizePx) {
                val onDrag = { _: PointerInputChange, dragAmount: Offset ->
                    currentState.onDrag(dragAmount)
                }
                val onDragStart = { offset: Offset ->
                    currentState.onDragStart(
                        dragTargetState.value.dataToDrop,
                        currentOffsetInBox,
                        offset,
                        draggableComposable,
                        currentSizePx
                    )
                }
                val onDragEnd = {
                    currentState.onDragEnd()
                }
                val onDragCancel = {
                    currentState.onDragCancel()
                }
                when (dragTargetState.value.dragType) {
                    DragType.LongPress -> {
                        detectDragGesturesAfterLongPress(
                            onDrag = onDrag,
                            onDragStart = onDragStart,
                            onDragEnd = onDragEnd,
                            onDragCancel = onDragCancel,
                        )
                    }

                    DragType.Immediate -> {
                        detectDragGestures(
                            onDrag = onDrag,
                            onDragStart = onDragStart,
                            onDragEnd = onDragEnd,
                            onDragCancel = onDragCancel
                        )
                    }
                }
            }
    }
}

@Composable
fun DragDropBox(
    modifier: Modifier = Modifier,
    scale: Float = 1.2f,
    alpha: Float = 0.9f,
    defaultDragType: DragType = DragType.LongPress,
    state: DragDropState = rememberDragDropState(scale, alpha, defaultDragType),
    content: @Composable BoxScope.() -> Unit
) {
    DragDropBox(state, modifier, content)
}

@Composable
fun DragDropBox(
    state: DragDropState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    CompositionLocalProvider(
        LocalDragDrop provides state
    ) {
        Box(
            modifier = modifier.attachAsContainer(),
        ) {
            content()
            DragDropOverlay()
        }
    }
}

@Composable
fun DragDropOverlay(state: DragDropState = LocalDragDrop.current) {
    if (state.isDragging) {
        val targetSizeDp = with(LocalDensity.current) {
            state.draggableSizePx.toSize().toDpSize()
        }
        Box(
            modifier = Modifier
                .size(targetSizeDp)
                .graphicsLayer {
                    val offset = state.calculateTargetOffset()
                    scaleX = state.scaleX
                    scaleY = state.scaleY
                    this.alpha = state.alpha
                    translationX = offset.x
                    translationY = offset.y
                },
        ) {
            state.draggableComposition?.invoke()
        }
    }
}

sealed interface DragType {
    data object LongPress : DragType
    data object Immediate : DragType
}

interface DragTargetInfo {
    var isDragging: Boolean
    var dragStartPosition: Offset
    var dragOffset: Offset
    var draggableComposition: (@Composable () -> Unit)?
    var draggableSizePx: IntSize
    var dataToDrop: Any?

    val scaleX: Float
    val scaleY: Float
    val alpha: Float
    val dragType: DragType
}

private class DragTargetInfoImpl(
    override val scaleX: Float,
    override val scaleY: Float,
    override val alpha: Float,
    override val dragType: DragType,
) : DragTargetInfo {
    override var isDragging by mutableStateOf(false)
    override var dragStartPosition by mutableStateOf(Offset.Zero)
    override var dragOffset by mutableStateOf(Offset.Zero)
    override var draggableComposition by mutableStateOf<(@Composable () -> Unit)?>(null)
    override var draggableSizePx by mutableStateOf(IntSize.Zero)
    override var dataToDrop by mutableStateOf<Any?>(null)
}

@Composable
fun rememberDragDropState(
    scaleX: Float,
    scaleY: Float,
    alpha: Float = 0.9f,
    defaultDragType: DragType = DragType.LongPress
): DragDropState {
    return remember(scaleX, scaleY, alpha, defaultDragType) {
        DragDropState(scaleX, scaleY, alpha, defaultDragType)
    }
}

@Composable
fun rememberDragDropState(
    scale: Float = 1.2f,
    alpha: Float = 0.9f,
    dragType: DragType = DragType.LongPress
): DragDropState {
    return rememberDragDropState(scale, scale, alpha, dragType)
}

@Suppress("NAME_SHADOWING")
@OptIn(FlowPreview::class)
@Composable
fun <T> RegisterDropTarget(boundInBox: State<Rect?>, onDrop: (T?) -> Unit) {
    val realOnDrop by rememberUpdatedState(onDrop)
    val state = LocalDragDrop.current
    val boundInBoxFlow = remember {
        snapshotFlow {
            boundInBox.value
        }.filter { it != Rect.Zero }.debounce(100)
    }

    val boundInBox = boundInBoxFlow.collectAsState(initial = null)
    boundInBox.value?.let { bound ->
        if (bound != Rect.Zero) {
            DisposableEffect(bound) {
                state.registerDropTarget(bound, realOnDrop)
                onDispose {
                    state.unregisterDropTarget(bound)
                }
            }
        }
    }
}

class DragDropState private constructor(
    private val dragTargetInfo: DragTargetInfo,
) : DragTargetInfo by dragTargetInfo {

    private var dragDropBoxCoordinates: LayoutCoordinates? = null

    private val dropTargets = hashMapOf<Rect, (Any?) -> Unit>()

    private fun reset() {
        isDragging = false
        dragStartPosition = Offset.Zero
        dragOffset = Offset.Zero
        draggableComposition = null
        draggableSizePx = IntSize.Zero
        dataToDrop = null
    }

    internal fun attach(layoutCoordinates: LayoutCoordinates) {
        this.dragDropBoxCoordinates = layoutCoordinates
    }

    internal fun onDragStart(
        dataToDrop: Any?,
        offsetInBox: Offset,
        dragStartOffset: Offset,
        content: @Composable () -> Unit,
        contentSizePx: IntSize
    ) {
        isDragging = true
        this.dataToDrop = dataToDrop
        dragStartPosition = offsetInBox + dragStartOffset
        draggableComposition = content
        draggableSizePx = contentSizePx
    }

    internal fun onDrag(dragAmount: Offset) {
        dragOffset += dragAmount
    }

    internal fun onDragEnd() {
        val offset = calculateDragPosition()
        val onDrop =
            dropTargets.firstNotNullOfOrNull { if (it.key.contains(offset)) it.value else null }
        onDrop?.invoke(dataToDrop)
        reset()
    }

    internal fun onDragCancel() {
        reset()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> registerDropTarget(bound: Rect, onDrop: (T?) -> Unit) {
        dropTargets[bound] = onDrop as (Any?) -> Unit
    }

    fun unregisterDropTarget(bound: Rect) {
        dropTargets.remove(bound)
    }

    fun positionInBox(dragTargetLayoutCoordinates: LayoutCoordinates): Offset {
        return dragDropBoxCoordinates!!.localPositionOf(dragTargetLayoutCoordinates, Offset.Zero)
    }

    fun boundInBox(dropTargetLayoutCoordinates: LayoutCoordinates): Rect {
        return dragDropBoxCoordinates!!.localBoundingBoxOf(dropTargetLayoutCoordinates)
    }

    fun calculateTargetOffset() = dragStartPosition + dragOffset - draggableSizePx.center.toOffset()

    fun calculateDragPosition() = dragStartPosition + dragOffset

    companion object {
        operator fun invoke(
            scaleX: Float,
            scaleY: Float,
            alpha: Float,
            defaultDragType: DragType
        ): DragDropState {
            return DragDropState(DragTargetInfoImpl(scaleX, scaleY, alpha, defaultDragType))
        }
    }
}

data class DragTargetState<T>(
    val dataToDrop: T?,
    val dragType: DragType,
)

@Composable
fun <T> DragTarget(
    dataToDrop: T?,
    modifier: Modifier = Modifier,
    enable: Boolean = true,
    dragType: DragType = LocalDragDrop.current.dragType,
    hiddenOnDragging: Boolean = false,
    customDragContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .dragTarget(
                enable,
                dataToDrop,
                dragType,
                customDragContent ?: content
            )
    ) {
        val state = LocalDragDrop.current
        if (!hiddenOnDragging || !state.isDragging) {
            content()
        }
    }
}

val LocalDragDrop = compositionLocalOf<DragDropState> { error("LocalDragDrop not present") }

class DropTargetState<T> {
    var isInBound by mutableStateOf(false)
        internal set
    var dataToDrop by mutableStateOf<T?>(null)
        internal set

    operator fun component1() = isInBound

    operator fun component2() = dataToDrop

    override fun toString(): String {
        return "DropTargetState(isInBound=$isInBound, dataToDrop=$dataToDrop)"
    }
}

@Composable
fun <T> rememberDropTargetState() = remember {
    DropTargetState<T>()
}

@Composable
fun <T> DropTarget(
    onDrop: (T?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    dropTargetState: DropTargetState<T> = rememberDropTargetState(),
    content: @Composable BoxScope.(isInBound: Boolean, data: T?) -> Unit
) {
    Box(modifier = modifier.dropTarget(dropTargetState, enabled, onDrop)) {
        content(dropTargetState.isInBound, dropTargetState.dataToDrop)
    }
}