package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens

typealias ImageParams = Triple<Float, Dp, Dp>

@Composable
fun imageAspectRatio(@DrawableRes resourceId: Int,
                     desiredWidthInFloat : Float? =  0.85f,
                     desiredHeightDp : Dp? = Dimens.account_landing_my_offers_image_height_dp): ImageParams {

    val configuration = LocalConfiguration.current
    val resources = LocalContext.current.resources
    val density = resources.displayMetrics.density
    val desiredImageHeightOnDevice = (desiredHeightDp?.times(density))?.value?.toInt()?.roundToDp() ?: desiredHeightDp

    //Determine the dimensions of the image in pixels.
    val resource = painterResource(id = resourceId).intrinsicSize
    val imageIntrinsicSize = remember { resource }
    val imageWidth = imageIntrinsicSize.width.dp
    val imageHeight = imageIntrinsicSize.height.dp

    val imageWidthInPx =  imageWidth.roundToPx()
    val imageHeightInPx =  imageHeight.roundToPx()

    //Determine the dimensions of the mobile device screen in pixels.
    val screenWidth =  configuration.screenWidthDp.dp * (desiredWidthInFloat ?: 1f)
    val screenHeight = desiredImageHeightOnDevice ?: 1.dp

    val screenWidthInPx = screenWidth.roundToPx()
    val screenHeightInPx = screenHeight.roundToPx()

    //Divide the width of the image by the width of the screen to get the horizontal scale factor.
    val horizontalScaleFactor = imageWidthInPx.div(screenWidthInPx)

    //Divide the height of the image by the height of the screen to get the vertical scale factor.
    val verticalScaleFactor = imageHeightInPx.div(screenHeightInPx)

    val aspectRatio = verticalScaleFactor / horizontalScaleFactor

    return ImageParams(aspectRatio, screenWidth, screenHeight)

}
