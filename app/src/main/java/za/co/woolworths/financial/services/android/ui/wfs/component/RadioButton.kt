package za.co.woolworths.financial.services.android.ui.wfs.component

import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.awfs.coordination.R

@Composable
fun CheckedUncheckedRadioButton(isChecked : Boolean  = false, locator: String,
                                onClick: () -> Unit){
    IconToggleButton( checked = isChecked,
        onCheckedChange = { onClick() })
    {
        Icon(
            painter = painterResource(if (isChecked) R.drawable.checked_item
            else R.drawable.ic_unchecked_radio),
            contentDescription = "Radio button icon",
            tint= Color.Unspecified
        )
    }
}