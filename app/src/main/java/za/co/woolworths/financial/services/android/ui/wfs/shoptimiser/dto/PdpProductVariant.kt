package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import za.co.woolworths.financial.services.android.models.dto.ProductDetails
/**
 * fbhProduct : Determine whether product type is clothing, homeware or beauty
 * isOnlinePromotionalTextNotNull : add top margin to accordion when promotional text not empty
 * price : Price on display
 */
@Parcelize
data class PdpProductVariant(val fbhProduct: FBHProduct? = FBHProduct.HomeWareProducts,
                             val isOnlinePromotionalTextNotNull : Boolean = false,
                             val productDetail : @RawValue ProductDetails?  = null,
                             val price: String?=null) : Parcelable