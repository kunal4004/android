package za.co.woolworths.financial.services.android.ui.fragments.click_and_collect

import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.Province
import za.co.woolworths.financial.services.android.models.dto.Suburb

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
    }

    interface EditDeliveryLocationPresenter {
        fun onDestroy()
        fun initGetProvinces()
        fun initGetSuburbs(locationId: String)
    }

    interface EditDeliveryLocationInteractor {
        fun executeGetProvinces(requestListener: IGenericAPILoaderView<Any>)
        fun executeGetSuburbs(locationId: String, requestListener: IGenericAPILoaderView<Any>)
    }
}
