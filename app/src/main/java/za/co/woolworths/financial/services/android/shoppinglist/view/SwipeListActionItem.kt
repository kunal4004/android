package za.co.woolworths.financial.services.android.shoppinglist.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily

@Composable
fun SwipeListActionItem(
    modifier: Modifier = Modifier,
    icon: Int = R.drawable.delete_24,
    tintColor: Color? = null,
    actionText: Int = R.string.remove,
    textStyle: TextStyle? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable {
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            tint = tintColor ?: Color.Unspecified,
            contentDescription = "Delete"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = actionText).uppercase(),
            style = textStyle ?: TextStyle(
                fontFamily = FuturaFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 12.sp,
                color = Color.Black
            ),
            letterSpacing = 1.5.sp
        )
    }
}