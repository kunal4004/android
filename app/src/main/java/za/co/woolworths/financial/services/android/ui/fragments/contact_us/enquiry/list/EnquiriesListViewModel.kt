package za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.list

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
class EnquiriesListViewModel @Inject constructor() : ViewModel() {

    private val _contactUsModel: MutableLiveData<ContactUsModel> = MutableLiveData(ContactUsModel())
    val contactUsModel: LiveData<ContactUsModel> get() = _contactUsModel
    var selectedEnquiry: MutableLiveData<ConfigOptions>? = MutableLiveData<ConfigOptions>()
    var validationErrors: MutableLiveData<ValidationErrors>? = MutableLiveData<ValidationErrors>()
    var emailUsRequest: MutableLiveData<EmailUsRequest> = MutableLiveData<EmailUsRequest>()

    companion object {
        const val EMAIL_US_REQUEST = "EMAIL_US_REQUEST"
    }

    fun enableSenButton(name: String?, email: String?, enquiry: String?, message: String?): Boolean {
        return (!name.isNullOrEmpty() && !email.isNullOrEmpty() && !enquiry.isNullOrEmpty() && !message.isNullOrEmpty())
    }

    fun contactUsValidation(
        name: String,
        email: String,
        message: String
    ): Boolean {
        var isValid = true
        if (name.length < 3) {
            validationErrors?.value = ValidationErrors.NameNotValid
            isValid = false
        }
        if (!validateEmail(email)) {
            validationErrors?.value = ValidationErrors.EmailNotValid
            isValid = false
        }
        if (message.length < 2) {
            validationErrors?.value = ValidationErrors.MessageNotValid
            isValid = false
        }
        if (isValid) {
            validationErrors?.value = ValidationErrors.ValidationSuccess
            emailUsRequest.value = EmailUsRequest(
                preferredName = name,
                preferredEmail = email,
                enquiryType = selectedEnquiry?.value?.key,
                emailBody = message
            )
        }
        return isValid
    }

    private fun validateEmail(email: String?): Boolean {
        val pattern = Pattern.compile(".+@.+\\.[a-z]+")
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }
}

sealed class ValidationErrors {
    object NameNotValid : ValidationErrors()
    object EmailNotValid : ValidationErrors()
    object EnquiryNotValid : ValidationErrors()
    object MessageNotValid : ValidationErrors()
    object ValidationSuccess : ValidationErrors()
}