package za.co.woolworths.financial.services.android.shoptoggle.common

import android.content.Intent
import android.os.Build
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem
import za.co.woolworths.financial.services.android.shoptoggle.presentation.ShopToggleActivity
import za.co.woolworths.financial.services.android.ui.views.UnsellableItemsBottomSheetDialog
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.wenum.Delivery

class UnsellableAccess {

    companion object{

        var updateUnsellableLiveData: Int = 1
        var resetUnsellableLiveData: Int = 0

        public fun launchShopToggleScreen(autoNavigation: Boolean = false,activity:FragmentActivity) {
            Intent(activity, ShopToggleActivity::class.java).apply {
                putExtra(BundleKeysConstants.TOGGLE_FULFILMENT_AUTO_NAVIGATION, autoNavigation)
                activity?.startActivityForResult(this, ShopToggleActivity.REQUEST_DELIVERY_TYPE)
            }
        }
        public fun getToggleFulfilmentResultWithUnSellable(intent: Intent?): ShopToggleActivity.ToggleFulfilmentWIthUnsellable? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent?.extras?.getParcelable(ShopToggleActivity.INTENT_DATA_TOGGLE_FULFILMENT_UNSELLABLE, ShopToggleActivity.ToggleFulfilmentWIthUnsellable::class.java)
            } else {
                intent?.extras?.getParcelable(ShopToggleActivity.INTENT_DATA_TOGGLE_FULFILMENT_UNSELLABLE)
            }
        }

        public fun navigateToUnsellableItemsFragment(
            unSellableCommerceItems: ArrayList<UnSellableCommerceItem>,
            deliveryType: Delivery,confirmAddressViewModel: ConfirmAddressViewModel,
            progressBar: ProgressBar,fragment: Fragment,fragmentManager: FragmentManager
        )
        {
            val unsellableItemsBottomSheetDialog =
                confirmAddressViewModel?.let { it1 ->
                    UnsellableItemsBottomSheetDialog.newInstance(
                        unSellableCommerceItems, deliveryType, progressBar = progressBar,
                        it1, fragment
                    )
                }
            unsellableItemsBottomSheetDialog?.show(
                fragmentManager,
                UnsellableItemsBottomSheetDialog::class.java.simpleName
            )
        }


    }


}