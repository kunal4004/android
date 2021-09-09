package za.co.woolworths.financial.services.android.checkout.viewmodel

import za.co.woolworths.financial.services.android.checkout.service.network.Slot

/**
 * Created by Kunal Uttarwar on 20/07/21.
 */
data class DeliveryGridModel(val grid_title: String?, var backgroundImgColor: Int, val slot: Slot)