package za.co.woolworths.financial.services.android.ui.views.order_again

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import za.co.woolworths.financial.services.android.ui.wfs.theme.Black
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorD85C11
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorF9E0DB
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.White

@Composable
fun OrderState(
    orderTitle: String = "",
    orderState: String = "",
    errorLabel: String = "",
    bgOrderStateColor: Color = ColorD85C11
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = orderTitle,
                style = TextStyle(
                    fontFamily = FuturaFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 18.sp,
                    color = Black
                )
            )

            Text(
                modifier = Modifier
                    .background(bgOrderStateColor, RoundedCornerShape(16.dp))
                    .padding(vertical = 4.dp, horizontal = 12.dp),
                text = orderState,
                style = TextStyle(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.W600,
                    fontFamily = FuturaFontFamily,
                    color = White,
                    textAlign = TextAlign.Center
                )
            )
        }

        if (errorLabel.isNotEmpty()) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp, bottom = 12.dp)
                    .background(ColorF9E0DB, RoundedCornerShape(4.dp))
                    .padding(12.dp),
                text = errorLabel,
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W400,
                    fontFamily = OpenSansFontFamily,
                    color = Black
                )
            )
        }
    }
}