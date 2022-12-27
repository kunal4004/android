package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import com.awfs.coordination.databinding.PayMyAccountRetryOnErrorDialogFragmentBinding
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class PayMyAccountRetryErrorFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: PayMyAccountRetryOnErrorDialogFragmentBinding
    val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = PayMyAccountRetryOnErrorDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pmaErrorRetryButton?.apply {
            setOnClickListener(this@PayMyAccountRetryErrorFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.pmaErrorRetryButton -> {
                payMyAccountViewModel.setNavigationResult(
                    PayMyAccountViewModel.OnNavigateBack.Retry)
                dismiss()
            }
        }
    }
}