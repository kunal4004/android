package za.co.woolworths.financial.services.android.ui.views

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.unsellable_items_bottom_sheet_dialog.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutActivity
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
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.unsellable_items_bottom_sheet_dialog,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        incSwipeCloseIndicator.visibility = View.VISIBLE
        removeItems?.setOnClickListener(this)
        arguments?.apply {
            deliveryType = getString(KEY_ARGS_DELIVERY_TYPE, Delivery.STANDARD.type)
            commerceItems = getSerializable(KEY_ARGS_UNSELLABLE_COMMERCE_ITEMS) as? ArrayList<UnSellableCommerceItem>
        }
        when(deliveryType) {
            Delivery.STANDARD.name -> {
                subTitle.text = getText(R.string.remove_items_standard_dialog_desc)
            }
            Delivery.CNC.name -> {
                subTitle.text = getText(R.string.remove_items_cnc_dialog_desc)
            }
            Delivery.DASH.name -> {
                subTitle.text = getText(R.string.remove_items_dash_dialog_desc)
            }
            else -> {
                subTitle.text = getText(R.string.remove_items_standard_dialog_desc)
            }
        }
        if(activity is CheckoutActivity) {
            initCheckoutUnsellableItemsView()
        } else {
            changeStore?.apply {
                visibility = View.VISIBLE
                paintFlags = Paint.UNDERLINE_TEXT_FLAG
                setOnClickListener(this@UnsellableItemsBottomSheetDialog)
            }
        }
        loadUnsellableItems()
    }

    /**
     * This function will get called when navigated from checkout page
     */
    private fun initCheckoutUnsellableItemsView() {
        changeStore?.visibility = View.INVISIBLE
        unsellableItemsFragmentRelativeLayout?.background =
            context?.let { ContextCompat.getDrawable(it, R.color.white) }
    }

    private fun loadUnsellableItems() {
        rcvItemsList?.layoutManager = LinearLayoutManager(activity)
        commerceItems?.let { rcvItemsList?.adapter = UnsellableItemsListAdapter(it) }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.changeStore -> {
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