package za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.loading

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.network.GenericResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.EmailUsRequest
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.usecase.ContactUsRepository
import javax.inject.Inject


@HiltViewModel
class EmailUsLoadingViewModel @Inject constructor(
    private val contactUsRepository: ContactUsRepository
) : ViewModel() {

    private val _emailUsResponse = MutableSharedFlow<ViewState<GenericResponse>>(0)
    val userEmailResponse: SharedFlow<ViewState<GenericResponse>> = _emailUsResponse

    val emailUsRequest: MutableLiveData<EmailUsRequest> =
        MutableLiveData<EmailUsRequest>()

    fun postContactUsEmail() = viewModelScope.launch {
        contactUsRepository.queryServicePostContactUsEmail(emailUsRequest.value)
            .collectLatest { result ->
                _emailUsResponse.emit(result)
            }
    }

}
