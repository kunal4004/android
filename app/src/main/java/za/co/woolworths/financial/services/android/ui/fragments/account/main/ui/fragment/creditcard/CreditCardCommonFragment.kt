package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.creditcard

import android.view.View
import androidx.lifecycle.Observer
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.fragments.account.helper.FirebaseEventDetailManager
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.ApiError
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.availablefunds.AvailableFundsFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.Result
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
    fun getABSAServiceGetUserCreditCardToken() {
        viewModel.creditCardService.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Result.Status.SUCCESS -> {
                    it.data?.let { tokenResponse ->
                        viewModel.handleUserCreditCardToken(
                            tokenResponse
                        )
                    }
                    binding.incViewStatementButton.statementProgressBarGroup.visibility = View.GONE
                }
                Result.Status.ERROR -> {
                    // Dimi suggestion , sealed class
                    when (it.apiError) {
                        ApiError.SessionTimeOut -> it.data?.response?.stsParams?.let { stsParams ->
                            handleSessionTimeOut(
                                stsParams
                            )
                        }
                        ApiError.SomethingWrong -> onABSACreditCardFailureHandler()
                        else -> handleUnknownHttpResponse(it.apiError?.value)
                    }
                    binding.incViewStatementButton.statementProgressBarGroup.visibility = View.GONE
                }

                Result.Status.LOADING ->
                    binding.incViewStatementButton.statementProgressBarGroup.visibility =
                        View.VISIBLE
            }
        })
    }


}