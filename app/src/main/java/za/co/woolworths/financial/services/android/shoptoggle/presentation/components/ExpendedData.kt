package za.co.woolworths.financial.services.android.shoptoggle.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import za.co.woolworths.financial.services.android.presentation.common.BlackButton
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.ToggleModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorD8D8D8
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily

@Composable
fun ExpendedData(
    isExpended: Boolean,
    item: ToggleModel,
) {
    if (isExpended) {
        Spacer(modifier = Modifier.height(Dimens.sixteen_dp))
        Divider(color = ColorD8D8D8, thickness = Dimens.oneDp)
        Spacer(modifier = Modifier.height(Dimens.sixteen_dp))
        Text(
            modifier = Modifier,
            textAlign = TextAlign.Start,
            text = item.deliveryType,
            style = TextStyle(
                fontFamily = OpenSansFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = Dimens.thirteen_sp,
                color = Color.Black
            )
        )
        Text(
            modifier = Modifier,
            textAlign = TextAlign.Start,
            text = item.deliveryTime,
            style = TextStyle(
                fontFamily = OpenSansFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = Dimens.thirteen_sp,
                color = Color.Black
            )
        )

        Text(
            modifier = Modifier,
            textAlign = TextAlign.Start,
            text = item.deliveryProduct,
            style = TextStyle(
                fontFamily = OpenSansFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = Dimens.thirteen_sp,
                color = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(Dimens.eight_dp))
        Text(
            modifier = Modifier,
            textAlign = TextAlign.Start,
            text = item.deliveryCost,
            style = TextStyle(
                fontFamily = OpenSansFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = Dimens.thirteen_sp,
                color = Color.Black
            )
        )

        Text(
            modifier = Modifier,
            textAlign = TextAlign.Start,
            text = item.learnMore,
            style = TextStyle(
                fontFamily = OpenSansFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = Dimens.thirteen_sp,
                color = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(Dimens.sixteen_dp))

        BlackButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.fifty_dp),
            text = item.deliveryButtonText.uppercase(),
            enabled = true,
        ) {

        }

    }
}
