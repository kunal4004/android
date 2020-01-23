package za.co.woolworths.financial.services.android.ui.fragments.account.detail.card

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.woolworths.financial.services.android.contracts.AccountCardDetailsContract
import za.co.woolworths.financial.services.android.contracts.ICommonView
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.OfferActive
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController

class AccountCardDetailPresenterImpl(private var mainView: AccountCardDetailsContract.AccountCardDetailView?, private var model: AccountCardDetailsContract.AccountCardDetailModel?) : AccountCardDetailsContract.AccountCardDetailPresenter, ICommonView<Any> {

    private var mOfferActive: OfferActive? = null
    private var mApplyNowAccountKeyPair: Pair<ApplyNowState, Account>? = null
    private var mStoreCardResponse: StoreCardsResponse? = null
    private var mIncreaseLimitController: IncreaseLimitController? = null

    init {
        mIncreaseLimitController = getAppCompatActivity()?.let { appCompatActivity -> IncreaseLimitController(appCompatActivity) }
    }

    override fun createCardHolderName(): String? {
        val jwtDecoded = SessionUtilities.getInstance()?.jwt
        val name = jwtDecoded?.name?.get(0)
        val familyName = jwtDecoded?.family_name?.get(0)
        return "$name $familyName"
    }

    override fun displayCardHolderName() {
        mainView?.displayCardHolderName(createCardHolderName())
    }

    override fun balanceProtectionInsuranceIsCovered(account: Account?): Boolean {
        return getAccount()?.insuranceCovered ?: false
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
    override fun getAccountStoreCardCards() {
        val account = getAccount()
        //store card api is disabled for Credit Card group code
        val productGroupCode = account?.productGroupCode?.toLowerCase()
        if (productGroupCode == "cc" || productGroupCode == "pl") return
        val storeCardsRequest: StoreCardsRequestBody? =
                account?.let { acc -> StoreCardsRequestBody(acc.accountNumber, acc.productOfferingId) }
        mainView?.showStoreCardProgress()
        model?.queryServiceGetAccountStoreCardCards(storeCardsRequest, this)
    }

    override fun getUserCLIOfferActive() {
        val account = getAccount()
        if (!cliProductOfferingGoodStanding()) {
            mainView?.hideProductNotInGoodStanding()
            return
        }

        val productOfferingId = account?.productOfferingId
        mainView?.showUserOfferActiveProgress()
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
                    mainView?.hideAccountStoreCardProgress()
                }

                is OfferActive -> {
                    mainView?.onOfferActiveSuccessResult()
                    when (httpCode) {
                        200 -> handleUserOfferActiveSuccessResult(this)
                        440 -> response?.stsParams?.let { stsParams -> mainView?.handleSessionTimeOut(stsParams) }
                        else -> handleUnknownHttpResponse(response?.desc)
                    }
                    mainView?.hideUserOfferActiveProgress()
                }

                else -> throw RuntimeException("onSuccess:: unknown response $apiResponse")
            }
        }
    }


    private fun handleUserOfferActiveSuccessResult(offerActive: OfferActive) {
        val activity = getAppCompatActivity() ?: return
        this.mOfferActive = offerActive
        val messageSummary = if (offerActive.messageSummary.isNullOrEmpty()) "" else offerActive.messageSummary

        if (messageSummary.equals(activity.resources?.getString(R.string.status_consents), ignoreCase = true)) {
            mainView?.disableContentStatusUI()
        } else {
            mainView?.enableContentStatusUI()
        }

        mainView?.handleCreditLimitIncreaseTagStatus(offerActive)
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

    override fun getOfferActive(): OfferActive? = mOfferActive

    override fun getProductOfferingId(): Int? = getAccount()?.productOfferingId

    override fun navigateToDebitOrderActivityOnButtonTapped() {
        getDebitOrder()?.let { debitOrder -> mainView?.navigateToDebitOrderActivity(debitOrder) }
    }

    override fun navigateToBalanceProtectionInsuranceOnButtonTapped() {
        mainView?.navigateToBalanceProtectionInsurance(getAccountInStringFormat())
    }

    override fun cliProductOfferingGoodStanding() = getAccount()?.productOfferingGoodStanding
            ?: false

    override fun getCreditLimitIncreaseController(): IncreaseLimitController? = mIncreaseLimitController


    override fun onDestroy() {
        mainView = null
    }

    override fun onFailure(error: Throwable?) {
        mainView?.hideAccountStoreCardProgress()
    }
}