package za.co.woolworths.financial.services.android.ui.fragments.credit_card_activation

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_activation_security_check_fragment.*
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardActivationSecurityCheckFragment : Fragment(), View.OnClickListener {

    var navController: NavController? = null
    lateinit var absaCardToken: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_activation_security_check_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        activateCardButton.setOnClickListener(this)
        inEnvelope.setOnClickListener(this)
        correctDetails.setOnClickListener(this)
        pinSealInTact.setOnClickListener(this)
        callCallCenter?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener(this@CreditCardActivationSecurityCheckFragment)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        absaCardToken = arguments?.getString("absaCardToken").toString()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.activateCardButton -> {
                //navController?.navigate(R.id.action_to_creditCardActivationProgressFragment, bundleOf("absaCardToken" to absaCardToken))
                val bundle = bundleOf("absaCardToken" to absaCardToken, "productOfferingId" to "20")
                navController?.navigate(R.id.action_to_RetrieveOTPFragment, bundleOf("bundle" to bundle))
            }
            R.id.callCallCenter -> activity?.apply { Utils.makeCall(this, "0861 50 20 20") }
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

    private fun validateSecurityCheck() {
        activateCardButton.isEnabled = (inEnvelopeCheck.isChecked && correctDetailsCheck.isChecked && pinSealInTactCheck.isChecked)
    }
}