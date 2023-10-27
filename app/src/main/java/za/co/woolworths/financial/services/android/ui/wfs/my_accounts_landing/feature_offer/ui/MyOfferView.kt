package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import za.co.woolworths.financial.services.android.ui.wfs.component.LazyListRowSnap
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight24dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight8dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerWidth16dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerWidth24dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SurfaceTag
import za.co.woolworths.financial.services.android.ui.wfs.component.TextOpenSansFamilyBoldH1
import za.co.woolworths.financial.services.android.ui.wfs.core.animationDurationMilis400
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.box_shimmer_image
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.my_offers_row
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.sign_out_my_offers_background_image
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.sign_out_my_offers_button
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.sign_out_my_offers_carousel
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.sign_out_my_offers_container_card
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.sign_out_my_offers_container_constraint_layout
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.sign_out_my_offers_container_constraint_layout_column
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.sign_out_my_offers_description_text
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.sign_out_my_offers_title_text
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.ImageParams
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.bounceClick
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.createLocator
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.imageAspectRatio
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature.screen.LazyColumnKeyConstant
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.schema.OfferProductType
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountOfferKeys
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.CommonItem
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.OfferClickEvent
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.ui.MyProductTitleText
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.FloatDimensions
import za.co.woolworths.financial.services.android.ui.wfs.theme.FontDimensions
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme

@Preview
@Composable
fun OfferViewMainPreview() {
    OneAppTheme {
        val item = OfferProductType.ViewFreeCreditReport.value()

        val listOfOffers = mutableMapOf<AccountOfferKeys, CommonItem.OfferItem?>(
            AccountOfferKeys.ViewApplicationStatus to OfferProductType.ViewApplicationStatus.value(),
            AccountOfferKeys.PetInsurance to OfferProductType.PetInsurance.value(),
            AccountOfferKeys.StoreCardApplyNow to OfferProductType.StoreCardApplyNow.value(),
            AccountOfferKeys.CreditCardApplyNow to OfferProductType.CreditCardApplyNow.value()
        )

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            LazyListRowSnap {
                item { SpacerWidth24dp() }
                item { OfferViewRow(item = item) }
                item { SpacerWidth24dp() }
            }
            SpacerHeight24dp()
           // OfferViewMainList(listOfOffers) {}
        }
    }
}

@Composable
fun OfferViewMainList(
    viewModel: UserAccountLandingViewModel,
    items: MutableMap<AccountOfferKeys, CommonItem.OfferItem?>,
    isLoading: Boolean = false,
    isBottomSpacerShown : Boolean = false,
    onClick: (OfferClickEvent) -> Unit) {

    val listOfOffers = remember { items.values.toMutableList()}
    val listOfOfferSize = remember { listOfOffers.size }

    if (listOfOfferSize == 1) {
        val offer = remember {listOfOffers[0]}
        val locator = offer?.automationLocatorKey
        Column(modifier = Modifier.padding(start = Margin.start, end = Margin.end)) {
            SpacerHeight8dp()
            offer?.let { item ->
                OfferViewCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testAutomationTag(createLocator(my_offers_row, locator)), item,
                    onClick = onClick
                ) {
                    OfferViewRow(item = item,  isLoading, listSize = listOfOfferSize)
                }
            }
            if (isBottomSpacerShown){
                SpacerHeight24dp()
            }
        }
    } else {
        LazyListRowSnap(modifier = Modifier.testAutomationTag(sign_out_my_offers_carousel)) {
            item(key = LazyColumnKeyConstant.OfferLazyListRowSnapSpacerWidth24dp) { SpacerWidth24dp() }
            items(items = listOfOffers,key = { offer -> offer?.key ?: "N/A" }, itemContent = {item ->
                item ?: return@items
                val locator = item.automationLocatorKey

                if (!item.data.isAnimationEnabled) {
                    OfferCards(locator, item, onClick, isLoading, listOfOfferSize)
                }

                    AnimatedVisibility(
                        visible = item.data.isAnimationEnabled,
                        enter = if (!viewModel.petInsuranceDidAnimateOnce) slideInHorizontally(animationSpec = tween(durationMillis = animationDurationMilis400, easing = LinearEasing))
                        else EnterTransition.None,
                        exit = ExitTransition.Companion.None
                    ) {
                        OfferCards(locator, item, onClick, isLoading, listOfOfferSize)
                        viewModel.petInsuranceDidAnimateOnce = true
                    }

            })


        }
    }
}

@Composable
private fun OfferCards(
    locator: String,
    item: CommonItem.OfferItem,
    onClick: (OfferClickEvent) -> Unit,
    isLoading: Boolean,
    listOfOfferSize: Int
) {
    Row(
        modifier = Modifier.testAutomationTag(
            createLocator(
                default = my_offers_row,
                locator
            )
        )
    ) {
        OfferViewCard(
            modifier =
            Modifier
                .fillMaxWidth()
                .testAutomationTag(createLocator(my_offers_row, locator)),
            item,
            onClick = onClick
        ) {
            OfferViewRow(
                item = item,
                isLoading = isLoading,
                listSize = listOfOfferSize
            )
        }
        SpacerWidth16dp()
    }
}

@Composable
fun OfferViewRow(
    item: CommonItem.OfferItem,
    isLoading: Boolean = false,
    listSize: Int = 0
) {

    val data = item.data
    val properties = item.properties
    val desiredWidthInFloat = if (listSize == 1) 1f else FloatDimensions.my_offer_card_width
    val title = stringResource(id = data.title).uppercase()
    val description = stringResource(id = data.description)
    val params: ImageParams = imageAspectRatio(resourceId = data.image, desiredWidthInFloat = desiredWidthInFloat)
    val locator = item.automationLocatorKey

    if (isLoading){
        OfferShimmerView(modifier = Modifier
            .width(params.second)
            .height(params.third)
            .testAutomationTag(createLocator(box_shimmer_image, locator)))
    }

    if (!isLoading){
        ConstraintLayout(
            modifier = Modifier
                .testAutomationTag(createLocator(sign_out_my_offers_container_constraint_layout, locator))
        ) {
            val (backgroundImageRef, columnRefs) = createRefs()
            val marginStartGuideline = createGuidelineFromStart(Margin.start)
            val marginEndGuideline = createGuidelineFromEnd(FloatDimensions.my_offer_margin_end_guideline)
            val marginTopGuideline = createGuidelineFromTop(Margin.top)
            val marginBottomGuideline = createGuidelineFromBottom(Margin.top)

            Image(
                modifier = Modifier
                    .width(params.second)
                    .height(params.third)
                    .testAutomationTag(createLocator(sign_out_my_offers_background_image, locator))
                    .constrainAs(backgroundImageRef) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    },
                painter = painterResource(id = data.image),
                contentDescription = stringResource(id = data.title),
                contentScale = ContentScale.FillBounds
            )

            Column(modifier = Modifier
                .fillMaxWidth()
                .testAutomationTag(
                    createLocator(
                        sign_out_my_offers_container_constraint_layout_column,
                        locator
                    )
                )
                .constrainAs(columnRefs) {
                    top.linkTo(marginTopGuideline)
                    start.linkTo(marginStartGuideline)
                    end.linkTo(marginEndGuideline)
                    bottom.linkTo(marginBottomGuideline)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }) {

                MyProductTitleText(titleLocator = createLocator(sign_out_my_offers_title_text, locator),
                    color = properties.titleColor,
                    title = title.uppercase())

                SpacerHeight8dp(bgColor = Color.Transparent, height = 9.dp)

                TextOpenSansFamilyBoldH1(
                    fontSize = FontDimensions.sp16,
                    lineHeight = Dimens.twenty_sp,
                    text = description,
                    letterSpacing= 0.sp,
                    locator = createLocator(sign_out_my_offers_description_text, locator),
                    minLines = 3,
                    color = properties.descriptionColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                SurfaceTag(
                    value = stringResource(id = data.buttonId),
                    modifier = Modifier.testAutomationTag(createLocator(sign_out_my_offers_button, locator)),
                    containerColor = properties.buttonBackgroundColor,
                    contentColor = properties.buttonTextColor
                )
            }
        }
    }

}

@Composable
fun OfferViewCard(
    modifier: Modifier = Modifier,
    item: CommonItem.OfferItem,
    onClick: (OfferClickEvent) -> Unit,
    content: @Composable () -> Unit
) {
        Card(
            shape = MaterialTheme.shapes.small,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = modifier.then(
                Modifier
                    .testAutomationTag(sign_out_my_offers_container_card)
                    .bounceClick { onClick(item.onClick) }
            )) {
            content()
        }
}

