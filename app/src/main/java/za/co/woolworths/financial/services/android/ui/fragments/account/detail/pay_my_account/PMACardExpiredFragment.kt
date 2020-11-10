package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.pma_card_has_expired_dialog.*
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class PMACardExpiredFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.pma_card_has_expired_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    override fun onClick(v: View?) {
        with(payMyAccountViewModel) {
            when (v?.id) {
                R.id.removeCardButton -> setNavigationResult(PayMyAccountViewModel.OnBackNavigation.REMOVE)
                R.id.addNewCardExpiredButton -> {
                    if (isPaymentMethodListSizeLimitedToTenItem()) {
                        setNavigationResult(PayMyAccountViewModel.OnBackNavigation.MAX_CARD_LIMIT)
                    } else {
                        setNavigationResult(PayMyAccountViewModel.OnBackNavigation.ADD)
                    }
                }
            }
        }
        dismiss()
    }
}