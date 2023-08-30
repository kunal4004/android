package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.InsuranceProducts
import za.co.woolworths.financial.services.android.models.dto.app_config.account_options.PetInsuranceConfig
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.application_status
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.model.ProductDetails
import za.co.woolworths.financial.services.android.ui.wfs.theme.Obsidian

sealed class AccountProductCardsGroup(
    open val isLoadingInProgress: LoadingOptions = LoadingOptions(),
    open val shimmerOptions: ShimmerOptions? = ShimmerOptions(),
    open val properties: ProductProperties,
    open val retryOptions: RetryOptions? = null,
    open val applyNowState: ApplyNowState? = null,
    open val productDetails: ProductDetails? = null) {

    data class StoreCard(
        val transformer: ProductTransformer,
        override val productDetails: ProductDetails?,
        override var retryOptions: RetryOptions = RetryOptions(),
        override val isLoadingInProgress: LoadingOptions = LoadingOptions(),
        override val shimmerOptions: ShimmerOptions? = ShimmerOptions(),
        override val properties: ProductProperties = ProductPropertiesViewType.StoreCard.value(),
        override val applyNowState: ApplyNowState? = ApplyNowState.STORE_CARD,
    ) : AccountProductCardsGroup(
        isLoadingInProgress = isLoadingInProgress,
        shimmerOptions = shimmerOptions,
        properties = properties,
        retryOptions = retryOptions,
        applyNowState = applyNowState,
        productDetails = productDetails
    )

    data class PersonalLoan(
        val transformer: ProductTransformer,
        override val productDetails: ProductDetails?,
        override var retryOptions: RetryOptions = RetryOptions(),
        override val isLoadingInProgress: LoadingOptions = LoadingOptions(),
        override val shimmerOptions: ShimmerOptions? = ShimmerOptions(),
        override val properties: ProductProperties = ProductPropertiesViewType.PersonalLoan.value(),
        override val applyNowState: ApplyNowState? = ApplyNowState.PERSONAL_LOAN
    ) : AccountProductCardsGroup(
        isLoadingInProgress = isLoadingInProgress,
        shimmerOptions = shimmerOptions,
        properties = properties,
        retryOptions = retryOptions,
        applyNowState = applyNowState,
        productDetails = productDetails
    )

    data class BlackCreditCard(
        val transformer: ProductTransformer,
        override val productDetails: ProductDetails?,
        override var retryOptions: RetryOptions = RetryOptions(),
        override val isLoadingInProgress: LoadingOptions = LoadingOptions(),
        override val shimmerOptions: ShimmerOptions? = ShimmerOptions(),
        override val properties: ProductProperties = ProductPropertiesViewType.BlackCreditCard.value(),
        override val applyNowState: ApplyNowState? = ApplyNowState.BLACK_CREDIT_CARD
    ) : AccountProductCardsGroup(
        isLoadingInProgress = isLoadingInProgress,
        shimmerOptions = shimmerOptions,
        properties = properties,
        retryOptions = retryOptions,
        applyNowState = applyNowState,
        productDetails = productDetails
    )


    data class GoldCreditCard(
        val transformer: ProductTransformer,
        override val productDetails: ProductDetails?,
        override var retryOptions: RetryOptions = RetryOptions(),
        override val isLoadingInProgress: LoadingOptions = LoadingOptions(),
        override val shimmerOptions: ShimmerOptions? = ShimmerOptions(),
        override val properties: ProductProperties = ProductPropertiesViewType.GoldCreditCard.value(),
        override val applyNowState: ApplyNowState? = ApplyNowState.GOLD_CREDIT_CARD
    ) : AccountProductCardsGroup(
        isLoadingInProgress = isLoadingInProgress,
        shimmerOptions = shimmerOptions,
        properties = properties,
        retryOptions = retryOptions,
        applyNowState = applyNowState,
        productDetails = productDetails
    )

    data class SilverCreditCard(
        val transformer: ProductTransformer,
        override val isLoadingInProgress: LoadingOptions = LoadingOptions(),
        override val productDetails: ProductDetails?,
        override var retryOptions: RetryOptions = RetryOptions(),
        override val shimmerOptions: ShimmerOptions? = ShimmerOptions(),
        override val properties: ProductProperties = ProductPropertiesViewType.SilverCreditCard.value(),
        override val applyNowState: ApplyNowState? = ApplyNowState.SILVER_CREDIT_CARD
    ) : AccountProductCardsGroup(
        isLoadingInProgress = isLoadingInProgress,
        shimmerOptions = shimmerOptions,
        properties = properties,
        retryOptions = retryOptions,
        applyNowState = applyNowState,
        productDetails = productDetails
    )

    data class PetInsurance(
        override val isLoadingInProgress: LoadingOptions = LoadingOptions(),
        val insuranceProducts: InsuranceProducts? = null,
        val title: String? = null,
        val description: String? = null,
        val action: String? = null,
        val petInsuranceConfig : PetInsuranceConfig? = null,
        override val shimmerOptions: ShimmerOptions? = ShimmerOptions(),
        override val properties: ProductProperties = ProductPropertiesViewType.PetInsurance.value()
    ) : AccountProductCardsGroup(
        isLoadingInProgress = isLoadingInProgress,
        shimmerOptions = shimmerOptions,
        properties = properties)

    data class ApplicationStatus(
        override val isLoadingInProgress: LoadingOptions = LoadingOptions(),
        @StringRes val title: Int = R.string.application_status,
        @StringRes val buttonLabel: Int = R.string.view,
        val color: Color = Obsidian,
        override val shimmerOptions: ShimmerOptions? = ShimmerOptions()
    ) : AccountProductCardsGroup(
        isLoadingInProgress = isLoadingInProgress, shimmerOptions = shimmerOptions,
        properties = ProductProperties(automationLocatorKey = application_status)
    )

    data class LinkYourWooliesCard(
        override val isLoadingInProgress: LoadingOptions = LoadingOptions(),
        override val properties: ProductProperties = ProductPropertiesViewType.PersonalLoan.value(),
        override val shimmerOptions: ShimmerOptions? = ShimmerOptions()
    ) : AccountProductCardsGroup(
        isLoadingInProgress = isLoadingInProgress,
        properties = properties,
        shimmerOptions = shimmerOptions
    )
}

@Immutable
data class ProductTransformer(
    val isAccountInArrears: Boolean = false,
    val isAccountChargedOff: Boolean = false,
    val currentAmount: String? = ""
)

@Immutable
data class RetryOptions(
    @StringRes val action: Int = R.string.retry_label,
    @StringRes var description: Int = R.string.my_products_cant_load_product,
    @StringRes var loading: Int = R.string.my_products_loading,
    var isRetryButtonEnabled: Boolean = false,
    var isRetryInProgress: Boolean = false
)

@Immutable
data class LoadingOptions(val isAccountLoading: Boolean = false)

@Immutable
data class ShimmerOptions(val brush: Brush? = null)