package za.co.woolworths.financial.services.android.ui.fragments.credit_card_activation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_activation_progress_layout.*
import kotlinx.android.synthetic.main.credit_card_activation_success_layout.*
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.findFragmentByTag
import za.co.woolworths.financial.services.android.ui.fragments.npc.ProgressStateFragment

class CreditCardActivationProgressFragment : Fragment(), CreditCardActivationContract.CreditCardActivationView, IProgressAnimationState {


    var presenter: CreditCardActivationContract.CreditCardActivationPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_activation_progress_layout, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = CreditCardActivationPresenterImpl(this, CreditCardActivationInteractorImpl())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activateCreditCard()
        okGotItButton?.setOnClickListener { activity?.onBackPressed() }
    }

    override fun activateCreditCard() {
        startProgress()
        presenter?.initCreditCardActivation("4103752306880391")
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
    }

    override fun onSessionTimeout() {

    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }


}