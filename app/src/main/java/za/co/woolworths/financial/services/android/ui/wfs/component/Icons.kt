package za.co.woolworths.financial.services.android.ui.wfs.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.awfs.coordination.R

@Composable
fun MyIcon(@DrawableRes id : Int?, modifier: Modifier? = Modifier,  @StringRes contentDescriptionId : Int? = R.string.app_name, locator: String = "") {
   id ?: return
    Icon(
        painter =  painterResource(id = id) ,
        contentDescription = locator.ifEmpty { contentDescriptionId?.let { stringResource(it) } },
        modifier = modifier ?: Modifier
    )
}