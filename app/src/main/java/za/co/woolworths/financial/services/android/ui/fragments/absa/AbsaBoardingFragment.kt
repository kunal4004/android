package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AbsaOnBoardingFragmentBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.ABSAOnlineBankingRegistrationActivity
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.account.helper.FirebaseEventDetailManager

class AbsaBoardingFragment : AbsaFragmentExtension(R.layout.absa_on_boarding_fragment), View.OnClickListener {

    private lateinit var binding: AbsaOnBoardingFragmentBinding
    private var mCreditCardNumber: String? = ""

    companion object {
        fun newInstance(creditAccountInfo: String?) = AbsaBoardingFragment().withArgs {
                putString("creditCardToken", creditAccountInfo)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            if (containsKey("creditCardToken")) {
                mCreditCardNumber = arguments?.getString("creditCardToken") ?: ""
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = AbsaOnBoardingFragmentBinding.bind(view)

        with(binding) {
            activity?.apply { (this as ABSAOnlineBankingRegistrationActivity).clearPageTitle() }
            scrollView.post { scrollView.scrollTo(0, scrollView.bottom) }
            setupLater.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setupPasscode.setOnClickListener(this@AbsaBoardingFragment)
            setupLater.setOnClickListener(this@AbsaBoardingFragment)
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.setupPasscode -> {
                activity?.apply { FirebaseEventDetailManager.tapped(FirebaseManagerAnalyticsProperties.ABSA_CC_SET_UP_PASSCODE, this) }
                replaceFragment(
                        fragment = AbsaEnterAtmPinCodeFragment.newInstance(mCreditCardNumber),
                        tag = AbsaEnterAtmPinCodeFragment::class.java.simpleName,
                        containerViewId = R.id.flAbsaOnlineBankingToDevice,
                        allowStateLoss = true,
                        enterAnimation = R.anim.slide_in_from_right,
                        exitAnimation = R.anim.slide_to_left,
                        popEnterAnimation = R.anim.slide_from_left,
                        popExitAnimation = R.anim.slide_to_right
                )
            }
            R.id.setupLater -> {
                (activity as ABSAOnlineBankingRegistrationActivity).finishActivity()
            }
        }
    }

}