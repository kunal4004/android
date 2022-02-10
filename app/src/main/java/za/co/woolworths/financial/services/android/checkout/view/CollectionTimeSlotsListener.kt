package za.co.woolworths.financial.services.android.checkout.view

import za.co.woolworths.financial.services.android.checkout.service.network.Slot

interface CollectionTimeSlotsListener {
    fun setSelectedTimeSlot(slot: Slot?)
}
