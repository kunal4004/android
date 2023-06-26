package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.json.JSONObject
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.account.PetInsuranceModel
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.CreditCardDeliveryStatusResponse
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.wfs.component.*
import za.co.woolworths.financial.services.android.ui.wfs.component.pull_to_refresh.WfsPullToRefreshUI
import za.co.woolworths.financial.services.android.ui.wfs.core.NetworkStatusUI
import za.co.woolworths.financial.services.android.ui.wfs.core.RetrofitFailureResult
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.findActivity
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature.enumtype.MyAccountSectionHeaderType
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_chat.ui.WfsChatView
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_general.stabletype.GeneralProductType
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_general.ui.GeneralItem
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.ui.OfferCarousel
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.ui.PetInsuranceView
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductCardsGroup
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.LoadingOptions
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.ShimmerOptions
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.model.UserAccountResponse
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.AccountLandingInstantLauncher
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.CommonItem
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.OfferClickEvent
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.OnAccountItemClickListener
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.ui.LinkYourWooliesCardUI
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.ui.NoC2IdNorProductView
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.ui.ProductContainerSwitcher
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.ui.ProductShimmerView
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.ui.ProductViewApplicationStatusView
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_version_info.ui.ApplicationInfoView
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_welcome.ui.WelcomeSectionView
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.FontDimensions
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppBackground
import za.co.woolworths.financial.services.android.ui.wfs.theme.White
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities
import za.co.woolworths.financial.services.android.util.SessionUtilities

@Composable
fun SignedInScreen(
    viewModel: UserAccountLandingViewModel,
    onProductClick : (AccountProductCardsGroup) -> Unit,
    onClick: (OnAccountItemClickListener) -> Unit) {

    val userAccounts by viewModel.getAllUserAccounts.collectAsStateWithLifecycle()
    val userAccountsByProductOfferingId by viewModel.getUserAccountsByProductOfferingId.collectAsStateWithLifecycle()
    val petInsuranceState by viewModel.fetchPetInsuranceState.collectAsStateWithLifecycle()
    val scheduleDeliveryNetworkState by viewModel.scheduleDeliveryNetworkState.collectAsStateWithLifecycle()
    val isAccountLoading = userAccounts.isLoading

    with(viewModel) {
        RequestMessageCount()

        BiometricsCollector(onClick = onClick)

        CollectFetchAccount(stateFetchAllAccounts = userAccounts)

        MyProductRetryOptions(model = userAccountsByProductOfferingId)

        RequestAllAccounts()

        LinkMyDevice()

        PetInsuranceCollector(petInsuranceState = petInsuranceState, onClick = onClick)

        ScheduleCreditCardDeliveryCollector(state = scheduleDeliveryNetworkState, onClick = onClick)

        FicaModelCollector(onClick = onClick)

        SignInContainer(isAccountLoading = isAccountLoading, onClick = onClick, onProductClick = onProductClick,  allUserAccounts = userAccounts)
    }

}

@Composable
fun UserAccountLandingViewModel.BiometricsCollector(onClick: (OnAccountItemClickListener) -> Unit) {
    LaunchedEffect(Unit) {
        if (isBiometricPopupEnabled) {
            onClick(AccountLandingInstantLauncher.BiometricIsRequired)
            isBiometricPopupEnabled = false
        }
    }
}

@Composable
fun UserAccountLandingViewModel.PetInsuranceCollector(
                                                      petInsuranceState: NetworkStatusUI<PetInsuranceModel>,
                                                      onClick: (OnAccountItemClickListener) -> Unit) {
    if (!petInsuranceState.isLoading) {
        petInsuranceState.data?.let { petModel ->
            this.handlePetInsuranceResult(petModel) { insuranceProduct ->
                onClick(
                    AccountLandingInstantLauncher.PetInsuranceNotCoveredAwarenessModel(
                        insuranceProduct
                    )
                )
            }
        }
    }
}

@Composable
private fun UserAccountLandingViewModel.FicaModelRequest() {
    LaunchedEffect(Unit) {
        queryFicaRemoteService()
    }
}

@Composable
private fun UserAccountLandingViewModel.FicaModelCollector(
    onClick: (OnAccountItemClickListener) -> Unit
) {
    LaunchedEffect(true) {
        ficaDeliveryNetworkState.collect { result ->
            result.data?.let { ficaModel ->
                onClick(AccountLandingInstantLauncher.FicaResultListener(ficaModel))
            }
            result.data = null
        }
    }
}

@Composable
private fun UserAccountLandingViewModel.SignInContainer(
    isAccountLoading: Boolean,
    onProductClick : (AccountProductCardsGroup) -> Unit,
    onClick: (OnAccountItemClickListener) -> Unit,
    allUserAccounts: NetworkStatusUI<UserAccountResponse>
) {

    Box(modifier = Modifier.fillMaxSize()) {
        BrushShimmerEffect { brush ->

            Column {
                WelcomeSectionView(
                    viewModel = this@SignInContainer,
                    isRotating = isRefreshButtonRotating,
                    isLoadingInProgress = isAccountLoading,
                    isRotatingState = { isRotating ->
                        isRefreshButtonRotating = isRotating
                    },
                    icon = getRefreshIcon(),
                    brush = brush,
                    onClick = { queryAccountLandingService() })

                SectionDivider()

                // Trigger pull to refresh
                WfsPullToRefreshUI( // state hoisting concept
                    isEnabled = !isAccountLoading && isC2UserOrMyProductItemExist(),
                    isAccountRefreshingTriggered = isAccountRefreshingTriggered,
                    isAccountRefreshing = { refresh ->
                        isAccountRefreshingTriggered = refresh
                        if (refresh) {
                            queryAccountLandingService()
                        }
                    })
                {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = rememberLazyListState()) {

                        myProductsSection(
                            isLoading = isAccountLoading,
                            brush = brush,
                            viewModel = this@SignInContainer,
                            onProductClick = onProductClick)

                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                SpacerHeight24dp()
                                SectionDivider()
                                SpacerBottom(height = Dimens.eight_dp)
                            }
                        }

                        offerViewGroup(
                            isLoading = isAccountLoading,
                            brush = brush,
                            viewModel = this@SignInContainer,
                            onClick
                        )

                        item { SectionDivider() }

                        profileAndGeneralViewGroup(
                            isLoading = isAccountLoading,
                            viewModel = this@SignInContainer,
                            brush = brush,
                            onClick
                        )

                    }
                }
            }
        }

        if (!isAccountLoading) {
            Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                WfsChatView(convertProductToAccountModel(allUserAccounts.data?.accountList))
            }
        }
    }
}


@Composable
fun UserAccountLandingViewModel.ScheduleCreditCardDeliveryCollector(
    state: NetworkStatusUI<CreditCardDeliveryStatusResponse>?,
    onClick: (OnAccountItemClickListener) -> Unit
) {
    state?.data?.let { response ->
        val creditCardDelivery = onScheduleCreditCardDeliveryResponse(response)
        creditCardDelivery?.let { item ->
            onClick(
                AccountLandingInstantLauncher.ScheduleCreditCardDelivery(
                    response,
                    item
                )
            )
        }
        state.data = null
    }
}

@Composable
private fun UserAccountLandingViewModel.RequestMessageCount() {
    LaunchedEffect(Unit) {
        queryUserMessagesService()
    }
}

@Composable
fun UserAccountLandingViewModel.MyProductRetryOptions(
    model: NetworkStatusUI<UserAccountResponse>
) {
    model.apply {

        if (isLoading) {
            setRetryButtonInProgress()
        }

        data?.let { it.account?.let { details -> setProductDetails(it, details) }
            data = null
        }

        if (hasError) {
            when (errorMessage) {
                is RetrofitFailureResult.ServerResponse<*> -> errorResponse.value =
                    (errorMessage.data as? UserAccountResponse)?.response

                else -> Unit
            }
            hasError = false
        }
    }
}

@Composable
private fun UserAccountLandingViewModel.CollectFetchAccount(
    stateFetchAllAccounts: NetworkStatusUI<UserAccountResponse>) {

    if (!stateFetchAllAccounts.isLoading) {
        if (stateFetchAllAccounts.hasError ) {
            when (val result = stateFetchAllAccounts.errorMessage) {

                RetrofitFailureResult.NoConnectionState -> {
                    isAutoReconnectActivated = false
                    isRefreshButtonRotating = false
                }

                is RetrofitFailureResult.ServerResponse<*> -> {
                    errorResponse.value = result.data as? ServerErrorResponse
                }

                is RetrofitFailureResult.SessionTimeout<*> -> {
                    val context = LocalContext.current
                    val activity = context.findActivity()
                    val serverResponse = (result.data as? UserAccountResponse)?.response
                    var stsParams = serverResponse?.stsParams
                    val message = serverResponse?.message ?: ""
                    if (stsParams?.isEmpty() == true && message.isNotEmpty()){
                        val messageObj = JSONObject(message)
                        stsParams = messageObj.getString("sts_params")
                    }
                    setUserUnAuthenticated(SSOActivity.SSOActivityResult.SIGNED_OUT.rawValue())
                    LaunchedEffect(Unit) {
                        SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, stsParams)
                        SessionExpiredUtilities.getInstance().showSessionExpireDialog(activity)

                    }
                }

                else -> Unit

            }
        }
        stopLoading()
        stateFetchAllAccounts.data?.let { accountResponse ->
            handleUserAccountResponse(
                accountResponse
            )
        }
        ScheduleCreditCreditDeliveryRequest()
        FicaModelRequest()
    }
}

@Composable
fun UserAccountLandingViewModel.ScheduleCreditCreditDeliveryRequest() {
    LaunchedEffect(Unit) {
        queryServiceScheduleYourDelivery()
    }
}

@Composable
private fun UserAccountLandingViewModel.RequestAllAccounts() {
    LaunchedEffect(true) {
        if (isAccountRefreshingTriggered) {
            queryAccountLandingService(true)
        }
    }
}

@Composable
private fun UserAccountLandingViewModel.LinkMyDevice() {
    LaunchedEffect(Unit) {
        remote.getAllLinkedDevices(true)
    }
}

private fun LazyListScope.profileAndGeneralViewGroup(
    isLoading: Boolean,
    viewModel: UserAccountLandingViewModel,
    brush: Brush,
    onClick: (OnAccountItemClickListener) -> Unit
) {
    viewModel.buildSignInList().forEach {
        item {
            viewModel.UiElements(isLoading = isLoading, it, brush, onClick)
        }
    }
}

private fun LazyListScope.offerViewGroup(
    isLoading: Boolean,
    brush: Brush,
    viewModel: UserAccountLandingViewModel,
    offerClicked: (OfferClickEvent) -> Unit
) {
    // My offer items
    item {
        val product = MyAccountSectionHeaderType.MyOffers.title()
        val locator = product.automationLocatorKey ?: ""
        HeaderItem(
            title = stringResource(id = product.title),
            locator = locator,
            isLoading = isLoading,
            brush = brush
        )
    }

    item {
        SpacerBottom(height = Dimens.sixteen_dp)
    }

    item {
        OfferCarousel(
            viewModel=viewModel,
            myOffers = viewModel.mapOfMyOffers,
            isLoading = isLoading,
            isBottomSpacerShown = viewModel.isC2User(),
            brush = brush
        ) {
            offerClicked(it)
        }
    }
}

private fun LazyListScope.myProductsSection(
    isLoading: Boolean,
    brush: Brush,
    viewModel: UserAccountLandingViewModel,
    onProductClick: (AccountProductCardsGroup) -> Unit
) {
    val shimmerOptions = ShimmerOptions(brush = brush)
    val loadingOptions = LoadingOptions(isAccountLoading = isLoading)
    val myProductList = viewModel.mapOfFinalProductItems

    productHeaderView(isLoading, brush)

    for (item in myProductList) {
        when (val productItems = item.value) {
            is AccountProductCardsGroup.ApplicationStatus -> item {
                ProductViewApplicationStatusView(
                    onClick = onProductClick,
                    applicationStatus = productItems.copy(
                        isLoadingInProgress = loadingOptions,
                        shimmerOptions = shimmerOptions
                    )
                )
            }


            is AccountProductCardsGroup.PetInsurance -> item (key = item.key) {
                if (loadingOptions.isAccountLoading) {
                    ProductShimmerView(
                        brush = shimmerOptions.brush,
                        key = productItems.properties.automationLocatorKey
                    )
                }

                if (!loadingOptions.isAccountLoading) {
                    PetInsuranceView(
                        productGroup = productItems,
                        petInsuranceDefaultConfig = viewModel.getPetInsuranceMobileConfig()?.defaultCopyPetPending,
                        onProductClick = onProductClick
                    )
                }
            }

            else -> item {
                productItems?.let { item ->
                    if (loadingOptions.isAccountLoading) {
                        ProductShimmerView(
                            brush = brush,
                            key = item.properties.automationLocatorKey
                        )
                    }
                    if (!loadingOptions.isAccountLoading) {
                        ProductContainerSwitcher(productGroup = item, onProductClick = onProductClick)
                    }
                }
            }
        }
    }

    if (myProductList.isEmpty()) {
        item {
            NoC2IdNorProductView(
                isLoadingInProgress = isLoading,
                brush = brush,
                isBottomSpacerShown = viewModel.isC2User(),
                onClick = onProductClick
            )
        }
    }

    if (!viewModel.isC2User()) {
        item {
            LinkYourWooliesCardUI(isLoading, brush, onProductClick)
        }
    }

}

private fun LazyListScope.productHeaderView(
    isLoading: Boolean,
    brush: Brush?
) {
    item {
        val product = MyAccountSectionHeaderType.MyProducts.title()
        val title = stringResource(product.title)
        val locator = product.automationLocatorKey ?: ""
        TextFuturaFamilyHeader1(
            text = title,
            locator = locator,
            textColor = Color.Black,
            modifier = Modifier
                .padding(start = Margin.start, top = Margin.end)
                .testAutomationTag(locator),
            isLoading = isLoading,
            brush = brush
        )
    }
    item { SpacerHeight8dp() }
}

@Composable
fun UserAccountLandingViewModel.UiElements(
    isLoading: Boolean,
    content: Any?,
    brush: Brush? = null,
    onClick: (OnAccountItemClickListener) -> Unit
) {
    val messageState by messageState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        when (content) {
            CommonItem.Divider -> DividerThicknessOne()
            is CommonItem.Header -> HeaderItem(
                title = stringResource(id = content.title),
                locator = content.automationLocatorKey ?: "",
                isLoading = isLoading,
                brush = brush
            )

            CommonItem.SectionDivider -> SectionDivider()
            CommonItem.Spacer24dp -> SpacerBottom(height = Dimens.dp24)
            CommonItem.Spacer8dp -> SpacerBottom(
                height = Dimens.eight_dp,
                bgColor = White
            )

            CommonItem.Spacer80dp -> SpacerBottom(
                height = Dimens.eighty_dp,
                bgColor = OneAppBackground
            )

            CommonItem.SpacerBottom -> SpacerBottom(
                Dimens.sixty_dp,
                bgColor = OneAppBackground
            )

            is CommonItem.UserAccountApplicationInfo -> ApplicationInfoView(
                applicationInfo = content,
                isLoading = isLoading,
                brush = brush
            )

            is GeneralProductType -> {
                if (content is GeneralProductType.Messages) {
                    content.unreadMessageCount = messageState.data?.unreadCount ?: 0
                }
                GeneralItem(
                    item = content,
                    isLoading = isLoading,
                    brush = brush
                ) { onClick(it) }
            }

            else -> Unit
        }
    }
}

@Composable
fun HeaderItem(
    title: String,
    locator: String,
    textAlign: TextAlign = TextAlign.Start,
    fontSize: TextUnit = FontDimensions.sp20,
    isLoading: Boolean = false,
    brush: Brush? = null) {
    TextFuturaFamilyHeader1(
        text = title,
        locator = locator,
        textAlign = textAlign,
        fontSize = fontSize,
        textColor = Color.Black,
        modifier = Modifier.padding(start = Margin.start, top = Margin.top),
        isLoading = isLoading,
        brush = brush
    )
}
