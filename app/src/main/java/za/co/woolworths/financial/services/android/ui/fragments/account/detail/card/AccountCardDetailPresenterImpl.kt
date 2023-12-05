package za.co.woolworths.financial.services.android.ui.fragments.account.detail.card

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import za.co.woolworths.financial.services.android.analytic.FirebaseCreditLimitIncreaseEvent
import za.co.woolworths.financial.services.android.contracts.IAccountCardDetailsContract
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplication
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.CreditCardDeliveryStatusResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.CreditLimitIncreaseStatus
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils.PRIMARY_CARD_POSITION
import za.co.woolworths.financial.services.android.util.wenum.VocTriggerEvent
import java.util.*

class AccountCardDetailPresenterImpl(private var mainView: IAccountCardDetailsContract.AccountCardDetailView?, private var model: IAccountCardDetailsContract.AccountCardDetailModel?) : IAccountCardDetailsContract.AccountCardDetailPresenter, IGenericAPILoaderView<Any> {

    var mOfferActiveCall: Call<OfferActive>? = null
    var mStoreCardCall: Call<StoreCardsResponse>? = null
    private var mOfferActive: OfferActive? = null
    var mApplyNowAccountKeyPair: Pair<ApplyNowState, Account>? = null
    private var mStoreCardResponse: StoreCardsResponse? = null
    private var mIncreaseLimitController: CreditLimitIncreaseStatus? = null

    init {
        mIncreaseLimitController = getAppCompatActivity()?.let { CreditLimitIncreaseStatus() }
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

    override fun getBpiInsuranceApplication(): BpiInsuranceApplication? {
        return getAccount()?.bpiInsuranceApplication
    }

    override fun showBalanceProtectionInsuranceLead() {
        val balanceProtectionInsurance = getBpiInsuranceApplication()
        if (balanceProtectionInsurance != null)
            mainView?.showBalanceProtectionInsuranceLead(balanceProtectionInsurance)
        else
            mainView?.hideBalanceProtectionInsurance()

    }

    override fun getAppCompatActivity(): AppCompatActivity? = WoolworthsApplication.getInstance()?.currentActivity as? AppCompatActivity

    override fun setAccountDetailBundle(arguments: Bundle?) {
        val account = arguments?.getString(AccountSignedInPresenterImpl.MY_ACCOUNT_RESPONSE)
        mApplyNowAccountKeyPair = Gson().fromJson(account, object : TypeToken<Pair<ApplyNowState, Account>>() {}.type)
    }

    override fun getAccount(): Account? = mApplyNowAccountKeyPair?.second

    override fun getDebitOrder() = getAccount()?.debitOrder

    override fun isDebitOrderActive(): Int? = if (getDebitOrder()?.debitOrderActive == true) VISIBLE else GONE

    override fun convertAccountObjectToJsonString(): String? = Gson().toJson(getAccount())

    @SuppressLint("DefaultLocale")
    override fun getAccountStoreCardCards() {
        val account = getAccount()
        //store card api is disabled for Credit Card group code
        val productGroupCode = account?.productGroupCode?.toLowerCase()
        if (productGroupCode == AccountsProductGroupCode.CREDIT_CARD.groupCode.toLowerCase() || productGroupCode == AccountsProductGroupCode.PERSONAL_LOAN.groupCode.toLowerCase()) return
        val storeCardsRequest: StoreCardsRequestBody? = account?.let { acc -> acc.accountNumber?.let { StoreCardsRequestBody(it, acc.productOfferingId) } }
        mainView?.showStoreCardProgress()
        mStoreCardCall = model?.queryServiceGetAccountStoreCardCards(storeCardsRequest, object : IGenericAPILoaderView<Any> {
            override fun onSuccess(response: Any?) {
                (response as? StoreCardsResponse)?.apply {
                    mainView?.hideStoreCardProgress()
                    if (WoolworthsApplication.getInstance()?.currentActivity !is AccountSignedInActivity) return
                    when (httpCode) {
                        200 -> handleStoreCardSuccessResponse(this)
                        440 -> this.response?.stsParams?.let { stsParams ->
                            mainView?.handleSessionTimeOut(stsParams)
                        }

                        else -> handleUnknownHttpResponse(this.response?.desc)
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                super.onFailure(error)
                mainView?.showOnStoreCardFailure(error)
            }

        })
    }

    override fun getUserCLIOfferActive() {
        val account = getAccount()
        if (!cliProductOfferingGoodStanding()) {
            mainView?.hideProductNotInGoodStanding()
            return
        }

        val productOfferingId = account?.productOfferingId
        mainView?.showUserOfferActiveProgress()
        mOfferActiveCall = productOfferingId?.let { offering_id -> model?.queryServiceGetUserCLIOfferActive(offering_id.toString(), this) }
    }

    override fun getStoreCardResponse(): StoreCardsResponse? {
        val accountInfo = getAccount()
        return mStoreCardResponse?.apply {
            storeCardsData?.productOfferingId = accountInfo?.productOfferingId.toString()
            storeCardsData?.visionAccountNumber = accountInfo?.accountNumber.toString()
        }
    }

    override fun getCreditCardToken() {
        if (!getAccount()?.productGroupCode.equals(AccountsProductGroupCode.CREDIT_CARD.groupCode, true)) return
        model?.queryServiceGetCreditCartToken(this)
    }

    override fun onSuccess(response: Any?) {
        with(response) {
            when (this) {

                is OfferActive -> {
                    mainView?.onOfferActiveSuccessResult()
                    when (httpCode) {
                        200 -> {
                            mainView?.hideUserOfferActiveProgress()
                            handleUserOfferActiveSuccessResult(this)
                        }
                        440, 502 -> this.response?.stsParams?.let { stsParams -> mainView?.handleSessionTimeOut(stsParams) }
                        else -> {
                            mainView?.hideUserOfferActiveProgress()
                            handleUnknownHttpResponse(this.response?.desc)
                        }
                    }
                }

                is CreditCardTokenResponse -> {
                    when (httpCode) {
                        200 -> {
                            mainView?.onGetCreditCArdTokenSuccess(this)
                        }
                        440 -> this.response?.stsParams?.let { stsParams -> mainView?.handleSessionTimeOut(stsParams) }
                        else -> {
                            mainView?.onGetCreditCardTokenFailure()
                        }
                    }
                }

                is CreditCardDeliveryStatusResponse -> {
                    when (httpCode) {
                        200 -> {
                            mainView?.onGetCreditCardDeliveryStatusSuccess(this)
                        }
                        440 -> this.response?.stsParams?.let { stsParams -> mainView?.handleSessionTimeOut(stsParams) }
                        else -> {
                            mainView?.onGetCreditCardDeliveryStatusFailure()
                        }
                    }
                }

                else -> throw RuntimeException("onSuccess:: unknown response $response")
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
        mainView?.handleStoreCardCardsSuccess(storeCardResponse)
    }

    override fun navigateToTemporaryStoreCard() {
        when (isVirtualCardEnabled()) {
            true -> navigateToGetTemporaryStoreCardPopupActivity()
            false -> navigateToMyCardDetailActivity()
        }
    }

    override fun navigateToGetTemporaryStoreCardPopupActivity() {
        getStoreCardResponse()?.let { storeCardsResponse -> mainView?.navigateToGetTemporaryStoreCardPopupActivity(storeCardsResponse) }
    }

    override fun navigateToMyCardDetailActivity(shouldStartWithUnblockStoreCardCall: Boolean) {
        getStoreCardResponse()?.let { storeCardsResponse -> mainView?.navigateToMyCardDetailActivity(storeCardsResponse, shouldStartWithUnblockStoreCardCall) }
    }

    override fun getOfferActive(): OfferActive? = mOfferActive

    override fun getProductOfferingId(): Int? = getAccount()?.productOfferingId

    override fun navigateToDebitOrderActivity() {
        getDebitOrder()?.let { debitOrder -> mainView?.navigateToDebitOrderActivity(debitOrder) }
    }

    override fun navigateToBalanceProtectionInsurance() {
        val bpiInsuranceApplication : BpiInsuranceApplication? = getBpiInsuranceApplication()
        mainView?.navigateToBalanceProtectionInsuranceApplication(convertAccountObjectToJsonString(), bpiInsuranceApplication?.status)
    }

    override fun cliProductOfferingGoodStanding() = getAccount()?.productOfferingGoodStanding
            ?: false

    override fun creditLimitIncrease(): CreditLimitIncreaseStatus? = mIncreaseLimitController

    override fun onDestroy() {
        mainView = null
    }

    override fun onFailure(error: Throwable?) {
        mainView?.hideStoreCardProgress()
    }

    override fun navigateToPaymentOptionActivity() {
        mainView?.navigateToPaymentOptionActivity()
    }

    override fun getCardWithPLCState(cards: ArrayList<Card>?): Card? {
        var cardWithPLCState: Card? = null
        cards?.apply {
            if (this.isNotEmpty())
                cardWithPLCState = this[0]
        }
        return cardWithPLCState
    }

    override fun getCreditCardDeliveryStatus(envelopeNumber: String?) {
        envelopeNumber?.let { model?.queryServiceGetCreditCardDeliveryStatus(getProductOfferingId().toString(), it, this) }
    }

    fun getCreditCardDeliveryStatus(envelopeNumber: String?, productOfferingId: String) {
        envelopeNumber?.let { model?.queryServiceGetCreditCardDeliveryStatus(productOfferingId, it, this) }
    }

    override fun isCreditCardSection(): Boolean {
        return getAccount()?.productGroupCode.equals(AccountsProductGroupCode.CREDIT_CARD.groupCode, ignoreCase = true)
    }

    override fun navigateToPayMyAccountActivity() {
        mainView?.navigateToPayMyAccountActivity()
    }

    override fun getStoreCardBlockType(): Boolean {
        val storeCardsData = getStoreCardResponse()?.storeCardsData
        if (storeCardsData == null || storeCardsData.primaryCards.isNullOrEmpty()) {
            return false
        }
        val primaryCard = storeCardsData?.primaryCards?.get(PRIMARY_CARD_POSITION)
        return primaryCard?.blockType?.toLowerCase(Locale.getDefault()) == TemporaryFreezeStoreCard.TEMPORARY
    }

    override fun isProductCodeStoreCard(): Boolean {
        return getAccount()?.productGroupCode.equals(AccountsProductGroupCode.STORE_CARD.groupCode, ignoreCase = true)
    }

    override fun onStartCreditLimitIncreaseFirebaseEvent(activity: Activity) {
        FirebaseCreditLimitIncreaseEvent(mApplyNowAccountKeyPair?.first, activity).forCLIStart()
    }

    override fun isVirtualCardEnabled(): Boolean {
        val response = getStoreCardResponse()
        return response?.storeCardsData?.generateVirtualCard == true && AppConfigSingleton.virtualTempCard?.isEnabled ?: false
    }

    override fun isVirtualCardObjectNotNull(): Boolean {
        val response = getStoreCardResponse()
        return response?.storeCardsData?.virtualCard != null
    }

    override fun isVirtualCardObjectBlockTypeNull(): Boolean {
        val response = getStoreCardResponse()
        return response?.storeCardsData?.virtualCard != null
                && (TextUtils.isEmpty(response?.storeCardsData?.virtualCard?.blockType)
                    || TemporaryFreezeStoreCard.TEMPORARY.equals(response?.storeCardsData?.virtualCard?.blockType, ignoreCase = true))
    }

    override fun isGeneterateVTC(): Boolean {
        val response = getStoreCardResponse()
        return response?.storeCardsData?.generateVirtualCard ?: false
    }

    override fun getPrimaryStoreCardBlockType(): String {
        val storeCardResponse = getStoreCardResponse()
        val storeCardsData = storeCardResponse?.storeCardsData
        if (storeCardsData == null || storeCardsData.primaryCards.isNullOrEmpty()) {
            return ""
        }
        val primaryCard = storeCardsData.primaryCards?.get(PRIMARY_CARD_POSITION)
        val blockType = primaryCard?.blockType?.toLowerCase(Locale.getDefault())
        return blockType ?: ""
    }

    // Determine if card is blocked: if blockCode is not null, card is blocked.
    override fun isReplacementCardAndVirtualCardViewEnabled(): Boolean {
        val storeCardResponse = getStoreCardResponse()
        val storeCardsData = storeCardResponse?.storeCardsData
        if (storeCardsData == null || storeCardsData.primaryCards.isNullOrEmpty()) {
            return false
        }
        val primaryCard = storeCardsData.primaryCards?.get(PRIMARY_CARD_POSITION)
        val blockType = primaryCard?.blockType?.toLowerCase(Locale.getDefault())
        return !storeCardsData.generateVirtualCard && AppConfigSingleton.instantCardReplacement?.isEnabled == true
                && TemporaryFreezeStoreCard.PERMANENT.equals(blockType, ignoreCase = true)
    }

    override fun isActivateVirtualTempCard(): Boolean {
        val storeCardResponse = getStoreCardResponse()
        val storeCardsData = storeCardResponse?.storeCardsData ?: return false

        //Conditions to Activate VTC
        //generateVirtualCard = true && vtc enabled from config.
        return (storeCardsData.generateVirtualCard
                && AppConfigSingleton.virtualTempCard?.isEnabled == true)
    }

    override fun isTemporaryCardEnabled(): Boolean {
        val response = getStoreCardResponse()
        if (response?.storeCardsData?.virtualCard != null
                && response.storeCardsData?.virtualCard?.number != null
                && (!TemporaryFreezeStoreCard.PERMANENT.equals(response.storeCardsData?.virtualCard?.blockType, ignoreCase = true))) {
            return true
        }
        return false
    }

    override fun isInstantCardReplacementEnabled(): Boolean {
        val response = getStoreCardResponse()
        if (response?.storeCardsData?.primaryCards.isNullOrEmpty()) {
            return false
        }
        val primaryCard = response?.storeCardsData?.primaryCards?.get(PRIMARY_CARD_POSITION)

        if (response?.storeCardsData?.generateVirtualCard == false
                && !TextUtils.isEmpty(primaryCard?.blockType)
                && TemporaryFreezeStoreCard.PERMANENT.equals(primaryCard?.blockType, ignoreCase = true)
                && AppConfigSingleton.instantCardReplacement?.isEnabled == true) {
            return true
        }
        return false
    }

    fun refreshAccount(account: Account?) {
        val balanceProtectionInsurance = account?.bpiInsuranceApplication
        if (balanceProtectionInsurance != null)
            mainView?.showBalanceProtectionInsuranceLead(balanceProtectionInsurance)
        else
            mainView?.hideBalanceProtectionInsurance()
    }
}