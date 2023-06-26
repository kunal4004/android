package za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.woolworths.financial.services.android.models.dto.app_config.ConfigOptions
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.EmailUsRequest
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.SessionUtilities
import java.util.regex.Pattern
import javax.inject.Inject


@HiltViewModel
class EnquiriesListViewModel @Inject constructor() : ViewModel() {

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
        if (!validateEmail(email)) {
            validationErrors?.value = ValidationErrors.EmailNotValid
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

    fun userEmailAddress(): String? {
        val userDetail = SessionUtilities.getInstance().jwt
        return if (userDetail.email != null)  userDetail.email[0] else ""
    }

    fun userName(): String? {
        val userDetail = SessionUtilities.getInstance().jwt
        val username = if (userDetail.name != null) userDetail.name[0] + " " +userDetail.family_name[0]else ""
        return KotlinUtils.capitaliseFirstWordAndLetters(username)?.toString()
}

sealed class ValidationErrors {
    object EmailNotValid : ValidationErrors()
    object EnquiryNotValid : ValidationErrors()
    object ValidationSuccess : ValidationErrors()
}
}
