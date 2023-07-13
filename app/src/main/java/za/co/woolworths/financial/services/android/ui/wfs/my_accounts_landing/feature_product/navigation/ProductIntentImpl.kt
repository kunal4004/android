package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountSection
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.Constants
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature.fragment.UserAccountsLandingFragment.Companion.ACCOUNT_CARD_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductCardsGroup
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_view_application_status.ViewApplicationStatusImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

interface ProductIntent {

    fun createStoreCardIntent(deepLinkParams: String? = null, productGroup: AccountProductCardsGroup.StoreCard?)
    fun createPersonalLoanIntent(deepLinkParams: String?= null,userAccountResponse: String?)
    fun createBlackCreditCardIntent(deepLinkParams: String?= null,userAccountResponse: String?)
    fun createSilverCreditCardIntent(deepLinkParams: String?= null,userAccountResponse: String?)
    fun createGoldCreditCardIntent(deepLinkParams: String?= null,userAccountResponse: String?)
    fun createViewApplicationStatusIntent(viewApplicationStatus: ViewApplicationStatusImpl)
    fun createLinkYourWooliesCardIntent(
        activityLauncher: BetterActivityResult<Intent, ActivityResult>?,
        viewModel: UserAccountLandingViewModel
    )
}

class ProductIntentImpl @Inject constructor(private val activity: Activity?) : ProductIntent {
    override fun createStoreCardIntent(
        deepLinkParams: String?,
        productGroup: AccountProductCardsGroup.StoreCard?
    ) {
        val account = Utils.objectToJson(productGroup?.productDetails)
        navigateToStoreCardActivity(deepLinkParams = deepLinkParams, account = account)
    }

    override fun createPersonalLoanIntent(deepLinkParams: String?,userAccountResponse: String?) {
        redirectToAccountSignInActivity(
            deepLinkParams = deepLinkParams,
                applyNowState = ApplyNowState.PERSONAL_LOAN,
                userAccountResponse = userAccountResponse
        )
    }

    override fun createBlackCreditCardIntent(deepLinkParams: String?,userAccountResponse: String?) {
        redirectToAccountSignInActivity(
                deepLinkParams = deepLinkParams,
                applyNowState = ApplyNowState.BLACK_CREDIT_CARD,
                userAccountResponse = userAccountResponse
        )
    }

    override fun createSilverCreditCardIntent(deepLinkParams: String?,userAccountResponse: String?) {
        redirectToAccountSignInActivity(
            deepLinkParams = deepLinkParams,
            applyNowState = ApplyNowState.SILVER_CREDIT_CARD,
            userAccountResponse = userAccountResponse
        )
    }

    override fun createGoldCreditCardIntent(deepLinkParams: String?, userAccountResponse: String?) {
        redirectToAccountSignInActivity(
            deepLinkParams = deepLinkParams,
            applyNowState = ApplyNowState.GOLD_CREDIT_CARD,
            userAccountResponse = userAccountResponse
        )
    }

    override fun createViewApplicationStatusIntent(viewApplicationStatus: ViewApplicationStatusImpl) {
        activity?.let {
            viewApplicationStatus.onClick(MyAccountSection.AccountLanding, activity = it)
        }
    }

    override fun createLinkYourWooliesCardIntent(
        activityLauncher: BetterActivityResult<Intent, ActivityResult>?,
        viewModel: UserAccountLandingViewModel
    ) {
        activity?.apply {
           val cardIntent =  Intent(this, SSOActivity::class.java)
            cardIntent.putExtra(SSOActivity.TAG_PROTOCOL, SSOActivity.Protocol.HTTPS.rawValue())
            cardIntent.putExtra(SSOActivity.TAG_HOST, SSOActivity.Host.STS.rawValue())
            cardIntent.putExtra(SSOActivity.TAG_PATH, SSOActivity.Path.SIGNIN.rawValue())
            cardIntent.putExtra(SSOActivity.TAG_SCOPE, "C2Id")
            activityLauncher?.launch(cardIntent, onActivityResult =  { result ->
                viewModel.setUserAuthenticated(result.resultCode)
            })
        }
    }

    private fun navigateToStoreCardActivity(deepLinkParams: String? = null, account: String) {
        activity?.let { context ->
            val intent = Intent(context, StoreCardActivity::class.java)
            intent.putExtra(Constants.ACCOUNT_PRODUCT_PAYLOAD, account)
            deepLinkParams?.let {
                intent.putExtra(AccountSignedInPresenterImpl.DEEP_LINKING_PARAMS, it)
            }
            context.startActivityForResult(intent, ACCOUNT_CARD_REQUEST_CODE)
            context.overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay)
        }
    }

    private fun redirectToAccountSignInActivity(
            applyNowState: ApplyNowState?,
            deepLinkParams: String? = null,
            userAccountResponse: String?) {
        if (applyNowState == null || userAccountResponse.isNullOrEmpty()) return
        activity?.let { context ->
            Intent(context, AccountSignedInActivity::class.java).apply {
                putExtra(AccountSignedInPresenterImpl.APPLY_NOW_STATE, applyNowState)
                putExtra(AccountSignedInPresenterImpl.MY_ACCOUNT_RESPONSE, userAccountResponse)
                deepLinkParams?.let {
                    putExtra(AccountSignedInPresenterImpl.DEEP_LINKING_PARAMS, deepLinkParams)
                }
                context.startActivityForResult(this, ACCOUNT_CARD_REQUEST_CODE)
                context.overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay)
            }
        }
    }

}