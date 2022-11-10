package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.awfs.coordination.R
import com.awfs.coordination.databinding.ProcessRequestFragmentBinding
import kotlinx.android.synthetic.main.circle_progress_layout.*
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.ui.views.tick_animation.AnimationState

open class ProcessYourRequestFragment : Fragment(R.layout.process_request_fragment) {

    protected lateinit var binding: ProcessRequestFragmentBinding
    var isAPICallSuccessFul: Boolean = false
    private var stateAnimation: IProgressAnimationState? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ProcessRequestFragmentBinding.bind(view)
    }

    fun circularProgressListener(codeSuccess: () -> Unit, codeFailure: () -> Unit) {
        circularProgressIndicator?.setOnAnimationStateChangedListener { _animationState ->
            _animationState?.apply {
                if (this == AnimationState.ANIMATING) {
                    if (isAPICallSuccessFul) {
                        success_tick?.apply {
                            visibility = View.VISIBLE
                            colorCode = R.color.success_tick_color
                            startTickAnim()
                            codeSuccess()
                        }
                    } else {
                        imFailureIcon?.visibility = View.VISIBLE
                        codeFailure()
                    }
                    stateAnimation?.onAnimationEnd(isAPICallSuccessFul)
                }
            }
        }
    }

    fun stopSpinning(isSuccess: Boolean) {
        this.isAPICallSuccessFul = isSuccess
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