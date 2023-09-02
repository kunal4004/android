package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import za.co.woolworths.financial.services.android.models.dto.app_config.account_options.DefaultCopyPetPending
import za.co.woolworths.financial.services.android.ui.wfs.component.SurfaceTextButton
import za.co.woolworths.financial.services.android.ui.wfs.component.TextWFuturaMedium
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_products_section_box_constraint_background_image
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_products_section_box_constraint_row_good_standing_my_cover_button
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_products_section_box_constraint_row_good_standing_retry_button
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_products_section_box_constraint_row_good_standing_view_button
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.createLocator
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.ui.backgroundImage
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.ProductProperties
import za.co.woolworths.financial.services.android.ui.wfs.theme.FontDimensions
import za.co.woolworths.financial.services.android.ui.wfs.theme.LetterSpacing
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin
import za.co.woolworths.financial.services.android.ui.wfs.theme.White

enum class MyProductButtonType { VIEW, RETRY, MY_COVER }
enum class ButtonState { IDLE, LOADING }

@Composable
fun ViewRetryMyCoverButtonGroup(
    petInsuranceDefaultConfig: DefaultCopyPetPending? = null,
    buttonType: MyProductButtonType,
    buttonState: ButtonState,
    viewButtonLabel: String,
    retryButtonLabel: String,
    locator: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(end = Margin.end),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.End
    ) {

        when (buttonType) {
            MyProductButtonType.VIEW -> SurfaceTextButton(
                    locator = createLocator(default = my_products_section_box_constraint_row_good_standing_view_button, key= locator),
                    isClickable = false,
                    buttonState = buttonState,
                    buttonLabel = viewButtonLabel
                ) {}


            MyProductButtonType.RETRY -> SurfaceTextButton(
                    locator =  createLocator(default = my_products_section_box_constraint_row_good_standing_retry_button, key= locator),
                    isClickable = false,
                    buttonState = buttonState,
                    buttonLabel = retryButtonLabel
                ) {}


            MyProductButtonType.MY_COVER -> SurfaceTextButton(
                    locator = createLocator(default = my_products_section_box_constraint_row_good_standing_my_cover_button, key= locator),
                    isClickable = false,
                    buttonState = buttonState,
                    buttonLabel = petInsuranceDefaultConfig?.action ?: ""

            ) {}
        }
    }
}

@Composable
fun BackgroundImage(
    properties: ProductProperties,
    title: String,
    locator : String) {
    val backgroundImageLocator = createLocator(my_products_section_box_constraint_background_image, locator)
    Image(
        painter = painterResource(id = properties.background),
        contentDescription = title,
        modifier = Modifier
            .fillMaxWidth()
            .testAutomationTag(backgroundImageLocator)
            .layoutId(backgroundImage),
        contentScale = ContentScale.FillWidth
    )
}

@Composable
fun MyProductTitleText(
    titleLocator: String,
    color: Color? = White,
    title: String) {
    TextWFuturaMedium(
        locator = titleLocator,
        text = title,
        color = color ?: White,
        textAlign = TextAlign.Start,
        fontSize = FontDimensions.offerTitleTextSP12,
        letterSpacing = LetterSpacing.ls05
    )
}
