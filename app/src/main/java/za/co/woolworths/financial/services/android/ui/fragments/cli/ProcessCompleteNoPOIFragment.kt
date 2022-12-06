package za.co.woolworths.financial.services.android.ui.fragments.cli

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CliProcessCompleteNoPoiBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.service.event.BusStation
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity
import za.co.woolworths.financial.services.android.ui.views.WButton
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.controller.CLIFragment

class ProcessCompleteNoPOIFragment : CLIFragment(R.layout.cli_process_complete_no_poi), View.OnClickListener {

    private lateinit var binding: CliProcessCompleteNoPoiBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CliProcessCompleteNoPoiBinding.bind(view)
        binding.btnProcessComplete.setOnClickListener(this)
        mCliStepIndicatorListener?.onStepSelected(5)
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(
            activity,
            FirebaseManagerAnalyticsProperties.ScreenNames.CLI_PROCESS_COMPLETE_NO_POI
        )
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnProcessComplete -> {
                val activity: Activity? = activity
                if (activity != null && activity is CLIPhase2Activity) {
                    (activity.getApplication() as WoolworthsApplication)
                        .bus()
                        .send(BusStation(true))
                    activity.finish()
                    activity.overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
                }
            }
        }
    }
}