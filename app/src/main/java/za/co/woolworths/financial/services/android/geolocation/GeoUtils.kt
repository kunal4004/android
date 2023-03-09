package za.co.woolworths.financial.services.android.geolocation

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.geolocation.network.model.Store
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

class GeoUtils {

    companion object {

        fun getPlaceId() = Utils
            .getPreferredDeliveryLocation()?.fulfillmentDetails?.let {
                it.address?.placeId
            }

        fun getDelivertyType() = KotlinUtils.getPreferredDeliveryType()

        fun getSelectedPlaceId(savedAddresses: SavedAddressResponse): String {
            savedAddresses.addresses?.forEach { address ->
                if (savedAddresses.defaultAddressNickname.equals(address.nickname)) {
                    return address.placesId.toString()
                }
            }
            return ""
        }

        fun getSelectedDefaultName(
            savedAddresses: SavedAddressResponse?,
            selectedAddressPosition: Int,
        ): Boolean {
            if (savedAddresses?.addresses?.getOrNull(selectedAddressPosition)?.nickname.equals(
                    savedAddresses?.defaultAddressNickname,
                    true)
            ) {
                return true
            }
            return false
        }

        fun getStoreDetails(storeId: String?, stores: List<Store>?): Store? {
            stores?.forEach {
                if (it.storeId == storeId) {
                    return it
                }
            }
            return null
        }

        fun navigateSafe(view : View, @IdRes actionId: Int, args: Bundle?) {
            view.findNavController().navigateSafe(actionId, args)
        }

        private fun NavController.navigateSafe(@IdRes actionId: Int, args: Bundle?) {
            currentDestination?.let { currentDestination ->
                val navAction = currentDestination.getAction(actionId)
                // to navigate successfully certain action should be explicitly stated in nav graph
                if (navAction != null) {
                    val destinationId = navAction.destinationId
                    if (destinationId != 0) {
                        val currentNode = currentDestination as? NavGraph ?: currentDestination.parent
                        if (currentNode?.id == destinationId ||
                                currentNode?.findNode(destinationId) != null
                        ) {
                            navigate(actionId, args, null)
                        }
                    }
                }
            }
        }

    }
}
