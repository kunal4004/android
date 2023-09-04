package za.co.woolworths.financial.services.android.shoptoggle.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.ToggleModel
import za.co.woolworths.financial.services.android.shoptoggle.presentation.viewmodel.ShopToggleViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily

@Composable
fun ShopToggleScreen(
    viewModel: ShopToggleViewModel,
    item: List<ToggleModel>,
) {

    Spacer(modifier = Modifier.height(Dimens.sixteen_dp))

    Text(
        modifier = Modifier,
        textAlign = TextAlign.Start,
        text = stringResource(R.string.what_would_you_like_to_shop),
        style = TextStyle(
            fontFamily = FuturaFontFamily,
            fontWeight = FontWeight.W600,
            fontSize = Dimens.twenty_sp,
            color = Color.Black
        )
    )
    Spacer(modifier = Modifier.height(Dimens.sixteen_dp))

    Text(
        modifier = Modifier,
        textAlign = TextAlign.Start,
        text = stringResource(R.string.you_can_choose_option),
        style = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = Dimens.thirteen_sp,
            color = Color.Black
        )
    )
    Spacer(modifier = Modifier.height(Dimens.dp24))

    ToggleExpandableList(viewModel, item)

}

