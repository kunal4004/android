package za.co.woolworths.financial.services.android.shoppinglist.view

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily
import java.util.Locale


/**
 * Created by Kunal Uttarwar on 26/09/23.
 */

@Composable
fun ChangeLocationView(
    icon: Int,
    deliveryType: String,
    deliveryLocation: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = icon),
            modifier = Modifier
                .padding(start = 26.dp, end = 10.dp, top = 18.dp, bottom = 18.dp)
                .size(27.dp),
            contentDescription = deliveryType
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = deliveryType.ifBlank { stringResource(R.string.standard_delivery) }
                    .replaceFirstChar {
                        if (it.isLowerCase())
                            it.titlecase(Locale.getDefault())
                        else
                            it.toString()
                    },
                style = TextStyle(
                    fontFamily = OpenSansFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = Color.Black
                )
            )
            Text(
                text = deliveryLocation.ifBlank { stringResource(id = R.string.default_location) }
                    .replaceFirstChar {
                        if (it.isLowerCase())
                            it.titlecase(Locale.getDefault())
                        else
                            it.toString()
                    },
                style = TextStyle(
                    fontFamily = OpenSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.Black
                )
            )
        }
        Image(
            painter = painterResource(id = R.drawable.ic_caret_black),
            contentDescription = null,
            modifier = Modifier.padding(end = 30.dp, top = 26.dp, bottom = 26.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun changeLocationViewPreview() {
    OneAppTheme {
        ChangeLocationView(
            icon = R.drawable.dash_delivery_icon,
            deliveryType = "Standard Delivery",
            deliveryLocation = "28 Eastlake Island Drive"
        ) {}
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun changeLocationViewNightPreview() {
    OneAppTheme {
        ChangeLocationView(
            icon = R.drawable.dash_delivery_icon,
            deliveryType = "Standard Delivery",
            deliveryLocation = "28 Eastlake Island Drive"
        ) {}
    }
}