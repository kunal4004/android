package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.pma_card_has_expired_dialog.*
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import java.util.*

class PMACardExpiredFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private var navController: NavController? = null

    private val pmaCardExpiredNavArgs: PMACardExpiredFragmentArgs by navArgs()
    val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.pma_card_has_expired_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = NavHostFragment.findNavController(this)

        val paymentMethod = pmaCardExpiredNavArgs.paymentMethod
        val vendor = paymentMethod.vendor.toLowerCase(Locale.getDefault())

        when (vendor) {
            "mastercard" -> R.drawable.card_mastercard_large
            "visa" -> R.drawable.card_visa_large
            else -> null
        }?.let { drawable -> mastercardImageView?.setImageResource(drawable) }

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
        when (v?.id) {
            R.id.removeCardButton -> {
                payMyAccountViewModel.setNavigationResult(PayMyAccountViewModel.OnBackNavigation.REMOVE)
                dismiss()
            }
            R.id.addNewCardExpiredButton -> {
                if (payMyAccountViewModel.isPaymentMethodListSizeLimitedToTenItem()) {
                    payMyAccountViewModel.setNavigationResult(PayMyAccountViewModel.OnBackNavigation.MAX_CARD_LIMIT)
                } else {
                    payMyAccountViewModel.setNavigationResult(PayMyAccountViewModel.OnBackNavigation.ADD)
                }
                dismiss()
            }
        }
    }

}