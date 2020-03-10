package za.co.woolworths.financial.services.android.ui.fragments.credit_card_activation

import android.app.Activity
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_activation_failure_layout.*
import kotlinx.android.synthetic.main.credit_card_activation_progress_layout.*
import kotlinx.android.synthetic.main.credit_card_activation_success_layout.*
import kotlinx.android.synthetic.main.credit_card_activation_success_layout.okGotItButton
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.findFragmentByTag
import za.co.woolworths.financial.services.android.ui.fragments.npc.ProgressStateFragment
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardActivationProgressFragment : Fragment(), CreditCardActivationContract.CreditCardActivationView, IProgressAnimationState, View.OnClickListener {

    var presenter: CreditCardActivationContract.CreditCardActivationPresenter? = null
    lateinit var absaCardToken: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_activation_progress_layout, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        presenter = CreditCardActivationPresenterImpl(this, CreditCardActivationInteractorImpl())
        arguments?.getBundle("bundle")?.apply {
            absaCardToken = getString("absaCardToken", "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activateCreditCard()
        okGotItButton?.setOnClickListener(this)
        callTheCallCenter?.setOnClickListener(this)
        cancel?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener(this@CreditCardActivationProgressFragment)
        }
    }

    override fun activateCreditCard() {
        startProgress()
        presenter?.initCreditCardActivation(absaCardToken)
    }

    override fun getProgressState(): ProgressStateFragment? = (activity as? AppCompatActivity)?.findFragmentByTag(ProgressStateFragment::class.java.simpleName) as? ProgressStateFragment

    override fun startProgress() {
        (activity as? AppCompatActivity)?.addFragment(
                fragment = ProgressStateFragment.newInstance(this),
                tag = ProgressStateFragment::class.java.simpleName,
                containerViewId = R.id.flProgressIndicator
        )
        activationProcessingLayout?.visibility = View.VISIBLE
    }

    override fun onCreditCardActivationSuccess() {
        getProgressState()?.animateSuccessEnd(true)
        activationProcessingLayout?.visibility = View.GONE
        activationSuccessView?.visibility = View.VISIBLE
    }

    override fun onCreditCardActivationFailure() {
        getProgressState()?.animateSuccessEnd(false)
        activationProcessingLayout?.visibility = View.GONE
        activationFailureView?.visibility = View.VISIBLE
    }

    override fun onSessionTimeout() {

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cancel -> activity?.onBackPressed()
            R.id.callTheCallCenter -> activity?.apply { Utils.makeCall("0861 50 20 20") }
            R.id.okGotItButton -> {
                activity?.apply {
                    setResult(Activity.RESULT_OK)
                    finish()
                    overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
                }
            }
            R.id.okGotItButton, R.id.cancel -> activity?.onBackPressed()
            R.id.callTheCallCenter -> activity?.apply { Utils.makeCall("0861 50 20 20") }
        }
    }

}