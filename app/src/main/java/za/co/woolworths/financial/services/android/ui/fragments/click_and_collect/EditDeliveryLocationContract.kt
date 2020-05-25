package za.co.woolworths.financial.services.android.ui.fragments.click_and_collect

import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.Province
import za.co.woolworths.financial.services.android.models.dto.Suburb
import za.co.woolworths.financial.services.android.util.DeliveryType

interface EditDeliveryLocationContract {

    interface EditDeliveryLocationView {
        fun onGetProvincesSuccess(regions: List<Province>)
        fun onGetProvincesFailure()
        fun onGetSuburbsSuccess(suburbs: List<Suburb>)
        fun onGetSuburbsFailure()
        fun onGenericFailure()
        fun getProvinces()
        fun getSuburbs()
        fun showGetProvincesProgress()
        fun showGetSuburbProgress()
        fun hideGetProvincesProgress()
        fun hideGetSuburbProgress()
        fun showErrorDialog()
        fun onSetSuburbSuccess()
        fun onSetSuburbFailure()
        fun validateConfirmLocationButtonAvailability()
        fun hideSetSuburbProgressBar()
        fun showSetSuburbProgressBar()
    }

    interface EditDeliveryLocationPresenter {
        fun onDestroy()
        fun initGetProvinces()
        fun initGetSuburbs(locationId: String, deliveryType: DeliveryType)
        fun initSetSuburb(suburbId: String)
        fun getDeliverableSuburbs(suburbs: List<Suburb>): List<Suburb>
    }

    interface EditDeliveryLocationInteractor {
        fun executeGetProvinces(requestListener: IGenericAPILoaderView<Any>)
        fun executeGetSuburbs(locationId: String, requestListener: IGenericAPILoaderView<Any>)
        fun executeSetSuburb(suburbId: String, requestListener: IGenericAPILoaderView<Any>)
    }
}
