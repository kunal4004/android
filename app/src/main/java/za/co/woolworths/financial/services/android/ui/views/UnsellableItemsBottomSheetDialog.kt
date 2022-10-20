package za.co.woolworths.financial.services.android.ui.views

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.UnsellableItemsBottomSheetDialogBinding
import za.co.woolworths.financial.services.android.geolocation.viewmodel.UnSellableItemsLiveData
import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem
import za.co.woolworths.financial.services.android.ui.adapters.UnsellableItemsListAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.wenum.Delivery

/**
 * Created by Kunal Uttarwar on 11/05/22.
 */
class UnsellableItemsBottomSheetDialog: WBottomSheetDialogFragment(),
    View.OnClickListener {

    private lateinit var binding: UnsellableItemsBottomSheetDialogBinding
    var bundle: Bundle? = null
    private var commerceItems: ArrayList<UnSellableCommerceItem>? = null
    private var deliveryType: String? = null
    companion object {
        const val KEY_ARGS_UNSELLABLE_COMMERCE_ITEMS = "UnSellableCommerceItems"
        const val KEY_ARGS_DELIVERY_TYPE = "deliveryType"

        fun newInstance(
            unsellableItemsList: ArrayList<UnSellableCommerceItem>,
            deliveryType: String,
        ) =
            UnsellableItemsBottomSheetDialog().withArgs {
                putSerializable(KEY_ARGS_UNSELLABLE_COMMERCE_ITEMS, unsellableItemsList)
                putString(KEY_ARGS_DELIVERY_TYPE, deliveryType)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = UnsellableItemsBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.init()
    }

    private fun UnsellableItemsBottomSheetDialogBinding.init() {
        incSwipeCloseIndicator?.root?.visibility = View.VISIBLE
        removeItems?.setOnClickListener(this@UnsellableItemsBottomSheetDialog)
        arguments?.apply {
            deliveryType = getString(KEY_ARGS_DELIVERY_TYPE, Delivery.STANDARD.type)
            commerceItems = getSerializable(KEY_ARGS_UNSELLABLE_COMMERCE_ITEMS) as? ArrayList<UnSellableCommerceItem>
        }

        val itemCount = commerceItems?.size?:0
        unsellableTitle?.text =
            resources.getQuantityString(R.plurals.unsellable_title, itemCount, itemCount)
        when(deliveryType) {
            Delivery.STANDARD.name -> {
                val standardDeliveryText =  resources.getQuantityText(R.plurals.remove_items_standard_dialog_desc, itemCount)
                unsellableSubTitle?.text =  standardDeliveryText
            }
            Delivery.CNC.name -> {
                val clickAndCollectText =  resources.getQuantityText(R.plurals.remove_items_cnc_dialog_desc, itemCount)
                unsellableSubTitle?.text = clickAndCollectText
            }
            Delivery.DASH.name -> {
                val dashText =  resources.getQuantityText(R.plurals.remove_items_dash_dialog_desc, itemCount)
                unsellableSubTitle?.text =  dashText
            }
            else -> {
                val standardDeliveryText = resources.getQuantityText(R.plurals.remove_items_standard_dialog_desc, itemCount)
                unsellableSubTitle?.text = standardDeliveryText
            }
        }
        cancelBtn?.apply {
            visibility = View.VISIBLE
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener(this@UnsellableItemsBottomSheetDialog)
        }
        loadUnsellableItems()
    }

    private fun UnsellableItemsBottomSheetDialogBinding.loadUnsellableItems() {
        rcvItemsList?.layoutManager = LinearLayoutManager(activity)
        commerceItems?.let { rcvItemsList?.adapter = UnsellableItemsListAdapter(it) }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cancel_btn -> {
                UnSellableItemsLiveData.value = false
                confirmRemoveItems()
            }

            R.id.removeItems -> {
                UnSellableItemsLiveData.value = true
                confirmRemoveItems()
            }
        }
    }

    private fun confirmRemoveItems() {
        dismiss()
    }
}