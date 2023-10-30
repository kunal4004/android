package za.co.woolworths.financial.services.android.shoppinglist.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.presentation.common.BlackButton
import za.co.woolworths.financial.services.android.presentation.common.UnderlineButton
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorD8D8D8
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily

@Composable
fun ShareListDialog(onShareButtonClick:(String)->Unit, onCancelClick:()->Unit) {

    val context = LocalContext.current
    var selectedOption by remember {
        mutableStateOf(context.getString(R.string.view_only_option))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Spacer(
            modifier = Modifier
                .width(50.dp)
                .height(5.dp)
                .background(ColorD8D8D8)
                .align(Alignment.CenterHorizontally)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .align(Alignment.Start)
        ) {
            Image(
                painter = painterResource(
                    id = if (selectedOption == stringResource(id = R.string.view_only_option)) R.drawable.check_mark_icon
                    else R.drawable.uncheck_item
                ),
                contentDescription = null,
                modifier = Modifier.clickable {
                    selectedOption = context.getString(R.string.view_only_option)
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.view_only_option), style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = OpenSansFontFamily,
                        lineHeight = 21.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.W600
                    )
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
                Text(
                    text = stringResource(id = R.string.view_only_desc), style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = OpenSansFontFamily,
                        lineHeight = 21.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.W400
                    )
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Image(
                painter = painterResource(
                    id = if (selectedOption == stringResource(id = R.string.edit_option)) R.drawable.check_mark_icon
                    else R.drawable.uncheck_item
                ),
                contentDescription = null,
                modifier = Modifier.clickable {
                    selectedOption = context.getString(R.string.edit_option)
                },
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.edit_option), style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = OpenSansFontFamily,
                        lineHeight = 21.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.W600
                    )
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
                Text(
                    text = stringResource(id = R.string.edit_option_desc), style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = OpenSansFontFamily,
                        lineHeight = 21.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.W400
                    )
                )
            }
        }

        BlackButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            text = stringResource(id = R.string.share_btn)
        ) {
            onShareButtonClick(
                selectedOption
            )
        }

        UnderlineButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 8.dp),
            text = stringResource(id = R.string.cancel)
        ) {
            onCancelClick()
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewShareListDialog() {
    OneAppTheme {
        ShareListDialog(onShareButtonClick = {},onCancelClick = {})
    }
}