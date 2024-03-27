import androidx.compose.ui.window.application
import com.programmersbox.common.UIShow

fun main() = application {
    WindowWithBar(
        onCloseRequest = ::exitApplication,
        windowTitle = "Solitaire"
    ) {
        UIShow()
    }
}
