package com.programmersbox.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.materialkolor.ktx.harmonizeWithPrimary
import com.programmersbox.common.generated.resources.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.stringResource

internal val Emerald = Color(0xFF2ecc71)
internal val Sunflower = Color(0xFFf1c40f)
internal val Alizarin = Color(0xFFe74c3c)

private val ColorsToUse = listOf(
    listOf(Color.Red, Color.Green, Color.Blue),
    listOf(Color.Magenta, Color.Yellow, Color.Cyan),
    listOf(Alizarin, Sunflower, Emerald),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun StatsView(
    database: SolitaireDatabase,
) {
    val highScores by database.getSolitaireHighScores().collectAsState(emptyList())
    val winCount by database.getWinCount().collectAsState(0)
    val scope = rememberCoroutineScope()

    TopAppBar(
        title = { Text(stringResource(Res.string.stats)) },
        actions = { Text(stringResource(Res.string.wins, winCount)) }
    )

    val colorScheme = MaterialTheme.colorScheme

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        itemsIndexed(highScores) { index, item ->
            HighScoreItem(
                score = item,
                onDelete = { scope.launch { database.removeHighScore(item) } },
                modifier = Modifier
                    .animateItem()
                    .then(
                        if (index < 3)
                            Modifier.animatedBorder(
                                borderColors = ColorsToUse[index].map { color ->
                                    colorScheme.harmonizeWithPrimary(color)
                                },
                                backgroundColor = Color.Transparent,
                                shape = CardDefaults.shape,
                                borderWidth = 4.dp,
                                animationDurationInMillis = (index + 1) * 1000,
                            )
                        else
                            Modifier
                    )
            )
        }
    }
}

@Composable
private fun HighScoreItem(
    score: SolitaireScoreHold,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isUsing24HourTime: Boolean = true,
) {
    var deleteDialog by remember { mutableStateOf(false) }
    val time = remember(isUsing24HourTime) {
        val d = Instant.fromEpochMilliseconds(score.time).toLocalDateTime(TimeZone.currentSystemDefault())
        d.format(
            LocalDateTime.Format {
                monthName(MonthNames.ENGLISH_FULL)
                char(' ')
                dayOfMonth()
                char(' ')
                year()
                chars(", ")
                if (isUsing24HourTime) {
                    hour()
                    char(':')
                    minute()
                } else {
                    amPmHour()
                    char(':')
                    minute()
                    char(' ')
                    amPmMarker("AM", "PM")
                }
            }
        )
    }
    if (deleteDialog) {
        AlertDialog(
            onDismissRequest = { deleteDialog = false },
            title = { Text(stringResource(Res.string.delete_score, score.score, time)) },
            text = { Text(stringResource(Res.string.are_you_sure)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        deleteDialog = false
                    }
                ) { Text(stringResource(Res.string.yes)) }
            },
            dismissButton = { TextButton(onClick = { deleteDialog = false }) { Text(stringResource(Res.string.no)) } }
        )
    }

    Card(
        modifier = modifier
    ) {
        ListItem(
            overlineContent = { Text(time) },
            headlineContent = { Text(stringResource(Res.string.score, score.score)) },
            supportingContent = {
                Column {
                    Text(stringResource(Res.string.time_taken, score.timeTaken))
                    Text(stringResource(Res.string.move_count, score.moves))
                    Text(stringResource(Res.string.difficulty_title, score.difficulty ?: Difficulty.Normal.name))
                }
            },
            trailingContent = {
                IconButton(
                    onClick = { deleteDialog = true }
                ) { Icon(Icons.Default.Delete, null) }
            }
        )
    }
}