package za.co.woolworths.financial.services.android.ui.fragments.account.main.util

import android.app.Activity
import android.content.Intent
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFragment
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

/**
 * This is a temporary class to handle navigation between the new refactored code and the other screens which not refactored yet
 */
interface IStoreCardNavigator {
    fun navigateToStatementActivity(activity: Activity?, product: Account? , applyNowState:ApplyNowState = ApplyNowState.STORE_CARD)
    fun navigateToRecentTransactionActivity(activity: Activity?, product: Account? , applyNowState:ApplyNowState = ApplyNowState.STORE_CARD,cardType: String)
}

class StoreCardNavigator @Inject constructor() : IStoreCardNavigator {

    override fun navigateToStatementActivity(activity: Activity?, product: Account?, applyNowState:ApplyNowState) {
        activity?.apply {
            val mAccountPair: Pair<ApplyNowState, Account?> = Pair(applyNowState,product)
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDSTATEMENTS, this)
            val openStatement = Intent(this, StatementActivity::class.java)
            openStatement.putExtra(ChatFragment.ACCOUNTS, Gson().toJson(mAccountPair))
            startActivity(openStatement)
            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }
    override fun navigateToRecentTransactionActivity(activity: Activity?, product: Account?, applyNowState:ApplyNowState ,cardType: String) {
        activity?.let {
            product?.apply {
                val intent = Intent(it, WTransactionsActivity::class.java)
                intent.putExtra(BundleKeysConstants.PRODUCT_OFFERINGID, productOfferingId.toString())
                if (cardType == AccountsProductGroupCode.CREDIT_CARD.groupCode && accountNumber?.isNotEmpty() == true) {
                    intent.putExtra("accountNumber", accountNumber.toString())
                }
                intent.putExtra(ChatFragment.ACCOUNTS, Gson().toJson(Pair(applyNowState, this)))
                intent.putExtra("cardType", cardType)
                it.startActivityForResult(intent, 0)
                it.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
        }
    }
}