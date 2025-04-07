import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.prashant.rtspstreamplayer.ui.theme.AddTypography
import com.prashant.rtspstreamplayer.ui.theme.Pink80
import com.prashant.rtspstreamplayer.ui.theme.Purple80
import com.prashant.rtspstreamplayer.ui.theme.PurpleGrey80

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

@Composable
fun RTSPViewerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AddTypography,
        content = content
    )
}