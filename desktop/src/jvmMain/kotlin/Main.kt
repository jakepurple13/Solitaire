import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.programmersbox.common.UIShow

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
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
            UIShow()
        }
    }
}
