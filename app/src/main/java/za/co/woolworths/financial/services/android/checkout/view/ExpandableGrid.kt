package za.co.woolworths.financial.services.android.checkout.view

import android.view.View
import android.widget.GridView
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.facebook.shimmer.Shimmer
import kotlinx.android.synthetic.main.checkout_add_address_retuning_user.*
import kotlinx.android.synthetic.main.checkout_delivery_time_slot_selection_fragment.*
import kotlinx.android.synthetic.main.checkout_grid_layout_other.*
import kotlinx.android.synthetic.main.checkout_how_would_you_delivered.*
import za.co.woolworths.financial.services.android.checkout.service.network.ConfirmDeliveryAddressResponse
import za.co.woolworths.financial.services.android.checkout.service.network.HeaderDate
import za.co.woolworths.financial.services.android.checkout.service.network.HourSlots
import za.co.woolworths.financial.services.android.checkout.service.network.Week
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.DeliveryType
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.WeekCounter.SECOND
import za.co.woolworths.financial.services.android.checkout.view.ExpandableGrid.DeliveryFoodOrOther.FOOD
import za.co.woolworths.financial.services.android.checkout.view.ExpandableGrid.DeliveryFoodOrOther.OTHER
import za.co.woolworths.financial.services.android.checkout.view.ExpandableGrid.SlotGridColors.*
import za.co.woolworths.financial.services.android.checkout.view.adapter.DeliverySlotsGridViewAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.SlotsDateGridViewAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.SlotsTimeGridViewAdapter
import za.co.woolworths.financial.services.android.checkout.viewmodel.DeliveryGridModel
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable

/**
 * Created by Kunal Uttarwar on 25/07/21.
 */
class ExpandableGrid(val fragment: Fragment) {

    private val slotGridList: HashMap<DeliveryFoodOrOther, ArrayList<DeliveryGridModel>> = HashMap()
    var deliverySlotsGridViewAdapter: DeliverySlotsGridViewAdapter? = null

    companion object {
        const val DEFAULT_POSITION = -1
    }

    enum class SlotGridColors(val color: Int) {
        LIGHT_GREEN(R.color.light_green),
        DARK_GREEN(R.color.dark_green),
        LIGHT_GREY(R.color.checkout_delivering_title_background),
        WHITE(R.color.white),
        LIGHT_BLUE(R.color.light_blue)
    }

    enum class DeliveryFoodOrOther(val number: Int) {
        FOOD(0),
        OTHER(1)
    }

    fun initialiseGridView(
        confirmDeliveryAddressResponse: ConfirmDeliveryAddressResponse?,
        weekNumber: Int,
        deliveryType: DeliveryType
    ) {

        val sortedDeliverySlots = when (deliveryType) {
            DeliveryType.MIXED_FOOD -> confirmDeliveryAddressResponse?.sortedFoodDeliverySlots
            DeliveryType.MIXED_OTHER -> confirmDeliveryAddressResponse?.sortedOtherDeliverySlots
            DeliveryType.ONLY_FOOD -> confirmDeliveryAddressResponse?.sortedJoinDeliverySlots
            DeliveryType.ONLY_OTHER -> confirmDeliveryAddressResponse?.sortedJoinDeliverySlots
            else -> null
        }

        if (sortedDeliverySlots.isNullOrEmpty() || weekNumber < 0 || weekNumber >= sortedDeliverySlots.size) {
            return
        }

        if (sortedDeliverySlots.size == SECOND.week) {
            if (DeliveryType.MIXED_OTHER == deliveryType || DeliveryType.ONLY_OTHER == deliveryType) {
                hidePreviousNextOtherBtn()
            } else
                hidePreviousNextFoodBtn()
        } else {
            if (DeliveryType.MIXED_OTHER == deliveryType || DeliveryType.ONLY_OTHER == deliveryType) {
                showPreviousNextOtherBtn()
            } else
                showPreviousNextFoodBtn()
        }

        when (deliveryType) {
            DeliveryType.MIXED_FOOD -> {
                val deliverySlots =
                    confirmDeliveryAddressResponse?.sortedFoodDeliverySlots?.get(weekNumber)
                createTimingsGrid(deliverySlots?.hourSlots, fragment.timingsGridViewFood)
                createDatesGrid(deliverySlots?.headerDates, fragment.dateGridViewFood)
                createTimeSlotGridView(
                    deliverySlots?.week,
                    deliverySlots?.hourSlots,
                    weekNumber,
                    fragment.timeSlotsGridViewFood,
                    deliveryType
                )
            }
            DeliveryType.MIXED_OTHER -> {
                val deliverySlots =
                    confirmDeliveryAddressResponse?.sortedOtherDeliverySlots?.get(weekNumber)
                createTimingsGrid(deliverySlots?.hourSlots, fragment.timingsGridViewOther)
                createDatesGrid(deliverySlots?.headerDates, fragment.dateGridViewOther)
                createTimeSlotGridView(
                    deliverySlots?.week,
                    deliverySlots?.hourSlots,
                    weekNumber,
                    fragment.timeSlotsGridViewOther,
                    deliveryType
                )
            }
            DeliveryType.ONLY_FOOD -> {
                val deliverySlots =
                    confirmDeliveryAddressResponse?.sortedJoinDeliverySlots?.get(weekNumber)
                createTimingsGrid(deliverySlots?.hourSlots, fragment.timingsGridViewFood)
                createDatesGrid(deliverySlots?.headerDates, fragment.dateGridViewFood)
                createTimeSlotGridView(
                    deliverySlots?.week,
                    deliverySlots?.hourSlots,
                    weekNumber,
                    fragment.timeSlotsGridViewFood,
                    deliveryType
                )
            }
            DeliveryType.ONLY_OTHER -> {
                val deliverySlots =
                    confirmDeliveryAddressResponse?.sortedJoinDeliverySlots?.get(weekNumber)
                createTimingsGrid(deliverySlots?.hourSlots, fragment.timingsGridViewOther)
                createDatesGrid(deliverySlots?.headerDates, fragment.dateGridViewOther)
                createTimeSlotGridView(
                    deliverySlots?.week,
                    deliverySlots?.hourSlots,
                    weekNumber,
                    fragment.timeSlotsGridViewOther,
                    deliveryType
                )
            }
            else -> {
                // Nothing
            }
        }
    }

    private fun createTimeSlotGridView(
        deliveryWeekSlots: List<Week>?,
        deliveryHoursSlots: List<HourSlots>?,
        weekNumber: Int,
        slotGridView: ExpandableGridViewScrollable,
        deliveryType: DeliveryType
    ) {
        val deliveryGridList: ArrayList<DeliveryGridModel> = ArrayList()
        if (!deliveryWeekSlots.isNullOrEmpty()) {
            for (weekItem in deliveryWeekSlots) {
                val slotsList = weekItem.slots
                if (!slotsList.isNullOrEmpty()) {
                    for (slot in slotsList) {
                        var gridTitle = ""
                        var gridColor = LIGHT_GREY.color
                        if (slot.available == true) {
                            if (slot.freeDeliverySlot == true) {
                                if (slot.hasReservation == true || slot.selected == true) {
                                    slot.selected = true
                                    gridColor = DARK_GREEN.color
                                } else {
                                    gridColor = LIGHT_GREEN.color
                                }
                                gridTitle = fragment.getString(R.string.free_delivery_slot)
                            } else {
                                if (slot.hasReservation == true || slot.selected == true) {
                                    slot.selected = true
                                    gridColor = LIGHT_BLUE.color
                                } else {
                                    gridColor = WHITE.color
                                }
                                gridTitle = fragment.getString(R.string.currency)
                                    .plus(slot.slotCost.toString())
                            }
                        } else {
                            gridColor = LIGHT_GREY.color
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
            // This condition is to keep two diff list for slots.
            if (deliveryType.equals(DeliveryType.ONLY_FOOD) || deliveryType.equals(DeliveryType.MIXED_FOOD)) {
                slotGridList[FOOD] = deliveryGridList
            } else {
                slotGridList[OTHER] = deliveryGridList
            }
        }
        deliverySlotsGridViewAdapter = fragment.context?.let {
            DeliverySlotsGridViewAdapter(
                it,
                R.layout.delivery_grid_card_item,
                deliveryGridList
            )
        }
        slotGridView.apply {
            numColumns = deliveryHoursSlots?.size ?: 0
            setViewExpanded(true)
            this.adapter = deliverySlotsGridViewAdapter
        }

        slotGridView.setOnItemClickListener { _, _, position, id ->
            val deliveryList =
                if (deliveryType.equals(DeliveryType.ONLY_FOOD) || deliveryType.equals(DeliveryType.MIXED_FOOD)) slotGridList[FOOD] else slotGridList[OTHER]
            if (deliveryList?.get(position)?.slot?.available == true) {
                gridOnClickListner(
                    deliveryType, position, weekNumber,
                    slotGridView.adapter as? DeliverySlotsGridViewAdapter
                )
            }
        }
    }

    fun gridOnClickListner(
        deliveryType: DeliveryType,
        position: Int,
        weekNumber: Int,
        adapter: DeliverySlotsGridViewAdapter?
    ) {
        val deliveryList =
            if (deliveryType.equals(DeliveryType.ONLY_FOOD) || deliveryType.equals(DeliveryType.MIXED_FOOD)) slotGridList[FOOD] else slotGridList[OTHER]

        if (!deliveryList.isNullOrEmpty()) {
            for (model in deliveryList) {
                model.slot.selected = false
                if (model.slot.available == true) {
                    if (model.slot.freeDeliverySlot == true)
                        model.backgroundImgColor = LIGHT_GREEN.color
                    else
                        model.backgroundImgColor = WHITE.color
                } else {
                    model.backgroundImgColor = LIGHT_GREY.color
                }
            }
        }

        if (position != DEFAULT_POSITION && !deliveryList.isNullOrEmpty()) {
            val deliveryGridModel: DeliveryGridModel = deliveryList[position]
            deliveryGridModel.slot.selected = true
            deliveryGridModel.backgroundImgColor =
                if (deliveryGridModel.slot.freeDeliverySlot == true) DARK_GREEN.color else LIGHT_BLUE.color
            //set selected slot in Main list
            if (fragment is CheckoutAddAddressReturningUserFragment) {
                setSlotSelection(
                    weekNumber,
                    position,
                    true,
                    fragment.getSelectedSlotResponse(deliveryType),
                    deliveryType
                )
                fragment.setSelectedFoodOrOtherSlot(deliveryGridModel.slot, deliveryType)
            }
        }
        adapter?.notifyDataSetChanged()
    }

    private fun setSlotSelection(
        weekNumber: Int,
        position: Int,
        isSelected: Boolean,
        selectedSlotResponse: ConfirmDeliveryAddressResponse?,
        deliveryType: DeliveryType
    ) {
        val hrsSlotSize =
            when {
                deliveryType.equals(DeliveryType.MIXED_FOOD) -> selectedSlotResponse?.sortedFoodDeliverySlots?.get(
                    weekNumber
                )?.hourSlots?.size ?: 0
                deliveryType.equals(DeliveryType.MIXED_OTHER) -> selectedSlotResponse?.sortedOtherDeliverySlots?.get(
                    weekNumber
                )?.hourSlots?.size ?: 0
                else -> selectedSlotResponse?.sortedJoinDeliverySlots?.get(weekNumber)?.hourSlots?.size
                    ?: 0
            }
        val weekPosition = position / hrsSlotSize
        val remainder = position % hrsSlotSize
        if (fragment is CheckoutAddAddressReturningUserFragment) {
            fragment.setSelectedSlotResponse(
                setAllSlotSelection(selectedSlotResponse, false, deliveryType),
                deliveryType
            )
        }
        when (deliveryType) {
            DeliveryType.MIXED_FOOD -> {
                selectedSlotResponse?.sortedFoodDeliverySlots?.get(weekNumber)?.week?.get(
                    weekPosition
                )?.slots?.get(
                    remainder
                )?.selected = isSelected
            }
            DeliveryType.MIXED_OTHER -> {
                selectedSlotResponse?.sortedOtherDeliverySlots?.get(weekNumber)?.week?.get(
                    weekPosition
                )?.slots?.get(
                    remainder
                )?.selected = isSelected
            }
            else -> {
                selectedSlotResponse?.sortedJoinDeliverySlots?.get(weekNumber)?.week?.get(
                    weekPosition
                )?.slots?.get(
                    remainder
                )?.selected = isSelected
            }
        }
    }

    private fun setAllSlotSelection(
        confirmDeliveryAddressResponse: ConfirmDeliveryAddressResponse?,
        isSelected: Boolean,
        deliveryType: DeliveryType
    ): ConfirmDeliveryAddressResponse? {
        when (deliveryType) {
            DeliveryType.MIXED_FOOD -> {
                val deliverySlots = confirmDeliveryAddressResponse?.sortedFoodDeliverySlots
                if (deliverySlots != null) {
                    for (slots in deliverySlots) {
                        setWeekSlotsResponse(slots.week, isSelected)
                    }
                }
            }
            DeliveryType.MIXED_OTHER -> {
                val deliverySlots = confirmDeliveryAddressResponse?.sortedOtherDeliverySlots
                if (deliverySlots != null) {
                    for (slots in deliverySlots) {
                        setWeekSlotsResponse(slots.week, isSelected)
                    }
                }
            }
            else -> {
                val deliverySlots = confirmDeliveryAddressResponse?.sortedJoinDeliverySlots
                if (deliverySlots != null) {
                    for (slots in deliverySlots) {
                        setWeekSlotsResponse(slots.week, isSelected)
                    }
                }
            }
        }
        return confirmDeliveryAddressResponse
    }

    private fun setWeekSlotsResponse(week: List<Week>?, isSelected: Boolean) {
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

    private fun createTimingsGrid(hoursSlots: List<HourSlots>?, timeGridView: GridView) {
        timeGridView.numColumns =
            hoursSlots?.size ?: 0 + 1 // Adding 1 only to match slots title grid with actual slots
        timeGridView.adapter = fragment.context?.let {
            hoursSlots?.let { it1 ->
                SlotsTimeGridViewAdapter(
                    it,
                    R.layout.checkout_delivery_slot_timedate_item,
                    it1
                )
            }
        }
    }

    private fun createDatesGrid(
        datesSlots: List<HeaderDate>?,
        dateGridView: ExpandableGridViewScrollable
    ) {
        dateGridView.setViewExpanded(true)
        dateGridView.adapter = fragment.context?.let {
            datesSlots?.let { it1 ->
                SlotsDateGridViewAdapter(
                    it,
                    R.layout.checkout_delivery_slot_timedate_item,
                    it1
                )
            }
        }
    }

    fun setUpShimmerView() {
        val shimmer = Shimmer.AlphaHighlightBuilder().build()
        fragment.howWouldYouDeliveredShimmerFrameLayout?.setShimmer(shimmer)
        fragment.selectDeliveryTimeSlotSubTitleShimmerFrameLayout?.setShimmer(shimmer)
    }

    fun showDeliveryTypeShimmerView() {
        fragment.howWouldYouDeliveredShimmerFrameLayout?.startShimmer()
        fragment.selectDeliveryTimeSlotSubTitleShimmerFrameLayout?.startShimmer()
        fragment.howWouldYouDeliveredTitle?.visibility = View.INVISIBLE
        fragment.selectDeliveryTimeSlotSubTitle?.visibility = View.INVISIBLE
    }

    fun hideDeliveryTypeShimmerView() {
        fragment.howWouldYouDeliveredShimmerFrameLayout?.stopShimmer()
        fragment.selectDeliveryTimeSlotSubTitleShimmerFrameLayout?.stopShimmer()
        fragment.howWouldYouDeliveredShimmerFrameLayout?.setShimmer(null)
        fragment.selectDeliveryTimeSlotSubTitleShimmerFrameLayout?.setShimmer(null)
        fragment.howWouldYouDeliveredTitle?.visibility = View.VISIBLE
        fragment.selectDeliveryTimeSlotSubTitle?.visibility = View.VISIBLE

        fragment.checkoutHowWouldYouDeliveredLayout?.visibility = View.GONE

    }

    fun enableNextBtnFood() {
        setEnableBackgroundColor(fragment.nextImgBtnFood, fragment.nextFoodTextView)
    }

    fun enableNextBtnOther() {
        setEnableBackgroundColor(fragment.nextImgBtnOther, fragment.nextOtherTextView)
    }

    fun enablePreviousBtnFood() {
        setEnableBackgroundColor(fragment.previousImgBtnFood, fragment.previousFoodTextView)
    }

    fun enablePreviousBtnOther() {
        setEnableBackgroundColor(fragment.previousImgBtnOther, fragment.previousOtherTextView)
    }

    fun disableNextBtnFood() {
        setDisableBackgroundColor(fragment.nextImgBtnFood, fragment.nextFoodTextView)
    }

    fun disableNextBtnOther() {
        setDisableBackgroundColor(fragment.nextImgBtnOther, fragment.nextOtherTextView)
    }

    fun disablePreviousBtnFood() {
        setDisableBackgroundColor(fragment.previousImgBtnFood, fragment.previousFoodTextView)
    }

    fun disablePreviousBtnOther() {
        setDisableBackgroundColor(fragment.previousImgBtnOther, fragment.previousOtherTextView)
    }

    private fun hidePreviousNextOtherBtn() {
        fragment.previousNextBtnLayoutOther.visibility = View.GONE
    }

    private fun showPreviousNextOtherBtn() {
        fragment.previousNextBtnLayoutOther.visibility = View.VISIBLE
    }

    private fun hidePreviousNextFoodBtn() {
        fragment.previousNextBtnLayoutFood.visibility = View.GONE
    }

    private fun showPreviousNextFoodBtn() {
        fragment.previousNextBtnLayoutFood.visibility = View.VISIBLE
    }

    private fun setDisableBackgroundColor(imgBtn: ImageButton, textView: TextView) {
        imgBtn.background = bindDrawable(R.drawable.transperant_options_button)
        textView.setTextColor(
            ContextCompat.getColor(
                fragment.requireContext(),
                R.color.color_cccccc
            )
        )
        imgBtn.isEnabled = false
        textView.isEnabled = false
    }

    private fun setEnableBackgroundColor(imgBtn: ImageButton, textView: TextView) {
        imgBtn.background = bindDrawable(R.drawable.rounded_view_grey_img_button)
        textView.setTextColor(
            ContextCompat.getColor(
                fragment.requireContext(),
                R.color.checkout_delivering_title
            )
        )
        imgBtn.isEnabled = true
        textView.isEnabled = true
    }
}