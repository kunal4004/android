package za.co.woolworths.financial.services.android.shoptoggle.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.LearnMore
import za.co.woolworths.financial.services.android.ui.wfs.theme.*


@Composable
fun LearnMoreBottomSheetListItem(
    learnMoreItem: LearnMore,
) {
    Row(
        modifier = Modifier,
    ) {
        Image(
            painter = painterResource(id = learnMoreItem.icon),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(Dimens.sixteen_dp))
        Column(modifier = Modifier) {
            Text(
                modifier = Modifier,
                textAlign = TextAlign.Start,
                text = learnMoreItem.title,
                style = TextStyle(
                    fontFamily = OpenSansFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = Dimens.fourteen_sp,
                    color = Color.Black
                ),
            )
            Text(
                modifier = Modifier,
                textAlign = TextAlign.Start,
                text = learnMoreItem.description,
                style = TextStyle(
                    fontFamily = OpenSansFontFamily,
                    fontWeight = FontWeight.W400,
                    fontSize = Dimens.fourteen_sp,
                    color = TitleSmall
                ),
            )
        }
    }
}

