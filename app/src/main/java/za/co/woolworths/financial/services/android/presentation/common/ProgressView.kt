package za.co.woolworths.financial.services.android.presentation.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.theme.Color444444
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily

/**
 * Circular indicator with optional title and description
 */
@Composable
fun ProgressView(
    modifier: Modifier = Modifier,
    circleColors: List<Color> = listOf(
        Color.Black,
        Color.White
    ),
    title: String = "",
    desc: String = ""
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        LoadingAnimation(
            circleColors = circleColors
        )

        if (title.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(top = 32.dp),
                text = title,
                style = TextStyle(
                    fontFamily = FuturaFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color.Black
                ),
                textAlign = TextAlign.Center
            )
        }

        if (desc.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = desc,
                style = TextStyle(
                    fontFamily = OpenSansFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = Color444444
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LoadingAnimation(
    indicatorSize: Dp = 80.dp,
    circleColors: List<Color> = listOf(
        Color.Black,
        Color.White
    ),
    animationDuration: Int = 1500
) {

    val infiniteTransition = rememberInfiniteTransition()

    val rotateAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDuration,
                easing = LinearEasing
            )
        )
    )

    CircularProgressIndicator(
        modifier = Modifier
            .size(size = indicatorSize)
            .rotate(degrees = rotateAnimation)
            .border(
                width = 4.dp,
                brush = Brush.sweepGradient(circleColors),
                shape = CircleShape
            ),
        progress = 1f,
        strokeWidth = 1.dp,
        color = MaterialTheme.colorScheme.background // Set background color
    )
}

@Preview(showBackground = true)
@Composable
private fun ProgressViewPreview() {
    OneAppTheme {
        ProgressView(
            title = stringResource(id = R.string.add_to_list_progress_title),
            desc = stringResource(id = R.string.processing_your_request_desc),
            circleColors =  listOf(
                Color(0xFF5851D8),
                Color(0xFF833AB4),
                Color(0xFFC13584),
                Color(0xFFE1306C),
                Color(0xFFFD1D1D),
                Color(0xFFF56040),
                Color(0xFFF77737),
                Color(0xFFFCAF45),
                Color(0xFFFFDC80),
                Color(0xFF5851D8)
            )
        )
    }
}