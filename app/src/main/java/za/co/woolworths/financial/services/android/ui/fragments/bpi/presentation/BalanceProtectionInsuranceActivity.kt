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
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.balance_protection_insurance_activity.*
import za.co.woolworths.financial.services.android.ui.extension.bindColor
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.BPIOverviewPresenter
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.BPIViewModel

class BalanceProtectionInsuranceActivity : AppCompatActivity() {

    private var bpiPresenter: BPIOverviewPresenter? = null
    private val bpiViewModel: BPIViewModel? by viewModels()

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.balance_protection_insurance_activity)

        actionBar()
        /*
        * Implementation of room db will eliminate bundle argument requirement by fetching account data directly from db
        */
        intent?.extras?.let { args -> bpiPresenter = bpiViewModel?.overviewPresenter(args) }
        bpiPresenter?.createNavigationGraph(
            fragmentContainerView = supportFragmentManager.findFragmentById(R.id.fragment_container_view) as? NavHostFragment,
            navHostFragmentId = R.navigation.my_account_bpi_navhost,
            startDestination = R.id.Overview,
            extras = intent.extras
        )

    }

    fun actionBar() {
        setSupportActionBar(bpiToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
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
}