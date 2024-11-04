package com.programmersbox.common

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowWidthSizeClass
import com.materialkolor.ktx.fixIfDisliked
import com.materialkolor.ktx.harmonize
import com.materialkolor.ktx.harmonizeWithPrimary
import com.programmersbox.common.dragdrop.AnimatedDragDropBox
import com.programmersbox.common.dragdrop.DragTarget
import com.programmersbox.common.dragdrop.DragType
import com.programmersbox.common.dragdrop.DropTarget
import com.programmersbox.common.generated.resources.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

data class CardLocation(val location: Int, val card: Card, val place: Int)

val DRAW_AMOUNTS = listOf(1, 3) //default 1, 3
const val WIN_CARD_VALUE = 13 //default 13
const val FIELD_HEIGHT = 100

private val cardSizeModifier = Modifier.height(125.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SolitaireScreen(
    database: SolitaireDatabase,
    settings: Settings?,
    info: SolitaireViewModel = viewModel {
        SolitaireViewModel(
            initialDifficulty = { settings?.initialDifficulty() ?: Difficulty.Normal },
            settings = settings
        )
    },
) {
    val drawAmount by rememberDrawAmount()
    val difficulty by rememberModeDifficulty()
    val cardBack by rememberCardBack()

    LaunchedEffect(drawAmount, difficulty) {
        combine(
            snapshotFlow { drawAmount }.distinctUntilChanged(),
            snapshotFlow { difficulty }.distinctUntilChanged()
        ) { _, m -> m }
            .drop(1)
            .onEach { info.newGame(it) }
            .launchIn(this)
    }

    val windowInfo = LocalWindowInfo.current.isWindowFocused
    LaunchedEffect(windowInfo) {
        if (windowInfo) info.resumeTimer()
        else info.pauseTimer()
    }

    val colorScheme = MaterialTheme.colorScheme

    val winModifier by remember {
        derivedStateOf {
            if (info.hasWon) {
                Modifier.animatedBorder(
                    borderColors = listOf(
                        colorScheme.harmonizeWithPrimary(Color.Red),
                        colorScheme.harmonizeWithPrimary(Color.Green),
                        colorScheme.harmonizeWithPrimary(Color.Blue)
                    ),
                    backgroundColor = Color.Transparent,
                    shape = PlayingCardDefaults.shape,
                    borderWidth = 4.dp
                )
            } else {
                Modifier
            }
        }
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    LaunchedEffect(info.hasWon) {
        if (info.hasWon) {
            GlobalScope.launch {
                database.addHighScore(
                    timeTaken = info.timeText,
                    moveCount = info.moveCount,
                    score = info.score,
                    difficulty = difficulty.ordinal
                )
            }
        }
    }

    val scope = rememberCoroutineScope()

    var showWinDialog by remember(info.hasWon) { mutableStateOf(info.hasWon) }

    if (showWinDialog) {
        AlertDialog(
            onDismissRequest = { showWinDialog = false },
            title = { Text(stringResource(Res.string.you_win)) },
            text = { Text(stringResource(Res.string.start_a_new_game_question)) },
            confirmButton = {
                TextButton(
                    onClick = { info.newGame(difficulty) },
                ) { Text(stringResource(Res.string.play_again)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showWinDialog = false },
                ) { Text(stringResource(Res.string.keep_looking_at_field)) }
            }
        )
    }

    var newGameDialog by remember { mutableStateOf(false) }
    if (newGameDialog) {
        AlertDialog(
            onDismissRequest = { newGameDialog = false },
            title = { Text(stringResource(Res.string.new_game_question)) },
            text = { Text(stringResource(Res.string.are_you_sure_new_game)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        info.newGame(difficulty)
                        newGameDialog = false
                        scope.launch { drawerState.close() }
                    }
                ) { Text(stringResource(Res.string.yes)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { newGameDialog = false }
                ) { Text(stringResource(Res.string.no)) }
            }
        )
    }

    BackHandlerForDrawer(drawerState)

    LaunchedEffect(drawerState) {
        snapshotFlow { drawerState.isOpen }
            .onEach {
                if (it) info.pauseTimer()
                else info.resumeTimer()
            }
            .launchIn(this)
    }

    val window = currentWindowAdaptiveInfo().windowSizeClass

    val snackbarHostState = remember { SnackbarHostState() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
            ) {
                //info.WinButton()

                SettingsView(
                    settings = settings,
                    database = database,
                    onNewGamePress = { newGameDialog = true },
                    startDailyGame = { info.startDailyGame(difficulty) },
                    onDrawerClose = { scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ToolTipWrapper(
                                title = { Text(stringResource(Res.string.settings)) },
                                text = { Text(stringResource(Res.string.open_settings_drawer)) }
                            ) {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } }
                                ) { Icon(Icons.Default.Settings, null) }
                            }

                            ToolTipWrapper(
                                title = { Text(stringResource(Res.string.new_game)) },
                                text = { Text(stringResource(Res.string.start_a_new_game)) }
                            ) {
                                IconButton(
                                    onClick = { newGameDialog = true }
                                ) { Icon(Icons.Default.Gamepad, null) }
                            }

                            ToolTipWrapper(
                                title = { Text(stringResource(Res.string.undo)) },
                                text = { Text(stringResource(Res.string.undo_description)) }
                            ) {
                                IconButton(
                                    onClick = { info.undo() },
                                    enabled = info.lastFewMoves.isNotEmpty()
                                ) { Icon(Icons.AutoMirrored.Filled.Undo, null) }
                            }
                        }
                    },
                    title = { Text(info.timeText) },
                    actions = {
                        ToolTipWrapper(
                            title = { Text(stringResource(Res.string.auto_move)) },
                            text = { Text(stringResource(Res.string.auto_move_description)) }
                        ) {
                            val animatedProgress = remember { Animatable(0f) }
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        val hasMoved = info.autoMove()
                                        launch {
                                            animatedProgress.animateTo(360f)
                                            animatedProgress.snapTo(0f)
                                        }
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        if (!hasMoved) {
                                            snackbarHostState.showSnackbar(
                                                message = getString(Res.string.nothing_moved),
                                                withDismissAction = true,
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoMode,
                                    contentDescription = null,
                                    modifier = Modifier.rotate(animatedProgress.value)
                                )
                            }
                        }

                        ToolTipWrapper(
                            title = { Text(stringResource(Res.string.info)) },
                            text = { Text(stringResource(Res.string.move_count, info.moveCount)) }
                        ) {
                            Text(stringResource(Res.string.score, animateIntAsState(info.score).value.toString()))
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                AnimatedVisibility(
                    info.hasWon,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    BottomAppBar {
                        Button(
                            onClick = { showWinDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(Res.string.you_won_start)) }
                    }
                }
            }
        ) { padding ->
            AnimatedDragDropBox(
                defaultDragType = DragType.Immediate,
                scale = 1f
            ) {
                LookaheadScope {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .padding(padding)
                    ) {
                        val topArea = remember {
                            movableContentWithReceiverOf<LookaheadScope, Modifier> {
                                //---------foundation---------
                                Foundations(
                                    info = info,
                                    winModifier = winModifier,
                                    cardBack = cardBack,
                                    database = database,
                                    modifier = it.animatePlacementInScope(this@LookaheadScope)
                                )
                                //---------draws---------
                                Draws(
                                    winModifier = winModifier,
                                    info = info,
                                    drawAmount = drawAmount,
                                    cardBack = cardBack,
                                    database = database,
                                    modifier = it.animatePlacementInScope(this@LookaheadScope)
                                )
                            }
                        }

                        when (window.windowWidthSizeClass) {
                            WindowWidthSizeClass.MEDIUM, WindowWidthSizeClass.EXPANDED -> {
                                Row {
                                    topArea(Modifier.weight(1f))
                                }
                            }

                            else -> {
                                topArea(Modifier)
                            }
                        }
                        //---------field---------
                        Field(
                            winModifier = winModifier,
                            info = info,
                            cardBack = cardBack,
                            database = database,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolTipWrapper(
    title: (@Composable () -> Unit)? = null,
    text: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            RichTooltip(
                title = title,
                text = text
            )
        },
        state = rememberTooltipState()
    ) { content() }
}

@Composable
private fun Foundations(
    info: SolitaireViewModel,
    database: SolitaireDatabase,
    cardBack: CardBack,
    winModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    val useNewDesign by rememberUseNewDesign()

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
        modifier = modifier
            //.fillMaxWidth()
            .animateContentSize()
    ) {
        info.foundations.forEach { foundation ->
            Column(
                verticalArrangement = Arrangement.spacedBy((-120).dp),
                modifier = Modifier
                    .weight(1f)
                    .animateContentSize()
            ) {
                foundation.value
                    .dropLast(1)
                    .takeLast(3)
                    .forEach {
                        PlayingCard(
                            card = it,
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                            useNewDesign = useNewDesign,
                            modifier = cardSizeModifier
                                .fillMaxSize()
                                .then(winModifier),
                        )
                    }

                DropTarget<CardLocation>(
                    onDrop = {
                        it?.let { cardLocation ->
                            info.foundationPlace(cardLocation, foundation.value)
                        }
                    },
                    enabled = !info.hasWon,
                    //modifier = Modifier.weight(1f)
                ) { d, f ->
                    val canPlace = f?.let { cardLocation ->
                        foundationCheck(cardLocation.card, foundation.value)
                                && d
                                && info.fieldToFoundationCheck(cardLocation)
                    } == true

                    val strokeColor by animateColorAsState(
                        if (canPlace)
                            Color.Green
                                .harmonize(MaterialTheme.colorScheme.primary)
                                .fixIfDisliked()
                        else
                            MaterialTheme.colorScheme.primary
                    )

                    foundation.value.lastOrNull()
                        ?.let {
                            DragTarget(
                                dataToDrop = CardLocation(FOUNDATION_LOCATION, it, foundation.key),
                                enable = !info.hasWon,
                            ) {
                                PlayingCard(
                                    card = it,
                                    border = BorderStroke(2.dp, strokeColor),
                                    useNewDesign = useNewDesign,
                                    modifier = cardSizeModifier.then(winModifier)
                                )
                            }
                        } ?: cardBack.CustomCardBackground(
                        database = database,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                        modifier = cardSizeModifier
                            .fillMaxSize()
                            .then(winModifier),
                    )
                }
            }
        }
    }
}

@Composable
private fun Draws(
    winModifier: Modifier,
    info: SolitaireViewModel,
    cardBack: CardBack,
    drawAmount: Int,
    database: SolitaireDatabase,
    modifier: Modifier = Modifier,
) {
    val useNewDesign by rememberUseNewDesign()

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.End),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy((-50).dp)
        ) {
            /*val transition = updateTransition(info.drawList.takeLast(3))
            transition.AnimatedContent(
                transitionSpec = {
                    if (targetState.isEmpty()) {
                        fadeIn() togetherWith fadeOut()
                    } else {
                        slideInHorizontally { height -> height } + fadeIn() togetherWith
                                slideOutHorizontally { height -> -height } + fadeOut()
                    }
                },
                contentAlignment = Alignment.CenterEnd
            ) { target ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-50).dp)
                ) {
                    target
                        .dropLast(1)
                        .forEach {
                            PlayingCard(
                                card = it,
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                modifier = cardSizeModifier.width(100.dp)
                            )
                        }

                    target.lastOrNull()?.let {
                        DragTarget(
                            dataToDrop = CardLocation(DRAW_LOCATION, it, 0),
                            enable = !info.hasWon,
                        ) {
                            PlayingCard(
                                card = it,
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                modifier = cardSizeModifier.width(100.dp)
                            )
                        }
                    } ?: EmptyCard(
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        cardBack = cardBack.toModifier(),
                        modifier = cardSizeModifier
                            .width(100.dp)
                            .then(winModifier)
                    )
                }
            }*/

            //draws
            info
                .drawList
                .dropLast(1)
                .takeLast(2)
                .forEach {
                    PlayingCard(
                        card = it,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        useNewDesign = useNewDesign,
                        modifier = cardSizeModifier.width(100.dp)
                    )
                }

            info.drawList.lastOrNull()?.let {
                DragTarget(
                    dataToDrop = CardLocation(DRAW_LOCATION, it, 0),
                    enable = !info.hasWon,
                    onDoubleTap = { it?.let { cardLocation -> info.autoMoveCard(cardLocation) } },
                ) {
                    PlayingCard(
                        card = it,
                        useNewDesign = useNewDesign,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        modifier = cardSizeModifier.width(100.dp)
                    )
                }
            } ?: cardBack.CustomCardBackground(
                database = database,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                modifier = cardSizeModifier
                    .width(100.dp)
                    .then(winModifier),
            )
        }

        Box(
            contentAlignment = Alignment.Center
        ) {
            cardBack.CustomCardBackground(
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                database = database,
                content = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(
                                    topEnd = 4.dp,
                                    topStart = 4.dp
                                )
                            )
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    ) {
                        Text(
                            stringResource(Res.string.cards_left),
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                        Text(
                            animateIntAsState(info.cardsLeft).value.toString(),
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    }
                },
                onClick = { info.draw(drawAmount) },
                modifier = cardSizeModifier
                    .width(100.dp)
                    .then(winModifier),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Field(
    winModifier: Modifier,
    info: SolitaireViewModel,
    cardBack: CardBack,
    database: SolitaireDatabase,
) {
    val useNewDesign by rememberUseNewDesign()

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        info.fieldSlots.forEach { fieldSlot ->
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    fieldSlot.value.faceDownSize().toString(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                            shape = MaterialTheme.shapes.medium.copy(
                                bottomEnd = CornerSize(0.dp),
                                bottomStart = CornerSize(0.dp)
                            )
                        )
                )
                DropTarget<CardLocation>(
                    onDrop = {
                        it?.let { cardLocation ->
                            info.fieldPlace(cardLocation, fieldSlot.value)
                        }
                    },
                    enabled = !info.hasWon,
                ) { d, f ->
                    val canPlace = f?.let { cardLocation ->
                        fieldCheck(cardLocation.card, fieldSlot.value) && d
                    } == true

                    val strokeColor by animateColorAsState(
                        if (canPlace)
                            Color.Green
                                .harmonize(MaterialTheme.colorScheme.primary)
                                .fixIfDisliked()
                        else
                            MaterialTheme.colorScheme.primary
                    )

                    if (fieldSlot.value.list.isEmpty()) {
                        cardBack.CustomCardBackground(
                            database = database,
                            border = BorderStroke(2.dp, strokeColor),
                            shape = MaterialTheme.shapes.medium.copy(
                                topEnd = CornerSize(0.dp),
                                topStart = CornerSize(0.dp)
                            ),
                            modifier = Modifier
                                .height(FIELD_HEIGHT.dp)
                                .fillMaxSize()
                                .then(winModifier)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(-(FIELD_HEIGHT * .75).dp),
                            modifier = Modifier.animateContentSize()
                        ) {
                            //TODO: Come back to this
                            /*items(fieldSlot.value.faceDownList) {
                                cardBack.CustomCardBackground(
                                    database = database,
                                    border = BorderStroke(2.dp, strokeColor),
                                    modifier = Modifier
                                        .height(FIELD_HEIGHT.dp)
                                        .offset(y = (50).dp)
                                        .fillMaxSize()
                                        .then(winModifier)
                                )
                            }*/
                            itemsIndexed(fieldSlot.value.list) { index, card ->
                                DragTarget(
                                    dataToDrop = CardLocation(fieldSlot.key, card, index),
                                    enable = !info.hasWon,
                                    onDoubleTap = { it?.let { cardLocation -> info.autoMoveCard(cardLocation) } },
                                    customDragContent = {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(-(FIELD_HEIGHT * .75).dp),
                                        ) {
                                            fieldSlot.value.getCards(index).forEach {
                                                PlayingCard(
                                                    card = it,
                                                    border = BorderStroke(2.dp, strokeColor),
                                                    showFullDetail = false,
                                                    useNewDesign = useNewDesign,
                                                    modifier = Modifier.height(FIELD_HEIGHT.dp)
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    PlayingCard(
                                        card = card,
                                        border = BorderStroke(2.dp, strokeColor),
                                        shape = if (index == 0)
                                            MaterialTheme.shapes.medium.copy(
                                                topEnd = CornerSize(0.dp),
                                                topStart = CornerSize(0.dp)
                                            )
                                        else
                                            PlayingCardDefaults.shape,
                                        showFullDetail = false,
                                        useNewDesign = useNewDesign,
                                        modifier = Modifier.height(FIELD_HEIGHT.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SolitaireViewModel.WinButton() {
    Button(
        onClick = {
            val aceS = Card(13, Suit.Spades)
            val aceC = Card(13, Suit.Clubs)
            val aceD = Card(13, Suit.Diamonds)
            val aceH = Card(13, Suit.Hearts)
            foundations[1]?.add(aceS)
            foundations[2]?.add(aceC)
            foundations[3]?.add(aceD)
            foundations[4]?.add(aceH)
        }
    ) { Text("Win Game") }
}