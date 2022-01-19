package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.view_get_payment_plan_activity.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.ProductGroupCode
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils

class GetAPaymentPlanActivity : AppCompatActivity(), View.OnClickListener {
    private var eligibilityPlan: EligibilityPlan? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_get_payment_plan_activity)

        //TODO: Dimitri : update R.layout.view_get_payment_plan_activity

        viewPlanOptionsButton?.setOnClickListener(this)

        eligibilityPlan = intent.getSerializableExtra(ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN) as EligibilityPlan?
    }

        override fun onClick(v: View?) {
            when(v?.id){
            R.id.viewPlanOptionsButton -> {

                var collectionsUrl: String? = ""
                var exitUrl: String? = ""

                when(eligibilityPlan?.productGroupCode){
                    ProductGroupCode.SC -> {
                        collectionsUrl = WoolworthsApplication.getAccountOptions()?.collectionsStartNewPlanJourney?.storeCard?.collectionsUrl
                        exitUrl = WoolworthsApplication.getAccountOptions()?.showTreatmentPlanJourney?.storeCard?.exitUrl
                    }

                    ProductGroupCode.PL -> {
                        collectionsUrl = WoolworthsApplication.getAccountOptions()?.collectionsStartNewPlanJourney?.storeCard?.collectionsUrl
                        exitUrl = WoolworthsApplication.getAccountOptions()?.showTreatmentPlanJourney?.personalLoan?.exitUrl
                    }

                    ProductGroupCode.CC -> {
                        collectionsUrl = WoolworthsApplication.getAccountOptions()?.collectionsStartNewPlanJourney?.storeCard?.collectionsUrl
                        exitUrl = WoolworthsApplication.getAccountOptions()?.collectionsStartNewPlanJourney?.creditCard?.exitUrl
                    }
                }

                val url = collectionsUrl + eligibilityPlan?.appGuid

                when (WoolworthsApplication.getAccountOptions()?.collectionsStartNewPlanJourney?.renderMode){
                    AvailableFundFragment.NATIVE_BROWSER ->
                        KotlinUtils.openUrlInPhoneBrowser(url, this)

                    else ->
                        KotlinUtils.openLinkInInternalWebView(this,
                            url,
                            true,
                            exitUrl
                        )
                }
            }
        }
    }
}