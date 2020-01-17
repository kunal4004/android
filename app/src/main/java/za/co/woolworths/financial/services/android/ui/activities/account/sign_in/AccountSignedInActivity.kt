package za.co.woolworths.financial.services.android.ui.activities.account.sign_in

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.awfs.coordination.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import kotlinx.android.synthetic.main.account_in_arrears_layout.*
import kotlinx.android.synthetic.main.account_signed_in_activity.*
import za.co.woolworths.financial.services.android.contracts.AccountSignedInContract
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.AccountHelpInformation
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.information.CardInformationHelpActivity
import za.co.woolworths.financial.services.android.util.KotlinUtils


class AccountSignedInActivity : AppCompatActivity(), AccountSignedInContract.MyAccountView, View.OnClickListener {

    private var mAccountSignedInPresenter: AccountSignedInPresenterImpl? = null
    private var sheetBehavior: BottomSheetBehavior<*>? = null
    private var mAccountHelpInformation: MutableList<AccountHelpInformation>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_signed_in_activity)
        KotlinUtils.setTransparentStatusBar(this)
        mAccountSignedInPresenter = AccountSignedInPresenterImpl(this, AccountSignedInModelImpl())
        mAccountSignedInPresenter?.apply {
            intent?.extras?.let { bundle -> getAccountBundle(bundle) }
            setAvailableFundBundleInfo(findNavController(R.id.nav_host_available_fund_fragment))
            setAccountCardDetailInfo(findNavController(R.id.nav_host_overlay_bottom_sheet_fragment))
            //requestGetStoreCardFromServer()
            setToolbarTopMargin()
        }

        accountInArrearsTextView?.setOnClickListener(this)
        infoIconImageView?.setOnClickListener(this)
        navigateBackImageButton?.setOnClickListener(this)
    }

    private fun setToolbarTopMargin() {
        val bar = findViewById<Toolbar>(R.id.toolbarContainer)
        val params = bar?.layoutParams as? ViewGroup.MarginLayoutParams
        params?.topMargin = KotlinUtils.getStatusBarHeight(this)
        bar?.layoutParams = params

        setUpBottomSheetDialog()
    }

    private fun setUpBottomSheetDialog() {
        val bottomSheetLayout = findViewById<LinearLayout>(R.id.bottomSheetLayout)
        val maximumExpandedHeight = mAccountSignedInPresenter?.maximumExpandableHeight(0f, toolbarContainer) ?: 0
        bottomSheetLayout?.setPadding(0, maximumExpandedHeight, 0, 0)

        sheetBehavior = BottomSheetBehavior.from<LinearLayout>(bottomSheetLayout)
        val overlayAnchoredHeight = mAccountSignedInPresenter?.getOverlayAnchoredHeight()?.plus(maximumExpandedHeight)
        sheetBehavior?.peekHeight = overlayAnchoredHeight ?: 0
        sheetBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                transitionBottomSheetBackgroundColor(slideOffset)
                navigateBackImageButton?.rotation = slideOffset * -90
            }
        })
    }

    override fun onBackPressed() {
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
        showAccountInArrearsDialog()
    }

    override fun hideAccountInArrears(account: Account) {
        toolbarTitleTextView?.visibility = VISIBLE
        accountInArrearsTextView?.visibility = GONE
    }

    override fun showAccountHelp(informationModelAccount: MutableList<AccountHelpInformation>) {
        this.mAccountHelpInformation = informationModelAccount
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.accountInArrearsTextView -> showAccountInArrearsDialog()
            R.id.infoIconImageView -> navigateToCardInformation()
            R.id.navigateBackImageButton -> onBackPressed()
            else -> throw RuntimeException("Unexpected onClick Id found ${v?.id}")
        }
    }

    private fun navigateToCardInformation() {
        val cardInformationHelpActivity = Intent(this, CardInformationHelpActivity::class.java)
        cardInformationHelpActivity.putExtra(CardInformationHelpActivity.HELP_INFORMATION, Gson().toJson(mAccountHelpInformation))
        startActivity(cardInformationHelpActivity)
        overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
    }

    private fun showAccountInArrearsDialog() {
        findNavController(R.id.nav_host_available_fund_fragment).navigate(R.id.accountInArrearsFragmentDialog)
    }

    private fun transitionBottomSheetBackgroundColor(slideOffset: Float) {
        val colorFrom = ContextCompat.getColor(this, android.R.color.transparent)
        val colorTo = ContextCompat.getColor(this, R.color.black_99)
        dimView?.setBackgroundColor(KotlinUtils.interpolateColor(slideOffset, colorFrom, colorTo))
    }
}