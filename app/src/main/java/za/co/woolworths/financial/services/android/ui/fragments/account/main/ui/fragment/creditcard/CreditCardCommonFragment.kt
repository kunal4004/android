package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.creditcard

import android.view.View.GONE
import android.view.View.VISIBLE
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.fragments.account.helper.FirebaseEventDetailManager
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.availablefunds.AvailableFundsFragment
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.NetworkManager

/* TODO: for future usage, when we want to use StoreCardAvailableFundsFragment for CC Cards*/
class CreditCardCommonFragment: AvailableFundsFragment() {

    fun navigateToABSAStatementActivity() {
        activity?.apply {
            FirebaseEventDetailManager.tapped(
                FirebaseManagerAnalyticsProperties.ABSA_CC_VIEW_STATEMENTS,
                this
            )
            if (NetworkManager().isConnectedToNetwork(this)) {
                getABSAServiceGetUserCreditCardToken()
            } else {
                ErrorHandlerView(this).showToast()
            }
        }
    }

    private fun getABSAServiceGetUserCreditCardToken() {
        viewModel.creditCardTokenLiveData.observe(viewLifecycleOwner) { data ->
            when (data) {
                is ViewState.Loading -> setProgress(data)
                is ViewState.RenderSuccess -> viewModel.handleUserCreditCardToken(data.output)
                is ViewState.RenderFailure -> {
                    when (data.throwable) {

//                        is ApiError.SessionTimeOut -> it.data?.response?.stsParams?.let { stsParams ->
//                            handleSessionTimeOut(
//                                stsParams
//                            )
//                        }
//                       is  ApiError.SomethingWrong -> onABSACreditCardFailureHandler()
//                        else -> handleUnknownHttpResponse(it.apiError?.value)
                    }
                }
                else -> {}
            }
        }
    }

    private fun setProgress(response: ViewState.Loading) {
        binding.incViewStatementButton.statementProgressBarGroup.visibility =
            if (response.isLoading) VISIBLE else GONE
    }
}