package za.co.woolworths.financial.services.android.ui.fragments.account.detail

import android.app.Activity
import android.content.Intent
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.DebitOrder
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplicationStatusType
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.DebitOrderActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.card.InstantStoreCardReplacementActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.activities.temporary_store_card.GetTemporaryStoreCardPopupActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity.Companion.BPI_OPT_IN
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity.Companion.BPI_PRODUCT_GROUP_CODE
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.BPIOverviewOverviewImpl.Companion.ACCOUNT_INFO
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
                startActivityForResult(intent, MyCardDetailActivity.ACTIVATE_VIRTUAL_TEMP_CARD_RESULT_CODE)
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

        fun navigateToBalanceProtectionInsurance(
            activity: Activity?,
            accountInfo: String?,
            accounts: Account?,
            bpiInsuranceStatus: BpiInsuranceApplicationStatusType?) {
            activity?.apply {

                val productGroupCode = when (accounts?.productGroupCode) {
                    AccountsProductGroupCode.CREDIT_CARD.groupCode -> FirebaseManagerAnalyticsProperties.MYACCOUNTSCREDITCARDBPI
                    AccountsProductGroupCode.STORE_CARD.groupCode -> FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDBPI
                    AccountsProductGroupCode.PERSONAL_LOAN.groupCode -> FirebaseManagerAnalyticsProperties.MYACCOUNTSPERSONALLOANBPI
                    else -> null
                }

                productGroupCode?.let { Utils.triggerFireBaseEvents(it, this) }
                val navigateToBalanceProtectionInsurance = Intent(this, BalanceProtectionInsuranceActivity::class.java)
                bpiInsuranceStatus?.let {
                    if(it == BpiInsuranceApplicationStatusType.NOT_OPTED_IN){
                        navigateToBalanceProtectionInsurance.putExtra(BPI_OPT_IN, true)
                        navigateToBalanceProtectionInsurance.putExtra(BPI_PRODUCT_GROUP_CODE, accounts?.productGroupCode)
                    }
                }
                navigateToBalanceProtectionInsurance.putExtra(ACCOUNT_INFO, accountInfo)
                startActivity(navigateToBalanceProtectionInsurance)
                overridePendingTransition(0, 0)
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