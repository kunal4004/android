package za.co.woolworths.financial.services.android.ui.fragments.click_and_collect

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.delivery_or_click_and_collect_selector_dialog.*
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.DeliveryType
import za.co.woolworths.financial.services.android.util.Utils

class DeliveryOrClickAndCollectSelectorDialogFragment(var listener: IDeliveryOptionSelection?) : WBottomSheetDialogFragment(), View.OnClickListener {


    interface IDeliveryOptionSelection {
        fun onDeliveryOptionSelected(deliveryType: DeliveryType)
    }

    companion object {
        fun newInstance(listener: IDeliveryOptionSelection?) = DeliveryOrClickAndCollectSelectorDialogFragment(listener)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.delivery_or_click_and_collect_selector_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utils.deliverySelectionModalShown()
        justBrowsing?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        justBrowsing?.setOnClickListener(this)
        delivery?.setOnClickListener(this)
        clickAndCollect?.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.justBrowsing -> dismissAllowingStateLoss()
            R.id.delivery -> {
                dismissDialogWithDeliveryOption(DeliveryType.DELIVERY)
            }
            R.id.clickAndCollect -> {
                dismissDialogWithDeliveryOption(DeliveryType.STORE_PICKUP)
            }
        }
    }

    fun dismissDialogWithDeliveryOption(deliveryType: DeliveryType) {
        listener?.onDeliveryOptionSelected(deliveryType)
        dismissAllowingStateLoss()
    }
}