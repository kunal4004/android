package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.R
import com.awfs.coordination.databinding.PmaCardHasExpiredDialogBinding
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class PMACardExpiredFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: PmaCardHasExpiredDialogBinding
    val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()
    val pmaCardExpiredFragmentArgs : PMACardExpiredFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = PmaCardHasExpiredDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            mastercardImageView?.setImageResource(payMyAccountViewModel.getVendorCardLargeDrawableId())

            removeCardButton?.apply {
                AnimationUtilExtension.animateViewPushDown(this)
                setOnClickListener(this@PMACardExpiredFragment)
            }

            addNewCardExpiredButton?.apply {
                AnimationUtilExtension.animateViewPushDown(this)
                setOnClickListener(this@PMACardExpiredFragment)
            }
        }
    }

    override fun onClick(v: View?) {
        with(payMyAccountViewModel) {
            when (v?.id) {
                R.id.removeCardButton -> setNavigationResult(PayMyAccountViewModel.OnNavigateBack.Remove(pmaCardExpiredFragmentArgs.expiredCardPosition, pmaCardExpiredFragmentArgs.cardHasExpired))
                R.id.addNewCardExpiredButton -> {
                    if (isPaymentMethodListSizeLimitedToTenItem()) {
                        setNavigationResult(PayMyAccountViewModel.OnNavigateBack.MaxCardLimit)
                    } else {
                        setNavigationResult(PayMyAccountViewModel.OnNavigateBack.Add)
                    }
                }
            }
        }
        dismiss()
    }
}