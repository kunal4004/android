package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.checkout_address_confirmation_selection_delivery_list.view.*
import za.co.woolworths.financial.services.android.checkout.service.network.OpenDayDeliverySlot
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment
import za.co.woolworths.financial.services.android.ui.extension.bindColor
import java.util.*

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
        const val DELIVERY_TYPE_STANDARD = "standard"
        const val DELIVERY_TYPE_VALUE = "value"
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

    inner class CheckoutDeliveryTypeSelectionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bindItem(position: Int) {
            itemView.apply {
                hideShimmer(this)
                openDayDeliverySlotsList?.get(position)?.let {
                    when (it) {
                        is OpenDayDeliverySlot -> {
                            val deliveryType =
                                it.deliveryType
                            title.text = deliveryType?.capitalize(Locale.ROOT)
                            subTitle.text = if (deliveryType.equals(DELIVERY_TYPE_TIMESLOT))
                                Html.fromHtml(it.description) else it.deliveryInDays
                            editAddressImageView.visibility = View.GONE
                            slotPriceButton.visibility = View.VISIBLE
                            slotPriceButton.text = context.getString(R.string.currency).plus(
                                it.amount?.toString()
                            )
                        }
                    }

                    selector.isChecked = checkedItemPosition == position
                    addressSelectionLayout.setBackgroundColor(
                        if (selector.isChecked) bindColor(R.color.selected_address_background_color) else bindColor(
                            R.color.white
                        )
                    )
                    title.setBackgroundColor(
                        if (selector.isChecked) bindColor(R.color.selected_address_background_color) else bindColor(
                            R.color.white
                        )
                    )
                    subTitle.setBackgroundColor(
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
                        listner.selectedDeliveryType(it, type, position)
                    }

                    checkedItemPosition = position
                    notifyDataSetChanged()
                }
            }
        }
    }

    private fun hideShimmer(view: View) {
        view.apply {
            selectorShimmerFrameLayout?.setShimmer(null)
            selectorShimmerFrameLayout?.stopShimmer()
            selector.visibility = View.VISIBLE

            titleShimmerLayout?.setShimmer(null)
            titleShimmerLayout?.stopShimmer()
            title.visibility = View.VISIBLE

            subtitleShimmerLayout?.setShimmer(null)
            subtitleShimmerLayout?.stopShimmer()
            subTitle.visibility = View.VISIBLE

            slotPriceButtonShimmerFrameLayout?.setShimmer(null)
            slotPriceButtonShimmerFrameLayout?.stopShimmer()
            slotPriceButton.visibility = View.VISIBLE
        }
    }

    interface EventListner {
        fun selectedDeliveryType(
            deliveryType: Any,
            type: CheckoutAddAddressReturningUserFragment.DeliveryType,
            position: Int
        )
    }
}
