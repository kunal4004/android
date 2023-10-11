package za.co.woolworths.financial.services.android.shoppinglist.view

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme

/**
 * Created by Kunal Uttarwar on 21/09/23.
 */

@Composable
fun CreateNewListView(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    onCreateListClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = title),
            style = TextStyle(
                fontFamily = FuturaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = Color.Black
            )
        )
        Image(
            modifier = Modifier
                .size(24.dp)
                .clickable { onCreateListClick() },
            painter = painterResource(id = icon),
            contentDescription = stringResource(id = title)
        )
    }
}
@Preview(showBackground = true)
@Composable
fun CreateNewListViewPreview() {
    OneAppTheme {
        CreateNewListView(icon = R.drawable.ic_add_circle, title = R.string.shop_create_list) {}
    }
}