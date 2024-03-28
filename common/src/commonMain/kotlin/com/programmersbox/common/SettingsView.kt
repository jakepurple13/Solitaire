package com.programmersbox.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsView(
    settings: Settings,
    onStatsClick: () -> Unit,
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
            contentPadding = padding
        ) {
            item {
                val drawAmount by settings.drawAmount.flow.collectAsStateWithLifecycle(DRAW_AMOUNT)
                var showDialogChange by remember { mutableStateOf(false) }
                if (showDialogChange) {
                    NewGameDialog(
                        title = "Change the draw amount?",
                        onConfirm = { scope.launch { settings.drawAmount.update(if (drawAmount == 1) 3 else 1) } },
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
                Button(
                    onClick = onStatsClick,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("View Stats") }
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