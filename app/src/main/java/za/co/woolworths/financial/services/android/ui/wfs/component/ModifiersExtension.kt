package za.co.woolworths.financial.services.android.ui.wfs.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppBackground
import za.co.woolworths.financial.services.android.ui.wfs.theme.White

val screenMarginModifier = Modifier
    .fillMaxSize()
    .padding(start = 24.dp, end = 15.dp, bottom = 0.dp, top = 0.dp)
    .background(OneAppBackground)

val screenModifier = Modifier
    .fillMaxSize()
    .background(OneAppBackground)


val lazyColumnModifier = Modifier
    .fillMaxWidth()
    .wrapContentHeight()
    .background(White)