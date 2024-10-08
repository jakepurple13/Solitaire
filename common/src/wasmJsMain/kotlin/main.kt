import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ComposeViewport
import com.programmersbox.common.*
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        MaterialTheme(
            colorScheme = if (isSystemInDarkTheme())
                darkColorScheme(
                    primary = Color(0xff90CAF9),
                    secondary = Color(0xff90CAF9),
                )
            else
                lightColorScheme(
                    primary = Color(0xff2196F3),
                    secondary = Color(0xff90CAF9),
                )
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
                App(null)
            }
        }
    }
}