package za.co.woolworths.financial.services.android.ui.fragments.account.detail

import android.app.Activity
import android.content.Intent
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.DebitOrder
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.DebitOrderActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.bpi.BPIBalanceProtectionActivity
import za.co.woolworths.financial.services.android.ui.activities.card.InstantStoreCardReplacementActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.activities.temporary_store_card.GetTemporaryStoreCardPopupActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard
import za.co.woolworths.financial.services.android.ui.fragments.npc.MyCardExtension
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.wenum.StoreCardViewType

class MyAccountsScreenNavigator {

    companion object {
        fun navigateToGetTemporaryStoreCardPopupActivity(activity: Activity?, storeCardResponse: StoreCardsResponse, screenType: StoreCardViewType = StoreCardViewType.DEFAULT) {
            activity?.apply {
                val intent = Intent(this, GetTemporaryStoreCardPopupActivity::class.java)
                intent.putExtra(MyCardDetailActivity.STORE_CARD_DETAIL, Utils.objectToJson(storeCardResponse))
                if (screenType != StoreCardViewType.DEFAULT)
                    intent.putExtra(MyCardDetailActivity.STORE_CARD_VIEW_TYPE, screenType)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
            }
        }

        fun navigateToMyCardDetailActivity(activity: Activity?, storeCardResponse: StoreCardsResponse, requestUnblockStoreCardCall: Boolean = false, screenType: StoreCardViewType = StoreCardViewType.DEFAULT) {
            activity?.apply {
                val displayStoreCardDetail = Intent(this, MyCardDetailActivity::class.java)
                displayStoreCardDetail.putExtra(MyCardDetailActivity.STORE_CARD_DETAIL, Utils.objectToJson(storeCardResponse))
                displayStoreCardDetail.putExtra(TemporaryFreezeStoreCard.ACTIVATE_UNBLOCK_CARD_ON_LANDING, requestUnblockStoreCardCall)
                if (screenType != StoreCardViewType.DEFAULT)
                    displayStoreCardDetail.putExtra(MyCardDetailActivity.STORE_CARD_VIEW_TYPE, screenType)
                startActivityForResult(displayStoreCardDetail, AccountSignedInActivity.REQUEST_CODE_BLOCK_MY_STORE_CARD)
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
            }
        }

        fun navigateToDebitOrderActivity(activity: Activity?, debitOrder: DebitOrder) {
            activity?.apply {
                val debitOrderIntent = Intent(this, DebitOrderActivity::class.java)
                debitOrderIntent.putExtra("DebitOrder", debitOrder)
                startActivity(debitOrderIntent)
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
            }
        }

        fun navigateToBalanceProtectionInsurance(activity: Activity?, accountInfo: String?, accounts: Account?) {
            activity?.apply {

                val productGroupCode = when (accounts?.productGroupCode) {
                    AccountsProductGroupCode.CREDIT_CARD.groupCode -> FirebaseManagerAnalyticsProperties.MYACCOUNTSCREDITCARDBPI
                    AccountsProductGroupCode.STORE_CARD.groupCode -> FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDBPI
                    AccountsProductGroupCode.PERSONAL_LOAN.groupCode -> FirebaseManagerAnalyticsProperties.MYACCOUNTSPERSONALLOANBPI
                    else -> null
                }

                productGroupCode?.let { Utils.triggerFireBaseEvents(it) }
                val navigateToBalanceProtectionInsurance = Intent(this, BPIBalanceProtectionActivity::class.java)
                navigateToBalanceProtectionInsurance.putExtra("account_info", accountInfo)
                startActivity(navigateToBalanceProtectionInsurance)
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
            }
        }

        fun navigateToLinkNewCardActivity(activity: Activity?, storeCard: String?) {
            activity?.apply {
                val openLinkNewCardActivity = Intent(this, InstantStoreCardReplacementActivity::class.java)
                openLinkNewCardActivity.putExtra(MyCardDetailActivity.STORE_CARD_DETAIL, storeCard)
                startActivityForResult(openLinkNewCardActivity, MyCardExtension.INSTANT_STORE_CARD_REPLACEMENT_REQUEST_CODE)
                overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
        }
    }
}