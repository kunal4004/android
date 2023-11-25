package za.co.woolworths.financial.services.android.presentation.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.theme.ShimmerColor

@Composable
fun CircleIcon(
    @DrawableRes icon: Int,
    background: Color = Color.Transparent,
    onIconClick: () -> Unit
) {
    Icon(
        modifier = Modifier
            .size(32.dp)
            .background(background, RoundedCornerShape(32.dp))
            .padding(8.dp)
            .clickable {
                onIconClick()
            },
        painter = painterResource(id = icon),
        contentDescription = ""
    )
}


@Preview
@Composable
private fun PreviewCircleIcon() {
    OneAppTheme {
        CircleIcon(R.drawable.add_black, ShimmerColor){}
    }
}