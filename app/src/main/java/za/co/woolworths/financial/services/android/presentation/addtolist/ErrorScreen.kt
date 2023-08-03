package za.co.woolworths.financial.services.android.presentation.addtolist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.theme.Color444444
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorEEEEEE
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily

@Composable
fun ErrorScreen(
    modifier: Modifier = Modifier,
    icon: Int = R.drawable.emptyoopssomethingwentwrong,
    title: String = "",
    desc: String = "",
    btnText: String = "",
    onButtonClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(painter = painterResource(id = icon), contentDescription = null)

            Text(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 40.dp),
                text = title,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = FuturaFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color.Black
                )
            )

            Text(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                text = desc,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = OpenSansFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = Color444444
                )
            )
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .height(50.dp)
                .background(color = Color.Black, shape = RectangleShape),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            onClick = {
                onButtonClick()
            }) {
            Text(
                text = btnText.uppercase(),
                letterSpacing = 1.5.sp,
                fontFamily = FuturaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = colorResource(id = R.color.white)
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun ErrorScreenPreview() {
    OneAppTheme {
        ErrorScreen(
            modifier = Modifier.background(ColorEEEEEE),
            title = stringResource(id = R.string.oops_something_went_wrong),
            desc = stringResource(id = R.string.no_connection_desc_1),
            btnText = stringResource(id = R.string.retry)
        ) {}
    }
}