package za.co.woolworths.financial.services.android.ui.wfs.contact_us.model

import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse

sealed class ContactUsResult {
    data class Loading(val isLoading: Boolean = false) : ContactUsResult()
    data class Response(val serverErrorResponse: ServerErrorResponse? = null) : ContactUsResult()
    data class Success(val contactUsModel: ContactUsRemoteModel? = null) : ContactUsResult()
}
