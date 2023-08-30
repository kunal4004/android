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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.theme.White

@Composable
fun ButtonBlack(onClick: () -> Unit) {
    TextFuturaFamilySemiBoldHeader1(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clickable { onClick() }
                .background(Color.Black)
                .wrapContentHeight(Alignment.CenterVertically),
            text = stringResource(id = R.string.retry),
            letterSpacing = 2.sp,
            isUpperCased = true,
            textColor = White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center

    )
}

@Composable
fun ButtonNoBackground(onClick: () -> Unit) {
    TextOpenSansFontFamily(text = stringResource(id = R.string.dismiss).uppercase(),
        locator = stringResource(id = R.string.dismiss),
        letterSpacing = 2.sp,
        fontSize = 12.sp,
        color = Color.Black,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 8.dp,
                bottom = 10.dp
            )
            .clickable { onClick() }
        )
}