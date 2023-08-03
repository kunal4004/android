package za.co.woolworths.financial.services.android.ui.activities.account.sign_in

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Parcelable
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
import com.awfs.coordination.databinding.AccountSignedInActivityBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.contracts.IAccountSignedInContract
import za.co.woolworths.financial.services.android.contracts.IBottomSheetBehaviourPeekHeightListener
import za.co.woolworths.financial.services.android.contracts.IShowChatBubble
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.ActionText
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.PMACardPopupModel
import za.co.woolworths.financial.services.android.models.dto.account.AccountHelpInformation
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl.Companion.ELITE_PLAN
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl.Companion.ELITE_PLAN_MODEL
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.information.CardInformationHelpActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity.Companion.PAYMENT_DETAIL_CARD_UPDATE
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity.Companion.PAY_MY_ACCOUNT_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.treatmentplan.ProductOfferingStatus
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatBubbleVisibility
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.AccountSixMonthArrearsFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment.Companion.REQUEST_ELITEPLAN
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PMA3DSecureProcessRequestFragment.Companion.PMA_TRANSACTION_COMPLETED_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PMA3DSecureProcessRequestFragment.Companion.PMA_UPDATE_CARD_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ShowTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature.fragment.UserAccountsLandingFragment.Companion.RELOAD_ACCOUNT_RESULT_CODE
import za.co.woolworths.financial.services.android.util.ActivityIntentNavigationManager
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.BALANCE_PROTECTION_INSURANCE_OPT_IN_SUCCESS_RESULT_CODE
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.BALANCE_PROTECTION_INSURANCE_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.wenum.PayMyAccountStartDestinationType

@AndroidEntryPoint
class AccountSignedInActivity : AppCompatActivity(), IAccountSignedInContract.MyAccountView,
    IBottomSheetBehaviourPeekHeightListener, View.OnClickListener, IShowChatBubble {

    companion object {
        const val ABSA_ONLINE_BANKING_REGISTRATION_REQUEST_CODE = 2111
        const val REQUEST_CODE_BLOCK_MY_STORE_CARD = 3021
        const val REQUEST_CODE_ACCOUNT_INFORMATION = 2112
    }

    private lateinit var binding: AccountSignedInActivityBinding
    private var isReloadCacheAccountDataEnabled: Boolean = false
    private var mAccountOptionsNavHost: NavHostFragment? = null
    private var mAvailableFundsNavHost: NavHostFragment? = null
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
        binding = AccountSignedInActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        KotlinUtils.setTransparentStatusBar(this)
        mAccountSignedInPresenter = AccountSignedInPresenterImpl(this, AccountSignedInModelImpl())
        mAccountSignedInPresenter?.apply {
            intent?.extras?.let { bundle -> getAccountBundle(bundle) }
            myAccountsRemoteApiViewModel.account = mAccountSignedInPresenter?.getAccount()
            mAvailableFundsNavHost = supportFragmentManager.findFragmentById(R.id.nav_host_available_fund_fragment) as? NavHostFragment
            mAccountOptionsNavHost = supportFragmentManager.findFragmentById(R.id.nav_host_overlay_bottom_sheet_fragment) as? NavHostFragment

            setAvailableFundBundleInfo(
                mAvailableFundsNavHost?.navController,
                myAccountsRemoteApiViewModel
            )
            setAccountCardDetailInfo(mAccountOptionsNavHost?.navController)

            setToolbarTopMargin(binding.includeAccountProductLandingToolbarAvailableFund.root)
            setToolbarTopMargin(binding.includeAccountProductLandingToolbarRemoveBlockOnCollection.root)
        }

        with(binding.includeAccountProductLandingToolbarAvailableFund.includeAccountInArrears) {
            KotlinUtils.roundCornerDrawable(accountInArrearsTextView, "#e41f1f")
            AnimationUtilExtension.animateViewPushDown(accountInArrearsTextView)

            accountInArrearsTextView?.setOnClickListener(this@AccountSignedInActivity)
            infoIconImageView?.setOnClickListener(this@AccountSignedInActivity)
            navigateBackImageButton?.setOnClickListener(this@AccountSignedInActivity)
        }

        with(binding.includeAccountProductLandingToolbarRemoveBlockOnCollection.includeAccountInArrears) {
            KotlinUtils.roundCornerDrawable(accountInArrearsTextView, "#e41f1f")
            AnimationUtilExtension.animateViewPushDown(accountInArrearsTextView)

            accountInArrearsTextView?.setOnClickListener(this@AccountSignedInActivity)
            infoIconImageView?.setOnClickListener(this@AccountSignedInActivity)
            navigateBackImageButton?.setOnClickListener(this@AccountSignedInActivity)
        }
    }

    private fun setToolbarTopMargin(toolbar: Toolbar) {
        val params = toolbar.layoutParams as? ViewGroup.MarginLayoutParams
        params?.topMargin = KotlinUtils.getStatusBarHeight()
        toolbar.layoutParams = params
    }

    private fun configureBottomSheetDialog() {
        val bottomSheetBehaviourLinearLayout =
            findViewById<LinearLayout>(R.id.bottomSheetBehaviourLinearLayout)
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
                binding.includeAccountProductLandingToolbarAvailableFund.includeAccountInArrears.navigateBackImageButton?.rotation = slideOffset * -90
                binding.includeAccountProductLandingToolbarRemoveBlockOnCollection.includeAccountInArrears.navigateBackImageButton?.rotation = slideOffset * -90
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
            setResult(RELOAD_ACCOUNT_RESULT_CODE)

        mAccountSignedInPresenter?.onBackPressed(this@AccountSignedInActivity)
    }

    override fun onDestroy() {
        super.onDestroy()
        mAccountSignedInPresenter?.onDestroy()
    }

    override fun toolbarTitle(title: String) {
        binding.includeAccountProductLandingToolbarAvailableFund.includeAccountInArrears.toolbarTitleTextView?.text = title
        binding.includeAccountProductLandingToolbarRemoveBlockOnCollection.includeAccountInArrears.toolbarTitleTextView?.text = title
    }

    override fun showAccountInArrears(account: Account?, showDialog: Boolean) {
        with(binding.includeAccountProductLandingToolbarAvailableFund.includeAccountInArrears) {
            toolbarTitleTextView?.visibility = GONE
            accountInArrearsTextView?.visibility = VISIBLE
        }
        with(binding.includeAccountProductLandingToolbarRemoveBlockOnCollection.includeAccountInArrears) {
            toolbarTitleTextView?.visibility = GONE
            accountInArrearsTextView?.visibility = VISIBLE
        }
        if (showDialog) {
            mAccountSignedInPresenter?.getMyAccountCardInfo()
                ?.let { accountKeyPair -> showAccountInArrearsDialog(accountKeyPair) }
        }
    }

    override fun showAboveSixMonthsAccountInDelinquencyPopup(eligibilityPlan: EligibilityPlan?) {
        val removeBlockOnCollectionFragmentContainerView =
            supportFragmentManager.findFragmentById(R.id.removeBlockOnCollectionFragmentContainerView) as? NavHostFragment
        val navigationController: NavController? =
            removeBlockOnCollectionFragmentContainerView?.navController
        showAboveSixMonthsInDelinquencyPopup(navigationController = navigationController,eligibilityPlan)
    }

    override fun hideAccountInArrears(account: Account) {
        with(binding.includeAccountProductLandingToolbarAvailableFund.includeAccountInArrears) {
            toolbarTitleTextView?.visibility = VISIBLE
            accountInArrearsTextView?.visibility = GONE
        }
        with(binding.includeAccountProductLandingToolbarRemoveBlockOnCollection.includeAccountInArrears) {
            toolbarTitleTextView?.visibility = VISIBLE
            accountInArrearsTextView?.visibility = GONE
        }
    }

    override fun showAccountHelp(informationModelAccount: MutableList<AccountHelpInformation>) {
        this.mAccountHelpInformation = informationModelAccount
    }

    override fun showViewTreatmentPlan(state: ApplyNowState, eligibilityPlan: EligibilityPlan?) {
        val bundle = Bundle()
        bundle.putSerializable(ViewTreatmentPlanDialogFragment.APPLY_NOW_STATE, state)
        bundle.putSerializable(ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN, eligibilityPlan)
        mAvailableFundsNavHost?.navController?.navigate(
            R.id.viewTreatmentPlanDialogFragment,
            bundle
        )
    }

    override fun showViewTreatmentPlan(viewPaymentOptions: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean(
            ShowTreatmentPlanDialogFragment.VIEW_PAYMENT_OPTIONS_VISIBILITY,
            viewPaymentOptions
        )
        if(ProductOfferingStatus(mAccountSignedInPresenter?.getAccount()).isChargedOff()){
            bundle.putString(ViewTreatmentPlanDialogFragment.APPLY_NOW_STATE, mAccountSignedInPresenter?.getAccount()?.productGroupCode)
        }

        mAvailableFundsNavHost?.navController?.navigate(
            R.id.showTreatmentPlanDialogFragment,
            bundle
        )
    }

    override fun removeBlocksWhenChargedOff() {
        binding.availableFundFragmentFrameLayout?.visibility = GONE
        binding.bottomSheetBehaviourLinearLayout?.visibility = GONE
        binding.removeBlockOnCollectionCustomerFrameLayout?.visibility = VISIBLE
        val removeBlockOnCollectionFragmentContainerView =
            supportFragmentManager.findFragmentById(R.id.removeBlockOnCollectionFragmentContainerView) as? NavHostFragment
        val navigationController: NavController? =
            removeBlockOnCollectionFragmentContainerView?.navController
        mAccountSignedInPresenter?.apply {
            when (getMyAccountCardInfo()?.first) {
                ApplyNowState.STORE_CARD, ApplyNowState.PERSONAL_LOAN -> {
                    navigationController?.navigate(R.id.removeBlockDCFragment)
                }
                else -> {
                    val bundle = Bundle()
                    bundle.putString(
                        AccountSignedInPresenterImpl.MY_ACCOUNT_RESPONSE,
                        Gson().toJson(getSixMonthOutstandingTitleAndCardResource())
                    )
                    navigationController?.navigate(R.id.accountInDelinquencyFragment, bundle)
                }
            }
        }
    }

    override fun removeBlocksOnCollectionCustomer() {
        binding.availableFundFragmentFrameLayout?.visibility = GONE
        binding.bottomSheetBehaviourLinearLayout?.visibility = GONE
        binding.removeBlockOnCollectionCustomerFrameLayout?.visibility = VISIBLE
        val removeBlockOnCollectionFragmentContainerView =
            supportFragmentManager.findFragmentById(R.id.removeBlockOnCollectionFragmentContainerView) as? NavHostFragment
        val navigationController: NavController? =
            removeBlockOnCollectionFragmentContainerView?.navController
        mAccountSignedInPresenter?.apply {
            when (getMyAccountCardInfo()?.first) {
                ApplyNowState.STORE_CARD, ApplyNowState.PERSONAL_LOAN -> {
                    navigationController?.graph?.startDestination = R.id.removeBlockDCFragment
                    navigationController?.setGraph(navigationController.graph, bundleOf())
                    showAboveSixMonthsInDelinquencyPopup(navigationController,getEligibilityPlan())
                }
                else -> {
                    window?.decorView?.fitsSystemWindows = true
                    Utils.updateStatusBarBackground(this@AccountSignedInActivity)
                    setAccountSixMonthInArrears(navigationController)
                    when(getEligibilityPlan()?.planType) {
                        ELITE_PLAN ->{showAboveSixMonthsInDelinquencyPopup(navigationController,getEligibilityPlan())}
                    }
                }
            }
        }
    }
    private fun showAboveSixMonthsInDelinquencyPopup(navigationController:NavController?, eligibilityPlan: EligibilityPlan?){
        val bundle = Bundle()
        bundle.putSerializable(
            ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN,
            eligibilityPlan
        )
        bundle.putSerializable(
            ViewTreatmentPlanDialogFragment.APPLY_NOW_STATE,
            payMyAccountViewModel.getApplyNowState()
        )
        navigationController?.navigate(
            R.id.removeBlockOnCollectionDialogFragment,
            bundle
        )
    }

    override fun bottomSheetIsExpanded(): Boolean {
        return sheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED
    }

    override fun chatToCollectionAgent(applyNowState: ApplyNowState, accountList: List<Account>?) {
        val chatToCollectionAgentView = ChatFloatingActionButtonBubbleView(
            this@AccountSignedInActivity,
            ChatBubbleVisibility(accountList, this@AccountSignedInActivity),
            binding.includeChatCollectAgentFloatingButton.chatBubbleFloatingButton,
            applyNowState,
            notificationBadge = binding.includeChatCollectAgentFloatingButton.badge,
            onlineChatImageViewIndicator = binding.includeChatCollectAgentFloatingButton.onlineIndicatorImageView,
            vocTriggerEvent = payMyAccountViewModel.getVocTriggerEventMyAccounts()
        )
        chatToCollectionAgentView.build()
    }

    @Throws(RuntimeException::class)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.accountInArrearsTextView -> mAccountSignedInPresenter?.getMyAccountCardInfo()
                ?.let { account ->
                    showAccountInArrearsDialog(account)
                    mAccountSignedInPresenter?.getAccount()
                }
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

    private fun showAccountInArrearsDialog(
        account: Pair<ApplyNowState, Account>, eligibilityPlan: EligibilityPlan? = null) {
        val accountApplyNowState = payMyAccountViewModel.getCardDetail()?.account
        if (accountApplyNowState == null)
            payMyAccountViewModel.setPMACardInfo(PMACardPopupModel(account = mAccountSignedInPresenter?.getMyAccountCardInfo()))
        val bundle = Bundle()
        bundle.putString(AccountSignedInPresenterImpl.MY_ACCOUNT_RESPONSE, Gson().toJson(account))
        bundle.putSerializable(
            ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN,
            eligibilityPlan
        )
        bundle.putSerializable(
            ViewTreatmentPlanDialogFragment.APPLY_NOW_STATE,
            payMyAccountViewModel.getApplyNowState()
        )
        mAvailableFundsNavHost?.navController?.navigate(R.id.accountInArrearsDialogFragment, bundle)
    }

    private fun transitionBottomSheetBackgroundColor(slideOffset: Float) {
        val colorFrom = ContextCompat.getColor(this, android.R.color.transparent)
        val colorTo = ContextCompat.getColor(this, R.color.black_99)
        binding.dimView?.setBackgroundColor(KotlinUtils.interpolateColor(slideOffset, colorFrom, colorTo))
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
                when (resultCode) {
                    BALANCE_PROTECTION_INSURANCE_OPT_IN_SUCCESS_RESULT_CODE -> {
                        isReloadCacheAccountDataEnabled = true
                        supportFragmentManager.fragments.apply {
                            if (this.isNotEmpty()) {
                                this[1].let {
                                    it.childFragmentManager.fragments.let { childFragments ->
                                        if (childFragments.isNotEmpty()) {
                                            childFragments[0].onActivityResult(
                                                requestCode,
                                                resultCode,
                                                data
                                            )
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

            REQUEST_ELITEPLAN -> {
                //elitePlanModel is the model extracted from callback url parameters
                if (extras?.containsKey(ELITE_PLAN_MODEL) == true) {
                    val elitePlanModel = extras.getParcelable(ELITE_PLAN_MODEL) as? Parcelable
                    ActivityIntentNavigationManager.presentPayMyAccountActivity(this, payMyAccountViewModel.getCardDetail(), PayMyAccountStartDestinationType.CREATE_USER, true,elitePlanModel)
                }else{
                    onTreatmentPlanStatusUpdateRequired()
                }
            }
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
        binding.availableFundFragmentFrameLayout?.visibility = GONE
        binding.bottomSheetBehaviourLinearLayout?.visibility = GONE
        binding.removeBlockOnCollectionCustomerFrameLayout?.visibility = VISIBLE
        val removeBlockOnCollectionFragmentContainerView =
            supportFragmentManager.findFragmentById(R.id.removeBlockOnCollectionFragmentContainerView) as? NavHostFragment
        val navigationController: NavController? =
            removeBlockOnCollectionFragmentContainerView?.navController
        mAccountSignedInPresenter?.apply {
            when (getMyAccountCardInfo()?.first) {
                ApplyNowState.STORE_CARD, ApplyNowState.PERSONAL_LOAN -> {
                    navigationController?.navigate(R.id.removeBlockDCFragment)
                }
                else -> {
                    val bundle = Bundle()
                    bundle.putString(
                        AccountSignedInPresenterImpl.MY_ACCOUNT_RESPONSE,
                        Gson().toJson(getSixMonthOutstandingTitleAndCardResource())
                    )
                    bundle.putBoolean(
                        AccountSixMonthArrearsFragment.IS_VIEW_TREATMENT_PLAN,
                        isViewTreatmentPlanActive
                    )
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
            if (eligibilityPlan?.actionText == ActionText.TAKE_UP_TREATMENT_PLAN.value) {
                fragment.showSetUpPaymentPlanButton(state, eligibilityPlan)
            } else if (eligibilityPlan?.actionText == ActionText.VIEW_TREATMENT_PLAN.value) {
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