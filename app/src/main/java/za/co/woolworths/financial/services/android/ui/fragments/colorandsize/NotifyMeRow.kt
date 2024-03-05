package za.co.woolworths.financial.services.android.ui.fragments.colorandsize

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
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
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.component.MatchingSetsUIEvents
import za.co.woolworths.financial.services.android.ui.wfs.theme.Black
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily

/**
 * Created by Kunal Uttarwar on 29/02/24.
 */

@Composable
fun NotifyMeRow(productName: String, onEvent: (event: MatchingSetsUIEvents) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {

        Icon(
            painter = painterResource(id = R.drawable.ic_alarm_20),
            contentDescription = productName
        )

        Text(
            text = stringResource(id = R.string.item_out_of_stock),
            modifier = Modifier
                .weight(.7f)
                .padding(start = 8.dp, end = 8.dp),
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontFamily = OpenSansFontFamily,
                fontWeight = FontWeight(400),
                color = Black,
            )
        )


        Text(
            text = stringResource(id = R.string.notify_me),
            modifier = Modifier
                .background(color = Black)
                .padding(8.dp)
                .clickable {
                    onEvent(MatchingSetsUIEvents.NotifyMeClick)
                },
            style = TextStyle(
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontFamily = OpenSansFontFamily,
                fontWeight = FontWeight(600),
                color = Color.White,
                letterSpacing = 1.sp
            )
        )
    }
}

@Preview
@Composable
fun PreviewNotifyMe() {
    NotifyMeRow(productName = "Bowl", onEvent = {})
}