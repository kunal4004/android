package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.checkout_delivery_time_slot_selection_fragment.*
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_food_substitution.*
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.*
import za.co.woolworths.financial.services.android.checkout.view.adapter.DeliverySlotsGridViewAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.SlotsDateGridViewAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.SlotsTimeGridViewAdapter
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.DeliveryGridModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.service.network.ResponseStatus


/**
 * Created by Kunal Uttarwar on 27/05/21.
 */
class CheckoutAddAddressReturningUserFragment : Fragment(), View.OnClickListener {

    private lateinit var checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel

    enum class FoodSubstitution(val rgb: String) {
        PHONE_CONFIRM("YES_CALL_CONFIRM"),
        SIMILAR_SUBSTITUTION("YES"),
        NO_THANKS("NO")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.checkout_add_address_retuning_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() {
        initializeDeliveryFoodItems()
        initializeFoodSubstitution()
    }

    /**
     * Initializes food substitution view and Set by default selection to [FoodSubstitution.SIMILAR_SUBSTITUTION]
     *
     * @see [FoodSubstitution]
     */
    private fun initializeFoodSubstitution() {
        var selectedFoodSubstitution = FoodSubstitution.SIMILAR_SUBSTITUTION
        radioGroupFoodSubstitution?.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioBtnPhoneConfirmation -> {
                    selectedFoodSubstitution = FoodSubstitution.PHONE_CONFIRM
                }
                R.id.radioBtnSimilarSubst -> {
                    selectedFoodSubstitution = FoodSubstitution.SIMILAR_SUBSTITUTION
                }
                R.id.radioBtnNoThanks -> {
                    selectedFoodSubstitution = FoodSubstitution.NO_THANKS
                }
            }
        }
    }

    private fun initializeDeliveryFoodItems() {
        setupViewModel()
        getAvailableDeliverySlots()
        previousImgBtn.setOnClickListener(this)
        nextImgBtn.setOnClickListener(this)
    }

    private fun initializeGrid(
        availableDeliverySlotsResponse: AvailableDeliverySlotsResponse?,
        weekNumber: Int
    ) {
        val deliverySlots = availableDeliverySlotsResponse?.sortedJoinDeliverySlots?.get(weekNumber)
        createTimingsGrid(deliverySlots?.hourSlots)
        createDatesGrid(deliverySlots?.headerDates)
        createTimeSlotGridView(deliverySlots)
    }

    private fun createTimeSlotGridView(deliverySlots: SortedJoinDeliverySlot?) {
        val deliveryGridList: ArrayList<DeliveryGridModel> = ArrayList()
        val weekList = deliverySlots?.week
        if (!weekList.isNullOrEmpty()) {
            for (weekItem in weekList) {
                val slotsList = weekItem.slots
                if (!slotsList.isNullOrEmpty()) {
                    for (slot in slotsList) {
                        var gridTitle = ""
                        var gridColor = R.color.checkout_delivering_title_background
                        var isSelected = slot.selected
                        if (slot.freeDeliverySlot == true) {
                            gridTitle = getString(R.string.free_delivery_slot)
                            gridColor = R.color.light_green
                        }
                        else
                            gridTitle = slot.slotCost.toString()
                        if (slot.hasReservation == true) {
                            isSelected = true
                            gridColor = R.color.dark_green
                        }
                        if (slot.available == true){
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
        val adapter = context?.let {
            DeliverySlotsGridViewAdapter(
                it,
                R.layout.delivery_grid_card_item,
                deliveryGridList
            )
        }
        timeSlotsGridView.numColumns = deliverySlots?.hourSlots?.size ?: 0
        timeSlotsGridView.adapter = adapter

        timeSlotsGridView.setOnItemClickListener { parent, view, position, id ->
            for (model in deliveryGridList) {
                model.isSelected = false
                model.backgroundImgColor = R.color.light_green
            }
            val deliveryGridModel: DeliveryGridModel = deliveryGridList[position]
            deliveryGridModel.isSelected = true
            deliveryGridModel.backgroundImgColor = R.color.dark_green
            adapter?.notifyDataSetChanged()
        }
    }

    private fun createTimingsGrid(hoursSlots: List<String>?) {
        timingsGridView.numColumns =
            hoursSlots?.size ?: 0 + 1 // Adding 1 only to match slots title grid with actual slots
        timingsGridView.adapter = context?.let {
            hoursSlots?.let { it1 ->
                SlotsTimeGridViewAdapter(
                    it,
                    R.layout.checkout_delivery_slot_timedate_item,
                    it1
                )
            }
        }
    }

    private fun createDatesGrid(datesSlots: List<HeaderDate>?) {
        dateGridView.adapter = context?.let {
            datesSlots?.let { it1 ->
                SlotsDateGridViewAdapter(
                    it,
                    R.layout.checkout_delivery_slot_timedate_item,
                    it1
                )
            }
        }
    }

    private fun setupViewModel() {
        checkoutAddAddressNewUserViewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(
                CheckoutAddAddressNewUserInteractor(
                    CheckoutAddAddressNewUserApiHelper(),
                    CheckoutMockApiHelper()
                )
            )
        ).get(CheckoutAddAddressNewUserViewModel::class.java)
    }

    private fun getAvailableDeliverySlots() {
        checkoutAddAddressNewUserViewModel.getAvailableDeliverySlots().observe(viewLifecycleOwner, {
            when (it.responseStatus) {
                ResponseStatus.SUCCESS -> {
                    loadingBar.visibility = View.GONE
                    if (it.data != null) {
                        initializeGrid(it.data as? AvailableDeliverySlotsResponse, 0)
                    }
                }
                ResponseStatus.LOADING -> {
                    loadingBar.visibility = View.VISIBLE
                }
                ResponseStatus.ERROR -> {
                    loadingBar.visibility = View.GONE
                }
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.previousImgBtn -> {

            }
            R.id.nextImgBtn -> {

            }
        }
    }
}