package za.co.woolworths.financial.services.android.ui.activities.cli

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.cli_phase2_activity.*
import za.co.woolworths.financial.services.android.analytic.FirebaseCreditLimitIncreaseEvent
import za.co.woolworths.financial.services.android.contracts.ICreditLimitDecrease
import za.co.woolworths.financial.services.android.contracts.IEditAmountSlider
import za.co.woolworths.financial.services.android.contracts.MaritalStatusListener
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.MaritalStatus
import za.co.woolworths.financial.services.android.models.dto.OfferActive
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.service.event.BusStation
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.cli.*
import za.co.woolworths.financial.services.android.util.DeclineOfferInterface
import za.co.woolworths.financial.services.android.util.FragmentUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.controller.CLIStepIndicatorListener
import za.co.woolworths.financial.services.android.util.controller.EventStatus
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController

class CLIPhase2Activity : AppCompatActivity(), View.OnClickListener, ICreditLimitDecrease, DeclineOfferInterface, IEditAmountSlider, MaritalStatusListener {

    private var mFirebaseCreditLimitIncreaseEvent: FirebaseCreditLimitIncreaseEvent? = null
    private var maritalStatus: MaritalStatus? = null
    private var mCLICreateOfferResponse: OfferActive? = null
    private var mOfferActivePayload: String? = null
    private var mOfferActive = false
    private var mCloseButtonEnabled = false
    private var mNextStep: String? = null
    var selectedMaritalStatusPosition : Int? = null
    var applyNowState: ApplyNowState? = ApplyNowState.STORE_CARD
    var eventStatus = EventStatus.NONE

    companion object {
        const val MARITAL_STATUS = "MARITAL_STATUS"
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cli_phase2_activity)
        Utils.updateStatusBarBackground(this)
        hideDeclineOffer()
        actionBar()
        listener()

        intent?.extras?.apply {
            mOfferActivePayload = getString("OFFER_ACTIVE_PAYLOAD")
            mOfferActive = getBoolean("OFFER_IS_ACTIVE")
            applyNowState = getSerializable(AccountSignedInPresenterImpl.APPLY_NOW_STATE) as? ApplyNowState
            mCLICreateOfferResponse = offerActiveObject()
            mFirebaseCreditLimitIncreaseEvent = FirebaseCreditLimitIncreaseEvent(applyNowState,this@CLIPhase2Activity)
            mCLICreateOfferResponse?.apply {
                mNextStep = nextStep
                loadFragment(mNextStep)
            }
        }
    }

    private fun listener() {
        tvDeclineOffer?.setOnClickListener(this)
        imBack?.setOnClickListener(this)
    }

    private fun actionBar() {
        mToolbar?.let { toolbar -> setSupportActionBar(toolbar) }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
        }
    }

    private fun loadFragment(nextStep: String?) {
        when (nextStep) {
            bindString(R.string.status_consents) -> {
                showView(imBack)
                openNextFragment(CLIEligibilityAndPermissionFragment())
            }
            bindString(R.string.status_poi_problem) -> {
                hideBurgerButton()
                hideView(imBack)
                openNextFragment(CLIPOIProblemFragment())
            }

            else -> {
                if (nextStep == bindString(R.string.status_i_n_e) && mOfferActive) {
                    openNextFragment(CLIMaritalStatusFragment.newInstance())
                    return
                }
                showView(imBack)
                moveToCLIAllStepsContainerFragment()
            }
        }
    }

    fun initFragment(cliStepIndicatorListener: CLIStepIndicatorListener?) {
        val nextStep = mNextStep
        val offerActive = mOfferActive
        val increaseLimitController = IncreaseLimitController(this@CLIPhase2Activity)
        val offerBundle = Bundle()
        if (nextStep.equals(bindString(R.string.status_consents), ignoreCase = true)) {
            val cLIEligibilityAndPermissionFragment = SupplyIncomeFragment()
            cLIEligibilityAndPermissionFragment.setStepIndicatorListener(cliStepIndicatorListener)
            eventStatus = EventStatus.CREATE_APPLICATION
            openFragment(cLIEligibilityAndPermissionFragment)
            return
        }
        if (nextStep.equals(bindString(R.string.status_i_n_e), ignoreCase = true) && offerActive) {
            val incomeHashMap = increaseLimitController.incomeHashMap(mCLICreateOfferResponse)
            val expenseHashMap = increaseLimitController.expenseHashMap(mCLICreateOfferResponse)
            val supplyIncomeDetailFragment = SupplyIncomeFragment()
            offerBundle.putSerializable(IncreaseLimitController.INCOME_DETAILS, incomeHashMap)
            offerBundle.putSerializable(IncreaseLimitController.EXPENSE_DETAILS, expenseHashMap)
            supplyIncomeDetailFragment.setStepIndicatorListener(cliStepIndicatorListener)
            supplyIncomeDetailFragment.arguments = offerBundle
            eventStatus = EventStatus.UPDATE_APPLICATION
            openFragment(supplyIncomeDetailFragment)
            return
        }
        if (nextStep.equals(bindString(R.string.status_i_n_e), ignoreCase = true) && !offerActive) {
            val supplyIncomeDetailFragment = SupplyIncomeFragment()
            supplyIncomeDetailFragment.setStepIndicatorListener(cliStepIndicatorListener)
            eventStatus = EventStatus.CREATE_APPLICATION
            openFragment(supplyIncomeDetailFragment)
            return
        }
        if (nextStep.equals(bindString(R.string.status_offer), ignoreCase = true)) {
            val icomeHashMap = increaseLimitController.incomeHashMap(mCLICreateOfferResponse)
            val expenseHashMap = increaseLimitController.expenseHashMap(mCLICreateOfferResponse)
            offerBundle.putSerializable(IncreaseLimitController.INCOME_DETAILS, icomeHashMap)
            offerBundle.putSerializable(IncreaseLimitController.EXPENSE_DETAILS, expenseHashMap)
            offerBundle.putSerializable(MARITAL_STATUS, maritalStatus)
            val offerCalculationFragment = OfferCalculationFragment()
            offerCalculationFragment.setStepIndicatorListener(cliStepIndicatorListener)
            offerCalculationFragment.arguments = offerBundle
            eventStatus = EventStatus.NONE
            openFragment(offerCalculationFragment)
            return
        }
        if (nextStep.equals(bindString(R.string.status_poi_required), ignoreCase = true)) {
            val bundle = Bundle()
            bundle.putString("OFFER_ACTIVE_PAYLOAD", mOfferActivePayload)
            val documentFragment = DocumentFragment()
            documentFragment.arguments = bundle
            documentFragment.setStepIndicatorListener(cliStepIndicatorListener)
            openFragment(documentFragment)
            return
        }
        if (nextStep.equals(bindString(R.string.status_complete), ignoreCase = true)) {
            val processCompleteNoPOIFragment = ProcessCompleteNoPOIFragment()
            processCompleteNoPOIFragment.setStepIndicatorListener(cliStepIndicatorListener)
            openFragment(processCompleteNoPOIFragment)
            hideDeclineOffer()
            return
        }
    }

    override fun onBackPressed() {
        onBack()
    }

    private fun onBack() {
        this@CLIPhase2Activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        hideSoftKeyboard()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
        if (closeButtonEnabled()) {
            finishActivity()
        } else {
            supportFragmentManager.apply {
                when{
                    (backStackEntryCount > 0) -> popBackStack()
                    else -> finishActivity()
                }
            }
        }
    }

    fun actionBarCloseIcon() {
        setCloseButtonEnabled(true)
        imBack?.setImageResource(R.drawable.close_24)
    }

    fun performClicked() {
        imBack?.performClick()
    }

    fun actionBarBackIcon() {
        setCloseButtonEnabled(false)
        imBack?.setImageResource(R.drawable.back24)
    }

    fun showDeclineOffer() {
        tvDeclineOffer?.visibility = View.VISIBLE
        pbDecline?.visibility = View.GONE
        tvDeclineOffer?.alpha = 1.0f
        pbDecline?.alpha = 1.0f
    }

    fun hideDeclineOffer() {
        tvDeclineOffer?.visibility = View.GONE
        pbDecline?.visibility = View.GONE
        tvDeclineOffer?.alpha = 0.0f
        pbDecline?.alpha = 0.0f
    }

    fun disableDeclineButton() {
        tvDeclineOffer!!.alpha = 0.5f
        tvDeclineOffer!!.isEnabled = false
    }

    fun enableDeclineButton() {
        tvDeclineOffer?.alpha = 1.0f
        tvDeclineOffer?.isEnabled = true
    }

    private fun showDeclineProgressBar() {
        pbDecline?.visibility = View.VISIBLE
        tvDeclineOffer?.visibility = View.GONE
        pbDecline?.indeterminateDrawable?.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)
    }

    fun offerActiveObject(): OfferActive {
        return Utils.strToJson(mOfferActivePayload, OfferActive::class.java) as OfferActive
    }

    private fun openNextFragment(fragment: Fragment) {
        FragmentUtils().currentFragment(this@CLIPhase2Activity, fragment, R.id.cliMainFrame)
    }

    private fun openFragment(fragment: Fragment) {
        FragmentUtils().currentFragment(this@CLIPhase2Activity, supportFragmentManager, fragment, R.id.cli_steps_container)
    }

    private fun moveToCLIAllStepsContainerFragment() {
        openNextFragment(CLIAllStepsContainerFragment())
    }

    private fun closeButtonEnabled(): Boolean {
        return mCloseButtonEnabled
    }

    private fun setCloseButtonEnabled(mCloseButtonEnabled: Boolean) {
        this.mCloseButtonEnabled = mCloseButtonEnabled
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBack()
                return true
            }
        }
        return false
    }

    fun finishActivity() {
        val mainFragmentContainer = supportFragmentManager.findFragmentById(R.id.cliMainFrame)
        if (mainFragmentContainer is CLIAllStepsContainerFragment) {
            if (closeButtonEnabled()) {
                (this@CLIPhase2Activity.application as? WoolworthsApplication)
                        ?.bus()
                        ?.send(BusStation(true))
            }
        }
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvDeclineOffer -> Utils.displayValidationMessage(this@CLIPhase2Activity, CustomPopUpWindow.MODAL_LAYOUT.CLI_DANGER_ACTION_MESSAGE_VALIDATION, "")
            R.id.imBack -> onBack()
            else -> {
            }
        }
    }

    private fun onDeclineLoad() {
        runOnUiThread { showDeclineProgressBar() }
    }

    private fun onDeclineComplete() {
        runOnUiThread { showDeclineOffer() }
    }

    fun hideBurgerButton() {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun hideSoftKeyboard() {
        currentFocus?.windowToken?.let { (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(it, 0) }
    }

    fun showView(v: View?) {
        v?.visibility = View.VISIBLE
    }

    fun hideView(v: View?) {
        v?.visibility = View.GONE
    }

    fun hideCloseIcon() {
        imBack?.visibility = View.INVISIBLE
    }

    override fun onLoad() {
        onDeclineLoad()
    }

    override fun onLoadComplete() {
        onDeclineComplete()
    }

    override fun onCreditDecreaseProceedWithMaximum() {
        val offerCalculationFragment = supportFragmentManager.findFragmentById(R.id.cli_steps_container) as? OfferCalculationFragment
        offerCalculationFragment?.animSeekBarToMaximum()
    }

    override fun slideAmount(amount: Int?, drawnDownAmount: Int?) {
        supportFragmentManager.popBackStack(EditSlideAmountFragment::class.java.simpleName, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        (application as? WoolworthsApplication)?.bus()?.send(amount?.let { amt -> drawnDownAmount?.let { drawnAmount -> BusStation(amt, drawnAmount) } })
    }

    override fun setMaritalStatus(maritalStatus: MaritalStatus) {
        this.maritalStatus = maritalStatus
    }

    override fun getMaritalStatus(): MaritalStatus {
        return maritalStatus ?: MaritalStatus(0, bindString(R.string.please_select))
    }

    fun getFirebaseEvent() = mFirebaseCreditLimitIncreaseEvent
}