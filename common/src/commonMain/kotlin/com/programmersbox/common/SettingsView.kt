package com.programmersbox.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsView(
    settings: Settings,
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
            contentPadding = padding
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