package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.circle_progress_layout.*
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.ui.views.tick_animation.AnimationState

class ProcessYourRequestFragment : Fragment() {

    var isSuccess: Boolean = false
    private var stateAnimation: IProgressAnimationState? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.process_your_request_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        circularProgressIndicator?.setOnAnimationStateChangedListener { _animationState ->
            _animationState?.apply {
                if (this == AnimationState.ANIMATING) {
                    if (isSuccess) {
                        success_tick?.apply {
                            visibility = View.VISIBLE
                            startTickAnim()
                        }
                    } else {
                        imFailureIcon?.visibility = View.VISIBLE
                    }
                    stateAnimation?.onAnimationEnd(isSuccess)
                }
            }
        }
    }

    fun stopSpinning(isSuccess: Boolean) {
        this.isSuccess = isSuccess
        circularProgressIndicator?.apply {
            stopSpinning()
            setValueAnimated(100f)
        }
    }

    fun startSpinning() {
        imFailureIcon?.visibility = View.GONE
        circularProgressIndicator?.spin()
    }
}