package za.co.woolworths.financial.services.android.ui.views.order_again

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight16dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight8dp
import za.co.woolworths.financial.services.android.ui.wfs.theme.Black
import za.co.woolworths.financial.services.android.ui.wfs.theme.Color444444
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.HeaderGrey
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.White

@Composable
fun EndlessAisleBarcodeView(
    barcodeStringValue: String = "",
    image: Bitmap
) {
    Column(
        modifier = Modifier
            .background(White)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.endless_aisle_barcode_title),
            style = TextStyle(
                fontFamily = FuturaFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 20.sp,
                color = HeaderGrey,
                textAlign = TextAlign.Center
            )
        )

        SpacerHeight16dp()

        Image(
            bitmap = image.asImageBitmap(),
            contentDescription = "Barcode",
            modifier = Modifier
                .height(74.dp)
                .width(314.dp)
        )

        SpacerHeight8dp()

        Text(
            text = barcodeStringValue,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.W600,
                fontFamily = FuturaFontFamily,
                textAlign = TextAlign.Center,
                color = Black,
                letterSpacing = 2.sp
            )
        )

        SpacerHeight16dp()

        Text(
            text = stringResource(R.string.endless_aisle_barcode_desc),
            style = TextStyle(
                fontFamily = OpenSansFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = Color444444
            )
        )
    }
}

@Preview
@Composable
private fun PreviewEndlessAisleBarcodeView() {
    OneAppTheme {
        EndlessAisleBarcodeView(
            "800 410 000 007 164 001",
            Bitmap.createBitmap(314, 74, Bitmap.Config.ARGB_8888)
        )
    }
}