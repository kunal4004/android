package za.co.woolworths.financial.services.android.ui.wfs.contact_us.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.extractPhoneNumberFromSentence
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.usecase.ContactUsRepository
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.usecase.IContactUsRepository
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.ChildrenItem
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.ContactUsType
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.Content
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.ContactUsRemoteModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.HyperlinkColor
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

@HiltViewModel
class ContactUsViewModel @Inject constructor(private val repository: ContactUsRepository) : ViewModel(),
    IContactUsRepository by repository {

    var enquiryList: MutableList<ChildrenItem> = mutableListOf()

    var subCategories:  Pair<String?, MutableList<ChildrenItem>> = Pair("", mutableListOf())

    var isLoadingSharedFlow by mutableStateOf(true)

    var serverErrorResponse: ServerErrorResponse? by mutableStateOf(null)
    var contactUsSuccessModel: ContactUsRemoteModel? by mutableStateOf(null)
    var errorThrowable: Throwable? by mutableStateOf(null)

    fun queryServiceContactUs() {
        viewModelScope.launch {
            queryServiceContactUsContentFromMobileConfig().collectLatest { result ->
               with(result){
                   renderLoading { isLoadingSharedFlow = isLoading }
                   renderSuccess { contactUsSuccessModel = output }
                   renderFailure { errorThrowable = throwable }
                   renderHttpFailureFromServer { serverErrorResponse = output.response }
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

    fun resetState() {
        serverErrorResponse = null
    }

    fun labelDescriptionWithPhoneNumber(description: String?): Pair<AnnotatedString, String?> {
        description ?: return buildAnnotatedString {  } to null
        val textWithPhoneNumber = extractPhoneNumberFromSentence(description)
        val startIndex = textWithPhoneNumber?.first?.first ?: 0
        val endIndex = textWithPhoneNumber?.first?.second ?: 0
        val phoneNumber = textWithPhoneNumber?.second

        return buildAnnotatedString {
            append(description)
            phoneNumber?.let { phone ->
                addStringAnnotation(
                    tag = phone,
                    annotation = phone,
                    start = startIndex,
                    end = endIndex
                )
            }
            addStyle(
                style = SpanStyle(color = HyperlinkColor,
                    fontFamily = OpenSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textDecoration = TextDecoration.Underline),
                start = startIndex,
                end = endIndex
            )

            toAnnotatedString()
        } to phoneNumber
    }
}