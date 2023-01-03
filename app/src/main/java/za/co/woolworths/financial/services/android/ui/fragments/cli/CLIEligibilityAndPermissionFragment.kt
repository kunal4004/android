package za.co.woolworths.financial.services.android.ui.fragments.cli

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CliEligibilityAndPermissionFragmentBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.app_config.credit_limit_increase.ConfigCreditLimitIncrease
import za.co.woolworths.financial.services.android.models.dto.app_config.credit_limit_increase.ConfigEligibilityQuestions
import za.co.woolworths.financial.services.android.models.dto.app_config.credit_limit_increase.ConfigPermissions
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity
import za.co.woolworths.financial.services.android.ui.extension.bindColor
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ErrorMessageDialog
import za.co.woolworths.financial.services.android.util.FragmentUtils
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController

class CLIEligibilityAndPermissionFragment : Fragment(R.layout.cli_eligibility_and_permission_fragment), View.OnClickListener {

    companion object {
        private const val SLIDE_UP_ANIM_DURATION = 300
        private var paddingDp: Int = 0
    }

    private lateinit var binding: CliEligibilityAndPermissionFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CliEligibilityAndPermissionFragmentBinding.bind(view)

        val creditLimitIncrease: ConfigCreditLimitIncrease? = AppConfigSingleton.creditLimitIncrease
        val eligibilityQuestions: ConfigEligibilityQuestions? = creditLimitIncrease?.eligibilityQuestions
        val permissions: ConfigPermissions? = creditLimitIncrease?.permissions
        (activity as? CLIPhase2Activity)?.selectedMaritalStatusPosition = null
        var eligibilityQuestionsDesc = ""
        eligibilityQuestions?.description?.forEach {
             it?.let { eligibilityQuestionsDesc += "• $it \n" }
        }

        binding.apply {
            eligibilityQuestion.text = eligibilityQuestions?.title
            eligibilityQuestionDesc?.text = eligibilityQuestionsDesc
            permissionsTitle?.text = permissions?.title
            permissionsDesc?.text = permissions?.description

            eligibilityNo?.setOnClickListener(this@CLIEligibilityAndPermissionFragment)
            eligibilityYes?.setOnClickListener(this@CLIEligibilityAndPermissionFragment)
            permissionYes?.setOnClickListener(this@CLIEligibilityAndPermissionFragment)
            permissionNo?.setOnClickListener(this@CLIEligibilityAndPermissionFragment)
        }
    }

    override fun onClick(view: View) {
        binding.apply {
            activity?.apply {
                when (view.id) {
                    R.id.eligibilityYes -> {
                        eligibilityNo?.setBackgroundColor(bindColor(android.R.color.transparent))
                        eligibilityNo?.setTextColor(bindColor(R.color.cli_yes_no_button_color))
                        eligibilityYes?.setBackgroundColor(bindColor(R.color.black))
                        eligibilityYes?.setTextColor(bindColor(R.color.white))
                        permissionView?.visibility = View.GONE
                        val infoDialog = ErrorMessageDialog(
                            R.string.cli_pop_insolvency_title,
                            R.string.cli_pop_insolvency_desc,
                            R.string.ok,
                            R.string.cli_pop_insolvency_content_desc_title,
                            R.string.cli_pop_insolvency_content_desc_desc,
                            R.string.global_button_label
                        )
                        KotlinUtils.cliErrorMessageDialog(
                            requireActivity() as? AppCompatActivity,
                            infoDialog
                        )
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
                            scrollView?.post {
                                ObjectAnimator.ofInt(scrollView, "scrollY", top)
                                    .setDuration(SLIDE_UP_ANIM_DURATION.toLong()).start()
                            }
                        }
                        Utils.setScreenName(
                            this,
                            FirebaseManagerAnalyticsProperties.ScreenNames.CLI_CONSENT
                        )
                    }
                    R.id.permissionYes -> {
                        permissionNo?.setBackgroundColor(
                            ContextCompat.getColor(
                                this,
                                android.R.color.transparent
                            )
                        )
                        permissionNo?.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.cli_yes_no_button_color
                            )
                        )
                        permissionYes?.setBackgroundColor(
                            ContextCompat.getColor(
                                this,
                                R.color.black
                            )
                        )
                        permissionYes?.setTextColor(ContextCompat.getColor(this, R.color.white))
                        llEligibilityView?.visibility = View.GONE
                        permissionView?.setPadding(0, paddingDp, 0, 0)
                        val fragmentUtils = FragmentUtils()
                        fragmentUtils.replaceFragment(
                            supportFragmentManager,
                            CLIMaritalStatusFragment.newInstance(),
                            R.id.cliMainFrame
                        )
                    }
                    R.id.permissionNo -> {
                        permissionNo?.setBackgroundColor(
                            ContextCompat.getColor(
                                this,
                                R.color.black
                            )
                        )
                        permissionNo?.setTextColor(ContextCompat.getColor(this, R.color.white))
                        val infoDialog = ErrorMessageDialog(
                            R.string.cli_pop_confidential_title,
                            R.string.cli_pop_confidential_desc,
                            R.string.ok,
                            R.string.cli_pop_confidential_content_desc_title,
                            R.string.cli_pop_confidential_content_desc_desc,
                            R.string.global_button_label
                        )
                        KotlinUtils.cliErrorMessageDialog(
                            requireActivity() as? AppCompatActivity,
                            infoDialog
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.let { Utils.setScreenName(it, FirebaseManagerAnalyticsProperties.ScreenNames.CLI_INSOLVENCY_CHECK) }
    }

}