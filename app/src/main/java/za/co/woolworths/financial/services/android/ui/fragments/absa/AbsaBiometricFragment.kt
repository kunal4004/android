package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AbsaBiometricFragmentBinding

class AbsaBiometricFragment : AbsaFragmentExtension(R.layout.absa_biometric_fragment) {

    companion object {
        fun newInstance() = AbsaBiometricFragment()
    }

    private lateinit var binding: AbsaBiometricFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = AbsaBiometricFragmentBinding.bind(view)
        binding.setUpViewsAndEvents()
        alwaysHideWindowSoftInputMode()
    }

    private fun AbsaBiometricFragmentBinding.setUpViewsAndEvents() {
        tvSkipStep.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        swEnableBiometric.setOnCheckedChangeListener { _, isChecked
            ->
            if (isChecked) {
                imFootPrintLogo.setImageResource(R.drawable.biometric_active)
                imFootPrintLogo.alpha = 1f
                ivNavigateToNextFragment.alpha = 1f
                ivNavigateToNextFragment.isEnabled = true
            } else {
                imFootPrintLogo.setImageResource(R.drawable.biometric_inactive)
                imFootPrintLogo.alpha = 0.5f
                ivNavigateToNextFragment.alpha = 0.5f
                ivNavigateToNextFragment.isEnabled = false
            }
        }

        //tvSkipStep.setOnClickListener { navigateToCompleteFragment() }
        //ivNavigateToNextFragment.setOnClickListener { navigateToCompleteFragment() }
    }

    /*private fun navigateToCompleteFragment() {
        replaceFragment(
                fragment = AbsaPinCodeSuccessFragment.newInstance(),
                tag = AbsaPinCodeSuccessFragment::class.java.simpleName,
                containerViewId = R.id.flAbsaOnlineBankingToDevice,
                allowStateLoss = true,
                enterAnimation = R.anim.slide_in_from_right,
                exitAnimation = R.anim.slide_to_left,
                popEnterAnimation = R.anim.slide_from_left,
                popExitAnimation = R.anim.slide_to_right
        )
    }*/

    override fun onResume() {
        super.onResume()
        alwaysHideWindowSoftInputMode()
    }
}