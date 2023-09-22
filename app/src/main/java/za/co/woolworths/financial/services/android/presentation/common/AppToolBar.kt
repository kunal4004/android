package za.co.woolworths.financial.services.android.presentation.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme

/**
 * Created by Kunal Uttarwar on 21/09/23.
 */

@Composable
fun AppToolBar(
    modifier: Modifier = Modifier,
    title: String = "",
    backIcon: Int = R.drawable.back24,
    onClick: (event: ToolbarEvents) -> Unit
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = backIcon),
            contentDescription = "Back Button",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 22.dp)
                .size(24.dp)
                .clickable {
                    onClick(ToolbarEvents.BackPressed)
                }
        )
        Text(
            text = title.uppercase(),
            modifier = Modifier.align(Alignment.Center),
            letterSpacing = 2.sp,
            style = TextStyle(
                fontFamily = FuturaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = Color.Black
            )
        )
    }
}
@Preview(showSystemUi = true, showBackground = true)
@Composable
fun AppToolbarPreview() {
    OneAppTheme {
        AppToolBar(title = "My Shopping Lists", onClick = {})
    }
}