package za.co.woolworths.financial.services.android.ui.wfs.contact_us.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.usecase.ContactUsRepository
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.usecase.IContactUsRepository
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.Children
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.ChildrenItem
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.Content
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.MobileConfigRemoteContentModel
import javax.inject.Inject

@HiltViewModel
class ContactUsViewModel @Inject constructor(private val repository: ContactUsRepository) :
    ViewModel(),
    IContactUsRepository by repository {

    var subCategories: MutableList<ChildrenItem> = mutableListOf()

    var contactUsList: MutableList<Content> = mutableListOf()

    var state by mutableStateOf(mutableListOf<Content>())
        private set

    fun queryServiceContactUs() {
        viewModelScope.launch {
            queryServiceContactUsContentFromMobileConfig().collectLatest { result ->
                Log.e("resultZmal", "${Gson().toJson(result)}")
            }
        }
    }

    fun setMobileConfigRemoteContentModel(content: MobileConfigRemoteContentModel) {
        contactUsList = content.content
    }

    fun setSubCategoryItem(children: MutableList<ChildrenItem>) {
        subCategories = children
    }

}