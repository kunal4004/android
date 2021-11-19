package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.balance_protection_insurance_activity.*
import za.co.woolworths.financial.services.android.ui.extension.bindColor
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.overview_detail.BPIOverviewDetailFragmentArgs
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.BPIOverviewPresenter
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.BPIViewModel

class BalanceProtectionInsuranceActivity : AppCompatActivity() {

    private var bpiOptIn: Boolean = false
    private var bpiProductGroupCode: String? = null
    private var bpiPresenter: BPIOverviewPresenter? = null
    private val bpiViewModel: BPIViewModel? by viewModels()

    companion object {
        const val BPI_OPT_IN = "bpi_opt_in"
        const val BPI_PRODUCT_GROUP_CODE = "bpi_product_group_code"
        const val BPI_MORE_INFO_HTML = "bpi_more_info_html"
        const val BPI_TERMS_CONDITIONS_HTML = "bpi_terms_conditions_html"
    }

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.balance_protection_insurance_activity)

        actionBar()
        /*
        * Implementation of room db will eliminate bundle argument requirement by fetching account data directly from db
        */
            intent?.extras?.let { args ->
                bpiPresenter = bpiViewModel?.overviewPresenter(args)
                bpiOptIn = args.getBoolean(BPI_OPT_IN, false)
                bpiProductGroupCode = args.getString(BPI_PRODUCT_GROUP_CODE)
            }
        if(bpiOptIn){
            bpiPresenter?.createNavigationGraph(
                fragmentContainerView = supportFragmentManager.findFragmentById(R.id.fragment_container_view) as? NavHostFragment,
                navHostFragmentId = R.navigation.my_account_bpi_navhost,
                startDestination =  R.id.BPIOptInCarouselFragment,
                extras = bundleOf(BPI_PRODUCT_GROUP_CODE to bpiProductGroupCode))
        }
        else{
            val overviewPair = bpiPresenter?.navigateToOverviewDetail()
            val hasOnlyOneInsuranceTypeItem = overviewPair?.second ?: false
            bpiPresenter?.createNavigationGraph(
                fragmentContainerView = supportFragmentManager.findFragmentById(R.id.fragment_container_view) as? NavHostFragment,
                navHostFragmentId = R.navigation.my_account_bpi_navhost,
                startDestination =  if(hasOnlyOneInsuranceTypeItem) R.id.OverViewDetail else R.id.Overview,
                extras =  if(hasOnlyOneInsuranceTypeItem) BPIOverviewDetailFragmentArgs.Builder(overviewPair?.first).build().toBundle() else  intent.extras)
        }
    }

    fun actionBar() {
        setSupportActionBar(bpiToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
        bpiToolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        val backPressedFragment = bpiPresenter?.navigateToPreviousFragment()
        if (backPressedFragment == false) {
            super.onBackPressed()
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
        }
    }

    fun setToolbarTitle(@StringRes titleIdRes: Int) {
        toolbarTitleTextView?.text = bindString(titleIdRes)
    }

    fun setToolbarTitle(title: String) {
        toolbarTitleTextView?.text = title
    }

    fun changeActionBarUI(@ColorRes colorId: Int = R.color.white, isActionBarTitleVisible: Boolean = true) {
        if (isActionBarTitleVisible) {
            appbar?.setBackgroundColor(bindColor(colorId))
            horizontalDivider?.visibility = VISIBLE
            toolbarTitleTextView?.visibility = VISIBLE
            supportActionBar?.apply {
                setBackgroundDrawable(ColorDrawable(Color.WHITE))
                setHomeAsUpIndicator(R.drawable.back24)
            }
        } else {
            appbar?.setBackgroundColor(Color.TRANSPARENT)
            horizontalDivider?.visibility = GONE
            toolbarTitleTextView?.visibility = GONE
            supportActionBar?.apply {
                setHomeAsUpIndicator(R.drawable.back_white)
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }
        supportActionBar?.elevation = 0f
    }

    fun changeActionBarUIForBPIOptIn() {
        appbar?.setBackgroundColor(Color.TRANSPARENT)
        horizontalDivider?.visibility = GONE
        toolbarTitleTextView?.visibility = GONE
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.back24)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        supportActionBar?.elevation = 0f
    }
}