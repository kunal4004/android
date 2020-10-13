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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import kotlinx.android.synthetic.main.account_in_arrears_layout.*
import kotlinx.android.synthetic.main.account_signed_in_activity.*
import kotlinx.android.synthetic.main.chat_collect_agent_floating_button_layout.*
import za.co.woolworths.financial.services.android.contracts.IAccountSignedInContract
import za.co.woolworths.financial.services.android.contracts.IBottomSheetBehaviourPeekHeightListener
import za.co.woolworths.financial.services.android.contracts.IShowChatBubble
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.AccountHelpInformation
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.information.CardInformationHelpActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatFloatingActionButtonBubbleView
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatBubbleVisibility
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class AccountSignedInActivity : AppCompatActivity(), IAccountSignedInContract.MyAccountView, IBottomSheetBehaviourPeekHeightListener, View.OnClickListener, IShowChatBubble {

    companion object {
        const val ABSA_ONLINE_BANKING_REGISTRATION_REQUEST_CODE = 2111
        const val REQUEST_CODE_BLOCK_MY_STORE_CARD = 3021
        const val REQUEST_CODE_ACCOUNT_INFORMATION = 2112
    }

    private var mPeekHeight: Int = 0
    var mAccountSignedInPresenter: AccountSignedInPresenterImpl? = null
    private var sheetBehavior: BottomSheetBehavior<*>? = null
    private var mAccountHelpInformation: MutableList<AccountHelpInformation>? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.account_signed_in_activity)
        KotlinUtils.setTransparentStatusBar(this)
        mAccountSignedInPresenter = AccountSignedInPresenterImpl(this, AccountSignedInModelImpl())
        mAccountSignedInPresenter?.apply {
            intent?.extras?.let { bundle -> getAccountBundle(bundle) }

            val availableFundsNavHost = supportFragmentManager.findFragmentById(R.id.nav_host_available_fund_fragment) as? NavHostFragment
            val accountOptionsNavHost = supportFragmentManager.findFragmentById(R.id.nav_host_overlay_bottom_sheet_fragment) as? NavHostFragment

            setAvailableFundBundleInfo(availableFundsNavHost?.navController)
            setAccountCardDetailInfo(accountOptionsNavHost?.navController)
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
        params?.topMargin = KotlinUtils.getStatusBarHeight(this)
        bar?.layoutParams = params
    }

    private fun configureBottomSheetDialog() {
        val bottomSheetBehaviourLinearLayout =
                findViewById<LinearLayout>(R.id.bottomSheetBehaviourLinearLayout)
        val layoutParams = bottomSheetBehaviourLinearLayout?.layoutParams
        layoutParams?.height =
                mAccountSignedInPresenter?.bottomSheetBehaviourHeight(this@AccountSignedInActivity)
        bottomSheetBehaviourLinearLayout?.requestLayout()
        sheetBehavior = BottomSheetBehavior.from(bottomSheetBehaviourLinearLayout)
        sheetBehavior?.peekHeight =
                mAccountSignedInPresenter?.bottomSheetBehaviourPeekHeight(this@AccountSignedInActivity)
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
        mAccountSignedInPresenter?.getMyAccountCardInfo()?.let { accountKeyPair -> showAccountInArrearsDialog(accountKeyPair) }
    }

    override fun hideAccountInArrears(account: Account) {
        toolbarTitleTextView?.visibility = VISIBLE
        accountInArrearsTextView?.visibility = GONE
    }

    override fun showAccountHelp(informationModelAccount: MutableList<AccountHelpInformation>) {
        this.mAccountHelpInformation = informationModelAccount
    }

    override fun showAccountChargeOffForMoreThan6Months() {
        window?.decorView?.fitsSystemWindows = true
        Utils.updateStatusBarBackground(this)
        frameLayout?.visibility = GONE
        bottomSheetBehaviourLinearLayout?.visibility = GONE
        sixMonthArrearsFrameLayout?.visibility = VISIBLE
        val accountSixMonthInArrearsNavHost = supportFragmentManager.findFragmentById(R.id.six_month_arrears_nav_host) as? NavHostFragment
        mAccountSignedInPresenter?.setAccountSixMonthInArrears(accountSixMonthInArrearsNavHost?.navController)
    }

    override fun bottomSheetIsExpanded(): Boolean {
        return sheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED
    }

    override fun chatToCollectionAgent(applyNowState: ApplyNowState, accountList: List<Account>?) {
        val chatToCollectionAgentView = ChatFloatingActionButtonBubbleView(this@AccountSignedInActivity, ChatBubbleVisibility(accountList, this@AccountSignedInActivity), chatBubbleFloatingButton, applyNowState)
        chatToCollectionAgentView.build()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.accountInArrearsTextView -> mAccountSignedInPresenter?.getMyAccountCardInfo()?.let { account -> showAccountInArrearsDialog(account) }
            R.id.infoIconImageView -> navigateToCardInformation()
            R.id.navigateBackImageButton -> onBackPressed()
            else -> throw RuntimeException("Unexpected onClick Id found ${v?.id}")
        }
    }

    private fun navigateToCardInformation() {
        val cardInformationHelpActivity = Intent(this, CardInformationHelpActivity::class.java)
        cardInformationHelpActivity.putExtra(CardInformationHelpActivity.HELP_INFORMATION, Gson().toJson(mAccountHelpInformation))
        startActivityForResult(cardInformationHelpActivity, REQUEST_CODE_ACCOUNT_INFORMATION)
        overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
    }

    private fun showAccountInArrearsDialog(account: Pair<ApplyNowState, Account>) {
        val bundle = Bundle()
        bundle.putString(AccountSignedInPresenterImpl.MY_ACCOUNT_RESPONSE, Gson().toJson(account))
        val availableFundsNavHost = supportFragmentManager.findFragmentById(R.id.nav_host_available_fund_fragment) as? NavHostFragment

        availableFundsNavHost?.navController?.navigate(R.id.accountInArrearsFragmentDialog, bundle)
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
        when (requestCode) {
            REQUEST_CODE_ACCOUNT_INFORMATION -> sheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
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

    override fun showChatBubble() {
        showChatToCollectionAgent()
    }
}