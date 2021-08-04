package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.checkout_address_confirmation_selection_delivery_list.view.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment
import za.co.woolworths.financial.services.android.ui.extension.bindColor

/**
 * Created by Kunal Uttarwar on 02/08/21.
 */
class CheckoutDeliveryTypeSelectionListAdapter(
    private var openDayDeliverySlotsList: List<Any>?,
    private val listner: EventListner,
    private val type: CheckoutAddAddressReturningUserFragment.DeliveryType
) :
    RecyclerView.Adapter<CheckoutDeliveryTypeSelectionListAdapter.CheckoutDeliveryTypeSelectionViewHolder>() {

    var checkedItemPosition = -1

    companion object {
        const val DELIVERY_TYPE_TIMESLOT = "Timeslot"
    }

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
        return openDayDeliverySlotsList?.size ?: 0
    }

    override fun onBindViewHolder(
        holder: CheckoutDeliveryTypeSelectionListAdapter.CheckoutDeliveryTypeSelectionViewHolder,
        position: Int
    ) {
        holder.bindItem(position)
    }

    fun getEstimatedDeliveryDates(deliverySlotsList: Map<Any, Double>, context: Context): String {
        val startDeliveryDay = ((deliverySlotsList).getValue("startDeliveryDay")).toInt().toString()
        val endDeliveryDay = ((deliverySlotsList).getValue("endDeliveryDay")).toInt().toString()
        return context.getString(R.string.working_days_text, startDeliveryDay, endDeliveryDay)
    }

    inner class CheckoutDeliveryTypeSelectionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bindItem(position: Int) {
            itemView.apply {
                openDayDeliverySlotsList?.get(position)?.let {
                    val deliveryType =
                        (openDayDeliverySlotsList?.get(position) as Map<Any, String>).getValue("deliveryType")
                    title.text = deliveryType
                    subTitle.text = if (deliveryType.equals(DELIVERY_TYPE_TIMESLOT)) {
                        Html.fromHtml(
                            (openDayDeliverySlotsList?.get(position) as Map<Any, String>).getValue(
                                "description"
                            )
                        )
                    } else {
                        getEstimatedDeliveryDates(
                            openDayDeliverySlotsList?.get(position) as Map<Any, Double>,
                            context
                        )
                    }
                    editAddressImageView.visibility = View.GONE
                    slotPriceButton.visibility = View.VISIBLE
                    slotPriceButton.text = context.getString(R.string.currency).plus(
                        (openDayDeliverySlotsList?.get(position) as Map<Any, Int>).getValue("amount")
                            .toString()
                    )
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
                        listner.selectedDeliveryType(it, type)
                    }

                    checkedItemPosition = position
                    notifyDataSetChanged()
                }
            }
        }
    }

    interface EventListner {
        fun selectedDeliveryType(
            deliveryType: Any,
            type: CheckoutAddAddressReturningUserFragment.DeliveryType
        )
    }
}
