package za.co.woolworths.financial.services.android.ui.fragments.cli

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.cli_eligibility_and_permission_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.CreditLimitIncrease
import za.co.woolworths.financial.services.android.models.dto.EligibilityQuestions
import za.co.woolworths.financial.services.android.models.dto.Permissions
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.extension.bindColor
import za.co.woolworths.financial.services.android.util.FragmentUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController

class CLIEligibilityAndPermissionFragment : Fragment(), View.OnClickListener {

    companion object {
        private const val SLIDE_UP_ANIM_DURATION = 300
        private var paddingDp: Int = 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.cli_eligibility_and_permission_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val creditLimitIncrease: CreditLimitIncrease? = WoolworthsApplication.getInstance()?.creditLimitIncrease
        val eligibilityQuestions: EligibilityQuestions? = creditLimitIncrease?.eligibilityQuestions
        val permissions: Permissions? = creditLimitIncrease?.permissions

        var eligibilityQuestionsDesc = ""
        eligibilityQuestions?.description?.forEach {
             it?.let { eligibilityQuestionsDesc += "• $it \n" }
        }

        eligibilityQuestion.text = eligibilityQuestions?.title
        eligibilityQuestionDesc?.text = eligibilityQuestionsDesc
        permissionsTitle?.text = permissions?.title
        permissionsDesc?.text = permissions?.description

        eligibilityNo?.setOnClickListener(this)
        eligibilityYes?.setOnClickListener(this)
        permissionYes?.setOnClickListener(this)
        permissionNo?.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        activity?.apply {
            when (view.id) {
                R.id.eligibilityYes -> {
                    eligibilityNo?.setBackgroundColor(bindColor(android.R.color.transparent))
                    eligibilityNo?.setTextColor(bindColor(R.color.cli_yes_no_button_color))
                    eligibilityYes?.setBackgroundColor(bindColor(R.color.black))
                    eligibilityYes?.setTextColor(bindColor(R.color.white))
                    permissionView?.visibility = View.GONE
                    Utils.displayValidationMessage(this, CustomPopUpWindow.MODAL_LAYOUT.INSOLVENCY, "")
                }
                R.id.eligibilityNo -> {
                    eligibilityYes?.setBackgroundColor(bindColor(android.R.color.transparent))
                    eligibilityYes?.setTextColor(bindColor(R.color.cli_yes_no_button_color))
                    eligibilityNo?.setBackgroundColor(bindColor(R.color.black))
                    eligibilityNo?.setTextColor(bindColor(R.color.white))
                    val ilc = IncreaseLimitController(this)
                    paddingDp = 16.times(resources?.displayMetrics?.density ?: 0f).toInt()
                    permissionView?.apply {
                        visibility = View.VISIBLE
                        setPadding(0, paddingDp, 0, ilc.getScreenHeight(activity))
                        scrollView?.post { ObjectAnimator.ofInt(scrollView, "scrollY", top).setDuration(SLIDE_UP_ANIM_DURATION.toLong()).start() }
                    }
                    Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.CLI_CONSENT)
                }
                R.id.permissionYes -> {
                    permissionNo?.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
                    permissionNo?.setTextColor(ContextCompat.getColor(this, R.color.cli_yes_no_button_color))
                    permissionYes?.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
                    permissionYes?.setTextColor(ContextCompat.getColor(this, R.color.white))
                    llEligibilityView?.visibility = View.GONE
                    permissionView?.setPadding(0, paddingDp, 0, 0)
                    val fragmentUtils = FragmentUtils()
                    fragmentUtils.replaceFragment(supportFragmentManager, CLIMaritalStatusFragment.newInstance(), R.id.cliMainFrame)
                }
                R.id.permissionNo -> {
                    permissionNo?.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
                    permissionNo?.setTextColor(ContextCompat.getColor(this, R.color.white))
                    Utils.displayValidationMessage(this, CustomPopUpWindow.MODAL_LAYOUT.CONFIDENTIAL, "")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.let { Utils.setScreenName(it, FirebaseManagerAnalyticsProperties.ScreenNames.CLI_INSOLVENCY_CHECK) }
    }

}