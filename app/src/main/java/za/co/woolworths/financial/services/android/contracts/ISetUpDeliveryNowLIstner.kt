package za.co.woolworths.financial.services.android.contracts

import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState

interface ISetUpDeliveryNowLIstner {

    fun onSetUpDeliveryNowButtonClick(applyNowState: ApplyNowState?)
}