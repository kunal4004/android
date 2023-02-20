package za.co.woolworths.financial.services.android.ui.fragments.cli

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CliEmailSentFragmentBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.service.event.BusStation
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.controller.CLIFragment

class CliEmailSentFragment : CLIFragment(R.layout.cli_email_sent_fragment), View.OnClickListener {

    private lateinit var binding: CliEmailSentFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CliEmailSentFragmentBinding.bind(view)
        if (mCliStepIndicatorListener != null) {
            mCliStepIndicatorListener!!.onStepSelected(5)
        }
        init()
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(
            activity,
            FirebaseManagerAnalyticsProperties.ScreenNames.CLI_POI_DOCUMENTS_UPLOAD
        )
    }

    private fun init() {
        populateDocument(binding.tvEmailAccount)
        binding.btnProcessComplete.setOnClickListener(this)
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

    private fun populateDocument(textView: WTextView) {
        val userDetail = SessionUtilities.getInstance().jwt
        if (userDetail != null) {
            textView.setText(userDetail.email[0])
        }
    }
}