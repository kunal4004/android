package za.co.woolworths.financial.services.android.presentation.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorD8D8D8
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.theme.ShimmerColor

@Composable
fun CircleIcon(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    isEnabled: Boolean = true,
    contentDesc: String = "",
    background: Color = Color.Transparent,
    onIconClick: () -> Unit
) {
    Icon(
        modifier = Modifier
            .size(32.dp)
            .background(background, RoundedCornerShape(32.dp))
            .padding(8.dp)
            .then(modifier)
            .clickable(enabled = isEnabled) {
                onIconClick()
            },
        painter = painterResource(id = icon),
        tint = LocalContentColor.current.copy(alpha = if (isEnabled) 1f else 0.5f),
        contentDescription = contentDesc
    )
}

@Composable
fun DialogHandle() {
    Spacer(
        modifier = Modifier
            .padding(vertical = 5.dp)
            .padding(bottom = 10.dp)
            .width(40.dp)
            .height(4.dp)
            .background(
                ColorD8D8D8,
                RoundedCornerShape(2.dp)
            )
    )
}


@Preview
@Composable
private fun PreviewCircleIcon() {
    OneAppTheme {
        CircleIcon(
            modifier = Modifier
                .size(12.dp)
                .padding(3.dp),
            icon = R.drawable.ic_minus_black,
            isEnabled = false,
            contentDesc = "",
            background = ShimmerColor
        ) {}
    }
}