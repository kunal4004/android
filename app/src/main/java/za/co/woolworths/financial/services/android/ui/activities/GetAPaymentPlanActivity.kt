package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.balance_protection_insurance_activity.*
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment

class GetAPaymentPlanActivity : AppCompatActivity(){

    private var eligibilityPlan: EligibilityPlan? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_get_payment_plan_activity)
        actionBar()
        eligibilityPlan = intent.getSerializableExtra(ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN) as? EligibilityPlan

        //Setup the navGraph for this activity
        val myNavHostFragment: NavHostFragment? = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as? NavHostFragment
        myNavHostFragment?.navController?.setGraph(R.navigation.nav_wfs_get_treatement_plan, intent.extras)
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

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(menuItem)
    }
}