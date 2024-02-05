package za.co.woolworths.financial.services.android.util.expand

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.component.DividerLight1dp
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorD8D8D8
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppBackground
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily

@Composable
fun OrderAgainView(
    modifier: Modifier = Modifier,
    isSignedIn: Boolean = false
) {
    val text by remember {
        derivedStateOf {
            if(isSignedIn) {
                R.string.order_again
            } else R.string.sign_in_to_order_again
        }
    }
    Column (Modifier.background(Color.White)){
        Box(
            modifier = modifier
                .padding(16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier
                    .height(84.dp)
                    .fillMaxWidth(),
                painter = painterResource(id = R.drawable.bg_order_again_category),
                contentDescription = stringResource(id = R.string.cd_order_again)
            )
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(14.dp)
            ) {
                Text(
                    modifier = Modifier,
                    text = stringResource(id = R.string.order_again_title),
                    style = TextStyle(
                        fontFamily = OpenSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.Black
                    )
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    modifier = Modifier
                        .background(Color.Black, RoundedCornerShape(4.dp))
                        .padding(6.dp),
                    text = stringResource(id = text).uppercase(),
                    style = TextStyle(
                        fontFamily = FuturaFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 12.sp,
                        color = Color.White
                    ),
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(
            modifier = Modifier
                .background(OneAppBackground)
                .height(1.dp)
                .fillMaxWidth()
        )
    }
}

@Preview(showSystemUi = false, showBackground = false)
@Composable
private fun OrderAgainPreview() {
    OneAppTheme {
        OrderAgainView()
    }
}