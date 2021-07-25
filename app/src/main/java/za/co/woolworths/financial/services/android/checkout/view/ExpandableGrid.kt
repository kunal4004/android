package za.co.woolworths.financial.services.android.checkout.view

import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.checkout_delivery_time_slot_selection_fragment.*
import za.co.woolworths.financial.services.android.checkout.service.network.HeaderDate
import za.co.woolworths.financial.services.android.checkout.service.network.SortedJoinDeliverySlot
import za.co.woolworths.financial.services.android.checkout.view.adapter.DeliverySlotsGridViewAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.SlotsDateGridViewAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.SlotsTimeGridViewAdapter
import za.co.woolworths.financial.services.android.checkout.viewmodel.DeliveryGridModel

/**
 * Created by Kunal Uttarwar on 25/07/21.
 */
class ExpandableGrid(val fragment: Fragment) {

    enum class SlotGridColors(val color: Int) {
        LIGHT_GREEN(R.color.light_green),
        DARK_GREEN(R.color.dark_green),
        LIGHT_GREY(R.color.checkout_delivering_title_background)
    }

    fun createTimeSlotGridView(deliverySlots: SortedJoinDeliverySlot?) {
        val deliveryGridList: ArrayList<DeliveryGridModel> = ArrayList()
        val weekList = deliverySlots?.week
        if (!weekList.isNullOrEmpty()) {
            for (weekItem in weekList) {
                val slotsList = weekItem.slots
                if (!slotsList.isNullOrEmpty()) {
                    for (slot in slotsList) {
                        var gridTitle: String
                        var gridColor = SlotGridColors.LIGHT_GREY.color
                        var isSelected = slot.selected
                        if (slot.freeDeliverySlot == true) {
                            gridTitle = fragment.getString(R.string.free_delivery_slot)
                            gridColor = SlotGridColors.LIGHT_GREEN.color
                        } else
                            gridTitle = slot.slotCost.toString()
                        if (slot.hasReservation == true) {
                            isSelected = true
                            gridColor = SlotGridColors.DARK_GREEN.color
                        }
                        if (slot.available == true) {
                            //TODO  enable the click for grid slot
                        }
                        deliveryGridList.add(
                            DeliveryGridModel(
                                gridTitle,
                                gridColor,
                                slot.slotId,
                                isSelected == true
                            )
                        )
                    }
                }
            }
        }
        val adapter = fragment.context?.let {
            DeliverySlotsGridViewAdapter(
                it,
                R.layout.delivery_grid_card_item,
                deliveryGridList
            )
        }
        fragment.timeSlotsGridView.numColumns = deliverySlots?.hourSlots?.size ?: 0
        fragment.timeSlotsGridView.setViewExpanded(true)
        fragment.timeSlotsGridView.adapter = adapter

        fragment.timeSlotsGridView.setOnItemClickListener { parent, view, position, id ->
            for (model in deliveryGridList) {
                model.isSelected = false
                model.backgroundImgColor = SlotGridColors.LIGHT_GREEN.color
            }
            val deliveryGridModel: DeliveryGridModel = deliveryGridList[position]
            deliveryGridModel.isSelected = true
            deliveryGridModel.backgroundImgColor = SlotGridColors.DARK_GREEN.color
            adapter?.notifyDataSetChanged()
        }
    }

    fun createTimingsGrid(hoursSlots: List<String>?) {
        fragment.timingsGridView.numColumns =
            hoursSlots?.size ?: 0 + 1 // Adding 1 only to match slots title grid with actual slots
        fragment.timingsGridView.adapter = fragment.context?.let {
            hoursSlots?.let { it1 ->
                SlotsTimeGridViewAdapter(
                    it,
                    R.layout.checkout_delivery_slot_timedate_item,
                    it1
                )
            }
        }
    }

    fun createDatesGrid(datesSlots: List<HeaderDate>?) {
        fragment.dateGridView.setViewExpanded(true)
        fragment.dateGridView.adapter = fragment.context?.let {
            datesSlots?.let { it1 ->
                SlotsDateGridViewAdapter(
                    it,
                    R.layout.checkout_delivery_slot_timedate_item,
                    it1
                )
            }
        }
    }
}