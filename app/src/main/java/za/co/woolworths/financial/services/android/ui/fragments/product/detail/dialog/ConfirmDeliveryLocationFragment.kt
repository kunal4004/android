package za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.confirm_deliverylocation_bottom_sheet_dialog.*
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.IOnConfirmDeliveryLocationActionListener
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.wenum.Delivery

class ConfirmDeliveryLocationFragment : WBottomSheetDialogFragment() {
    private var listener: IOnConfirmDeliveryLocationActionListener? = null

    companion object {
        fun newInstance() = ConfirmDeliveryLocationFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            listener = parentFragment as IOnConfirmDeliveryLocationActionListener?
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement Callback interface")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.confirm_deliverylocation_bottom_sheet_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnDefaultLocation.setOnClickListener {
            listener?.onConfirmLocation()
            dismissAllowingStateLoss()
        }
        btnSetNewLocation.setOnClickListener {
            listener?.onSetNewLocation()
            dismissAllowingStateLoss()
        }
        configureUI()
    }

    private fun configureUI() {
        Utils.getPreferredDeliveryLocation()?.apply {
            val storePickup = KotlinUtils.getPreferredDeliveryType() == Delivery.CNC
            btnSetNewLocation.setPaintFlags(btnSetNewLocation.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
            btnSetNewLocation.setText(activity?.resources?.getString(R.string.edt_location))
            title?.text = activity?.resources?.getString(if (storePickup) R.string.confirm_collection_location_title else R.string.confirm_delivery_location_title)
            description?.text = activity?.resources?.getString(if (storePickup) R.string.confirm_collection_location_desc else R.string.current_delivery_location_desc)
            deliverLocationIcon?.setBackgroundResource(if (storePickup) R.drawable.basket else R.drawable.ic_delivery_truck)
            tvLocation.text = KotlinUtils.getPreferredDeliveryAddressOrStoreName()
        }
    }

}