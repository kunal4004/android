package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CheckoutAddressConfirmationSelectionDeliveryListBinding
import za.co.woolworths.financial.services.android.checkout.service.network.OpenDayDeliverySlot
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment
import za.co.woolworths.financial.services.android.ui.extension.bindColor
import java.util.*

/**
 * Created by Kunal Uttarwar on 02/08/21.
 */
class CheckoutDeliveryTypeSelectionListAdapter(
    private var openDayDeliverySlotsList: List<OpenDayDeliverySlot>?,
    private val listner: EventListner,
    private val type: CheckoutAddAddressReturningUserFragment.DeliveryType,
    selectedOpedDayDeliverySlot: OpenDayDeliverySlot
) :
    RecyclerView.Adapter<CheckoutDeliveryTypeSelectionListAdapter.CheckoutDeliveryTypeSelectionViewHolder>() {
    var checkedItemPosition = -1

    companion object {
        const val DELIVERY_TYPE_TIMESLOT = "Timeslot"
    }

    init {
        // comming back on same screen should pre select the option.
        if (!selectedOpedDayDeliverySlot.deliveryType.isNullOrEmpty()) {
            openDayDeliverySlotsList?.forEach {
                if (it.deliveryType == selectedOpedDayDeliverySlot.deliveryType) {
                    checkedItemPosition = openDayDeliverySlotsList?.indexOf(it) ?: -1
                    onItemClicked(checkedItemPosition)
                    return@forEach
                }
            }

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CheckoutDeliveryTypeSelectionListAdapter.CheckoutDeliveryTypeSelectionViewHolder {
        return CheckoutDeliveryTypeSelectionViewHolder(
            CheckoutAddressConfirmationSelectionDeliveryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class CheckoutDeliveryTypeSelectionViewHolder(private val binding: CheckoutAddressConfirmationSelectionDeliveryListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindItem(position: Int) {
            with(binding){
            itemView.apply {
                hideShimmer(this)
                openDayDeliverySlotsList?.get(position)?.let { it ->
                    val deliveryType =
                        it.deliveryType
                    title.text = deliveryType?.capitalize(Locale.ROOT)
                    subTitle.text = if (deliveryType.equals(DELIVERY_TYPE_TIMESLOT))
                        Html.fromHtml(it.description) else it.deliveryInDays
                    editAddressImageView.visibility = View.GONE
                    slotPriceButton.visibility = View.VISIBLE
                    if (it.amount != 0L) {
                        slotPriceButton.text = context.getString(R.string.currency).plus(
                            it.amount?.toString()
                        )
                    } else {
                        slotPriceButton.text = context.getString(R.string.free_delivery_slot)
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
                    onItemClicked(position)
                }
            }
            }
        }
    }

    private fun onItemClicked(position: Int) {
        if (position < 0 || position >= itemCount) {
            return
        }
        openDayDeliverySlotsList?.get(position)?.let {
            listner.selectedDeliveryType(it, type, position)
            notifyItemChanged(position, it)
        }
        // update last position as well
        val previousPosition = checkedItemPosition
        checkedItemPosition = position

        if (previousPosition < 0 || previousPosition >= itemCount) {
            return
        }
        openDayDeliverySlotsList?.get(previousPosition)?.let {
            notifyItemChanged(previousPosition, it)
        }
    }

    private fun CheckoutAddressConfirmationSelectionDeliveryListBinding.hideShimmer(view: View) {
        view.apply {
            selectorShimmerFrameLayout.setShimmer(null)
            selectorShimmerFrameLayout.stopShimmer()
            selector.visibility = View.VISIBLE
            titleShimmerLayout.setShimmer(null)
            titleShimmerLayout.stopShimmer()
            title.visibility = View.VISIBLE
            subtitleShimmerLayout.setShimmer(null)
            subtitleShimmerLayout.stopShimmer()
            subTitle.visibility = View.VISIBLE
            slotPriceButtonShimmerFrameLayout.setShimmer(null)
            slotPriceButtonShimmerFrameLayout.stopShimmer()
            slotPriceButton.visibility = View.VISIBLE
        }
    }

    interface EventListner {
        fun selectedDeliveryType(
            openDayDeliverySlot: OpenDayDeliverySlot,
            type: CheckoutAddAddressReturningUserFragment.DeliveryType,
            position: Int
        )
    }
}