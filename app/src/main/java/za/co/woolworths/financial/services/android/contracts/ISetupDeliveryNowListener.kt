package za.co.woolworths.financial.services.android.contracts

import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState

interface ISetupDeliveryNowListener {

    fun onSetUpDeliveryNowButtonClick(applyNowState: ApplyNowState?)
}