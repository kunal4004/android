package za.co.woolworths.financial.services.android.ui.fragments.click_and_collect

import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.request

class EditDeliveryLocationInteractorImpl : EditDeliveryLocationContract.EditDeliveryLocationInteractor {
    override fun executeGetProvinces(requestListener: IGenericAPILoaderView<Any>) {
        request(OneAppService.getProvinces(), requestListener)
    }

    override fun executeGetSuburbs(locationId: String, requestListener: IGenericAPILoaderView<Any>) {
        request(OneAppService.getSuburbs(locationId), requestListener)
    }

    override fun executeSetSuburb(suburbId: String, requestListener: IGenericAPILoaderView<Any>) {
        request(OneAppService.setSuburb(suburbId), requestListener)
    }

    override fun executeValidateSelectedSuburb(suburbId: String, isStore:Boolean, requestListener: IGenericAPILoaderView<Any>) {
        request(OneAppService.validateSelectedSuburb(suburbId, isStore), requestListener)
    }

}