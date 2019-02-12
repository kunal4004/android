package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.graphics.Paint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_biometric_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment

class ABSABiometricFragment : Fragment() {

    companion object {
        fun newInstance() = ABSABiometricFragment()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.absa_biometric_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewsAndEvents()
    }

    private fun setUpViewsAndEvents() {
        tvSkipStep.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        swEnableBiometric.setOnCheckedChangeListener { _, isChecked
            ->
            if (isChecked) {
                imFootPrintLogo.setImageResource(R.drawable.biometric_active)
                ivNavigateToNextFragment.alpha = 1f
                ivNavigateToNextFragment.isEnabled = true
            } else {
                imFootPrintLogo.setImageResource(R.drawable.biometric_inactive)
                ivNavigateToNextFragment.alpha = 0.5f
                ivNavigateToNextFragment.isEnabled = false
            }
        }

        tvSkipStep.setOnClickListener { navigateToCompleteFragment() }
        ivNavigateToNextFragment.setOnClickListener { navigateToCompleteFragment() }
    }

    private fun navigateToCompleteFragment() {
        replaceFragment(
                fragment = ABSAPinCodeSuccessFragment.newInstance(),
                tag = ABSAPinCodeSuccessFragment::class.java.simpleName,
                containerViewId = R.id.flAbsaOnlineBankingToDevice,
                allowStateLoss = true,
                enterAnimation = R.anim.slide_in_from_right,
                exitAnimation = R.anim.slide_to_left,
                popEnterAnimation = R.anim.slide_from_left,
                popExitAnimation = R.anim.slide_to_right
        )
    }
}