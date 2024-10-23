import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.window.ComposeViewport
import com.programmersbox.common.*
import com.programmersbox.common.generated.resources.Res
import com.programmersbox.common.generated.resources.jetbrains_mono_regular
import kotlinx.browser.document
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.Font

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        val ff = FontFamily(Font(resource = Res.font.jetbrains_mono_regular))

        MaterialTheme(
            colorScheme = buildColorScheme(isSystemInDarkTheme()),
            typography = Typography(
                displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = ff),
                displayMedium = MaterialTheme.typography.displayMedium.copy(fontFamily = ff),
                displaySmall = MaterialTheme.typography.displaySmall.copy(fontFamily = ff),
                headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = ff),
                headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = ff),
                headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontFamily = ff),
                titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = ff),
                titleMedium = MaterialTheme.typography.titleMedium.copy(fontFamily = ff),
                titleSmall = MaterialTheme.typography.titleSmall.copy(fontFamily = ff),
                bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = ff),
                bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = ff),
                bodySmall = MaterialTheme.typography.bodySmall.copy(fontFamily = ff),
                labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = ff),
                labelMedium = MaterialTheme.typography.labelMedium.copy(fontFamily = ff),
                labelSmall = MaterialTheme.typography.labelSmall.copy(fontFamily = ff),
            )
        ) {
            CompositionLocalProvider(
                LocalCardColor provides ComposeCardColor(
                    black = MaterialTheme.colorScheme.onBackground,
                    red = Color.Red
                ),
                /*LocalCardShowing provides CardShow(
                    full = { it.toSymbolString() },
                    suit = { it.symbol }
                )*/
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
                                override fun customCardBacks(): Flow<List<CustomCardBackHolder>> = emptyFlow()

                                override suspend fun saveCardBack(image: ImageBitmap) {}

                                override suspend fun removeCardBack(image: ImageBitmap) {}

                                override fun getCustomCardBack(uuid: String): Flow<CustomCardBackHolder?> = emptyFlow()
                            }
                        )
                    }
                )
            }
        }
    }
}
