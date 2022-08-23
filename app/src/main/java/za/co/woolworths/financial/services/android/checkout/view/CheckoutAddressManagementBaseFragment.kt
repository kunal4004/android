package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.SAVED_ADDRESS_KEY
import za.co.woolworths.financial.services.android.util.Utils

/**
 * Created by Kunal Uttarwar on 15/09/21.
 */
open class CheckoutAddressManagementBaseFragment : Fragment() {

    companion object {
        var baseFragBundle: Bundle? = Bundle()
        var baseFragSavedAddressResponse: SavedAddressResponse? = null
        const val IS_DELIVERY = "isDelivery"
        const val GEO_SLOT_SELECTION =  "geo_slot_selection"
        const val DASH_SLOT_SELECTION =  "dash_slot_selection"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
        *  To Share the value of savedAddress within diff fragments
        * */
        arguments?.apply {
            if (containsKey(SAVED_ADDRESS_KEY)) {
                baseFragSavedAddressResponse = Utils.jsonStringToObject(
                    getString(SAVED_ADDRESS_KEY),
                    SavedAddressResponse::class.java
                ) as? SavedAddressResponse
                    ?: getSerializable(SAVED_ADDRESS_KEY) as? SavedAddressResponse
            }
        }
    }
}
