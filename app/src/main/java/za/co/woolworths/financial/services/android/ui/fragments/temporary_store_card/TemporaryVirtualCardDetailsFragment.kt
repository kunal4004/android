package za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.temporary_virtual_card_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.temporary_store_card.HowToUseTemporaryStoreCardActivity
import za.co.woolworths.financial.services.android.util.Utils

class TemporaryVirtualCardDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = TemporaryVirtualCardDetailsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity?.let { Utils.updateStatusBarBackground(it, R.color.grey_bg) }
        return inflater.inflate(R.layout.temporary_virtual_card_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        howItWorks.setOnClickListener {
            activity?.apply {
                startActivity(Intent(this, HowToUseTemporaryStoreCardActivity::class.java))
                overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
        }

        payWithCard.setOnClickListener {
            activity?.supportFragmentManager?.apply {
                ScanBarcodeToPayDialogFragment.newInstance().show((this), ScanBarcodeToPayDialogFragment::class.java.simpleName)
            }
        }
    }
}