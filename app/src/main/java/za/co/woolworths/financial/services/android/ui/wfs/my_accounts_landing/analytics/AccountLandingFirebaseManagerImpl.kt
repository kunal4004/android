package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.firebase.installations.FirebaseInstallations
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.ACCOUNTSEVENTSAPPEARED
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.Acc_My_Orders
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.MYACCOUNTSCREDITCARDAPPLYNOW
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.MYACCOUNTSPERSONALLOANAPPLYNOW
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.MYACCOUNTSREGISTER
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.MYACCOUNTSSIGNIN
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.MYACCOUNTSSTORECARDAPPLYNOW
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.MY_ACCOUNT_INBOX
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.Myaccounts_creditview
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.PET_INSURANCE_AWARENESS_MODEL_LEARN_MORE
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.PET_INSURANCE_GET_INSURANCE_PRODUCT
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.SHOPMYLISTS
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.Companion.SIGN_UP
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.saveToLocalDatabase
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.logException
import javax.inject.Inject


interface AccountLandingFirebaseManager {
    fun onCreatePutC2Id()
    fun setScreenNameMyAccount()
    suspend fun onViewCreatedSaveFirebaseDeviceId()
    fun onSignInButton()
    fun onRegisterButton()
    fun onRegisterSignUpButton()
    fun onMessagingItem()
    fun onApplyNowStoreCardItem()
    fun onApplyNowCreditCardItem()
    fun onApplyNowPersonalLoanItem()
    fun onMyOrderItem()
    fun onShoppingListItem()
    fun onCreditReportItem()
    fun petInsuranceLearnMore()
    fun petInsuranceGetInsuranceProduct()
    fun sendEvent(event: String)
    fun sendEvent(event: String, arguments: Map<String, String>)
}

class AccountLandingFirebaseManagerImpl @Inject constructor(
    private val activity: Activity?
) :
    AccountLandingFirebaseManager {
    override fun onCreatePutC2Id() {
        val jwtDecodedModel = SessionUtilities.getInstance().jwt
        val arguments: MutableMap<String, String> = HashMap()
        val c2Id = jwtDecodedModel.C2Id ?: ""
        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.C2ID] = c2Id
        sendEvent(ACCOUNTSEVENTSAPPEARED, arguments)
    }

    override fun setScreenNameMyAccount() {
        Utils.setScreenName(FirebaseManagerAnalyticsProperties.ScreenNames.MY_ACCOUNTS)
    }

    override suspend fun onViewCreatedSaveFirebaseDeviceId() {
        try {
            val firebaseInstance = FirebaseInstallations.getInstance()
            firebaseInstance.id.addOnCompleteListener { task: Task<String?> ->
                if (task.isSuccessful) {
                    saveToLocalDatabase(key= SessionDao.KEY.DEVICE_ID,value = task.result)
                }
            }
        } catch (ex: Exception) {
            logException(ex)
        }
    }

    override fun onSignInButton() {
        sendEvent(MYACCOUNTSSIGNIN)
    }

    override fun onRegisterButton() {
        sendEvent(MYACCOUNTSREGISTER)
    }

    override fun onRegisterSignUpButton() {
        sendEvent(SIGN_UP)
    }

    override fun onMessagingItem() {
        sendEvent(MY_ACCOUNT_INBOX)
    }

    override fun onApplyNowStoreCardItem() {
        sendEvent(MYACCOUNTSSTORECARDAPPLYNOW)
    }

    override fun onApplyNowCreditCardItem() {
        sendEvent(MYACCOUNTSCREDITCARDAPPLYNOW)
    }

    override fun onApplyNowPersonalLoanItem() {
        sendEvent(MYACCOUNTSPERSONALLOANAPPLYNOW)
    }

    override fun onMyOrderItem() {
        sendEvent(Acc_My_Orders)
    }

    override fun onShoppingListItem() {
        sendEvent(SHOPMYLISTS)
    }

    override fun onCreditReportItem() {
        sendEvent(Myaccounts_creditview)
    }

    override fun petInsuranceLearnMore() {
        sendEvent(PET_INSURANCE_AWARENESS_MODEL_LEARN_MORE)
    }

    override fun petInsuranceGetInsuranceProduct() {
        sendEvent(PET_INSURANCE_GET_INSURANCE_PRODUCT)
    }

    override fun sendEvent(event: String) {
        activity ?: return
        Utils.triggerFireBaseEvents(event, activity)
    }

    override fun sendEvent(event: String, arguments: Map<String, String>) {
        activity ?: return
        Utils.triggerFireBaseEvents(event, arguments, activity)
    }
}