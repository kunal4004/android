package za.co.woolworths.financial.services.android.presentation.addtolist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme

@Composable
fun AddToListSnackbarView(
    modifier: Modifier = Modifier,
    title: String = "",
    desc: String = "",
    btnTxt: String = stringResource(id = R.string.view),
    count: Int = 0,
    listName: String = "",
    onButtonClick: () -> Unit
) {

    val spannedTitle = buildAnnotatedString {
        append(title.uppercase())
        if (count > 0) {
            addStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.W600
                ),
                start = 0,
                end = count.toString().length
            )
        }
        if (listName.isNotEmpty()) {
            addStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.W600
                ),
                start = title.indexOf(listName),
                end = title.length
            )
        }
    }

    Row(
        modifier = modifier.background(
            Color(0xCC030303)
        ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = spannedTitle,
                style = TextStyle(
                    color = Color.White,
                    fontFamily = FuturaFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            )
            Text(
                modifier = Modifier.padding(vertical = 4.dp),
                text = desc.uppercase(),
                style = TextStyle(
                    color = Color.White,
                    fontFamily = FuturaFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 8.sp,
                    letterSpacing = 1.sp
                )
            )

        }

        Button(
            colors = ButtonDefaults.buttonColors(Color.Transparent, Color.Transparent),
            onClick = { onButtonClick() }
        ) {
            Text(
                text = btnTxt.uppercase(),
                style = TextStyle(
                    color = Color.White,
                    fontFamily = FuturaFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddToListSnackbarViewPreview() {
    OneAppTheme {
        AddToListSnackbarView(
            modifier = Modifier.padding(24.dp),
            title = "3 items added to FAVOURITES",
            desc = "THIS EXCLUDES FREE GIFT ITEMS",
            count = 3,
            listName = "FAVOURITES"
        ) {

        }
    }
}