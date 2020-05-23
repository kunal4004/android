package za.co.woolworths.financial.services.android.ui.fragments.click_and_collect

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.delivery_or_click_and_collect_selector_dialog.*
import za.co.woolworths.financial.services.android.ui.activities.click_and_collect.EditDeliveryLocationActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils

class DeliveryOrClickAndCollectSelectorDialogFragment : WBottomSheetDialogFragment() {

    private var mDescription: String? = null

    companion object {
        private const val DESCRIPTION = "DESCRIPTION"
        fun newInstance() = DeliveryOrClickAndCollectSelectorDialogFragment().withArgs {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mDescription = getString(DESCRIPTION)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.delivery_or_click_and_collect_selector_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utils.deliverySelectionModalShown()
        justBrowsing?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        justBrowsing?.setOnClickListener {
            activity?.apply {
                val mIntent = Intent(this, EditDeliveryLocationActivity::class.java)
                val mBundle = Bundle()
                mIntent.putExtra("bundle", mBundle)
                startActivity(mIntent)
                overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
        }
    }
}