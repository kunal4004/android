package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.pma_card_has_expired_dialog.*
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class PMACardExpiredFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private var navController: NavController? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.pma_card_has_expired_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = NavHostFragment.findNavController(this)

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
            R.id.removeCardButton -> dismiss()
            R.id.addNewCardExpiredButton -> dismiss()
        }
    }

}