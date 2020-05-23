package za.co.woolworths.financial.services.android.ui.fragments.click_and_collect

import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.ProvincesResponse
import za.co.woolworths.financial.services.android.models.dto.SuburbsResponse

class EditDeliveryLocationPresenterImpl(var mainView: EditDeliveryLocationContract.EditDeliveryLocationView?, var getInteractor: EditDeliveryLocationContract.EditDeliveryLocationInteractor?) : EditDeliveryLocationContract.EditDeliveryLocationPresenter, IGenericAPILoaderView<Any> {
    override fun onDestroy() {
        mainView = null
    }

    override fun initGetProvinces() {
        getInteractor?.executeGetProvinces(this)
    }

    override fun initGetSuburbs(locationId: String) {
        getInteractor?.executeGetSuburbs(locationId, this)
    }

    override fun onSuccess(response: Any?) {
        with(response) {
            when (this) {
                is ProvincesResponse -> {
                    when (httpCode) {
                        200 -> mainView?.onGetProvincesSuccess(regions)
                        else -> mainView?.onGetProvincesFailure()
                    }
                }
                is SuburbsResponse -> {
                    when (httpCode) {
                        200 -> mainView?.onGetSuburbsSuccess(suburbs)
                        else -> mainView?.onGetSuburbsFailure()
                    }
                }
                else -> throw RuntimeException("onSuccess:: unknown response $response")
            }
        }
    }

    override fun onFailure(error: Throwable?) {
        mainView?.onGenericFailure()
    }

}