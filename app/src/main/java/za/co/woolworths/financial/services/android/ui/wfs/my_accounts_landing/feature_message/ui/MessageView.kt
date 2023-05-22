package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_message.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import za.co.woolworths.financial.services.android.ui.wfs.component.TextOpenSansMediumH3
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.circleLayout
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin
import za.co.woolworths.financial.services.android.ui.wfs.theme.Obsidian
import za.co.woolworths.financial.services.android.ui.wfs.theme.White

@Composable
fun UpdateMessageCount(messageCount: Int) {
    Box (modifier = Modifier.padding(end = Margin.end)){
        TextOpenSansMediumH3(
            text = "$messageCount",
            textAlign = TextAlign.Center,
            fontSize = 10.sp,
            color = White,
            modifier = Modifier
                .background(Obsidian, shape = CircleShape)
                .defaultMinSize(Dimens.icon_size_dp)
                .padding(Dimens.four_dp)
                .wrapContentSize()
                .circleLayout()
        )
    }
}