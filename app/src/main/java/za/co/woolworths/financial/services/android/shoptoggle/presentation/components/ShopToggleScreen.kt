package za.co.woolworths.financial.services.android.shoptoggle.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import za.co.woolworths.financial.services.android.shoptoggle.data.ShopToggleData
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily

@Composable
fun shopToggleScreen(item: List<ShopToggleData>) {

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        modifier = Modifier,
        textAlign = TextAlign.Start,
        text = "What would you like to shop?",
        style = TextStyle(
            fontFamily = FuturaFontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 20.sp,
            color = Color.Black
        )
    )
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        modifier = Modifier,
        textAlign = TextAlign.Start,
        text = "You can choose between our 3 delivery options here:",
        style = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = Color.Black
        )
    )
    Spacer(modifier = Modifier.height(24.dp))

    ToggleExpandableList(item)

}

