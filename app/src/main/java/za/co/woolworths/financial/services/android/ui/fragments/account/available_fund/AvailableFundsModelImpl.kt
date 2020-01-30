package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund

import za.co.woolworths.financial.services.android.contracts.AvailableFundsContract
import za.co.woolworths.financial.services.android.contracts.ICommonView
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.request

class AvailableFundsModelImpl : AvailableFundsContract.AvailableFundsModel  {

    override fun queryABSAServiceGetUserCreditCardToken(requestListener: ICommonView<Any>) {
        return request(OneAppService.getCreditCardToken(), requestListener)
    }
}