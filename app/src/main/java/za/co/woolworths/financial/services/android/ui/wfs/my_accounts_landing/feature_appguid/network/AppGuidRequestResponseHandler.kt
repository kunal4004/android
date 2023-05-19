package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_appguid.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.wfs.core.NetworkStatusUI
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_appguid.model.AppGUIDResponse
import javax.inject.Inject

interface  AppGuidRequestResponseHandler {

    fun isAppGuidNumberNullOrEmpty(response: AppGUIDResponse?)  : Boolean

    fun isAppGuidInProgress(
        result: ViewState.Loading,
        state: MutableStateFlow<NetworkStatusUI<AppGUIDResponse>>
    )

    fun handleAppGuidSuccess(
        output: AppGUIDResponse,
        state: MutableStateFlow<NetworkStatusUI<AppGUIDResponse>>
    )

    fun handleAppGuidFailure(state: MutableStateFlow<NetworkStatusUI<AppGUIDResponse>>)
}
class AppGuidRequestResponseHandlerImpl @Inject constructor() : AppGuidRequestResponseHandler {

    override fun isAppGuidNumberNullOrEmpty(response: AppGUIDResponse?): Boolean {
        return response?.appGuid.isNullOrEmpty()
    }

    override fun isAppGuidInProgress(
        result: ViewState.Loading,
        state: MutableStateFlow<NetworkStatusUI<AppGUIDResponse>>
    ) {
        state.update {
            it.copy(
                data = if (result.isLoading) null else state.value.data,
                isLoading = result.isLoading,
                hasError = false,
                errorMessage = null
            )
        }
        if (result.isLoading) {
            state.value.data = null
        }
    }

    override fun handleAppGuidSuccess(
        output: AppGUIDResponse,
        state: MutableStateFlow<NetworkStatusUI<AppGUIDResponse>>
    ) {
        state.update {
            it.copy(
                data = output,
                isLoading = false,
                hasError = false,
                errorMessage = null
            )
        }
    }

    override fun handleAppGuidFailure(state: MutableStateFlow<NetworkStatusUI<AppGUIDResponse>>) {
        state.update {
            it.copy(
                data = null,
                isLoading = false,
                hasError = true,
                errorMessage = null
            )
        }
    }

}