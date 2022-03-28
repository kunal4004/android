package za.co.woolworths.financial.services.android.ui.activities.account.sign_in

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.account_in_arrears_layout.*
import kotlinx.android.synthetic.main.account_signed_in_activity.*
import kotlinx.android.synthetic.main.chat_collect_agent_floating_button_layout.*
import za.co.woolworths.financial.services.android.contracts.IAccountSignedInContract
import za.co.woolworths.financial.services.android.contracts.IBottomSheetBehaviourPeekHeightListener
import za.co.woolworths.financial.services.android.contracts.IShowChatBubble
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.ActionText
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.PMACardPopupModel
import za.co.woolworths.financial.services.android.models.dto.account.AccountHelpInformation
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.information.CardInformationHelpActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity.Companion.PAYMENT_DETAIL_CARD_UPDATE
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity.Companion.PAY_MY_ACCOUNT_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountsFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PMA3DSecureProcessRequestFragment.Companion.PMA_TRANSACTION_COMPLETED_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PMA3DSecureProcessRequestFragment.Companion.PMA_UPDATE_CARD_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatBubbleVisibility
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.AccountSixMonthArrearsFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ShowTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.BALANCE_PROTECTION_INSURANCE_OPT_IN_SUCCESS_RESULT_CODE
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.BALANCE_PROTECTION_INSURANCE_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

@AndroidEntryPoint
class AccountSignedInActivity : AppCompatActivity(), IAccountSignedInContract.MyAccountView,
    IBottomSheetBehaviourPeekHeightListener, View.OnClickListener, IShowChatBubble {

    companion object {
        const val ABSA_ONLINE_BANKING_REGISTRATION_REQUEST_CODE = 2111
        const val REQUEST_CODE_BLOCK_MY_STORE_CARD = 3021
        const val REQUEST_CODE_ACCOUNT_INFORMATION = 2112
    }

    private var isReloadCacheAccountDataEnabled: Boolean = false
    var mAccountOptionsNavHost: NavHostFragment? = null
    var mAvailableFundsNavHost: NavHostFragment? = null
    private var mPeekHeight: Int = 0
    var mAccountSignedInPresenter: AccountSignedInPresenterImpl? = null
    private var sheetBehavior: BottomSheetBehavior<*>? = null
    private var mAccountHelpInformation: MutableList<AccountHelpInformation>? = null

    private val payMyAccountViewModel: PayMyAccountViewModel by viewModels()
    private val myAccountsRemoteApiViewModel: MyAccountsRemoteApiViewModel by viewModels()

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.account_signed_in_activity)
        KotlinUtils.setTransparentStatusBar(this)
        mAccountSignedInPresenter = AccountSignedInPresenterImpl(this, AccountSignedInModelImpl())
        mAccountSignedInPresenter?.apply {
            intent?.extras?.let { bundle -> getAccountBundle(bundle) }
            mAvailableFundsNavHost = supportFragmentManager.findFragmentById(R.id.nav_host_available_fund_fragment) as? NavHostFragment
            mAccountOptionsNavHost = supportFragmentManager.findFragmentById(R.id.nav_host_overlay_bottom_sheet_fragment) as? NavHostFragment

            setAvailableFundBundleInfo(mAvailableFundsNavHost?.navController, myAccountsRemoteApiViewModel)
            setAccountCardDetailInfo(mAccountOptionsNavHost?.navController)

            setToolbarTopMargin()
        }

        KotlinUtils.roundCornerDrawable(accountInArrearsTextView, "#e41f1f")
        AnimationUtilExtension.animateViewPushDown(accountInArrearsTextView)

        accountInArrearsTextView?.setOnClickListener(this)
        infoIconImageView?.setOnClickListener(this)
        navigateBackImageButton?.setOnClickListener(this)
    }

    private fun setToolbarTopMargin() {
        val bar = findViewById<Toolbar>(R.id.toolbarContainer)
        val params = bar?.layoutParams as? ViewGroup.MarginLayoutParams
        params?.topMargin = KotlinUtils.getStatusBarHeight()
        bar?.layoutParams = params
    }

    private fun configureBottomSheetDialog() {
        val bottomSheetBehaviourLinearLayout = findViewById<LinearLayout>(R.id.bottomSheetBehaviourLinearLayout)
        val layoutParams = bottomSheetBehaviourLinearLayout?.layoutParams
        layoutParams?.height = mAccountSignedInPresenter?.bottomSheetBehaviourHeight()
        bottomSheetBehaviourLinearLayout?.requestLayout()
        sheetBehavior = BottomSheetBehavior.from(bottomSheetBehaviourLinearLayout)
        sheetBehavior?.peekHeight = mAccountSignedInPresenter?.bottomSheetBehaviourPeekHeight()
            ?: 0
        sheetBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                transitionBottomSheetBackgroundColor(slideOffset)
                navigateBackImageButton?.rotation = slideOffset * -90
            }
        })
    }

    override fun onBackPressed() {
        // Collapse overlay view if view is opened, else navigate to previous screen
        if (sheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            return
        }

        if (isReloadCacheAccountDataEnabled)
            setResult(MyAccountsFragment.RELOAD_ACCOUNT_RESULT_CODE)

        mAccountSignedInPresenter?.onBackPressed(this@AccountSignedInActivity)
    }

    override fun onDestroy() {
        super.onDestroy()
        mAccountSignedInPresenter?.onDestroy()
    }

    override fun toolbarTitle(title: String) {
        toolbarTitleTextView?.text = title
    }

    override fun showAccountInArrears(account: Account) {
        toolbarTitleTextView?.visibility = GONE
        accountInArrearsTextView?.visibility = VISIBLE
        mAccountSignedInPresenter?.getMyAccountCardInfo()
            ?.let { accountKeyPair -> showAccountInArrearsDialog(accountKeyPair) }
    }

    override fun hideAccountInArrears(account: Account) {
        toolbarTitleTextView?.visibility = VISIBLE
        accountInArrearsTextView?.visibility = GONE
    }

    override fun showAccountHelp(informationModelAccount: MutableList<AccountHelpInformation>) {
        this.mAccountHelpInformation = informationModelAccount
    }

    override fun showViewTreatmentPlan(state: ApplyNowState, eligibilityPlan: EligibilityPlan?){
        val bundle = Bundle()
        bundle.putSerializable(ViewTreatmentPlanDialogFragment.APPLY_NOW_STATE, state)
        bundle.putSerializable(ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN, eligibilityPlan)
        mAvailableFundsNavHost?.navController?.navigate(R.id.viewTreatmentPlanDialogFragment, bundle)
    }

    override fun showViewTreatmentPlan(viewPaymentOptions: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean(ShowTreatmentPlanDialogFragment.VIEW_PAYMENT_OPTIONS_VISIBILITY, viewPaymentOptions)
        mAvailableFundsNavHost?.navController?.navigate(R.id.showTreatmentPlanDialogFragment, bundle)
    }

    override fun removeBlocksWhenChargedOff() {
        availableFundFragmentFrameLayout?.visibility = GONE
        bottomSheetBehaviourLinearLayout?.visibility = GONE
        removeBlockOnCollectionCustomerFrameLayout?.visibility = VISIBLE
        val removeBlockOnCollectionFragmentContainerView = supportFragmentManager.findFragmentById(R.id.removeBlockOnCollectionFragmentContainerView) as? NavHostFragment
        val navigationController: NavController? = removeBlockOnCollectionFragmentContainerView?.navController
        mAccountSignedInPresenter?.apply {
            when (getMyAccountCardInfo()?.first) {
                ApplyNowState.STORE_CARD, ApplyNowState.PERSONAL_LOAN -> {
                    navigationController?.navigate(R.id.removeBlockDCFragment)
                }
                else -> {
                    val bundle = Bundle()
                    bundle.putString(AccountSignedInPresenterImpl.MY_ACCOUNT_RESPONSE, Gson().toJson(getSixMonthOutstandingTitleAndCardResource()))
                    navigationController?.navigate(R.id.accountInDelinquencyFragment, bundle)
                }
            }
        }
    }

    override fun removeBlocksOnCollectionCustomer() {
        availableFundFragmentFrameLayout?.visibility = GONE
        bottomSheetBehaviourLinearLayout?.visibility = GONE
        removeBlockOnCollectionCustomerFrameLayout?.visibility = VISIBLE
        val removeBlockOnCollectionFragmentContainerView = supportFragmentManager.findFragmentById(R.id.removeBlockOnCollectionFragmentContainerView) as? NavHostFragment
        val navigationController: NavController? = removeBlockOnCollectionFragmentContainerView?.navController
        mAccountSignedInPresenter?.apply {
            when (getMyAccountCardInfo()?.first) {
                ApplyNowState.STORE_CARD, ApplyNowState.PERSONAL_LOAN -> {
                    navigationController?.graph?.startDestination = R.id.removeBlockDCFragment
                    navigationController?.setGraph(navigationController.graph, bundleOf())
                    navigationController?.navigate(R.id.removeBlockOnCollectionDialogFragment)
                }
                else -> {
                    window?.decorView?.fitsSystemWindows = true
                    Utils.updateStatusBarBackground(this@AccountSignedInActivity)
                    setAccountSixMonthInArrears(navigationController)
                }
            }
        }
    }

    override fun bottomSheetIsExpanded(): Boolean {
        return sheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED
    }

    override fun chatToCollectionAgent(applyNowState: ApplyNowState, accountList: List<Account>?) {
        val chatToCollectionAgentView = ChatFloatingActionButtonBubbleView(
            this@AccountSignedInActivity,
            ChatBubbleVisibility(accountList, this@AccountSignedInActivity),
            chatBubbleFloatingButton,
            applyNowState,
            notificationBadge = badge,
            onlineChatImageViewIndicator = onlineIndicatorImageView,
            vocTriggerEvent = payMyAccountViewModel.getVocTriggerEventMyAccounts()
        )
        chatToCollectionAgentView.build()
    }

    @Throws(RuntimeException::class)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.accountInArrearsTextView -> mAccountSignedInPresenter?.getMyAccountCardInfo()
                ?.let { account -> showAccountInArrearsDialog(account) }
            R.id.infoIconImageView -> navigateToCardInformation()
            R.id.navigateBackImageButton -> onBackPressed()
            else -> throw RuntimeException("Unexpected onClick Id found ${v?.id}")
        }
    }

    private fun navigateToCardInformation() {
        val cardInformationHelpActivity = Intent(this, CardInformationHelpActivity::class.java)
        cardInformationHelpActivity.putExtra(
            CardInformationHelpActivity.HELP_INFORMATION,
            Gson().toJson(mAccountHelpInformation)
        )
        startActivityForResult(cardInformationHelpActivity, REQUEST_CODE_ACCOUNT_INFORMATION)
        overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
    }

    private fun showAccountInArrearsDialog(account: Pair<ApplyNowState, Account>) {
        val accountApplyNowState = payMyAccountViewModel.getCardDetail()?.account
        if (accountApplyNowState == null)
            payMyAccountViewModel.setPMACardInfo(PMACardPopupModel(account = mAccountSignedInPresenter?.getMyAccountCardInfo()))
        val bundle = Bundle()
        bundle.putString(AccountSignedInPresenterImpl.MY_ACCOUNT_RESPONSE, Gson().toJson(account))
        mAvailableFundsNavHost?.navController?.navigate(R.id.accountInArrearsDialogFragment, bundle)
    }

    private fun transitionBottomSheetBackgroundColor(slideOffset: Float) {
        val colorFrom = ContextCompat.getColor(this, android.R.color.transparent)
        val colorTo = ContextCompat.getColor(this, R.color.black_99)
        dimView?.setBackgroundColor(KotlinUtils.interpolateColor(slideOffset, colorFrom, colorTo))
    }

    override fun onBottomSheetPeekHeight(pixel: Int) {
        runOnUiThread {
            mPeekHeight = pixel
            configureBottomSheetDialog()
        }
    }

    private fun showChatToCollectionAgent() {
        mAccountSignedInPresenter?.chatWithCollectionAgent()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val extras = data?.extras
        when (requestCode) {
            BALANCE_PROTECTION_INSURANCE_REQUEST_CODE -> {
                when(resultCode){
                    BALANCE_PROTECTION_INSURANCE_OPT_IN_SUCCESS_RESULT_CODE -> {
                        isReloadCacheAccountDataEnabled = true
                        supportFragmentManager.fragments.apply {
                            if (this.isNotEmpty()) {
                                this[1].let {
                                    it.childFragmentManager.fragments.let { childFragments ->
                                        if (childFragments.isNotEmpty()) {
                                            childFragments[0].onActivityResult(requestCode, resultCode, data)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            PAY_MY_ACCOUNT_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_OK, PMA_UPDATE_CARD_RESULT_CODE -> {
                        extras?.getString(PAYMENT_DETAIL_CARD_UPDATE)?.apply {
                            queryGetPaymentMethod()
                            payMyAccountViewModel.setPMACardInfo(this)
                        }
                    }

                    // on back to my account pressed (R.string.back_to_my_account_button)
                    PMA_TRANSACTION_COMPLETED_RESULT_CODE -> {
                        extras?.getString(PAYMENT_DETAIL_CARD_UPDATE)?.apply {
                            queryGetPaymentMethod()
                            payMyAccountViewModel.setPMACardInfo(this)
                        }
                        mAvailableFundsNavHost?.navController?.navigateUp()
                    }
                }
            }
            REQUEST_CODE_ACCOUNT_INFORMATION -> sheetBehavior?.state =
                BottomSheetBehavior.STATE_COLLAPSED
            else -> supportFragmentManager.fragments.apply {
                if (this.isNotEmpty()) {
                    this[1].let {
                        it.childFragmentManager.fragments.let { childFragments ->
                            if (childFragments.isNotEmpty()) {
                                childFragments[0].onActivityResult(requestCode, resultCode, data)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun removeBlocksWhenChargedOff(isViewTreatmentPlanActive: Boolean) {
        availableFundFragmentFrameLayout?.visibility = GONE
        bottomSheetBehaviourLinearLayout?.visibility = GONE
        removeBlockOnCollectionCustomerFrameLayout?.visibility = VISIBLE
        val removeBlockOnCollectionFragmentContainerView = supportFragmentManager.findFragmentById(R.id.removeBlockOnCollectionFragmentContainerView) as? NavHostFragment
        val navigationController: NavController? = removeBlockOnCollectionFragmentContainerView?.navController
        mAccountSignedInPresenter?.apply {
            when (getMyAccountCardInfo()?.first) {
                ApplyNowState.STORE_CARD, ApplyNowState.PERSONAL_LOAN -> {
                    navigationController?.navigate(R.id.removeBlockDCFragment)
                }
                else -> {
                    val bundle = Bundle()
                    bundle.putString(AccountSignedInPresenterImpl.MY_ACCOUNT_RESPONSE, Gson().toJson(getSixMonthOutstandingTitleAndCardResource()))
                    bundle.putBoolean(AccountSixMonthArrearsFragment.IS_VIEW_TREATMENT_PLAN, isViewTreatmentPlanActive)
                    navigationController?.navigate(R.id.accountInDelinquencyFragment, bundle)
                }
            }
        }
    }

    private fun queryGetPaymentMethod() {
        val fragment = mAvailableFundsNavHost?.childFragmentManager?.primaryNavigationFragment
        if (fragment is AvailableFundFragment) {
            payMyAccountViewModel.isQueryPayUPaymentMethodComplete = false
            fragment.queryPaymentMethod()
        }
    }

    override fun showChatBubble() {
        showChatToCollectionAgent()
    }

    override fun showPlanButton(state: ApplyNowState, eligibilityPlan: EligibilityPlan?) {
        val fragment = mAccountOptionsNavHost?.childFragmentManager?.primaryNavigationFragment
        if (fragment is AccountsOptionFragment) {
            if(eligibilityPlan?.actionText == ActionText.TAKE_UP_TREATMENT_PLAN.value){
                fragment.showSetUpPaymentPlanButton(state, eligibilityPlan)
            }
            else if(eligibilityPlan?.actionText == ActionText.VIEW_TREATMENT_PLAN.value){
                fragment.showViewTreatmentPlanButton(state, eligibilityPlan)
            }
        }
    }

    private fun hideTreatmentPlanButtons() {
        val fragment = mAccountOptionsNavHost?.childFragmentManager?.primaryNavigationFragment
        if (fragment is AccountsOptionFragment) {
            fragment.hideTreatmentPlanButtons()
        }
    }

    fun onTreatmentPlanStatusUpdateRequired() {
        mAccountSignedInPresenter?.apply {
            getMyAccountCardInfo()?.first?.let { applyNowState ->
                hideTreatmentPlanButtons()
                showProductOfferOutstanding(applyNowState, myAccountsRemoteApiViewModel, false)
            }
        }
    }
}