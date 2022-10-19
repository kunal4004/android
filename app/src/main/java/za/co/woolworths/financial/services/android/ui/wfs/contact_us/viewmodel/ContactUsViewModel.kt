package za.co.woolworths.financial.services.android.ui.wfs.contact_us.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.*
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.usecase.ContactUsRepository
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.usecase.IContactUsRepository
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.ChildrenItem
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.ContactUsType
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.Content
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.RemoteMobileConfigModel
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

sealed class ContactUsResult {
    data class Loading(val isLoading: Boolean = false) : ContactUsResult()
    data class Response(val serverErrorResponse: ServerErrorResponse? = null) : ContactUsResult()
    data class Success(val contactUsModel: RemoteMobileConfigModel? = null) : ContactUsResult()
}

@HiltViewModel
class ContactUsViewModel @Inject constructor(private val repository: ContactUsRepository) :
    ViewModel(),
    IContactUsRepository by repository {

    var enquiryList: MutableList<ChildrenItem> = mutableListOf()

    var subCategories:  Pair<String?, MutableList<ChildrenItem>> = Pair("", mutableListOf())

    var isLoadingSharedFlow by mutableStateOf(true)

    var remoteFailureResponse: ServerErrorResponse? by mutableStateOf(null)
    var remoteMobileConfig: RemoteMobileConfigModel? by mutableStateOf(null)
    var isFailureSharedFlow: Throwable? by mutableStateOf(Throwable())

    fun queryServiceContactUs() {
        viewModelScope.launch {
            queryServiceContactUsContentFromMobileConfig().collectLatest { result ->
               with(result){
                   renderHttpFailureFromServer {  }
                   renderLoading { isLoadingSharedFlow = isLoading }
                   renderSuccess { viewModelScope.launch { remoteMobileConfig = output }}
                   renderFailure { isFailureSharedFlow = throwable }
                   renderHttpFailureFromServer { remoteFailureResponse = output.response }
               }
            }
        }
    }

    fun setSubCategoryItem(children : Pair<String?, MutableList<ChildrenItem>>) {
        subCategories = children
    }

    fun setEnquiryTypeList(children: MutableList<ChildrenItem>) {
        enquiryList = children
    }

    fun call(phoneNumber : String?) {
        Utils.makeCall(phoneNumber)
    }

    fun getShimmerModel(): MutableList<Content> = ContactUsType.NONE.getShimmerModel()
}