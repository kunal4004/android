package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak.Companion.Simple
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import za.co.woolworths.financial.services.android.models.dto.account.InsuranceProducts
import za.co.woolworths.financial.services.android.models.dto.app_config.account_options.DefaultCopyPetPending
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight24dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight8dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerWidth4dp
import za.co.woolworths.financial.services.android.ui.wfs.component.TextOpenSansFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.component.TextWFuturaMedium
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_product_pet_insurance_plan_type_title
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_product_policy_number_label
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_product_policy_number_value
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_products_section_box
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.bounceClick
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.createLocator
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.ProductProperties
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.ProductPropertiesViewType
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductCardsGroup
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.ui.BackgroundImage
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.ui.ButtonState
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.ui.MyProductButtonType
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.ui.ViewRetryMyCoverButtonGroup
import za.co.woolworths.financial.services.android.ui.wfs.theme.BrightGray
import za.co.woolworths.financial.services.android.ui.wfs.theme.FontDimensions
import za.co.woolworths.financial.services.android.ui.wfs.theme.LetterSpacing
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.White

const val columnRef = "columnRef"
 const val backgroundImage = "backgroundImage"

private val  insuranceProduct =  InsuranceProducts(
    type = "pet",
    status = "COVERED",
    policyNumber = null,
    planType = null
)

private val properties = ProductPropertiesViewType.PetInsurance.value()

private val defaultConfig = DefaultCopyPetPending(
    title = "WPet Care",
    subtitle = "Pet Insurance Application Pet Insurance Application Pet Insurance Application",
    action = "My Cover")

@Preview
@Composable
fun PetInsurancePreview(){
    val insuranceProductPending = insuranceProduct.copy(
        planType = "WPet Care Origin",
        status = "PENDING",
        policyNumber = "1234568123456812345681234568123456812345681234568123456812345681234568"
    )

    val insuranceProductPendingNoPlanTypeNoPolicyNumber = insuranceProduct.copy(status = "PENDING")

    OneAppTheme {
        Column(modifier = Modifier.background(Color.Black)) {
            SpacerHeight24dp(bgColor = Color.Transparent)
            ProductPetInsuranceRow(
                insuranceProduct = insuranceProduct,
                defaultConfig = defaultConfig,
                properties = properties
            )
            SpacerHeight8dp()
            ProductPetInsuranceRow(
                insuranceProduct = insuranceProductPending,
                defaultConfig = defaultConfig,
                properties = properties
            )
            SpacerHeight8dp()
            ProductPetInsuranceRow(
                insuranceProduct = insuranceProductPendingNoPlanTypeNoPolicyNumber,
                defaultConfig = defaultConfig,
                properties = properties
            )
            SpacerHeight24dp(bgColor = Color.Transparent)
        }
    }
}

@Composable
fun ProductPetInsuranceRow(
    insuranceProduct: InsuranceProducts?,
    defaultConfig: DefaultCopyPetPending?,
    properties: ProductProperties
) {
    val title = insuranceProduct?.planType ?: defaultConfig?.title ?: ""
    val policyNumber = insuranceProduct?.policyNumber
    val descLabel = if (policyNumber != null) stringResource(id = properties.availableProduct) else defaultConfig?.subtitle ?: ""
    TextWFuturaMedium(
        locator = my_product_pet_insurance_plan_type_title,
        text = title.uppercase(),
        fontSize = FontDimensions.sp12,
        letterSpacing = LetterSpacing.ls10,
        color = White
    )

    SpacerHeight8dp(bgColor = Color.Transparent)

    Row(verticalAlignment = Alignment.Top) {
        TextOpenSansFontFamily(
            color = BrightGray,
            text = descLabel,
            textAlign = TextAlign.Start,
            style = LocalTextStyle.current.copy(
                lineBreak = Simple
            ),
            locator = my_product_policy_number_label,
            fontSize = FontDimensions.policyLabel15Sp)

        policyNumber?.let {
            SpacerWidth4dp()
            TextOpenSansFontFamily(
                text = it,
                locator = my_product_policy_number_value,
                fontFamily = OpenSansFontFamily,
                maxLines = 2,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.SemiBold,
                 style = LocalTextStyle.current.copy(
                    lineBreak = Simple
                ),
                fontSize = FontDimensions.policyNumberValue15Sp,
                color = White
            )
        }
    }
}


fun createConstraints(): ConstraintSet {
    return ConstraintSet {
        val imageRef = createRefFor(backgroundImage)
        val columnRef = createRefFor(columnRef)
        // Constraint the background image to the parent edges
        constrain(imageRef) {
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        // Constraint the column to the edges of the background image and set its height to match constraints
        constrain(columnRef) {
            top.linkTo(imageRef.top)
            bottom.linkTo(imageRef.bottom)
            start.linkTo(imageRef.start)
            end.linkTo(imageRef.end)
            height = Dimension.fillToConstraints
        }
    }
}



@Composable
fun PetInsuranceView(
    modifier : Modifier = Modifier,
    productGroup: AccountProductCardsGroup.PetInsurance,
    petInsuranceDefaultConfig: DefaultCopyPetPending?,
    onProductClick: (AccountProductCardsGroup) -> Unit) {

        Box(
            modifier = modifier
                .wrapContentSize()
                .testAutomationTag(createLocator(my_products_section_box, properties.automationLocatorKey))
                .padding(start = Margin.start, end = Margin.end, top = Margin.dp16)
                .bounceClick { onProductClick.invoke(productGroup) },
        ) {
            ConstraintLayout(constraintSet = createConstraints()) {
                BackgroundImage(properties,  stringResource(id = properties.productTitle), properties.automationLocatorKey)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .layoutId(columnRef)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.69f)
                            .fillMaxHeight()
                            .padding(start = Margin.start),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        ProductPetInsuranceRow(
                            insuranceProduct = productGroup.insuranceProducts,
                            petInsuranceDefaultConfig,
                            properties
                        )
                    }

                    ViewRetryMyCoverButtonGroup(
                        petInsuranceDefaultConfig,
                        buttonType = MyProductButtonType.MY_COVER,
                        buttonState = ButtonState.IDLE,
                        viewButtonLabel = "",
                        retryButtonLabel = "",
                        locator = properties.automationLocatorKey
                    )
                }
            }
    }
}
