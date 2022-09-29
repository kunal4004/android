package za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CircularProgressViewWithTickBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.views.tick_animation.AnimationState

class CircularProgressIndicatorFragment : Fragment(R.layout.circular_progress_view_with_tick) {

    val viewModel: CircularProgressIndicatorViewModel by activityViewModels()

    enum class CircularProgressAnimatedStatus { Success, Failure, None }

    private var mCircularProgressAnimatedStatus = CircularProgressAnimatedStatus.None

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = CircularProgressViewWithTickBinding.bind(view)
        with(binding) {
            subscribeObserver()
            setListener()
        }
    }

    private fun CircularProgressViewWithTickBinding.setListener() {
        circularProgressIndicator.setOnAnimationStateChangedListener { animationState ->
            when (animationState) {
                AnimationState.ANIMATING -> when (mCircularProgressAnimatedStatus) {
                    CircularProgressAnimatedStatus.Success -> showSuccessfulTickIcon()
                    CircularProgressAnimatedStatus.Failure -> showFailureIcon()
                    CircularProgressAnimatedStatus.None -> Unit
                }
                else -> Unit
            }
        }
    }

    private fun CircularProgressViewWithTickBinding.showFailureIcon() {
        imFailureIcon.visibility = View.VISIBLE
    }

    private fun CircularProgressViewWithTickBinding.showSuccessfulTickIcon() {
        successTickView.visibility = View.VISIBLE
        with(successTickView) {
            colorCode = R.color.dark_green
            startTickAnim()
        }
    }

    private fun CircularProgressViewWithTickBinding.subscribeObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.progressIndicator.collectLatest { result ->
                when (result) {
                    ProgressIndicator.Spinning -> showLoadingUI()
                    ProgressIndicator.Idle -> showIdleUI()
                    ProgressIndicator.Success -> showSuccessUI()
                    ProgressIndicator.Failure,
                    ProgressIndicator.NoConnection,
                    ProgressIndicator.UnknownError -> showFailureUI()
                }
            }
        }
    }

    private fun CircularProgressViewWithTickBinding.showIdleUI() {
        circularProgressIndicator.stopSpinning()
        circularProgressIndicator.setValueAnimated(MAX_PROGRESS_VALUE)
    }

    private fun CircularProgressViewWithTickBinding.showSuccessUI() {
        mCircularProgressAnimatedStatus = CircularProgressAnimatedStatus.Success
        showIdleUI()
    }

    private fun CircularProgressViewWithTickBinding.showLoadingUI() {
        mCircularProgressAnimatedStatus = CircularProgressAnimatedStatus.None
        circularProgressIndicator.spin()
        successTickView.visibility = View.GONE
        imFailureIcon.visibility = View.GONE
    }

    private fun CircularProgressViewWithTickBinding.showFailureUI() {
        mCircularProgressAnimatedStatus = CircularProgressAnimatedStatus.Failure
        showIdleUI()
    }

    companion object {
        const val MAX_PROGRESS_VALUE = 100f
    }
}