package com.programmersbox.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsView(
    settings: Settings?,
    onStatsClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(2.dp)
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
                var showDropdown by remember { mutableStateOf(false) }
                OutlinedCard(
                    onClick = { showDropdown = !showDropdown }
                ) {
                    ListItem(
                        headlineContent = { Text("Card Back: ${cardBack.name}") },
                        trailingContent = { Icon(Icons.Default.ArrowDropDown, null) },
                        supportingContent = {
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
                        }
                    )
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