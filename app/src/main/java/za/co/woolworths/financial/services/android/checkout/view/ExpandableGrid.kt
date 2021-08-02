package za.co.woolworths.financial.services.android.checkout.view

import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.checkout_delivery_time_slot_selection_fragment.*
import za.co.woolworths.financial.services.android.checkout.service.network.AvailableDeliverySlotsResponse
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
        LIGHT_GREY(R.color.checkout_delivering_title_background),
        WHITE(R.color.white),
        LIGHT_BLUE(R.color.light_blue)
    }

    fun createTimeSlotGridView(deliverySlots: SortedJoinDeliverySlot?, weekNumber: Int) {
        val deliveryGridList: ArrayList<DeliveryGridModel> = ArrayList()
        val weekList = deliverySlots?.week
        if (!weekList.isNullOrEmpty()) {
            for (weekItem in weekList) {
                val slotsList = weekItem.slots
                if (!slotsList.isNullOrEmpty()) {
                    for (slot in slotsList) {
                        var gridTitle = ""
                        var gridColor = SlotGridColors.LIGHT_GREY.color
                        if (slot.available == true) {
                            if (slot.freeDeliverySlot == true) {
                                if (slot.hasReservation == true || slot.selected == true) {
                                    slot.selected = true
                                    gridColor = SlotGridColors.DARK_GREEN.color
                                } else {
                                    gridColor = SlotGridColors.LIGHT_GREEN.color
                                }
                                gridTitle = fragment.getString(R.string.free_delivery_slot)
                            } else {
                                if (slot.hasReservation == true || slot.selected == true) {
                                    slot.selected = true
                                    gridColor = SlotGridColors.LIGHT_BLUE.color
                                } else {
                                    gridColor = SlotGridColors.WHITE.color
                                }
                                gridTitle = fragment.getString(R.string.currency).plus(slot.slotCost.toString())
                            }
                        } else {
                            gridColor = SlotGridColors.LIGHT_GREY.color
                        }
                        deliveryGridList.add(
                            DeliveryGridModel(
                                gridTitle,
                                gridColor,
                                slot
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
            if (deliveryGridList[position].slot.available == true) {
                for (model in deliveryGridList) {
                    model.slot.selected = false
                    if (model.slot.available == true) {
                        if (model.slot.freeDeliverySlot == true)
                            model.backgroundImgColor = SlotGridColors.LIGHT_GREEN.color
                        else
                            model.backgroundImgColor = SlotGridColors.WHITE.color
                    } else {
                        model.backgroundImgColor = SlotGridColors.LIGHT_GREY.color
                    }
                }

                val deliveryGridModel: DeliveryGridModel = deliveryGridList[position]
                deliveryGridModel.slot.selected = true
                deliveryGridModel.backgroundImgColor =
                    if (deliveryGridModel.slot.freeDeliverySlot == true) SlotGridColors.DARK_GREEN.color else SlotGridColors.LIGHT_BLUE.color
                //set selected slot in Main list
                if (fragment is CheckoutAddAddressReturningUserFragment) {
                    setSlotSelection(weekNumber, position, true, fragment.getSelectedSlotResponse())
                    fragment.setSelectedFoodSlot(deliveryGridModel.slot)
                }
                adapter?.notifyDataSetChanged()
            }
        }
    }

    fun setSlotSelection(
        weekNumber: Int,
        position: Int,
        isSelected: Boolean,
        selectedSlotResponse: AvailableDeliverySlotsResponse?
    ) {
        val hrsSlotSize =
            selectedSlotResponse?.sortedJoinDeliverySlots?.get(weekNumber)?.hourSlots?.size ?: 0
        val weekPosition = position / hrsSlotSize
        val remainder = position % hrsSlotSize
        if (fragment is CheckoutAddAddressReturningUserFragment) {
            fragment.setSelectedSlotResponse(setAllSlotSelection(selectedSlotResponse, false))
        }
        selectedSlotResponse?.sortedJoinDeliverySlots?.get(weekNumber)?.week?.get(weekPosition)?.slots?.get(
            remainder
        )?.selected = isSelected
    }

    fun setAllSlotSelection(
        availableDeliverySlotsResponse: AvailableDeliverySlotsResponse?,
        isSelected: Boolean
    ): AvailableDeliverySlotsResponse? {
        val deliverySlots = availableDeliverySlotsResponse?.sortedJoinDeliverySlots
        if (deliverySlots != null) {
            for (slots in deliverySlots) {
                val week = slots.week
                if (week != null) {
                    for (weeks in week) {
                        val slot = weeks.slots
                        if (slot != null) {
                            for (slots in slot) {
                                slots.selected = isSelected
                            }
                        }
                    }
                }
            }
        }
        return availableDeliverySlotsResponse
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