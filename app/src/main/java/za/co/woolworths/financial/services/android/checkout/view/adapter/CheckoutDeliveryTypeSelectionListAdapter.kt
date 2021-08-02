package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.checkout_address_confirmation_selection_delivery_list.view.*
import za.co.woolworths.financial.services.android.ui.extension.bindColor

/**
 * Created by Kunal Uttarwar on 02/08/21.
 */
class CheckoutDeliveryTypeSelectionListAdapter(
    private var openDayDeliverySlotsList: List<Any>?,
    private val listner: EventListner
) :
    RecyclerView.Adapter<CheckoutDeliveryTypeSelectionListAdapter.CheckoutDeliveryTypeSelectionViewHolder>() {

    var checkedItemPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CheckoutDeliveryTypeSelectionListAdapter.CheckoutDeliveryTypeSelectionViewHolder {
        return CheckoutDeliveryTypeSelectionViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.checkout_address_confirmation_selection_delivery_list,
                    parent,
                    false
                )
        )
    }

    override fun getItemCount(): Int {
        if (openDayDeliverySlotsList?.isNullOrEmpty() == true)
            return 0
        return openDayDeliverySlotsList?.size!!
    }

    override fun onBindViewHolder(
        holder: CheckoutDeliveryTypeSelectionListAdapter.CheckoutDeliveryTypeSelectionViewHolder,
        position: Int
    ) {
        holder.bindItem(position)
    }

    fun setData(openDayDeliverySlotsList: List<Any>?) {
        this.openDayDeliverySlotsList = openDayDeliverySlotsList
    }

    inner class CheckoutDeliveryTypeSelectionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bindItem(position: Int) {
            itemView.apply {
                openDayDeliverySlotsList?.get(position)?.let {
                    title.text =
                        (openDayDeliverySlotsList?.get(position) as Map<Any, String>).getValue("deliveryType")
                    subTitle.text =
                        (openDayDeliverySlotsList?.get(position) as Map<Any, String>).getValue("deliveryDate")
                    selector.isChecked = checkedItemPosition == position
                    addressSelectionLayout.setBackgroundColor(
                        if (selector.isChecked) bindColor(R.color.selected_address_background_color) else bindColor(
                            R.color.white
                        )
                    )
                    if (selector.isChecked) {
                        //listner.hideErrorView()
                    }
                }

                setOnClickListener {
                    openDayDeliverySlotsList?.get(position)?.let {
                        listner.selectedDeliveryType(it)
                    }

                    checkedItemPosition = position
                    notifyDataSetChanged()
                }
            }
        }
    }

    interface EventListner {
        fun selectedDeliveryType(deliveryType: Any)
    }
}
