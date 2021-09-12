package za.co.woolworths.financial.services.android.checkout.viewmodel

import za.co.woolworths.financial.services.android.checkout.service.network.Address

/**
 * Created by Kunal Uttarwar on 15/06/21.
 */
class SelectedPlacesAddress {
    var savedAddress = Address()
    var store: String = ""
    var storeId: String = ""
}