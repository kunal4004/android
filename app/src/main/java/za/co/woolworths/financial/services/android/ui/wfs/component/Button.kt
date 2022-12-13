package za.co.woolworths.financial.services.android.ui.wfs.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell.LabelMediumText
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell.LabelTitle
import za.co.woolworths.financial.services.android.ui.wfs.theme.TitleMedium
import za.co.woolworths.financial.services.android.ui.wfs.theme.White

@Composable
fun ButtonBlack(onClick: () -> Unit) {
    LabelTitle(
        params = LabelProperties(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clickable { onClick() }
                .background(Color.Black)
                .wrapContentHeight(Alignment.CenterVertically)
            ,
            stringId = R.string.retry,
            letterSpacing = 2.sp,
            isUpperCased = true,
            textColor = White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    )
}

@Composable
fun ButtonNoBackground(onClick: () -> Unit) {
    LabelMediumText(
        params = LabelProperties(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top= 8.dp, bottom = 10.dp)
                .clickable {onClick()},
            textAlign = TextAlign.Center,
            isUpperCased =  true,
            stringId = R.string.dismiss,
            letterSpacing = 2.sp,
            textDecoration = TextDecoration.Underline,
            fontSize = 12.sp,
            textColor = TitleMedium
        )
    )
}