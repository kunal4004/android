package za.co.woolworths.financial.services.android.ui.wfs.contact_us.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.*
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.helper.JSONResourceReader
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.usecase.ContactUsRepository
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.usecase.IContactUsRepository
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.ChildrenItem
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.Content
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.RemoteMobileConfigModel
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

@HiltViewModel
class ContactUsViewModel @Inject constructor(private val repository: ContactUsRepository) :
    ViewModel(),
    IContactUsRepository by repository {

    var contentListFromRawFolder: MutableList<Content>? = null
    var wasEnquiryListOpenedFromEmailFragment : Boolean = false

    var enquiryList: MutableList<ChildrenItem> = mutableListOf()

    var subCategories:  Pair<String, MutableList<ChildrenItem>> = Pair("", mutableListOf())

    private val _contentList = MutableSharedFlow<MutableList<Content>>()
    val contentList = _contentList.asSharedFlow()

    private val _isLoadingSharedFlow = MutableSharedFlow<Boolean>()
    val isLoadingSharedFlow = _isLoadingSharedFlow.asSharedFlow()

    private val _isFailureSharedFlow = MutableSharedFlow<Throwable>()
    val isFailureSharedFlow = _isFailureSharedFlow.asSharedFlow()

    fun queryServiceContactUs() {
        viewModelScope.launch {
            queryServiceContactUsContentFromMobileConfig().collectLatest { result ->
               with(result){
                   renderNoConnection {  }
                   renderLoading { viewModelScope.launch { _isLoadingSharedFlow.emit(isLoading)} }
                   renderSuccess { viewModelScope.launch { _contentList.emit(output.content ?: mutableListOf())}}
                   renderFailure { _isFailureSharedFlow.tryEmit(throwable) }
               }
            }
        }
    }

    fun setMobileConfigRemoteContentModel(reader: JSONResourceReader) {
       val config = reader.constructUsingGson(RemoteMobileConfigModel::class.java)
        contentListFromRawFolder = config.content
    }

    fun setSubCategoryItem(children : Pair<String, MutableList<ChildrenItem>>) {
        subCategories = children
    }

    fun setEnquiryTypeList(children: MutableList<ChildrenItem>) {
        enquiryList = children
    }

    fun call(phoneNumber : String?) {
        Utils.makeCall(phoneNumber)
    }
}