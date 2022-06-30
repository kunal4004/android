package za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.loading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.app_config.ConfigOptions
import za.co.woolworths.financial.services.android.models.network.GenericResponse
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.ContactUsModel
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.ContactUsDataSource
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.EmailUsRequest
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.ApiResult
import java.util.regex.Pattern
import javax.inject.Inject


@HiltViewModel
class EmailUsLoadingViewModel @Inject constructor(
    private val contactUsDataSource: ContactUsDataSource
) : ViewModel() {

    val emailUsResponse: MutableLiveData<ApiResult<GenericResponse>> =
        MutableLiveData<ApiResult<GenericResponse>>()
    val emailUsRequest: MutableLiveData<EmailUsRequest> =
        MutableLiveData<EmailUsRequest>()

    fun start(emailUsRequest:EmailUsRequest) {
        this.emailUsRequest.value = emailUsRequest
        viewModelScope.launch {
            emailUsResponse.value = contactUsDataSource.makeEnquiry(emailUsRequest)
        }
    }
}
