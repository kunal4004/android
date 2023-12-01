package za.co.woolworths.financial.services.android.cart.viewmodel

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.cart.service.network.CartItemGroup
import za.co.woolworths.financial.services.android.cart.service.network.CartResponse
import za.co.woolworths.financial.services.android.cart.view.CartFragment
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.models.dto.LiquorCompliance
import za.co.woolworths.financial.services.android.models.dto.ProductDetails
import za.co.woolworths.financial.services.android.models.dto.item_limits.ProductCountMap
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.Voucher
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.cart.GiftWithPurchaseDialogDetailFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.ScreenManager

class CartUtils {
    companion object {

        //Threshold level is 24 to hide the blue banner as per the requirement and it 0 to show it
        public const val THRESHOLD_FOR_DASH_CART_LIMIT_BANNER = 24

        fun filterCommerceItemFromCartResponse(cartResponse: CartResponse, commerceId: String,
        ): CommerceItem? {
            var commerceItem: CommerceItem? = null
            cartResponse.cartItems?.forEach { group ->
                commerceItem = group.commerceItems?.find {
                    it.commerceItemInfo.commerceId.equals(
                        commerceId,
                        true
                    )
                }
                if (commerceItem != null)
                    return commerceItem
            }
            return commerceItem
        }

        fun getAppliedVouchersCount(vouchers: ArrayList<Voucher>): Int {
            return vouchers.filter { it.voucherApplied }.size
        }

        fun updateItemLimitsBanner(
            productCountMap: ProductCountMap?,
            banner: ConstraintLayout?,
            message: TextView?,
            counter: TextView?,
            showBanner: Boolean,
        ) {
            banner?.visibility = View.GONE
            productCountMap?.let {
                if (it.quantityLimit?.foodLayoutColour != null && showBanner && it.totalProductCount ?: 0 > 0) {
                    message?.text = it.quantityLimit.foodLayoutMessage ?: ""
                    if (it.quantityLimit.other != null && it.totalProductCount != null && it.totalProductCount > it.quantityLimit.other) {
                        counter?.text =
                            ((it.totalProductCount - it.quantityLimit.other).toString() + "/" + it.quantityLimit.foodMaximumQuantity)
                                ?: ""
                    }
                    if (it.quantityLimit.foodLayoutColour.isNotEmpty() && !it.quantityLimit.foodLayoutMessage.isNullOrEmpty()) {
                        banner?.visibility = View.VISIBLE
                        banner?.setBackgroundColor(Color.parseColor(it.quantityLimit.foodLayoutColour))
                    }
                }
            }
        }

        fun openProductDetailFragment(commerceItem: CommerceItem, activity: Activity?) {
            if (activity == null || activity !is BottomNavigationActivity) {
                return
            }
            val productDetails = ProductDetails()
            val commerceItemInfo = commerceItem.commerceItemInfo
            productDetails.externalImageRefV2 = commerceItemInfo.externalImageRefV2
            productDetails.productName = commerceItemInfo.productDisplayName
            productDetails.fromPrice = commerceItem.priceInfo.getAmount().toFloat()
            productDetails.productId = commerceItemInfo.productId
            productDetails.sku = commerceItemInfo.catalogRefId
            val strProductList = Gson().toJson(productDetails)

            BottomNavigationActivity.preventShopTooltip = true
            // Move to shop tab first.
            (activity as? BottomNavigationActivity)?.bottomNavigationById?.currentItem =
                BottomNavigationActivity.INDEX_PRODUCT
            ScreenManager.openProductDetailFragment(activity, "", strProductList)
        }

        fun onGiftItemClicked(activity: Activity) {
            val giftWithPurchaseDialogDetailFragment = GiftWithPurchaseDialogDetailFragment()
            giftWithPurchaseDialogDetailFragment.show(
                (activity as AppCompatActivity).supportFragmentManager,
                GiftWithPurchaseDialogDetailFragment::class.java.simpleName
            )
        }

        fun onLocationSelectionClicked(activity: Activity, liquorCompliance: LiquorCompliance?) {
            KotlinUtils.presentEditDeliveryGeoLocationActivity(
                activity,
                CartFragment.REQUEST_SUBURB_CHANGE,
                KotlinUtils.getPreferredDeliveryType(),
                KotlinUtils.getPreferredPlaceId(),
                isComingFromCheckout = false,
                isComingFromSlotSelection = false,
                savedAddressResponse = null,
                defaultAddress = null,
                whoISCollecting = "",
                liquorCompliance = liquorCompliance
            )
        }

        fun getUpdatedCommerceItem(
            cartItems: ArrayList<CartItemGroup>,
            commerceId: String,
        ): CommerceItem? {
            for (cartItemGroup: CartItemGroup in cartItems) {
                for (commerceItem: CommerceItem in cartItemGroup.commerceItems) {
                    if (commerceItem.commerceItemInfo.commerceId.equals(
                            commerceId,
                            ignoreCase = true
                        )
                    ) return commerceItem
                }
            }
            return null
        }
    }
}