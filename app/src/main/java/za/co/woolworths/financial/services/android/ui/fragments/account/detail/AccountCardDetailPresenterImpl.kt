package za.co.woolworths.financial.services.android.ui.fragments.account.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.woolworths.financial.services.android.contracts.AccountPaymentOptionsContract
import za.co.woolworths.financial.services.android.contracts.ICommonView
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.util.SessionUtilities

class AccountCardDetailPresenterImpl(private var mainView: AccountPaymentOptionsContract.AccountCardDetailView?, private var model: AccountPaymentOptionsContract.AccountCardDetailModel?) : AccountPaymentOptionsContract.AccountCardDetailPresenter, ICommonView<Any> {

    private var mApplyNowAccountKeyPair: Pair<ApplyNowState, Account>? = null
    private var mStoreCardResponse: StoreCardsResponse? = null

    override fun createCardHolderName(): String? {
        val jwtDecoded = SessionUtilities.getInstance()?.jwt
        val name = jwtDecoded?.name?.get(0)
        val familyName = jwtDecoded?.family_name?.get(0)
        return "$name $familyName"
    }

    override fun displayCardHolderName() {
        mainView?.displayCardHolderName(createCardHolderName())
    }

    override fun balanceProtectionInsuranceIsCovered(account: Account?): String? {
        val resources = getAppCompatActivity()?.resources
        return if (account?.insuranceCovered == true) resources?.getString(R.string.bpi_covered) else resources?.getString(R.string.bpi_not_covered)
    }

    override fun setBalanceProtectionInsuranceState() {
        mainView?.setBalanceProtectionInsuranceState(balanceProtectionInsuranceIsCovered(getAccount()))
    }

    override fun getAppCompatActivity(): AppCompatActivity? = WoolworthsApplication.getInstance()?.currentActivity as? AppCompatActivity

    override fun setAccountDetailBundle(arguments: Bundle?) {
        val account = arguments?.getString(AccountSignedInPresenterImpl.MY_ACCOUNT_RESPONSE)
        mApplyNowAccountKeyPair =
                Gson().fromJson(account, object : TypeToken<Pair<ApplyNowState, Account>>() {}.type)
    }

    override fun getAccount(): Account? = mApplyNowAccountKeyPair?.second

    override fun getDebitOrder() = getAccount()?.debitOrder

    override fun isDebitOrderActive(): Int? = if (getDebitOrder()?.debitOrderActive == true) VISIBLE else GONE

    override fun getAccountInStringFormat(): String? = Gson().toJson(getAccount())

    @SuppressLint("DefaultLocale")
    override fun requestGetAccountStoreCardCardsFromServer() {
        val account = getAccount()
        //store card api is disabled for Credit Card group code
        if (account?.productGroupCode?.toLowerCase() == "cc") return
        val storeCardsRequest: StoreCardsRequestBody? =
                account?.let { acc -> StoreCardsRequestBody(acc.accountNumber, acc.productOfferingId) }
        showProgress()
        model?.queryServiceGetAccountStoreCardCards(storeCardsRequest, this)
    }

    override fun requestGetUserCLIOfferActiveFromServer() {
        val account = getAccount()
        val productOfferingId = account?.productOfferingId
        productOfferingId?.let { offering_id -> model?.queryServiceGetUserCLIOfferActive(offering_id.toString(), this) }
    }

    override fun getStoreCardResponse(): StoreCardsResponse? {
        val accountInfo = getAccount()
        return mStoreCardResponse?.apply {
            storeCardsData?.productOfferingId = accountInfo?.productOfferingId.toString()
            storeCardsData?.visionAccountNumber = accountInfo?.accountNumber.toString()
        }
    }

    override fun onSuccess(apiResponse: Any?) {
        with(apiResponse) {
            when (this) {
                is StoreCardsResponse -> {
                    when (httpCode) {
                        200 -> handleStoreCardSuccessResponse(this)
                        440 -> response?.stsParams?.let { stsParams -> mainView?.handleSessionTimeOut(stsParams) }
                        else -> handleUnknownHttpResponse(response?.desc)
                    }
                    hideProgress()
                }
            }
        }
    }

    override fun handleUnknownHttpResponse(description: String?) {
        val resources = getAppCompatActivity()?.resources
        val message: String? =
                if (description.isNullOrEmpty()) resources?.getString(R.string.general_error_desc) else description
        mainView?.handleUnknownHttpCode(message)
    }

    override fun handleStoreCardSuccessResponse(storeCardResponse: StoreCardsResponse) {
        this.mStoreCardResponse = storeCardResponse
        mainView?.displayViewCardText()
        navigateToTemporaryStoreCardOnButtonTapped()
    }

    override fun navigateToTemporaryStoreCardOnButtonTapped() {
        when (getStoreCardResponse()?.storeCardsData?.generateVirtualCard == true && WoolworthsApplication.getVirtualTempCard().isEnabled) {
            true -> navigateToGetTemporaryStoreCardPopupActivity()
            false -> navigateToMyCardDetailActivity()
        }
    }

    override fun navigateToGetTemporaryStoreCardPopupActivity() {
        getStoreCardResponse()?.let { storeCardsResponse -> mainView?.navigateToGetTemporaryStoreCardPopupActivity(storeCardsResponse) }
    }

    override fun navigateToMyCardDetailActivity() {
        getStoreCardResponse()?.let { storeCardsResponse -> mainView?.navigateToMyCardDetailActivity(storeCardsResponse) }
    }

    override fun navigateToDebitOrderActivityOnButtonTapped() {
        getDebitOrder()?.let { debitOrder -> mainView?.navigateToDebitOrderActivity(debitOrder) }
    }

    override fun navigateToBalanceProtectionInsuranceOnButtonTapped() {
        mainView?.navigateToBalanceProtectionInsurance(getAccountInStringFormat())
    }

    override fun onDestroy() {
        mainView = null
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
    }

    override fun showProgress() {
        mainView?.showStoreCardProgress()
    }

    override fun hideProgress() {
        mainView?.onStoreCardProgressCompleted()
    }
}