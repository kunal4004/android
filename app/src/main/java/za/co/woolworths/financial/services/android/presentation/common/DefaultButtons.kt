package za.co.woolworths.financial.services.android.presentation.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import za.co.woolworths.financial.services.android.ui.wfs.theme.Color666666
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily

@Composable
fun BlackButton(
    modifier: Modifier = Modifier,
    text: String = "",
    enabled: Boolean = true,
    onButtonClick: () -> Unit
) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        onClick = { onButtonClick() },
        colors = ButtonDefaults.buttonColors(Color.Black),
        shape = RectangleShape,
        enabled = enabled
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = FuturaFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 12.sp,
                color = Color.White
            ),
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
fun UnderlineButton(
    modifier: Modifier = Modifier,
    text: String = "",
    texColor: Color = Color666666,
    enabled: Boolean = true,
    onButtonClick: () -> Unit
) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        onClick = { onButtonClick() },
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        shape = RectangleShape,
        enabled = enabled
    ) {
        Text(
            text = text.uppercase(),
            style = TextStyle(
                fontFamily = FuturaFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = texColor,
                textDecoration = TextDecoration.Underline
            ),
            letterSpacing = 1.5.sp
        )
    }
}