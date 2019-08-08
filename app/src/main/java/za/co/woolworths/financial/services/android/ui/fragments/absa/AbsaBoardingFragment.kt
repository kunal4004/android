package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_on_boarding_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.ABSAOnlineBankingRegistrationActivity
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.inputmethod.InputMethodManager
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.Utils


class AbsaBoardingFragment : AbsaFragmentExtension(), View.OnClickListener {

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.absa_on_boarding_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.apply { (this as ABSAOnlineBankingRegistrationActivity).clearPageTitle()  }
        setupLater.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        setupPasscode.setOnClickListener(this)
        setupLater.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.setupPasscode -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.ABSA_CC_SET_UP_PASSOCDE)
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