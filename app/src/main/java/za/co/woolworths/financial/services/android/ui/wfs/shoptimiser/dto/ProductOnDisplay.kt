package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.dto

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize
import za.co.woolworths.financial.services.android.models.dto.app_config.WfsPaymentMethods

/**
 * Represents the details of a product displayed on the screen.
 *
 * This data class encapsulates information about a product's display status, available funds,
 * cashback earnings, associated drawable resource, installment amount, availability of sufficient
 * funds, available payment methods, and whether it's the last product to be displayed.
 * @property isLoading Indicates whether the product is in a loading state.
 * @property availableFunds The available funds for the product.
 * @property earnCashBack The cashback earnings associated with the product.
 * @property drawableId The Drawable resource ID for the product's visual representation.
 * @property installmentAmount The installment amount for the product.
 * @property isSufficientFundsAvailable Indicates if sufficient funds are available for the product.
 * @property wfsPaymentMethods The available payment methods for the product.
 * @property isLastProduct Indicates whether this product is the last one to be displayed.
 */

@Parcelize
data class ProductOnDisplay(
    val isLoading : Boolean = false,
    val availableFunds: String? = null,
    val earnCashBack :String?=null,
    @DrawableRes val drawableId : Int,
    val installmentAmount : String? = null,
    val isSufficientFundsAvailable: AvailableFundsSufficiency? = AvailableFundsSufficiency.INSUFFICIENT,
    val wfsPaymentMethods : WfsPaymentMethods? = null,
    val isLastProduct : Boolean = false
) : Parcelable

enum class AvailableFundsSufficiency { SUFFICIENT , INSUFFICIENT }