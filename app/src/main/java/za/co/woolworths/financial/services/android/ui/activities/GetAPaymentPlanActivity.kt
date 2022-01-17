package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.google.gson.JsonParser
import com.huawei.hms.support.log.common.Base64
import kotlinx.android.synthetic.main.view_get_payment_plan_activity.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ActionText
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.ProductGroupCode
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils

class GetAPaymentPlanActivity : AppCompatActivity(), View.OnClickListener {
    private var eligibilityPlan: EligibilityPlan? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_get_payment_plan_activity)

        viewPlanOptionsButton?.setOnClickListener(this)

        eligibilityPlan = intent.getSerializableExtra(ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN) as EligibilityPlan?
    }

        override fun onClick(v: View?) {
            when(v?.id){
            R.id.viewPlanOptionsButton -> {
                //TODO: Take up treatment plan - do not use hardcoded url
                val url = "https://dev.woolworths.wfs.co.za/IntegrationLanding/Entry.aspx?appguid=" + eligibilityPlan?.appGuid

                when (WoolworthsApplication.getAccountOptions()?.takeUpTreatmentPlanJourney?.renderMode){
                    AvailableFundFragment.NATIVE_BROWSER ->
                        KotlinUtils.openUrlInPhoneBrowser(url, this)

                    else ->
                        KotlinUtils.openLinkInInternalWebView(this,
                            url,
                            true,
                            WoolworthsApplication.getAccountOptions()?.takeUpTreatmentPlanJourney?.storeCard?.exitUrl
                        )
                }
            }
        }
    }
}