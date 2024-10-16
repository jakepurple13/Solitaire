package com.programmersbox.common

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.materialkolor.ktx.from
import com.materialkolor.palettes.TonalPalette
import com.materialkolor.rememberDynamicColorScheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun SettingsView(
    settings: Settings?,
    onStatsClick: () -> Unit,
) {
    var showCardBacksDropdown by remember { mutableStateOf(false) }

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
            item {
                var drawAmount by rememberDrawAmount()
                var showDialogChange by remember { mutableStateOf(false) }
                if (showDialogChange) {
                    NewGameDialog(
                        title = "Change the draw amount?",
                        onConfirm = { drawAmount = if (drawAmount == 1) 3 else 1 },
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
            item {
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

            item {
                var cardBack by rememberCardBack()

                OutlinedCard(
                    onClick = { showCardBacksDropdown = !showCardBacksDropdown }
                ) {
                    ListItem(
                        headlineContent = { Text("Card Back: ${cardBack.name}") },
                        trailingContent = { Icon(Icons.Default.ArrowDropDown, null) },
                        /*supportingContent = {
                            AnimatedVisibility(showDropdown) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(CardBack.entries.filter { it.includeGsl() }) {
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
                                    }
                                }
                            }
                        }*/
                    )
                }
            }

            if (true) {
                item {
                    var cardBack by rememberCardBack()

                    AnimatedVisibility(showCardBacksDropdown) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(CardBack.entries.filter { it.includeGsl() }) {
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
                            }
                        }
                    }
                }
            }

            item {
                var showThemes by remember { mutableStateOf(false) }
                var themeColor by rememberThemeColor()
                var isAmoled by rememberIsAmoled()

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

            item {
                Button(
                    onClick = onStatsClick,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("View Stats") }
            }

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