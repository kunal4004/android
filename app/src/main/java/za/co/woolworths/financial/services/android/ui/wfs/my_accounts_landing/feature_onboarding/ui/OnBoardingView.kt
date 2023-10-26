package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_onboarding.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.awfs.coordination.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.wfs.component.*
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.sign_out_on_boarding_carousel
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.sign_out_on_boarding_carousel_box
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.sign_out_on_boarding_carousel_circle_indicator_carousel
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.sign_out_on_boarding_carousel_column
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.sign_out_on_boarding_carousel_desc
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.sign_out_on_boarding_carousel_image
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.sign_out_on_boarding_carousel_register_button
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.sign_out_on_boarding_carousel_sign_in_button
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.LazyRowSnapAnimation
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.createLocator
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_onboarding.schema.WalkThrough
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.ManageLoginRegister
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.OnAccountItemClickListener
import za.co.woolworths.financial.services.android.ui.wfs.theme.*

@Composable
fun OnBoardingCarousel(
    items: List<WalkThrough>,
    onClick: (OnAccountItemClickListener) -> Unit
) {
    val configuration = LocalConfiguration.current
    val coroutineScope = rememberCoroutineScope()
    val signIn = stringResource(id = R.string.sign_in).uppercase()
    val register = stringResource(id = R.string.register).uppercase()

    LazyRowSnapAnimation(width = configuration.screenWidthDp.dp) { listState ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testAutomationTag(sign_out_on_boarding_carousel_column)
                .wrapContentHeight()
        ) {
            OnBoardingSection(listState, items)
            Spacer(modifier = Modifier.height(Dimens.thirty_one_dp))
            IndicatorSection(coroutineScope, listState, items)
            Spacer(modifier = Modifier.height(Dimens.twenty_three_dp))
            SignInAndRegisterButtonSection(okLabel = signIn, cancelLabel = register) { onClick(it) }
        }
    }
}

@Composable
private fun OnBoardingSection(
    listState: LazyListState,
    items: List<WalkThrough>
) {
    BoxWithConstraints {
        LazyRow(
            state = listState,
            modifier =
            Modifier
                .fillMaxWidth()
                .testAutomationTag(locator = sign_out_on_boarding_carousel)
        ) {
            /**
             * Unstable width when using fillParentMaxWidth() inside a LazyRow
             * https://issuetracker.google.com/issues/182490045
             **/

            items(items = items) { item ->
                val boxCarousel =   createLocator(default = sign_out_on_boarding_carousel_box, key = item.automationTestScreenLocator )
                Box(
                    modifier = Modifier
                        .testAutomationTag(locator = boxCarousel)
                        .fillParentMaxHeight(0.7f)
                        .fillParentMaxWidth()
                ) {
                    OnBoardingItem(item)
                }
            }
        }
    }
}

@Composable
fun OnBoardingItem(item: WalkThrough) {

    val imageLocator = createLocator(
        default = sign_out_on_boarding_carousel_image ,
        key = item.automationTestScreenLocator
    )

    val textLocator =  createLocator(sign_out_on_boarding_carousel_desc ,
        item.automationTestScreenLocator)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = Dimens.thirty_two_dp,
                end = Dimens.thirty_two_dp
            )
    ) {

        Image(
            modifier = Modifier
                .testAutomationTag(locator = imageLocator)
                .fillMaxWidth(),
            painter = painterResource(id = item.resourceId),
            contentDescription = stringResource(id = item.stringId),
            contentScale = ContentScale.FillBounds
        )
        Spacer(modifier = Modifier.height(Dimens.fifty_seven_dp))

        TextFuturaFamilyHeader1(
            text = stringResource(id = item.stringId),
            locator = textLocator,
            textColor = Black,
            textAlign = TextAlign.Center,
            fontSize = FontDimensions.sp16,
            isLoading = false)

    }
}

@Composable
fun IndicatorSection(
    coroutineScope: CoroutineScope,
    listState: LazyListState,
    walkThroughItems: List<WalkThrough>
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .testAutomationTag(locator = sign_out_on_boarding_carousel_circle_indicator_carousel),
        state = listState,
        horizontalArrangement = Arrangement.Center
    ) {
        items(items = walkThroughItems) { item ->
            val currentPosition = remember { derivedStateOf { listState.firstVisibleItemIndex } }

            val imageLocator =  createLocator(
                default = sign_out_on_boarding_carousel_image,
                key = item.automationTestScreenLocator
            )

            Image(
                painter = painterResource(id = if (walkThroughItems[currentPosition.value] == item) R.drawable.ic_indicator_filled else R.drawable.ic_indicator),
                contentDescription = stringResource(
                    id = item.stringId
                ),
                modifier = Modifier
                    .width(Dimens.ten_dp)
                    .height(Dimens.ten_dp)
                    .testAutomationTag(imageLocator)
                    .padding(end = Dimens.four_dp)
                    .clickable {
                        coroutineScope.launch {
                            val position = walkThroughItems.indexOf(item)
                            listState.animateScrollToItem(position)
                        }
                    }
            )
        }
    }
}

@Composable
fun SignInAndRegisterButtonSection(
    okLabel: String,
    cancelLabel: String,
    onClick: (OnAccountItemClickListener) -> Unit
) {

    val  buttonSignIn = sign_out_on_boarding_carousel_sign_in_button
    val  buttonRegister = sign_out_on_boarding_carousel_register_button

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = Dimens.dp24,
                end = Dimens.dp24
            )
          ,
        horizontalArrangement = Arrangement.spacedBy(Dimens.eight_dp)
    ) {

        ButtonBorder(modifier = Modifier
            .weight(1f)
            .testAutomationTag(buttonSignIn)
            , textAlign = TextAlign.Center
            , label = okLabel) {
            onClick(ManageLoginRegister.SignIn)
        }

        ButtonBorder(modifier = Modifier
            .weight(1f)
            .testAutomationTag(buttonRegister)
            , textAlign = TextAlign.Center
            , label = cancelLabel) {
            onClick(ManageLoginRegister.Register)
        }
    }
}

@Preview
@Composable
fun WfsOutlinedButton() {

    val signIn = stringResource(id = R.string.sign_in).uppercase()
    val register = stringResource(id = R.string.register).uppercase()

    OneAppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
        ) {
            Spacer(Modifier.height(Dimens.dp24))
            ButtonBorder(signIn) {}
            Spacer(Modifier.height(Dimens.dp24))
            SignInAndRegisterButtonSection(okLabel = signIn, cancelLabel = register) {}
            Spacer(Modifier.height(Dimens.dp24))
        }
    }

}
