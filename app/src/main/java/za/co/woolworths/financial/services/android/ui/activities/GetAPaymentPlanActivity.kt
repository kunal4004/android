package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.google.gson.JsonParser
import com.huawei.hms.support.log.common.Base64
import kotlinx.android.synthetic.main.view_get_payment_plan_activity.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils

class GetAPaymentPlanActivity : AppCompatActivity(), View.OnClickListener {
    private var takeUpIntegrationJwt: String? = null
    private var takeUpProduct: String? = null
    private var c2id: String? = null
    private var takeUpFunction: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_get_payment_plan_activity)

        viewPlanOptionsButton?.setOnClickListener(this)

        takeUpIntegrationJwt = intent.getStringExtra(ViewTreatmentPlanDialogFragment.ELIGIBILITY_INTEGRATION_JWT)
        takeUpProduct = intent.getStringExtra(ViewTreatmentPlanDialogFragment.TAKE_UP_PRODUCT)
        //TODO: "TmV3UGxhbg==" get from configs
        takeUpFunction = "TmV3UGxhbg==" // NewPlan

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
                val url = "https://dev.woolworths.wfs.co.za/CustomerCollections/interauth?" +
                        "Token=" +takeUpIntegrationJwt + "&" +
                        "Product=" + takeUpProduct + "&" +
                        "C2ID=" + c2id + "&" +
                        "Function=" + takeUpFunction

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