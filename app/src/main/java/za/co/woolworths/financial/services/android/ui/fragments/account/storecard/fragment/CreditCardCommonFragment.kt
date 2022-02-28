package za.co.woolworths.financial.services.android.ui.fragments.account.storecard.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.helper.FirebaseEventDetailManager
import za.co.woolworths.financial.services.android.ui.fragments.account.storecard.data.remote.ApiError
import za.co.woolworths.financial.services.android.ui.fragments.account.storecard.utils.Result
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.NetworkManager

/* TODO: for future usage, when we want to use StoreCardAvailableFundsFragment for CC Cards*/
class CreditCardCommonFragment:StoreCardAvailableFundsFragment() {

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