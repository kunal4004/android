package za.co.woolworths.financial.services.android.ui.fragments.order_again

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.geolocation.GeoUtils
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_PRODUCT
import za.co.woolworths.financial.services.android.ui.wfs.common.contentView
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.OrderAgainScreen
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema.OrderAgainScreenEvents
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.OrderAgainViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.KotlinUtils

@AndroidEntryPoint
class OrderAgainFragment : Fragment() {

    private val viewModel: OrderAgainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = contentView(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)) {
        OneAppTheme {
            OrderAgainScreen(viewModel, onBackPressed = {
                hideBottomNavigation(false)
                requireActivity().onBackPressed()
            }) {
                when (it) {
                    OrderAgainScreenEvents.DeliveryLocationClick -> deliverySelectionIntent()
                    OrderAgainScreenEvents.StartShoppingClicked -> onStartShoppingClicked()
                    is OrderAgainScreenEvents.HideBottomBar -> hideBottomNavigation(it.hidden)
                    else -> {}
                }
            }
        }
    }

    private fun onStartShoppingClicked() {
        (requireActivity() as? BottomNavigationActivity)?.apply {
            navigateToTabIndex(INDEX_PRODUCT, null)
        }
    }

    private fun hideBottomNavigation(hide: Boolean) {
        (requireActivity() as? BottomNavigationActivity)?.apply {
            if (hide) {
                hideBottomNavigationMenu()
            } else {
                showBottomNavigationMenu()
            }
        }
    }

    private fun deliverySelectionIntent() {
        activity ?: return
        KotlinUtils.presentEditDeliveryGeoLocationActivity(
            requireActivity(),
            AppConstant.REQUEST_CODE_DELIVERY_LOCATION_CHANGE,
            KotlinUtils.getPreferredDeliveryType(),
            GeoUtils.getPlaceId(),
            isFromDashTab = false,
            isComingFromCheckout = false,
            isComingFromSlotSelection = false,
            isMixedBasket = false,
            isFBHOnly = false,
            savedAddressResponse = null,
            defaultAddress = null,
            whoISCollecting = null,
            liquorCompliance = null
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstant.REQUEST_CODE_DELIVERY_LOCATION_CHANGE) {
            viewModel.setDeliveryLocation()
            viewModel.refreshInventory()
        }
    }
}