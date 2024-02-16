package za.co.woolworths.financial.services.android.ui.fragments.colorandsize

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import coil.compose.AsyncImage
import za.co.woolworths.financial.services.android.ui.wfs.theme.Black
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily

@Composable
fun ProductDetailRow(productUrl: String, productName: String, productPrice: String) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
            AsyncImage(
                modifier = Modifier
                    .height(56.dp)
                    .width(40.dp)
                    .fillMaxHeight()
                    .weight(.1f),
                model = productUrl,
                placeholder = painterResource(id = R.drawable.placeholder_product_list),
                error = painterResource(id = R.drawable.placeholder_product_list),
                contentDescription = stringResource(id = R.string.description),
            )

            Text(
                text = productName, overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(.7f)
                    .padding(start = 16.dp, end = 8.dp),
                maxLines = 2, style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 15.6.sp,
                    fontFamily = OpenSansFontFamily,
                    fontWeight = FontWeight(400),
                    color = Black,
                )
            )


        Text(
            text = productPrice,
            style = TextStyle(
                fontSize = 13.sp,
                lineHeight = 19.5.sp,
                fontFamily = OpenSansFontFamily,
                fontWeight = FontWeight(600),
                color = Color.Black
            )
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewProductDetailRow() {
    ProductDetailRow(
        "https://pm.epages.com/webroot/store/shops/apidocu/51e7/f905/2e4c/78c2/30c2/ac14/145f/a4e5/013-headphone-red_h.jpg ",
        "Organic Cotton Ribbed Velour Joggers",
        "R 499.99"
    )
}