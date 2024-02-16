package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.component.MatchingSetsUIEvents
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily

/**
 * Created by Kunal Uttarwar on 14/02/24.
 */

@Composable
fun MatchingSetHeaderView(
    modifier: Modifier = Modifier,
    onEvent: (event: MatchingSetsUIEvents) -> Unit,
    seeMoreText: Int
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f, true),
            text = stringResource(id = R.string.matching_set_title),
            style = TextStyle(
                fontFamily = FuturaFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.W600,
                textAlign = TextAlign.Start,
                color = Color.Black,
                lineHeight = 27.sp
            )
        )
        Text(
            text = stringResource(id = seeMoreText),
            style = TextStyle(
                fontFamily = FuturaFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.W500,
                textAlign = TextAlign.End,
                color = Color.Black,
                lineHeight = 18.sp,
                letterSpacing = 1.sp,
            ),
            modifier = Modifier.clickable {
                onEvent(MatchingSetsUIEvents.seeMoreClick(seeMoreText != R.string.matching_set_see_less_button_text))
            }
        )
    }
}