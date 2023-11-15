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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.BalanceProtectionInsuranceActivityBinding
import za.co.woolworths.financial.services.android.ui.extension.bindColor
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in.otp.BPIProcessingRequestFragment
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in.otp.BpiEnterOtpFragment
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.overview_detail.BPIOverviewDetailFragmentArgs
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.BPIOverviewPresenter
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.BPIViewModel

class BalanceProtectionInsuranceActivity : AppCompatActivity() {

    lateinit var binding: BalanceProtectionInsuranceActivityBinding
    private var bpiOptIn: Boolean = false
    private var bpiProductGroupCode: String? = null
    private var bpiPresenter: BPIOverviewPresenter? = null
    private val bpiViewModel: BPIViewModel? by viewModels()

    companion object {
        const val BPI_OPT_IN = "bpi_opt_in"
        const val BPI_PRODUCT_GROUP_CODE = "bpi_product_group_code"
        const val ACCOUNT_RESPONSE = "ACCOUNT_RESPONSE"
    }

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BalanceProtectionInsuranceActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bpiViewModel?.setAccount(intent?.extras)
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
        } else {
            val overviewPair = bpiPresenter?.navigateToOverviewDetail()
            val hasOnlyOneInsuranceTypeItem = overviewPair?.second ?: false
            bpiPresenter?.createNavigationGraph(
                fragmentContainerView = supportFragmentManager.findFragmentById(R.id.fragment_container_view) as? NavHostFragment,
                navHostFragmentId = R.navigation.my_account_bpi_navhost,
                startDestination =  if(hasOnlyOneInsuranceTypeItem && overviewPair?.first != null) R.id.OverViewDetail else R.id.Overview,
                extras =  if(hasOnlyOneInsuranceTypeItem && overviewPair?.first != null) BPIOverviewDetailFragmentArgs.Builder(overviewPair?.first).build().toBundle() else  intent.extras)
        }
    }

    fun actionBar() {
        setSupportActionBar(binding.bpiToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
        binding.bpiToolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onBackPressed() {
        // disable onBackPressed for BPIProcessingRequestFragment scenario's
        if (supportFragmentManager.fragments.first()?.findNavController()?.backStack?.last?.destination?.label
            == BPIProcessingRequestFragment::class.java.simpleName) {
            return
        }


        var backPressedFragment = bpiPresenter?.navigateToPreviousFragment()

        if(BpiEnterOtpFragment.shouldBackPressed){
            BpiEnterOtpFragment.shouldBackPressed = false
            backPressedFragment = bpiPresenter?.navigateToPreviousFragment()
        }

        if (backPressedFragment == false) {
            super.onBackPressed()
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
        }
    }

    fun setToolbarTitle(@StringRes titleIdRes: Int) {
        binding.toolbarTitleTextView?.text = bindString(titleIdRes)
    }

    fun setToolbarTitle(title: String) {
        binding.toolbarTitleTextView?.text = title
    }

    fun changeActionBarUI(@ColorRes colorId: Int = R.color.white, isActionBarTitleVisible: Boolean = true) {
        with(binding) {
            if (isActionBarTitleVisible) {
                appbar?.setBackgroundColor(bindColor(colorId))
                horizontalDivider?.visibility = VISIBLE
                toolbarTitleTextView?.visibility = VISIBLE
                btnClose?.visibility = GONE
                supportActionBar?.apply {
                    setDisplayHomeAsUpEnabled(true)
                    setBackgroundDrawable(ColorDrawable(Color.WHITE))
                    setHomeAsUpIndicator(R.drawable.back24)
                }
            } else {
                appbar?.setBackgroundColor(Color.TRANSPARENT)
                horizontalDivider?.visibility = GONE
                toolbarTitleTextView?.visibility = GONE
                btnClose?.visibility = GONE
                supportActionBar?.apply {
                    setDisplayHomeAsUpEnabled(true)
                    setHomeAsUpIndicator(R.drawable.back_white)
                    setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
            }
            supportActionBar?.elevation = 0f
        }
    }

    fun changeActionBarUIForBPIOptIn() {
        with(binding) {
            appbar?.setBackgroundColor(Color.TRANSPARENT)
            horizontalDivider?.visibility = GONE
            toolbarTitleTextView?.visibility = GONE
            btnClose?.visibility = GONE
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.back24)
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            supportActionBar?.elevation = 0f
        }
    }

    fun changeActionBarUIForBPITermsConditions() {
        with(binding) {
            appbar?.setBackgroundColor(Color.WHITE)
            horizontalDivider?.visibility = GONE
            toolbarTitleTextView?.visibility = VISIBLE
            btnClose?.visibility = VISIBLE
            btnClose?.setOnClickListener {
                bpiPresenter?.navigateToPreviousFragment()
            }
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(false)
            }
            supportActionBar?.elevation = 0f
        }
    }

    fun hideDisplayHomeAsUpEnabled(){
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    fun showDisplayHomeAsUpEnabled(){
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    val currentFragment: Fragment?
        get() = (supportFragmentManager.fragments.first()
                as? NavHostFragment)?.childFragmentManager?.findFragmentById(R.id.bpi_navigation)

}