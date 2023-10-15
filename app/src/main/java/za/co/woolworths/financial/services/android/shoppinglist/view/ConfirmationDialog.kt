package za.co.woolworths.financial.services.android.shoppinglist.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.presentation.common.BlackButton
import za.co.woolworths.financial.services.android.presentation.common.UnderlineButton
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorD8D8D8
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily

@Composable
fun Confirmationdoalog(
    title: String, desc: String, onRemoveButtonClick: () -> Unit, onCancelButtonClick: () -> Unit
) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp)
    ) {
        Spacer(
            modifier = Modifier
                .width(50.dp)
                .height(5.dp)
                .background(ColorD8D8D8)
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp)
        )

        Text(
            text = title,
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = FuturaFontFamily,
                fontWeight = FontWeight.W600,
                color = Color.Black
            ),
            modifier = Modifier
                .padding(top = 40.dp)
                .align(Alignment.CenterHorizontally)
        )

        Text(
            text = desc,
            style = TextStyle(
                fontSize = 13.sp,
                fontFamily = OpenSansFontFamily,
                fontWeight = FontWeight.W400,
                color = Color(R.color.color_444444),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally)
        )

        BlackButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            text = context.getString(R.string.remove_title)
        ) {
            onRemoveButtonClick()
        }

        UnderlineButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            text = context.getString(R.string.cancel),
            textColor = Color.Black
        ) {
            onCancelButtonClick()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewShowConfirmationDilog() {
    Confirmationdoalog(
        "Are You Sure?",
        "If you remove these products from your list, you can still shop for them in the app.",
        {

        },{

        }
    )
}