package com.programmersbox.common

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.crop
import com.attafitamim.krop.core.crop.rememberImageCropper
import com.attafitamim.krop.ui.ImageCropperDialog
import com.github.skydoves.colorpicker.compose.*
import com.materialkolor.ktx.from
import com.materialkolor.ktx.toHex
import com.materialkolor.palettes.TonalPalette
import com.materialkolor.rememberDynamicColorScheme
import com.programmersbox.common.generated.resources.*
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.*

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
internal fun SettingsView(
    @Suppress("UNUSED_PARAMETER") settings: Settings?,
    database: SolitaireDatabase,
    onNewGamePress: () -> Unit,
    startDailyGame: () -> Unit,
    onDrawerClose: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                ListItem(
                    supportingContent = { Text(stringResource(Res.string.play_style)) },
                    headlineContent = {},
                    leadingContent = { Icon(Icons.Default.SportsEsports, null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            DifficultChange(onDrawerClose)
            DrawAmountChange(onDrawerClose)

            item {
                ListItem(
                    supportingContent = { Text(stringResource(Res.string.theme_card_design)) },
                    headlineContent = {},
                    leadingContent = { Icon(Icons.Default.StarOutline, null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            CardBackChange(database, scope)
            ThemeChange(database)

            item {
                ListItem(
                    supportingContent = { Text(stringResource(Res.string.advanced)) },
                    headlineContent = {},
                    leadingContent = { Icon(Icons.Default.Settings, null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            CardDesignChange()
            CardBorderChange()

            item {
                ListItem(
                    supportingContent = { Text(stringResource(Res.string.info)) },
                    headlineContent = {},
                    leadingContent = { Icon(Icons.Default.Info, null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            Instructions()

            item {
                Card(
                    modifier = Modifier.alpha(0f)
                ) { ListItem(headlineContent = {}) }
            }

            DailyGame(startDailyGame, onDrawerClose)
            NewGame(onNewGamePress)
            ViewStats(database)

            item {
                Text(
                    stringResource(Res.string.version, getPlatformName()),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun LazyListScope.Instructions() = item {
    var showInstructions by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    if (showInstructions) {
        ModalBottomSheet(
            onDismissRequest = { showInstructions = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.how_to_play)) },
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
                            contentDescription = null
                        )
                    }
                }
            )
            Text(
                text = stringResource(Res.string.solitaire_instructions).trimIndent(),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }

    OutlinedCard(
        onClick = { showInstructions = true },
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        ListItem(
            headlineContent = { Text(stringResource(Res.string.how_to_play)) },
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

    TextButton(
        onClick = { showStats = true },
        modifier = Modifier.fillMaxWidth()
    ) { Text(stringResource(Res.string.view_stats)) }
}

private fun LazyListScope.NewGame(onNewGamePress: () -> Unit) = item {
    FilledTonalButton(
        onClick = onNewGamePress,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Add, null)
        Spacer(Modifier.width(4.dp))
        Text(stringResource(Res.string.new_game))
    }
}

private fun LazyListScope.DailyGame(
    startDailyGame: () -> Unit,
    onDrawerClose: () -> Unit,
) = item {
    var showNewGameDialog by remember { mutableStateOf(false) }
    if (showNewGameDialog) {
        NewGameDialog(
            title = stringResource(Res.string.start_daily_game_title),
            onDismiss = { showNewGameDialog = false },
            onConfirm = {
                showNewGameDialog = false
                startDailyGame()
                onDrawerClose()
            }
        )
    }

    Button(
        onClick = { showNewGameDialog = true },
        modifier = Modifier.fillMaxWidth()
    ) { Text(stringResource(Res.string.start_daily_game)) }
}

enum class ColorPickerType(val label: StringResource) {
    ColorWheel(Res.string.wheel),
    Hex(Res.string.hex),
    Image(Res.string.image)
}

private val colorRegex = Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun ColorPicker(
    database: SolitaireDatabase,
    customColor: Color,
    colorChange: (Color) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.custom_color)) },
        text = {
            var pickerType by remember { mutableStateOf(ColorPickerType.ColorWheel) }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(Res.string.select_color_title))
                SingleChoiceSegmentedButtonRow {
                    ColorPickerType.entries.forEach {
                        SegmentedButton(
                            pickerType == it,
                            onClick = { pickerType = it },
                            label = { Text(stringResource(it.label)) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = it.ordinal,
                                count = ColorPickerType.entries.size
                            ),
                        )
                    }
                }
                val controller = rememberColorPickerController()
                LaunchedEffect(Unit) {
                    controller.selectByColor(customColor, false)
                }
                LaunchedEffect(Unit) {
                    controller.getColorFlow()
                        .onEach { colorChange(it.color) }
                        .launchIn(this)
                }
                val currentColorEnvelope by remember(customColor) {
                    derivedStateOf {
                        ColorEnvelope(
                            customColor,
                            customColor.toHex(),
                            false,
                        )
                    }
                }

                Crossfade(
                    pickerType,
                    modifier = Modifier.animateContentSize()
                ) { target ->
                    when (target) {
                        ColorPickerType.ColorWheel -> {
                            HsvColorPicker(
                                controller = controller,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .padding(10.dp),
                            )
                        }

                        ColorPickerType.Hex -> {
                            var hexCode by remember {
                                mutableStateOf(currentColorEnvelope.hexCode.removePrefix("#"))
                            }
                            OutlinedTextField(
                                value = hexCode,
                                onValueChange = {
                                    hexCode = it
                                    runCatching { Color(it.hexToInt()) }
                                        .onSuccess { color ->
                                            controller.selectByColor(color, true)
                                        }
                                },
                                leadingIcon = { Text("#") },
                                label = { Text(stringResource(Res.string.hex_code)) },
                                singleLine = true,
                                //isError = colorRegex.containsMatchIn("#$hexCode"),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                            )
                        }

                        ColorPickerType.Image -> {
                            val cards by database.customCardBacks().collectAsState(emptyList())
                            var showImagePicker by remember { mutableStateOf(false) }
                            Column {
                                if (showImagePicker)
                                    Popup(
                                        onDismissRequest = { showImagePicker = false },
                                    ) {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            LazyRow(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                modifier = Modifier.padding(16.dp)
                                            ) {
                                                itemsIndexed(cards) { index, it ->
                                                    Box(
                                                        contentAlignment = Alignment.Center,
                                                        modifier = Modifier
                                                            .wrapContentSize()
                                                            .animateItem()
                                                    ) {
                                                        EmptyCard(
                                                            border = BorderStroke(
                                                                2.dp,
                                                                MaterialTheme.colorScheme.primary
                                                            ),
                                                            onClick = {
                                                                controller.setPaletteImageBitmap(it.image)
                                                                showImagePicker = false
                                                            },
                                                            content = {
                                                                Image(
                                                                    bitmap = it.image,
                                                                    contentDescription = null,
                                                                    contentScale = ContentScale.Fit,
                                                                    modifier = Modifier.fillMaxSize()
                                                                )
                                                                Text(
                                                                    stringResource(Res.string.custom_index, index),
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
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                ImageColorPicker(
                                    controller = controller,
                                    paletteImageBitmap = remember {
                                        cards
                                            .firstOrNull()
                                            ?.image
                                    } ?: imageResource(Res.drawable.card_back),
                                    paletteContentScale = PaletteContentScale.FIT,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .padding(10.dp),
                                )

                                Button(
                                    onClick = { showImagePicker = true },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) { Text(stringResource(Res.string.change_image)) }
                            }
                        }
                    }
                }

                Text(currentColorEnvelope.hexCode)
                AlphaTile(
                    controller = controller,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(6.dp)),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) { Text(stringResource(Res.string.done)) }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
private fun LazyListScope.ThemeChange(
    database: SolitaireDatabase,
) = item {
    var showThemes by remember { mutableStateOf(false) }
    var themeColor by rememberThemeColor()
    var isAmoled by rememberIsAmoled()

    var customColor by rememberCustomColor()

    var showColorPicker by remember { mutableStateOf(false) }

    if (showColorPicker) {
        ColorPicker(
            database = database,
            onDismiss = { showColorPicker = false },
            customColor = customColor,
            colorChange = { customColor = it }
        )
    }

    if (showThemes) {
        ModalBottomSheet(
            onDismissRequest = { showThemes = false },
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            ) {
                Card(
                    onClick = { isAmoled = !isAmoled },
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    ListItem(
                        headlineContent = { Text(stringResource(Res.string.is_amoled)) },
                        trailingContent = {
                            Switch(
                                checked = isAmoled,
                                onCheckedChange = { isAmoled = it }
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent,
                        )
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
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
                                        seedColor = it.seedColor,
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
                            seedColor = customColor,
                            isAmoled = isAmoled,
                            isDark = isSystemInDarkTheme()
                        )
                    )
                }
            }
        }
    }

    OutlinedCard(
        onClick = { showThemes = !showThemes },
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        ListItem(
            headlineContent = { Text(stringResource(Res.string.theme)) },
            supportingContent = { Text(themeColor.name) },
            /*leadingContent = {
                SelectableMiniPalette(
                    modifier = Modifier.scale(0.5f),
                    selected = false,
                    colorScheme = when(themeColor) {
                        ThemeColor.Dynamic -> MaterialTheme.colorScheme
                        ThemeColor.Custom -> rememberDynamicColorScheme(
                            seedColor = customColor,
                            isAmoled = isAmoled,
                            isDark = isSystemInDarkTheme()
                        )
                        else -> rememberDynamicColorScheme(
                            seedColor = themeColor.seedColor,
                            isAmoled = isAmoled,
                            isDark = isSystemInDarkTheme()
                        )
                    }
                )
            }*/
        )
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
                        contentDescription = null,
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
        text = { Text(stringResource(Res.string.changing_will_restart_title)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) { Text(stringResource(Res.string.yes)) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) { Text(stringResource(Res.string.no)) }
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

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        DRAW_AMOUNTS.forEachIndexed { index, drawAmountItem ->
            var showDialogChange by remember { mutableStateOf(false) }
            if (showDialogChange) {
                NewGameDialog(
                    title = stringResource(Res.string.change_draw_amount),
                    onConfirm = {
                        drawAmount = drawAmountItem
                        onDrawerClose()
                    },
                    onDismiss = { showDialogChange = false }
                )
            }
            SegmentedButton(
                selected = drawAmount == drawAmountItem,
                onClick = { showDialogChange = true },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = DRAW_AMOUNTS.size),
                label = { Text(stringResource(Res.string.card_draw, drawAmountItem)) }
            )
        }
    }
}

private fun LazyListScope.DifficultChange(
    onDrawerClose: () -> Unit,
) = item {
    var difficulty by rememberModeDifficulty()
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Difficulty.entries.forEachIndexed { index, difficultyItem ->
            var showDialogChange by remember { mutableStateOf(false) }
            if (showDialogChange) {
                NewGameDialog(
                    title = stringResource(Res.string.change_difficulty),
                    onConfirm = {
                        difficulty = difficultyItem
                        onDrawerClose()
                    },
                    onDismiss = {
                        showDialogChange = false
                    }
                )
            }
            SegmentedButton(
                selected = difficulty == difficultyItem,
                onClick = { showDialogChange = true },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                label = { Text(difficultyItem.name) }
            )
        }
    }
}

private fun LazyListScope.CardDesignChange() = item {
    var useNewDesign by rememberUseNewDesign()

    OutlinedCard(
        onClick = { useNewDesign = !useNewDesign },
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        ListItem(
            headlineContent = { Text(stringResource(Res.string.use_new_card_design)) },
            trailingContent = {
                Switch(
                    checked = useNewDesign,
                    onCheckedChange = { useNewDesign = it }
                )
            }
        )
    }
}

private fun LazyListScope.CardBorderChange() = item {
    var backgroundForBorder by rememberBackgroundForBorder()

    OutlinedCard(
        onClick = { backgroundForBorder = !backgroundForBorder },
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        ListItem(
            headlineContent = { Text(stringResource(Res.string.background_for_border)) },
            trailingContent = {
                Switch(
                    checked = backgroundForBorder,
                    onCheckedChange = { backgroundForBorder = it }
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
                            headlineContent = { Text(stringResource(Res.string.default)) },
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
                                headlineContent = { Text(stringResource(Res.string.special)) },
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
                            headlineContent = { Text(stringResource(Res.string.custom)) },
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
                                        stringResource(Res.string.custom_index, index),
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
                            title = stringResource(Res.string.pick_an_image),
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
                                        stringResource(Res.string.add),
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
            }
        }
    }


    OutlinedCard(
        onClick = { showCardBacks = true },
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        ListItem(
            headlineContent = { Text(stringResource(Res.string.card_back)) },
            supportingContent = { Text(cardBack.name) }
        )
    }
}