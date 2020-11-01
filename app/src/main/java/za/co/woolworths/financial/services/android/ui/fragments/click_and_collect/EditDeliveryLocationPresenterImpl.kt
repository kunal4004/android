package za.co.woolworths.financial.services.android.ui.fragments.click_and_collect

import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.ValidateSelectedSuburbResponse
import za.co.woolworths.financial.services.android.models.dto.ProvincesResponse
import za.co.woolworths.financial.services.android.models.dto.SetDeliveryLocationSuburbResponse
import za.co.woolworths.financial.services.android.models.dto.Suburb
import za.co.woolworths.financial.services.android.models.dto.SuburbsResponse
import za.co.woolworths.financial.services.android.util.DeliveryType

class EditDeliveryLocationPresenterImpl(var mainView: EditDeliveryLocationContract.EditDeliveryLocationView?, var getInteractor: EditDeliveryLocationContract.EditDeliveryLocationInteractor?) : EditDeliveryLocationContract.EditDeliveryLocationPresenter, IGenericAPILoaderView<Any> {
    var deliveryType: DeliveryType? = null

    override fun onDestroy() {
        mainView = null
    }

    override fun initGetProvinces() {
        getInteractor?.executeGetProvinces(this)
    }

    override fun initGetSuburbs(locationId: String, deliveryType: DeliveryType) {
        this.deliveryType = deliveryType
        getInteractor?.executeGetSuburbs(locationId, this)
    }

    override fun initSetSuburb(suburbId: String) {
        getInteractor?.executeSetSuburb(suburbId, this)
    }

    override fun validateSelectedSetSuburb(suburbId: String, isStore: Boolean) {
        getInteractor?.executeValidateSelectedSuburb(suburbId, isStore, this)
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
                        200 -> mainView?.onGetSuburbsSuccess(if (deliveryType == DeliveryType.DELIVERY) suburbs else stores)
                        else -> mainView?.onGetSuburbsFailure()
                    }
                }
                is SetDeliveryLocationSuburbResponse -> {
                    when (httpCode) {
                        200 -> mainView?.onSetSuburbSuccess()
                        else -> mainView?.onSetSuburbFailure()
                    }
                }
                is ValidateSelectedSuburbResponse -> {
                    when (httpCode) {
                        200 -> mainView?.onValidateSelectedSuburbSuccess(validatedSuburbProducts)
                        else -> mainView?.onValidateSelectedSuburbFailure()
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