package com.programmersbox.common

import androidx.compose.animation.*
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.viewmodel.viewModel


data class CardLocation(val location: Int, val card: Card, val place: Int)

const val DRAW_AMOUNT = 3 //default 3
const val WIN_CARD_VALUE = 13 //default 13
val FIELD_HEIGHT = 100

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun SolitaireScreen(
    info: SolitaireViewModel = viewModel(SolitaireViewModel::class) { SolitaireViewModel() },
    settings: Settings,
) {
    val drawAmount by settings.drawAmount.flow.collectAsStateWithLifecycle(DRAW_AMOUNT)

    LaunchedEffect(drawAmount) {
        snapshotFlow { drawAmount }
            .distinctUntilChanged()
            .onEach { info.newGame() }
            .launchIn(this)
    }

    val cardSizeModifier = Modifier.height(150.dp)

    val scope = rememberCoroutineScope()

    var showWinDialog by remember(info.hasWon) { mutableStateOf(info.hasWon) }

    if (showWinDialog) {
        AlertDialog(
            onDismissRequest = { showWinDialog = false },
            title = { Text("You win!") },
            text = { Text("Start a new game?") },
            confirmButton = {
                TextButton(
                    onClick = { info.newGame() },
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
                        info.newGame()
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

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    LaunchedEffect(drawerState.isOpen) {
        snapshotFlow { drawerState.isOpen }
            .onEach {
                if (it) info.pauseTimer()
                else info.resumeTimer()
            }
            .launchIn(this)
    }

    DragDropBox(
        defaultDragType = DragType.Immediate
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    SettingsView(settings)
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
                        .padding(padding)
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
                                modifier = Modifier.weight(1f)
                            ) { d, f ->
                                val canPlace = f?.let { cardLocation ->
                                    foundationCheck(cardLocation.card, foundation.value) && d
                                } ?: false

                                val strokeColor by animateColorAsState(
                                    if (canPlace) Color.Green else MaterialTheme.colorScheme.primary
                                )

                                foundation.value.lastOrNull()
                                    ?.let {
                                        DragTarget(
                                            dataToDrop = CardLocation(FOUNDATION_LOCATION, it, foundation.key)
                                        ) {
                                            PlayingCard(
                                                card = it,
                                                border = BorderStroke(2.dp, strokeColor),
                                                modifier = cardSizeModifier
                                            )
                                        }
                                    }
                                    ?: EmptyCard(
                                        border = BorderStroke(2.dp, strokeColor),
                                        modifier = cardSizeModifier.fillMaxSize(),
                                    )
                            }
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.End),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy((-50).dp)
                        ) {
                            info
                                .drawList
                                .dropLast(1)
                                .takeLast(2)
                                .forEach {
                                    PlayingCard(
                                        card = it,
                                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                        modifier = cardSizeModifier.width(100.dp)
                                    )
                                }

                            info.drawList.lastOrNull()?.let {
                                DragTarget(
                                    dataToDrop = CardLocation(DRAW_LOCATION, it, 0)
                                ) {
                                    PlayingCard(
                                        card = it,
                                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                        modifier = cardSizeModifier.width(100.dp)
                                    )
                                }
                            } ?: EmptyCard(
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                modifier = cardSizeModifier.width(100.dp)
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyCard(
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                modifier = cardSizeModifier.width(100.dp),
                            ) { info.draw(drawAmount) }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.matchParentSize()
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
                        }
                    }
                    //---------field---------
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
                                ) { d, f ->
                                    val canPlace = f?.let { cardLocation ->
                                        fieldCheck(cardLocation.card, fieldSlot.value) && d
                                    } ?: false

                                    val strokeColor by animateColorAsState(
                                        if (canPlace) Color.Green else MaterialTheme.colorScheme.primary
                                    )

                                    if (fieldSlot.value.list.isEmpty()) {
                                        EmptyCard(
                                            border = BorderStroke(2.dp, strokeColor),
                                            modifier = Modifier
                                                .height(FIELD_HEIGHT.dp)
                                                .border(
                                                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                                    shape = MaterialTheme.shapes.medium.copy(
                                                        topEnd = CornerSize(0.dp),
                                                        topStart = CornerSize(0.dp)
                                                    )
                                                )
                                                .fillMaxSize(),
                                        )
                                    } else {
                                        LazyColumn(
                                            verticalArrangement = Arrangement.spacedBy(-(FIELD_HEIGHT * .75).dp),
                                            modifier = Modifier.animateContentSize()
                                        ) {
                                            itemsIndexed(fieldSlot.value.list) { index, card ->
                                                DragTarget(
                                                    dataToDrop = CardLocation(fieldSlot.key, card, index),
                                                    modifier = Modifier.animateItemPlacement(),
                                                    customDragContent = {
                                                        PlayingCard(
                                                            card = card,
                                                            border = BorderStroke(2.dp, strokeColor),
                                                            modifier = Modifier.height(FIELD_HEIGHT.dp),
                                                            showFullDetail = false
                                                        )
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
            }
        }
    }

    if (info.hasWon) {
        Explosion()
    }
}

@Composable
internal fun Explosion() {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        CreateParticles(
            modifier = Modifier.matchParentSize(),
            x = with(LocalDensity.current) { maxWidth.toPx() / 2 },
            y = -50f,
            velocity = Velocity(xDirection = 1f, yDirection = 1f, randomize = true),
            force = Force.Gravity(0.01f),
            acceleration = Acceleration(),
            particleSize = ParticleSize.RandomSizes(10..30),
            particleColor = ParticleColor.RandomColors(
                listOf(
                    Color.Yellow,
                    Color.Blue,
                    Color.Red,
                    Color.White,
                    Color.Magenta,
                    Color.Green
                )
            ),
            lifeTime = LifeTime(255f, 0.01f),
            emissionType = EmissionType.FlowEmission(maxParticlesCount = 300, emissionRate = 0.5f),
            durationMillis = 10 * 1000
        )
    }
}
