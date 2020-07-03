package za.co.woolworths.financial.services.android.contracts

import za.co.woolworths.financial.services.android.models.dto.Order

interface IPresentOrderDetailInterface  {
    fun presentOrderDetailsPage(item: Order)
}