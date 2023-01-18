package za.co.woolworths.financial.services.android.ui.fragments.credit_card_activation

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CreditCardActivationSecurityCheckFragmentBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.ACTION_LOWER_CASE
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.activationRequested
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardActivationSecurityCheckFragment : Fragment(R.layout.credit_card_activation_security_check_fragment), View.OnClickListener {

    private lateinit var binding: CreditCardActivationSecurityCheckFragmentBinding
    var navController: NavController? = null
    var bundle: Bundle? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CreditCardActivationSecurityCheckFragmentBinding.bind(view)

        navController = Navigation.findNavController(view)
        binding.apply {
            activateCardButton.setOnClickListener(this@CreditCardActivationSecurityCheckFragment)
            inEnvelope.setOnClickListener(this@CreditCardActivationSecurityCheckFragment)
            correctDetails.setOnClickListener(this@CreditCardActivationSecurityCheckFragment)
            pinSealInTact.setOnClickListener(this@CreditCardActivationSecurityCheckFragment)
            callCallCenter?.apply {
                paintFlags = Paint.UNDERLINE_TEXT_FLAG
                setOnClickListener(this@CreditCardActivationSecurityCheckFragment)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v?.id) {
                R.id.activateCardButton -> {
                    activity?.apply {
                        Utils.triggerFireBaseEvents(
                            FirebaseManagerAnalyticsProperties.CC_ACTIVATE_MY_CARD,
                            hashMapOf(Pair(ACTION_LOWER_CASE, activationRequested)),
                            this
                        )
                    }
                    navController?.navigate(
                        if (AppConfigSingleton.creditCardActivation != null && AppConfigSingleton.creditCardActivation!!.otpEnabledForCreditCardActivation)
                            R.id.action_to_RetrieveOTPFragment
                        else
                            R.id.action_to_creditCardActivationProgressFragment,
                        bundleOf("bundle" to bundle)
                    )
                }
                R.id.callCallCenter -> activity?.apply { Utils.makeCall("0861 50 20 20") }
                R.id.inEnvelope -> {
                    inEnvelopeCheck.isChecked = !inEnvelopeCheck.isChecked
                    validateSecurityCheck()
                }
                R.id.correctDetails -> {
                    correctDetailsCheck.isChecked = !correctDetailsCheck.isChecked
                    validateSecurityCheck()
                }
                R.id.pinSealInTact -> {
                    pinSealInTactCheck.isChecked = !pinSealInTactCheck.isChecked
                    validateSecurityCheck()
                }
            }
        }
    }

    private fun validateSecurityCheck() {
        binding.apply {
            activateCardButton.isEnabled =
                (inEnvelopeCheck.isChecked && correctDetailsCheck.isChecked && pinSealInTactCheck.isChecked)
        }
    }
}