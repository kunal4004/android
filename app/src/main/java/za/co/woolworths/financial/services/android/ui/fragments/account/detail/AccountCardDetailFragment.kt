package za.co.woolworths.financial.services.android.ui.fragments.account.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_card_detail_fragment.*
import kotlinx.android.synthetic.main.account_detail_header_fragment.*
import kotlinx.android.synthetic.main.account_options_layout.*
import za.co.woolworths.financial.services.android.contracts.AccountPaymentOptionsContract
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.DebitOrder
import za.co.woolworths.financial.services.android.models.dto.OfferActive
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.DebitOrderActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.bpi.BPIBalanceProtectionActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.activities.temporary_store_card.GetTemporaryStoreCardPopupActivity
import za.co.woolworths.financial.services.android.ui.fragments.WStoreCardFragment
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

open class AccountCardDetailFragment : Fragment(), View.OnClickListener, AccountPaymentOptionsContract.AccountCardDetailView {

    var mCardPresenterImpl: AccountCardDetailPresenterImpl? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCardPresenterImpl = AccountCardDetailPresenterImpl(this, AccountCardDetailModelImpl())
        mCardPresenterImpl?.setAccountDetailBundle(arguments)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.account_card_detail_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        balanceProtectionInsuranceView?.setOnClickListener(this)
        cardImageRootView?.setOnClickListener(this)
        debitOrderView?.setOnClickListener(this)
        cardDetailImageView?.setOnClickListener(this)
        tvIncreaseLimit?.setOnClickListener(this)
        relIncreaseMyLimit?.setOnClickListener(this)
        llIncreaseLimitContainer?.setOnClickListener(this)

        mCardPresenterImpl?.apply {
            setBalanceProtectionInsuranceState()
            displayCardHolderName()
            getCreditLimitIncreaseController()?.defaultIncreaseLimitView(logoIncreaseLimit, llCommonLayer, tvIncreaseLimit)
            requestGetUserCLIOfferActiveFromServer()
        }
    }

    override fun showStoreCardProgress() {
        loadStoreCardProgressBar?.visibility = VISIBLE
        storeCardLoaderView?.visibility = VISIBLE
        cardImageRootView?.isEnabled = false
    }

    @SuppressLint("DefaultLocale")
    override fun hideAccountStoreCardProgress() {
        if (fragmentIsAlreadyAdded()) return
        loadStoreCardProgressBar?.visibility = GONE
        storeCardLoaderView?.visibility = GONE
        // Boolean check will enable clickable event only when text is "view card"
        cardImageRootView?.isEnabled =
                myCardDetailTextView?.text?.toString()?.toLowerCase()?.contains("view") == true
    }

    override fun handleUnknownHttpCode(description: String?) {
        if (fragmentIsAlreadyAdded()) return
        activity?.supportFragmentManager?.let { fragmentManager -> Utils.showGeneralErrorDialog(fragmentManager, description) }
    }

    override fun handleSessionTimeOut(stsParams: String?) {
        if (fragmentIsAlreadyAdded()) return
        (activity as? AccountSignedInActivity)?.let { accountSignedInActivity -> SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, stsParams, accountSignedInActivity) }
    }

    private fun fragmentIsAlreadyAdded(): Boolean = !isAdded

    override fun onClick(v: View?) {
        mCardPresenterImpl?.apply {
            when (v?.id) {
                R.id.balanceProtectionInsuranceView -> navigateToBalanceProtectionInsuranceOnButtonTapped()
                R.id.debitOrderView -> navigateToDebitOrderActivityOnButtonTapped()
                R.id.cardImageRootView -> navigateToTemporaryStoreCardOnButtonTapped()
                R.id.cardDetailImageView -> mCardPresenterImpl?.requestGetAccountStoreCardCardsFromServer()
                R.id.tvIncreaseLimit, R.id.relIncreaseMyLimit, R.id.llIncreaseLimitContainer -> {
                    getCreditLimitIncreaseController()?.nextStep(getOfferActive(), getProductOfferingId()?.toString())
                }
            }
        }
    }

    override fun onDestroy() {
        mCardPresenterImpl?.onDestroy()
        super.onDestroy()
    }

    override fun navigateToGetTemporaryStoreCardPopupActivity(storeCardResponse: StoreCardsResponse) {
        if (fragmentIsAlreadyAdded()) return
        activity?.apply {
            val intent = Intent(this, GetTemporaryStoreCardPopupActivity::class.java)
            intent.putExtra(MyCardDetailActivity.STORE_CARD_DETAIL, Utils.objectToJson(storeCardResponse))
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    override fun navigateToMyCardDetailActivity(storeCardResponse: StoreCardsResponse) {
        if (fragmentIsAlreadyAdded()) return
        activity?.apply {
            val displayStoreCardDetail = Intent(this, MyCardDetailActivity::class.java)
            displayStoreCardDetail.putExtra(MyCardDetailActivity.STORE_CARD_DETAIL, Utils.objectToJson(storeCardResponse))
            startActivityForResult(displayStoreCardDetail, WStoreCardFragment.REQUEST_CODE_BLOCK_MY_STORE_CARD)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    override fun navigateToDebitOrderActivity(debitOrder: DebitOrder) {
        if (fragmentIsAlreadyAdded()) return
        activity?.apply {
            val debitOrderIntent = Intent(this, DebitOrderActivity::class.java)
            debitOrderIntent.putExtra("DebitOrder", debitOrder)
            startActivity(debitOrderIntent)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    override fun navigateToBalanceProtectionInsurance(accountInfo: String?) {
        if (fragmentIsAlreadyAdded()) return
        activity?.apply {
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSCREDITCARDBPI)
            val navigateToBalanceProtectionInsurance =
                    Intent(this, BPIBalanceProtectionActivity::class.java)
            navigateToBalanceProtectionInsurance.putExtra("account_info", accountInfo)
            startActivity(navigateToBalanceProtectionInsurance)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    override fun setBalanceProtectionInsuranceState(coveredText: Boolean) {
        when(coveredText){
            true -> {
                balanceProtectInsuranceTextView?.text = activity?.resources?.getString(R.string.bpi_covered)
                balanceProtectInsuranceTextView?.setBackgroundResource(R.drawable.round_green_corner)
            }
            false -> {
                balanceProtectInsuranceTextView?.text = activity?.resources?.getString(R.string.bpi_not_covered)
                balanceProtectInsuranceTextView?.setBackgroundResource(R.drawable.round_amber_corner)
            }
        }
    }

    override fun displayCardHolderName(name: String?) {
        userNameTextView?.text = name
    }

    override fun displayViewCardText() {
        if (fragmentIsAlreadyAdded()) return
        myCardDetailTextView?.text = activity?.getString(R.string.view_card)
    }

    override fun hideUserOfferActiveProgress() {
        llIncreaseLimitContainer?.isEnabled = true
        relIncreaseMyLimit?.isEnabled = true
        progressCreditLimit?.visibility = GONE
        tvIncreaseLimit?.visibility = VISIBLE
    }

    override fun showUserOfferActiveProgress() {
        llIncreaseLimitContainer?.isEnabled = false
        relIncreaseMyLimit?.isEnabled = false
        progressCreditLimit?.visibility = VISIBLE
        progressCreditLimit?.indeterminateDrawable?.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)
        tvApplyNowIncreaseLimit?.visibility = GONE
        tvIncreaseLimit?.visibility = VISIBLE
    }

    override fun disableContentStatusUI() {
        relIncreaseMyLimit?.isEnabled = false
        llIncreaseLimitContainer?.isEnabled = false
        tvIncreaseLimit?.isEnabled = false
    }

    override fun enableContentStatusUI() {
        relIncreaseMyLimit?.isEnabled = true
        llIncreaseLimitContainer?.isEnabled = true
        tvIncreaseLimit?.isEnabled = true
    }

    override fun handleCreditLimitIncreaseTagStatus(offerActive: OfferActive) {
        activity?.runOnUiThread {
            mCardPresenterImpl?.getCreditLimitIncreaseController()?.accountCLIStatus(llCommonLayer, tvIncreaseLimit, tvApplyNowIncreaseLimit, tvIncreaseLimitDescription, logoIncreaseLimit, offerActive)
        }
    }

    override fun hideProductNotInGoodStanding() {
        llIncreaseLimitContainer?.visibility = GONE
    }
}


