import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ComposeViewport
import com.programmersbox.common.*
import kotlinx.browser.document
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        MaterialTheme(
            colorScheme = buildColorScheme(isSystemInDarkTheme())
        ) {
            CompositionLocalProvider(
                LocalCardColor provides ComposeCardColor(
                    black = MaterialTheme.colorScheme.onBackground,
                    red = Color.Red
                ),
                LocalCardShowing provides CardShow(
                    full = { it.toSymbolString() },
                    suit = { it.symbol }
                )
            ) {
                App(
                    settings = null,
                    solitaireDatabase = remember {
                        SolitaireDatabase(
                            object : DatabaseStuff {
                                override suspend fun addHighScore(
                                    timeTaken: String,
                                    moveCount: Int,
                                    score: Int,
                                    difficulty: Int,
                                ) = Unit

                                override suspend fun removeHighScore(scoreItem: SolitaireScoreHold) = Unit
                                override fun getSolitaireHighScores(): Flow<List<SolitaireScoreHold>> = emptyFlow()
                            }
                        )
                    }
                )
            }
        }
    }
}