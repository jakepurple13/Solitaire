package com.programmersbox.common

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.programmersbox.common.dragdrop.AnimatedDragDropBox
import com.programmersbox.common.dragdrop.DragTarget
import com.programmersbox.common.dragdrop.DragType
import com.programmersbox.common.dragdrop.DropTarget
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.BackHandler
import moe.tlaster.precompose.viewmodel.viewModel


data class CardLocation(val location: Int, val card: Card, val place: Int)

const val DRAW_AMOUNT = 3 //default 3
const val WIN_CARD_VALUE = 13 //default 13
const val FIELD_HEIGHT = 100

private val cardSizeModifier = Modifier.height(150.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SolitaireScreen(
    database: SolitaireDatabase = remember { SolitaireDatabase() },
    settings: Settings?,
    info: SolitaireViewModel = viewModel(SolitaireViewModel::class) {
        SolitaireViewModel(
            initialDifficulty = { settings?.initialDifficulty() ?: Difficulty.Normal }
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

    val winModifier by remember {
        derivedStateOf {
            if (info.hasWon) {
                Modifier.animatedBorder(
                    borderColors = listOf(Color.Red, Color.Green, Color.Blue),
                    backgroundColor = Color.Transparent,
                    shape = PlayingCardDefaults.shape,
                    borderWidth = 4.dp
                )
            } else {
                Modifier
            }
        }
    }

    LaunchedEffect(info.hasWon) {
        if (info.hasWon) {
            database.addHighScore(
                timeTaken = info.timeText,
                moveCount = info.moveCount,
                score = info.score,
                difficulty = difficulty.ordinal
            )
        }
    }

    val scope = rememberCoroutineScope()

    var showWinDialog by remember(info.hasWon) { mutableStateOf(info.hasWon) }

    if (showWinDialog) {
        AlertDialog(
            onDismissRequest = { showWinDialog = false },
            title = { Text("You win!") },
            text = { Text("Start a new game?") },
            confirmButton = {
                TextButton(
                    onClick = { info.newGame(difficulty) },
                ) { Text("Play again!") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showWinDialog = false },
                ) { Text("Keep looking at the field") }
            }
        )
    }

    var newGameDialog by remember { mutableStateOf(false) }
    if (newGameDialog) {
        AlertDialog(
            onDismissRequest = { newGameDialog = false },
            title = { Text("New Game?") },
            text = { Text("Are you sure you want to start a new game?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        info.newGame(difficulty)
                        newGameDialog = false
                    }
                ) { Text("Yes") }
            },
            dismissButton = {
                TextButton(
                    onClick = { newGameDialog = false }
                ) { Text("No") }
            }
        )
    }

    var showStats by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showStats) {
        ModalBottomSheet(
            onDismissRequest = { showStats = false },
            sheetState = sheetState
        ) {
            StatsView(database)
        }
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    BackHandler(drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    LaunchedEffect(drawerState) {
        snapshotFlow { drawerState.isOpen }
            .onEach {
                if (it) info.pauseTimer()
                else info.resumeTimer()
            }
            .launchIn(this)
    }

    AnimatedDragDropBox(
        defaultDragType = DragType.Immediate
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                ModalDrawerSheet {
                    SettingsView(
                        settings = settings,
                        onStatsClick = { showStats = true }
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
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } }
                                ) { Icon(Icons.Default.Settings, null) }
                                TextButton(
                                    onClick = { newGameDialog = true }
                                ) { Text("New Game") }
                            }
                        },
                        title = { Text(info.timeText) },
                        actions = {
                            IconButton(
                                onClick = { info.autoMove() }
                            ) { Icon(Icons.Default.AutoMode, null) }

                            Text("Score: " + animateIntAsState(info.score).value.toString())
                        }
                    )
                },
                bottomBar = {
                    AnimatedVisibility(
                        info.hasWon,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        BottomAppBar {
                            Button(
                                onClick = { showWinDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("You won! Start New Game!") }
                        }
                    }
                }
            ) { padding ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .padding(padding)
                ) {
                    Foundations(
                        info = info,
                        winModifier = winModifier,
                        cardBack = cardBack
                    )
                    //---------draws---------
                    Draws(
                        winModifier = winModifier,
                        info = info,
                        drawAmount = drawAmount,
                        cardBack = cardBack
                    )
                    //---------field---------
                    Field(
                        winModifier = winModifier,
                        info = info,
                        cardBack = cardBack
                    )
                }
            }
        }
    }
}

@Composable
private fun Foundations(
    info: SolitaireViewModel,
    cardBack: CardBack,
    winModifier: Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxWidth()
    ) {
        info.foundations.forEach { foundation ->
            DropTarget<CardLocation>(
                onDrop = {
                    it?.let { cardLocation ->
                        info.foundationPlace(cardLocation, foundation.value)
                    }
                },
                enabled = !info.hasWon,
                modifier = Modifier.weight(1f)
            ) { d, f ->
                val canPlace = f?.let { cardLocation ->
                    foundationCheck(cardLocation.card, foundation.value) && d
                } == true

                val strokeColor by animateColorAsState(
                    if (canPlace) Color.Green else MaterialTheme.colorScheme.primary
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
                                modifier = cardSizeModifier.then(winModifier)
                            )
                        }
                    }
                    ?: EmptyCard(
                        border = BorderStroke(2.dp, strokeColor),
                        cardBack = cardBack.toModifier(),
                        modifier = cardSizeModifier
                            .fillMaxSize()
                            .then(winModifier),
                    )
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
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.End),
        modifier = Modifier.fillMaxWidth()
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
            //var delay = 100L
            info
                .drawList
                .dropLast(1)
                .takeLast(2)
                .forEach {
                    //delay.also { delay += 50 }
                    PlayingCard(
                        card = it,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        modifier = cardSizeModifier.width(100.dp)
                    )
                }

            info.drawList.lastOrNull()?.let {
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

        Box(
            contentAlignment = Alignment.Center
        ) {
            EmptyCard(
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                cardBack = cardBack.toModifier(),
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
                            "Cards left:",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            animateIntAsState(info.cardsLeft).value.toString(),
                            textAlign = TextAlign.Center
                        )
                    }
                },
                modifier = cardSizeModifier
                    .width(100.dp)
                    .then(winModifier),
            ) { info.draw(drawAmount) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Field(
    winModifier: Modifier,
    info: SolitaireViewModel,
    cardBack: CardBack,
) {
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
                        if (canPlace) Color.Green else MaterialTheme.colorScheme.primary
                    )

                    if (fieldSlot.value.list.isEmpty()) {
                        EmptyCard(
                            border = BorderStroke(2.dp, strokeColor),
                            cardBack = cardBack.toModifier(),
                            modifier = Modifier
                                .height(FIELD_HEIGHT.dp)
                                .border(
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                    shape = MaterialTheme.shapes.medium.copy(
                                        topEnd = CornerSize(0.dp),
                                        topStart = CornerSize(0.dp)
                                    )
                                )
                                .fillMaxSize()
                                .then(winModifier),
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(-(FIELD_HEIGHT * .75).dp),
                            modifier = Modifier.animateContentSize()
                        ) {
                            itemsIndexed(fieldSlot.value.list) { index, card ->
                                DragTarget(
                                    dataToDrop = CardLocation(fieldSlot.key, card, index),
                                    enable = !info.hasWon,
                                    customDragContent = {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(-(FIELD_HEIGHT * .75).dp),
                                        ) {
                                            fieldSlot.value.getCards(index).forEach {
                                                PlayingCard(
                                                    card = it,
                                                    border = BorderStroke(2.dp, strokeColor),
                                                    modifier = Modifier.height(FIELD_HEIGHT.dp),
                                                    showFullDetail = false
                                                )
                                            }
                                        }
                                        /*PlayingCard(
                                            card = card,
                                            border = BorderStroke(2.dp, strokeColor),
                                            modifier = Modifier.height(FIELD_HEIGHT.dp),
                                            showFullDetail = false
                                        )*/
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
                                        modifier = Modifier.height(FIELD_HEIGHT.dp),
                                        showFullDetail = false
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