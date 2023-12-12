package za.co.woolworths.financial.services.android.models.dto.app_config.native_checkout

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.Province

/**
 * Created by Kunal Uttarwar on 07/06/21.
 */
@Parcelize
data class ConfigNativeCheckout(
    val isNativeCheckoutEnabled: Boolean,
    val addressTypes: List<String>,
    var regions: List<Province>,
    val checkoutPaymentURL: String?,
    val checkoutPaymentUrlPayInStore: String?,
    val checkoutPostPaymentURL: String?,
    val currentShoppingBag: ConfigCurrentShoppingBag,
    val newShoppingBag: ConfigNewShoppingBag,
    val googlePlacesAddressErrorMessage: String?
) : Parcelable
