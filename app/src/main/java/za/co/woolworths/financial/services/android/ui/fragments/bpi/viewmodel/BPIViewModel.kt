package za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.dto.bpi.InsuranceTypeOptInBody
import za.co.woolworths.financial.services.android.models.dto.otp.ValidateOTPRequest
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.GenericResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.fragments.account.UpdateMyAccount
import za.co.woolworths.financial.services.android.ui.fragments.bpi.helper.NavGraphRouterImpl
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.SessionUtilities


sealed class FailureHandler {
    object NoInternetConnection : FailureHandler()
    data class SessionTimeout(val stsParams : String?) : FailureHandler()
    data class UnknownHttpCode(val response: Response?) : FailureHandler()
    data class UnknownException(val message: String?,val error: Throwable?) : FailureHandler()
}

class BPIViewModel : ViewModel() {

    var mValidateOTPRequest: ValidateOTPRequest?  = null
    var mAccount: Account?  = null

    var bpiPresenter: BPIOverviewPresenter? = null

    private val _failureHandler = MutableLiveData<FailureHandler?>()
    val failureHandler: LiveData<FailureHandler?>
        get() = _failureHandler


    private val _insuranceLeadGenOptIn = MutableLiveData<Account?>()
        val insuranceLeadGenOptIn: LiveData<Account?>
            get() = _insuranceLeadGenOptIn

    companion object {
        const val externalURL = "https://www.woolworths.co.za/store/fragments/corporate/corporate-index.jsp?content=corporate-content&contentId=cmp208540"
    }

    fun overviewPresenter(argument: Bundle?): BPIOverviewPresenter? {
        bpiPresenter = BPIOverviewPresenter(
            BPIOverviewOverviewImpl(argument),
            BPISubmitClaimImpl(),
            NavGraphRouterImpl(),
            BPIDefaultLabelListImpl()
        )
        return bpiPresenter
    }

    fun insuranceLeadGenCarouselList(): Array<InsuranceLeadCarousel> {
        return InsuranceLeadCarousel.values()
    }

    fun fetchInsuranceLeadGenOptIn(activity: Activity?,insurance : String, insuranceTypeOptInBody: InsuranceTypeOptInBody){
        if (!NetworkManager().isConnectedToNetwork(WoolworthsApplication.getAppContext())){
            handleFailure(FailureHandler.NoInternetConnection)
            return
        }

        OneAppService().postInsuranceLeadGenOptIn(insurance, insuranceTypeOptInBody).enqueue(
            CompletionHandler(object : IResponseListener<GenericResponse> {
                override fun onSuccess(response: GenericResponse?) {
                    when(response?.httpCode) {
                        AppConstant.HTTP_OK -> fetchAccount(activity)
                        AppConstant.HTTP_SESSION_TIMEOUT_440 ,
                        AppConstant.HTTP_SESSION_TIMEOUT_400-> handleFailure(FailureHandler.SessionTimeout(response.response?.stsParams))
                        else -> handleFailure(FailureHandler.UnknownHttpCode(response?.response))
                    }
                }

                override fun onFailure(error: Throwable?) {
                    handleFailure(FailureHandler.UnknownException(error?.message, error))
                }
            }, GenericResponse::class.java)
        )
    }

    private fun handleFailure(failureHandler: FailureHandler?) {
        _failureHandler.postValue(failureHandler)
    }

    fun setAccount(extras: Bundle?) {
       mAccount  = extras?.getString(BPIOverviewOverviewImpl.ACCOUNT_INFO, "")?.let { Gson().fromJson(it, Account::class.java) }
    }

    fun setValidateOTPRequest(validateOTPRequest: ValidateOTPRequest) {
        mValidateOTPRequest = validateOTPRequest
    }

    fun getProductGroupCode(): String? {
        return mAccount?.productGroupCode
    }

    private fun fetchAccount(activity: Activity?){
       val reloadMyAccount = UpdateMyAccount(null ,null)
        reloadMyAccount.fetchAccount(true,{ accountsResponse ->
            when (accountsResponse?.httpCode) {
                AppConstant.HTTP_OK -> {
                    val productGroupCode = getProductGroupCode()
                    accountsResponse.accountList?.forEach { account ->
                        if (account.productGroupCode == productGroupCode) {
                            _insuranceLeadGenOptIn.postValue(account)
                            return@forEach
                        }
                    }
                }
                AppConstant.HTTP_SESSION_TIMEOUT_400 -> {
                    SessionUtilities.getInstance().setSessionState(
                        SessionDao.SESSION_STATE.INACTIVE,
                        accountsResponse.response?.stsParams,
                        activity
                    )
                }
                else -> _insuranceLeadGenOptIn.postValue(null)

            }
        }, { error ->
            handleFailure(FailureHandler.UnknownException(error?.message, error))
        })
    }


}