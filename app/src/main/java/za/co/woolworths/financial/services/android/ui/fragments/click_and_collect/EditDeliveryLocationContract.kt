package za.co.woolworths.financial.services.android.ui.fragments.click_and_collect

import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.ValidatedSuburbProducts
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
        fun onSetSuburbFailure(desc: String?)
        fun validateConfirmLocationButtonAvailability()
        fun hideSetSuburbProgressBar()
        fun showSetSuburbProgressBar()
        fun onValidateSelectedSuburbSuccess(validatedSuburbProducts: ValidatedSuburbProducts?)
        fun onValidateSelectedSuburbFailure()
        fun validateSelectedSuburb(suburbId: String, isStore: Boolean)
        fun showAvailableDeliveryDateMessage()
        fun hideAvailableDeliveryDateMessagee()
        fun showStoreClosedMessage()
        fun hideStoreClosedMessage()
        fun navigateToUnsellableItemsFragment()
        fun navigateToSuburbConfirmationFragment()
        fun executeSetSuburb()
        fun navigateToProvinceSelection(regions: List<Province>)
        fun navigateToSuburbSelection(suburbs: List<Suburb>)
    }

    interface EditDeliveryLocationPresenter {
        fun onDestroy()
        fun initGetProvinces()
        fun initGetSuburbs(locationId: String, deliveryType: DeliveryType)
        fun initSetSuburb(suburbId: String)
        fun validateSelectedSetSuburb(suburbId: String, isStore: Boolean)
    }

    interface EditDeliveryLocationInteractor {
        fun executeGetProvinces(requestListener: IGenericAPILoaderView<Any>)
        fun executeGetSuburbs(locationId: String, requestListener: IGenericAPILoaderView<Any>)
        fun executeSetSuburb(suburbId: String, requestListener: IGenericAPILoaderView<Any>)
        fun executeValidateSelectedSuburb(suburbId: String, isStore:Boolean, requestListener: IGenericAPILoaderView<Any>)
    }
}
