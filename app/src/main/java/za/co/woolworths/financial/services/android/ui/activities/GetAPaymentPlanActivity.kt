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
    private var c2id: String? = null
    private var functionCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_get_payment_plan_activity)

        viewPlanOptionsButton?.setOnClickListener(this)

        eligibilityPlan = intent.getSerializableExtra(ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN) as EligibilityPlan?

        functionCode = when(eligibilityPlan?.actionText) {
            ActionText.TAKE_UP_TREATMENT_PLAN.value -> "TmV3UGxhbg=="
            ActionText.VIEW_TREATMENT_PLAN.value -> "RXhpc3RpbmdQbGFu"
            else -> null
        }

        val splitToken = OneAppService.getSessionToken().split(".")
        if(splitToken.size > 1){
            val decodedBytes = Base64.decode(splitToken[1])
            c2id = Base64.encode((JsonParser.parseString(String(decodedBytes)).asJsonObject["C2Id"].asString).toByteArray())
        }
    }

        override fun onClick(v: View?) {
            when(v?.id){
            R.id.viewPlanOptionsButton -> {
                //TODO: Take up treatment plan - do not use hardcoded url

                val product: String? = when (eligibilityPlan?.productGroupCode?.value) {
                    ProductGroupCode.CC.value -> "CreditCard"
                    ProductGroupCode.PL.value -> "PersonalLoan"
                    ProductGroupCode.SC.value -> "StoreCard"
                    else -> null
                }

                val url = "https://dev.woolworths.wfs.co.za/CustomerCollections/interauth?" +
                        "Token=" + eligibilityPlan?.appGuid + "&" +
                        "Product=" + product + "&" +
                        "C2ID=" + c2id + "&" +
                        "Function=" + functionCode

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