package com.programmersbox.common

import androidx.compose.animation.*
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalLayoutDirection
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

private val CARD_HEIGHT = 70.dp
private val CARD_WIDTH = 50.dp

private val cardSizeModifier = Modifier.height(125.dp)

private val cardSize = Modifier
    //.height(CARD_HEIGHT)
    .sizeIn(CARD_WIDTH, CARD_HEIGHT)
//.fillMaxWidth(.5f)
//.requiredSizeIn(CARD_WIDTH, CARD_HEIGHT)
//.defaultMinSize(CARD_WIDTH, CARD_HEIGHT)

enum class GameLocation {
    Start,
    Center,
    End
}

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

    val snackbarHostState = remember { SnackbarHostState() }

    var gameLocation by rememberGameLocation()

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val isBig = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM
            || windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED

    LookaheadScope {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = MaterialTheme.colorScheme.background,
                ) {
                    //For testing!
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
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = 8.dp,
                                horizontal = 8.dp
                            )
                            .windowInsetsPadding(WindowInsets.statusBars)
                    ) {
                        ToolTipWrapper(
                            title = { Text(stringResource(Res.string.settings)) },
                            text = { Text(stringResource(Res.string.open_settings_drawer)) }
                        ) {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                            ) { Icon(Icons.Default.Settings, null) }
                        }

                        ToolTipWrapper(
                            title = { Text(stringResource(Res.string.new_game)) },
                            text = { Text(stringResource(Res.string.start_a_new_game)) }
                        ) {
                            IconButton(
                                onClick = { newGameDialog = true },
                            ) { Icon(Icons.Default.Gamepad, null) }
                        }

                        Card(
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(stringResource(Res.string.moves, info.moveCount))
                                Text(stringResource(Res.string.points, animateIntAsState(info.score).value))
                                Text(info.timeText)
                            }
                        }
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        ToolTipWrapper(
                            title = { Text(stringResource(Res.string.undo)) },
                            text = { Text(stringResource(Res.string.undo_description)) }
                        ) {
                            Button(
                                onClick = info::undo,
                                enabled = info.lastFewMoves.isNotEmpty(),
                                modifier = Modifier.animatePlacementInScope(this@LookaheadScope)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Undo, null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(Res.string.undo))
                            }
                        }

                        AnimatedVisibility(
                            isBig,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically(),
                            modifier = Modifier.animatePlacementInScope(this@LookaheadScope)
                        ) {
                            ToolTipWrapper(
                                title = { Text(stringResource(Res.string.game_location)) },
                                text = { Text(stringResource(Res.string.game_location_description)) }
                            ) {
                                SingleChoiceSegmentedButtonRow {
                                    GameLocation.entries.forEachIndexed { index, game ->
                                        SegmentedButton(
                                            selected = game == gameLocation,
                                            onClick = { gameLocation = game },
                                            shape = SegmentedButtonDefaults.itemShape(index, GameLocation.entries.size)
                                        ) { Text(game.name) }
                                    }
                                }
                            }
                        }

                        ToolTipWrapper(
                            title = { Text(stringResource(Res.string.auto_move)) },
                            text = { Text(stringResource(Res.string.auto_move_description)) }
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val hasMoved = info.autoMove()
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        if (!hasMoved) {
                                            snackbarHostState.showSnackbar(
                                                message = getString(Res.string.nothing_moved),
                                                withDismissAction = true,
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.animatePlacementInScope(this@LookaheadScope)
                            ) {
                                Icon(Icons.Default.AutoAwesome, null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(Res.string.auto_move))
                            }
                        }
                    }
                }
            ) { padding ->
                AnimatedDragDropBox(
                    defaultDragType = DragType.Immediate,
                    scale = 1f
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp)
                            .padding(padding)
                    ) {
                        val paddingType = if (isBig) {
                            val insets = WindowInsets.safeGestures.asPaddingValues()
                            Modifier.padding(
                                start = insets.calculateStartPadding(LocalLayoutDirection.current),
                                end = insets.calculateEndPadding(LocalLayoutDirection.current)
                            )
                        } else {
                            Modifier
                        }

                        Row(
                            horizontalArrangement = if (isBig)
                                Arrangement.spacedBy(
                                    space = 8.dp,
                                    alignment = when (gameLocation) {
                                        GameLocation.Start -> Alignment.Start
                                        GameLocation.Center -> Alignment.CenterHorizontally
                                        GameLocation.End -> Alignment.End
                                    }
                                )
                            else
                                Arrangement.SpaceBetween,
                            modifier = paddingType.fillMaxWidth()
                        ) {
                            //---------foundation---------
                            Foundations(
                                info = info,
                                winModifier = winModifier,
                                cardBack = cardBack,
                                database = database,
                                modifier = Modifier.animatePlacementInScope(this@LookaheadScope)
                            )
                            //---------draws---------
                            Draws(
                                winModifier = winModifier,
                                info = info,
                                drawAmount = drawAmount,
                                cardBack = cardBack,
                                database = database,
                                modifier = Modifier.animatePlacementInScope(this@LookaheadScope)
                            )
                        }
                        //---------field---------
                        Field(
                            winModifier = winModifier,
                            info = info,
                            cardBack = cardBack,
                            database = database,
                            isBig = isBig,
                            gameLocation = gameLocation,
                            lookaheadScope = this@LookaheadScope,
                            modifier = paddingType.animatePlacementInScope(this@LookaheadScope),
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
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.Start),
        modifier = modifier
            //.fillMaxWidth()
            .animateContentSize()
    ) {
        info.foundations.forEach { foundation ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(-(CARD_HEIGHT - 5.dp)),
                modifier = Modifier
                    .weight(1f, false)
                    .animateContentSize()
            ) {
                foundation.value
                    .dropLast(1)
                    .takeLast(3)
                    .forEach {
                        PlayingCard(
                            card = it,
                            border = borderStroke(MaterialTheme.colorScheme.outline),
                            useNewDesign = useNewDesign,
                            modifier = cardSize
                                //.fillMaxSize()
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
                                    border = borderStroke(strokeColor),
                                    useNewDesign = useNewDesign,
                                    modifier = cardSize.then(winModifier)
                                )
                            }
                        } ?: cardBack.CustomCardBackground(
                        database = database,
                        border = borderStroke(MaterialTheme.colorScheme.outline),
                        modifier = cardSize
                            //.fillMaxSize()
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
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(-(CARD_WIDTH / 2))
        ) {
            //draws
            val lastTwo = info
                .drawList
                .dropLast(1)
                .takeLast(2)
            /*.forEach {
                PlayingCard(
                    card = it,
                    border = borderStroke(MaterialTheme.colorScheme.primary),
                    useNewDesign = useNewDesign,
                    modifier = cardSize//.width(100.dp)
                )
            }*/

            when (lastTwo.size) {
                1 -> {
                    cardBack.CustomCardBackground(
                        database = database,
                        border = borderStroke(MaterialTheme.colorScheme.outline),
                        modifier = cardSize.then(winModifier)
                    )
                    lastTwo.forEach {
                        PlayingCard(
                            card = it,
                            border = borderStroke(MaterialTheme.colorScheme.primary),
                            useNewDesign = useNewDesign,
                            modifier = cardSize
                        )
                    }
                }

                2 -> {
                    lastTwo.forEach {
                        PlayingCard(
                            card = it,
                            border = borderStroke(MaterialTheme.colorScheme.primary),
                            useNewDesign = useNewDesign,
                            modifier = cardSize
                        )
                    }
                }

                else -> {
                    repeat(2) {
                        cardBack.CustomCardBackground(
                            database = database,
                            border = borderStroke(MaterialTheme.colorScheme.outline),
                            modifier = cardSize.then(winModifier)
                        )
                    }
                }
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
                        border = borderStroke(MaterialTheme.colorScheme.primary),
                        modifier = cardSize//.width(100.dp)
                    )
                }
            } ?: cardBack.CustomCardBackground(
                database = database,
                border = borderStroke(MaterialTheme.colorScheme.outline),
                modifier = cardSize
                    //.width(100.dp)
                    .then(winModifier),
            )
        }

        /*Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {*/
        ToolTipWrapper(
            title = { Text(stringResource(Res.string.draw_title)) },
            text = { Text(stringResource(Res.string.draw_description)) }
        ) {
            cardBack.CustomCardBackground(
                border = borderStroke(MaterialTheme.colorScheme.primary),
                database = database,
                onClick = { info.draw(drawAmount) },
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
                            .width(CARD_WIDTH)
                            .align(Alignment.BottomCenter)
                    ) {
                        Text(
                            animateIntAsState(info.cardsLeft).value.toString(),
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                        Text(
                            "Cards",
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    }
                },
                modifier = cardSize.then(winModifier),
            )
        }
        /*   Text(
               animateIntAsState(info.cardsLeft).value.toString() + " Cards",
               fontSize = 12.sp
           )
       }*/
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Field(
    winModifier: Modifier,
    info: SolitaireViewModel,
    cardBack: CardBack,
    database: SolitaireDatabase,
    isBig: Boolean,
    gameLocation: GameLocation,
    lookaheadScope: LookaheadScope,
    modifier: Modifier = Modifier,
) {
    val useNewDesign by rememberUseNewDesign()

    Row(
        horizontalArrangement = if (isBig) {
            Arrangement.spacedBy(
                space = 2.dp,
                alignment = when (gameLocation) {
                    GameLocation.Start -> Alignment.Start
                    GameLocation.Center -> Alignment.CenterHorizontally
                    GameLocation.End -> Alignment.End
                }
            )
        } else {
            Arrangement.SpaceEvenly
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        info.fieldSlots.forEach { fieldSlot ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f, false)
                    .animatePlacementInScope(lookaheadScope)
            ) {
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
                            border = borderStroke(strokeColor),
                            modifier = Modifier
                                .then(cardSize)
                                //.fillMaxSize()
                                .then(winModifier)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(-(CARD_HEIGHT.value * .95).dp),
                            modifier = Modifier.animateContentSize()
                        ) {
                            items(fieldSlot.value.faceDownList) {
                                cardBack.CustomCardBackground(
                                    database = database,
                                    border = borderStroke(strokeColor),
                                    modifier = Modifier
                                        .then(cardSize)
                                        //.fillMaxSize()
                                        .then(winModifier)
                                )
                            }
                            item {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(-(CARD_HEIGHT.value * .55).dp),
                                ) {
                                    fieldSlot.value.list.forEachIndexed { index, card ->
                                        DragTarget(
                                            dataToDrop = CardLocation(fieldSlot.key, card, index),
                                            enable = !info.hasWon,
                                            onDoubleTap = { it?.let { cardLocation -> info.autoMoveCard(cardLocation) } },
                                            customDragContent = {
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(-(CARD_HEIGHT.value * .55).dp),
                                                ) {
                                                    fieldSlot.value.getCards(index).forEach {
                                                        PlayingCard(
                                                            card = it,
                                                            border = borderStroke(strokeColor),
                                                            showFullDetail = false,
                                                            useNewDesign = useNewDesign,
                                                            modifier = cardSize
                                                        )
                                                    }
                                                }
                                            }
                                        ) {
                                            PlayingCard(
                                                card = card,
                                                border = borderStroke(strokeColor),
                                                showFullDetail = false,
                                                useNewDesign = useNewDesign,
                                                modifier = cardSize
                                            )
                                        }
                                    }
                                }
                            }
                            /*itemsIndexed(fieldSlot.value.list) { index, card ->
                                DragTarget(
                                    dataToDrop = CardLocation(fieldSlot.key, card, index),
                                    enable = !info.hasWon,
                                    onDoubleTap = { it?.let { cardLocation -> info.autoMoveCard(cardLocation) } },
                                    customDragContent = {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(-(CARD_HEIGHT.value * .65).dp),
                                        ) {
                                            fieldSlot.value.getCards(index).forEach {
                                                PlayingCard(
                                                    card = it,
                                                    border = BorderStroke(2.dp, strokeColor),
                                                    showFullDetail = false,
                                                    useNewDesign = useNewDesign,
                                                    modifier = cardSize//Modifier.height(FIELD_HEIGHT.dp)
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    PlayingCard(
                                        card = card,
                                        border = BorderStroke(2.dp, strokeColor),
                                        showFullDetail = false,
                                        useNewDesign = useNewDesign,
                                        modifier = cardSize//Modifier.height(FIELD_HEIGHT.dp)
                                    )
                                }
                            }*/
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

@Composable
private fun borderStroke(color: Color): BorderStroke {
    val backgroundForBorder by rememberBackgroundForBorder()
    return BorderStroke(
        2.dp,
        if (backgroundForBorder) MaterialTheme.colorScheme.background else color
    )
}