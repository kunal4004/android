package za.co.woolworths.financial.services.android.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ViewGetPaymentPlanActivityBinding
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils

class GetAPaymentPlanActivity : AppCompatActivity(){

    private lateinit var binding: ViewGetPaymentPlanActivityBinding
    private var eligibilityPlan: EligibilityPlan? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewGetPaymentPlanActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        actionBar()
        eligibilityPlan = intent.getSerializableExtra(ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN) as? EligibilityPlan

        //Setup the navGraph for this activity
        val myNavHostFragment: NavHostFragment? = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as? NavHostFragment
        myNavHostFragment?.navController?.setGraph(R.navigation.nav_wfs_get_treatement_plan, intent.extras)
    }

    fun actionBar() {
        setSupportActionBar(binding.bpiToolbar)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == KotlinUtils.RESULT_CODE_CLOSE_VIEW){
            when (resultCode){
                    RESULT_OK -> {
                        setResult(RESULT_OK,data)
                        finish()
                        overridePendingTransition(0,0)}
                    else -> Unit

            }
        }
    }
}