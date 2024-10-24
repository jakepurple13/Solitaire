package com.programmersbox.common

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.crop
import com.attafitamim.krop.core.crop.rememberImageCropper
import com.attafitamim.krop.ui.ImageCropperDialog
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.materialkolor.ktx.from
import com.materialkolor.palettes.TonalPalette
import com.materialkolor.rememberDynamicColorScheme
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class,
    ExperimentalResourceApi::class
)
@Composable
internal fun SettingsView(
    settings: Settings?,
    database: SolitaireDatabase,
    onNewGamePress: () -> Unit,
    startDailyGame: () -> Unit,
    onDrawerClose: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            NewGame(onNewGamePress)

            DailyGame(startDailyGame, onDrawerClose)

            Instructions()

            DrawAmountChange(onDrawerClose)
            DifficultChange(onDrawerClose)

            divider()

            CardBackChange(database, scope)
            CardDesignChange()

            divider()

            AmoledChange()
            ThemeChange()

            divider()

            ViewStats(database)

            item {
                Text(
                    "Version: ${getPlatformName()}",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun LazyListScope.divider() = item {}

@OptIn(ExperimentalMaterial3Api::class)
private fun LazyListScope.Instructions() = item {
    var showInstructions by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    if (showInstructions) {
        ModalBottomSheet(
            onDismissRequest = { showInstructions = false },
            sheetState = sheetState,
        ) {
            CenterAlignedTopAppBar(
                title = { Text("How to Play") },
                windowInsets = WindowInsets(0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            scope.launch { sheetState.hide() }
                                .invokeOnCompletion { showInstructions = false }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Back"
                        )
                    }
                }
            )
            Text(
                text = SolitaireInstructions,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }

    OutlinedCard(
        onClick = { showInstructions = true },
    ) {
        ListItem(
            headlineContent = { Text("How to Play") },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun LazyListScope.ViewStats(database: SolitaireDatabase) = item {
    var showStats by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showStats) {
        ModalBottomSheet(
            onDismissRequest = { showStats = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            StatsView(database)
        }
    }

    Button(
        onClick = { showStats = true },
        modifier = Modifier.fillMaxWidth()
    ) { Text("View Stats") }
}

private fun LazyListScope.NewGame(onNewGamePress: () -> Unit) = item {
    OutlinedCard(
        onClick = onNewGamePress,
        border = CardDefaults.outlinedCardBorder(true).copy(
            brush = SolidColor(MaterialTheme.colorScheme.secondary)
        )
    ) {
        ListItem(
            headlineContent = { Text("New Game") },
        )
    }
}

private fun LazyListScope.DailyGame(
    startDailyGame: () -> Unit,
    onDrawerClose: () -> Unit,
) = item {
    var showNewGameDialog by remember { mutableStateOf(false) }
    if (showNewGameDialog) {
        NewGameDialog(
            title = "Start the Daily Game?",
            onDismiss = { showNewGameDialog = false },
            onConfirm = {
                showNewGameDialog = false
                startDailyGame()
                onDrawerClose()
            }
        )
    }
    OutlinedCard(
        onClick = { showNewGameDialog = true },
        border = CardDefaults.outlinedCardBorder(true).copy(
            brush = SolidColor(MaterialTheme.colorScheme.primary)
        )
    ) {
        ListItem(
            headlineContent = { Text("Start Daily Game") },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
private fun LazyListScope.ThemeChange() = item {
    var showThemes by remember { mutableStateOf(false) }
    var themeColor by rememberThemeColor()
    val isAmoled by rememberIsAmoled()

    var customColor by rememberCustomColor()

    var showColorPicker by remember { mutableStateOf(false) }

    if (showColorPicker) {
        AlertDialog(
            onDismissRequest = { showColorPicker = false },
            title = { Text("Custom Color") },
            text = {
                Column {
                    Text("Select a color to use as the background color of the app.")
                    val controller = rememberColorPickerController()
                    HsvColorPicker(
                        onColorChanged = { colorEnvelope: ColorEnvelope ->
                            customColor = colorEnvelope.color
                        },
                        controller = controller,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(450.dp)
                            .padding(10.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showColorPicker = false }
                ) { Text("Done") }
            },
        )
    }

    OutlinedCard(
        onClick = { showThemes = !showThemes },
    ) {
        ListItem(
            headlineContent = { Text("Theme") },
            trailingContent = { Icon(Icons.Default.ArrowDropDown, null) },
            supportingContent = { Text(themeColor.name) },
        )

        AnimatedVisibility(showThemes) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                ThemeColor
                    .entries
                    .filter { it != ThemeColor.Custom }
                    .forEach {
                        ThemeItem(
                            onClick = { themeColor = it },
                            selected = themeColor == it,
                            themeColor = it,
                            colorScheme = if (it == ThemeColor.Dynamic)
                                MaterialTheme.colorScheme
                            else
                                rememberDynamicColorScheme(
                                    it.seedColor,
                                    isAmoled = isAmoled,
                                    isDark = isSystemInDarkTheme()
                                )
                        )
                    }

                ThemeItem(
                    onClick = {
                        themeColor = ThemeColor.Custom
                        showColorPicker = true
                    },
                    selected = themeColor == ThemeColor.Custom,
                    themeColor = ThemeColor.Custom,
                    colorScheme = rememberDynamicColorScheme(
                        customColor,
                        isAmoled = isAmoled,
                        isDark = isSystemInDarkTheme()
                    )
                )
            }
        }
    }
}

@Composable
private fun ThemeItem(
    onClick: () -> Unit,
    selected: Boolean,
    themeColor: ThemeColor,
    colorScheme: ColorScheme,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.inverseOnSurface,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            SelectableMiniPalette(
                selected = selected,
                colorScheme = colorScheme
            )

            Text(themeColor.name)
        }
    }
}

@Composable
private fun SelectableMiniPalette(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: (() -> Unit)? = null,
    colorScheme: ColorScheme,
) {
    SelectableMiniPalette(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        accents = remember(colorScheme) {
            listOf(
                TonalPalette.from(colorScheme.primary),
                TonalPalette.from(colorScheme.secondary),
                TonalPalette.from(colorScheme.tertiary)
            )
        }
    )
}

@Composable
private fun SelectableMiniPalette(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: (() -> Unit)? = null,
    accents: List<TonalPalette>,
) {
    val content: @Composable () -> Unit = {
        Box {
            Surface(
                modifier = Modifier
                    .size(50.dp)
                    .offset((-25).dp, 25.dp),
                color = Color(accents[1].tone(85)),
            ) {}
            Surface(
                modifier = Modifier
                    .size(50.dp)
                    .offset(25.dp, 25.dp),
                color = Color(accents[2].tone(75)),
            ) {}
            val animationSpec = spring<Float>(stiffness = Spring.StiffnessMedium)
            AnimatedVisibility(
                visible = selected,
                enter = scaleIn(animationSpec) + fadeIn(animationSpec),
                exit = scaleOut(animationSpec) + fadeOut(animationSpec),
            ) {
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Checked",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(16.dp),
                        tint = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }
    }
    onClick?.let {
        Surface(
            onClick = onClick,
            modifier = modifier
                .padding(12.dp)
                .size(50.dp),
            shape = CircleShape,
            color = Color(accents[0].tone(60)),
        ) { content() }
    } ?: Surface(
        modifier = modifier
            .padding(12.dp)
            .size(50.dp),
        shape = CircleShape,
        color = Color(accents[0].tone(60)),
    ) { content() }
}

@Composable
private fun NewGameDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text("Changing this will start a new game.") },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) { Text("Yes") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) { Text("No") }
        }
    )
}

@Composable
private fun CardBackItem(
    cardBack: CardBack,
    isSelected: Boolean,
    database: SolitaireDatabase,
    modifier: Modifier = Modifier,
    onCardBackSelected: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.wrapContentSize()
    ) {
        cardBack.CustomCardBackground(
            database = database,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            content = {
                Text(
                    cardBack.name,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Color.Black.copy(alpha = .5f),
                            shape = RoundedCornerShape(
                                topEnd = 4.dp,
                                topStart = 4.dp
                            )
                        )
                )
            },
            onClick = { onCardBackSelected() },
            modifier = Modifier
                .height(150.dp)
                .width(100.dp)
        )
        if (isSelected) {
            Icon(
                Icons.Default.CheckCircle,
                null,
                modifier = Modifier.background(
                    Color.Black.copy(alpha = .5f),
                    CircleShape
                )
            )
        }
    }
}

private fun LazyListScope.DrawAmountChange(
    onDrawerClose: () -> Unit,
) = item {
    var drawAmount by rememberDrawAmount()
    var showDialogChange by remember { mutableStateOf(false) }
    if (showDialogChange) {
        NewGameDialog(
            title = "Change the draw amount?",
            onConfirm = {
                drawAmount = if (drawAmount == 1) 3 else 1
                onDrawerClose()
            },
            onDismiss = { showDialogChange = false }
        )
    }
    OutlinedCard(
        modifier = Modifier.toggleable(
            value = drawAmount == DRAW_AMOUNT,
            onValueChange = { showDialogChange = true }
        )
    ) {
        ListItem(
            headlineContent = { Text("Draw Amount: $drawAmount") },
            trailingContent = {
                Switch(
                    checked = drawAmount == DRAW_AMOUNT,
                    onCheckedChange = { showDialogChange = true }
                )
            }
        )
    }
}

private fun LazyListScope.DifficultChange(
    onDrawerClose: () -> Unit,
) = item {
    var difficulty by rememberModeDifficulty()
    var showDropdown by remember { mutableStateOf(false) }
    OutlinedCard(
        onClick = { showDropdown = true }
    ) {
        ListItem(
            headlineContent = { Text("Difficulty: $difficulty") },
            trailingContent = {
                if (showDropdown) {
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        Difficulty.entries.forEach {
                            var showDialogChange by remember { mutableStateOf(false) }
                            if (showDialogChange) {
                                NewGameDialog(
                                    title = "Change difficulty?",
                                    onConfirm = {
                                        difficulty = it
                                        showDropdown = false
                                        onDrawerClose()
                                    },
                                    onDismiss = {
                                        showDialogChange = false
                                        showDropdown = false
                                    }
                                )
                            }

                            DropdownMenuItem(
                                text = { Text(it.name) },
                                onClick = { showDialogChange = true }
                            )
                        }
                    }
                }
                Icon(Icons.Default.ArrowDropDown, null)
            }
        )
    }
}

private fun LazyListScope.CardDesignChange() = item {
    var useNewDesign by rememberUseNewDesign()

    OutlinedCard(
        modifier = Modifier.toggleable(
            value = useNewDesign,
            onValueChange = { useNewDesign = it }
        )
    ) {
        ListItem(
            headlineContent = { Text("Use New Card Design") },
            trailingContent = {
                Switch(
                    checked = useNewDesign,
                    onCheckedChange = { useNewDesign = it }
                )
            }
        )
    }
}

private fun LazyListScope.AmoledChange() = item {
    var isAmoled by rememberIsAmoled()

    OutlinedCard(
        modifier = Modifier.toggleable(
            value = isAmoled,
            onValueChange = { isAmoled = it }
        )
    ) {
        ListItem(
            headlineContent = { Text("Is Amoled") },
            trailingContent = {
                Switch(
                    checked = isAmoled,
                    onCheckedChange = { isAmoled = it }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalResourceApi::class)
private fun LazyListScope.CardBackChange(
    database: SolitaireDatabase,
    scope: CoroutineScope,
) = item {
    val cards by database.customCardBacks().collectAsState(emptyList())
    var cardBack by rememberCardBack()
    var customCardBackHolder by rememberCustomBackChoice()

    var showCardBacks by remember { mutableStateOf(false) }

    if (showCardBacks) {
        ModalBottomSheet(
            onDismissRequest = { showCardBacks = false },
            containerColor = MaterialTheme.colorScheme.background
        ) {
            val defaultList = CardBack.entries
                .filter { !it.isGsl }
                .filter { it != CardBack.Custom }

            val gslList = CardBack.entries
                .filter { it.isGsl }
                .filter { it != CardBack.Custom }

            var showDefaultBacks by remember { mutableStateOf(cardBack in defaultList) }
            var showGsl by remember { mutableStateOf(cardBack in gslList) }
            var showCustom by remember { mutableStateOf(cardBack == CardBack.Custom) }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(100.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    Card(
                        onClick = { showDefaultBacks = !showDefaultBacks },
                    ) {
                        ListItem(
                            headlineContent = { Text("Default") },
                            trailingContent = { Icon(Icons.Default.ArrowDropDown, null) },
                        )
                    }
                }

                if (showDefaultBacks) {
                    items(
                        items = defaultList,
                        key = { it.name },
                        contentType = { it }
                    ) {
                        CardBackItem(
                            cardBack = it,
                            isSelected = it == cardBack,
                            onCardBackSelected = { cardBack = it },
                            database = database,
                            modifier = Modifier.animateItem()
                        )
                    }
                }

                if (hasDisplayGsl()) {
                    item(
                        span = { GridItemSpan(maxLineSpan) },
                    ) {
                        Card(
                            onClick = { showGsl = !showGsl },
                        ) {
                            ListItem(
                                headlineContent = { Text("Special") },
                                trailingContent = { Icon(Icons.Default.ArrowDropDown, null) },
                            )
                        }
                    }

                    if (showGsl) {
                        items(
                            items = gslList,
                            key = { it.name },
                            contentType = { it }
                        ) {
                            CardBackItem(
                                cardBack = it,
                                isSelected = it == cardBack,
                                onCardBackSelected = { cardBack = it },
                                database = database,
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }

                item(
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    Card(
                        onClick = { showCustom = !showCustom },
                    ) {
                        ListItem(
                            headlineContent = { Text("Custom") },
                            trailingContent = { Icon(Icons.Default.ArrowDropDown, null) },
                        )
                    }
                }

                if (showCustom) {
                    itemsIndexed(cards) { index, it ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .wrapContentSize()
                                .animateItem()
                        ) {
                            EmptyCard(
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                content = {
                                    Image(
                                        bitmap = it.image,
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Text(
                                        "Custom #$index",
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .background(
                                                Color.Black.copy(alpha = .5f),
                                                shape = RoundedCornerShape(
                                                    topEnd = 4.dp,
                                                    topStart = 4.dp
                                                )
                                            )
                                    )
                                },
                                modifier = Modifier
                                    .height(150.dp)
                                    .width(100.dp)
                                    .combinedClickable(
                                        onClick = {
                                            cardBack = CardBack.Custom
                                            customCardBackHolder = it.uuid
                                        },
                                        onLongClick = {
                                            cardBack = CardBack.None
                                            scope.launch { database.removeCardBack(it.image) }
                                        }
                                    )
                            )

                            if (it.uuid == customCardBackHolder && cardBack == CardBack.Custom) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    modifier = Modifier.background(
                                        Color.Black.copy(alpha = .5f),
                                        CircleShape
                                    )
                                )
                            }
                        }
                    }

                    item {
                        val imageCropper = rememberImageCropper()
                        imageCropper.cropState?.let {
                            ImageCropperDialog(
                                state = it,
                                topBar = {
                                    TopAppBar(
                                        title = {},
                                        navigationIcon = {
                                            IconButton(
                                                onClick = { it.done(accept = false) }
                                            ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                                        },
                                        actions = {
                                            IconButton(
                                                onClick = { it.reset() }
                                            ) { Icon(Icons.Default.Restore, null) }
                                            IconButton(
                                                onClick = { it.done(accept = true) },
                                                enabled = !it.accepted
                                            ) { Icon(Icons.Default.Done, null) }
                                        },
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = MaterialTheme.colorScheme.background
                                        )
                                    )
                                }
                            )
                        }

                        val filePicker = rememberFilePickerLauncher(
                            type = PickerType.Image,
                            title = "Pick an Image",
                        ) {
                            scope.launch {
                                it?.readBytes()
                                    ?.decodeToImageBitmap()
                                    ?.let {
                                        when (
                                            val result = imageCropper.crop(
                                                bmp = it,
                                                maxResultSize = IntSize(500, 500)
                                            )
                                        ) {
                                            is CropResult.Success -> {
                                                database.saveCardBack(result.bitmap)
                                            }

                                            else -> {}
                                        }
                                    }

                            }
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .wrapContentSize()
                                .animateItem()
                        ) {
                            EmptyCard(
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                content = {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                    Text(
                                        "Add",
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .background(
                                                Color.Black.copy(alpha = .5f),
                                                shape = RoundedCornerShape(
                                                    topEnd = 4.dp,
                                                    topStart = 4.dp
                                                )
                                            )
                                    )
                                },
                                modifier = Modifier
                                    .height(150.dp)
                                    .width(100.dp)
                            ) { filePicker.launch() }
                        }
                    }
                }

                /*if (imageCropper.cropState == null)
                    items(
                        items = CardBack.entries
                            .filter { it.includeGsl() }
                            .filter { it != CardBack.Custom },
                        key = { it.name },
                        contentType = { it }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.wrapContentSize()
                        ) {
                            EmptyCard(
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                cardBack = it.toModifier(),
                                content = {
                                    Text(
                                        it.name,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .background(
                                                Color.Black.copy(alpha = .5f),
                                                shape = RoundedCornerShape(
                                                    topEnd = 4.dp,
                                                    topStart = 4.dp
                                                )
                                            )
                                    )
                                },
                                modifier = Modifier
                                    .height(150.dp)
                                    .width(100.dp)
                            ) { cardBack = it }
                            if (it == cardBack) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    modifier = Modifier.background(
                                        Color.Black.copy(alpha = .5f),
                                        CircleShape
                                    )
                                )
                            }
                        }
                    }*/


            }
        }
    }


    OutlinedCard(
        onClick = { showCardBacks = true }
    ) {
        ListItem(
            headlineContent = { Text("Card Back: ${cardBack.name}") },
        )
    }
}