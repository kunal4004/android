package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.checkout_add_address_retuning_user.*
import kotlinx.android.synthetic.main.checkout_delivery_time_slot_selection_fragment.*
import kotlinx.android.synthetic.main.checkout_grid_layout_other.*
import kotlinx.android.synthetic.main.checkout_how_would_you_delivered.*
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_food_substitution.*
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_instructions.*
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.AvailableDeliverySlotsResponse
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutAddAddressNewUserApiHelper
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutMockApiHelper
import za.co.woolworths.financial.services.android.checkout.service.network.Slot
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.DeliveryType.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.FulfillmentsType.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.WeekCounter.*
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutDeliveryTypeSelectionListAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutDeliveryTypeSelectionListAdapter.Companion.DELIVERY_TYPE_TIMESLOT
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
import za.co.woolworths.financial.services.android.util.Utils


/**
 * Created by Kunal Uttarwar on 27/05/21.
 */
class CheckoutAddAddressReturningUserFragment : Fragment(), View.OnClickListener,
    CheckoutDeliveryTypeSelectionListAdapter.EventListner {

    private lateinit var checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel
    private val expandableGrid = ExpandableGrid(this)
    private var selectedSlotResponseFood: AvailableDeliverySlotsResponse? = null
    private var selectedSlotResponseOther: AvailableDeliverySlotsResponse? = null
    private var selectedFoodSlot = Slot()
    private var selectedOtherSlot = Slot()
    private var foodType = DeliveryType.ONLY_FOOD
    private var otherType = DeliveryType.ONLY_OTHER
    private var checkoutDeliveryTypeSelectionListAdapter: CheckoutDeliveryTypeSelectionListAdapter? =
        null

    enum class FoodSubstitution(val rgb: String) {
        PHONE_CONFIRM("YES_CALL_CONFIRM"),
        SIMILAR_SUBSTITUTION("YES"),
        NO_THANKS("NO")
    }

    enum class DeliveryType(val type: String) {
        ONLY_FOOD("only_food"),
        MIXED_FOOD("mixed_food"),
        MIXED_OTHER("mixed_other"),
        ONLY_OTHER("only_other")
    }

    enum class WeekCounter(val week: Int) {
        FIRST(0),
        SECOND(1)
    }

    enum class FulfillmentsType(val type: String) {
        FOOD("01"),
        OTHER("02")
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

        activity?.apply {
            view?.setOnClickListener {
                Utils.hideSoftKeyboard(this)
            }
        }

        switchSpecialDeliveryInstruction?.setOnCheckedChangeListener { buttonView, isChecked ->
            edtTxtSpecialDeliveryInstruction?.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }

        switchGiftInstructions?.setOnCheckedChangeListener { buttonView, isChecked ->
            edtTxtGiftInstructions?.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }
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

    private fun initializeDeliveryTypeSelectionView(
        openDayDeliverySlots: List<Any>?,
        type: DeliveryType
    ) {
        // To show How would you like it to delivered.
        checkoutHowWouldYouDeliveredLayout.visibility = View.VISIBLE
        val timeSlotListItem: MutableMap<Any, Any> = HashMap()
        timeSlotListItem["deliveryType"] = DELIVERY_TYPE_TIMESLOT
        timeSlotListItem["amount"] = (selectedSlotResponseFood?.timedDeliveryCosts?.other!!)

        val date = selectedSlotResponseFood?.timedDeliveryStartDates?.other
        val deliveryText = getString(R.string.earliest_delivery_date_text)
        timeSlotListItem["description"] = "$deliveryText <b>$date</b>"

        (openDayDeliverySlots as ArrayList).add(timeSlotListItem)
        checkoutDeliveryTypeSelectionListAdapter =
            CheckoutDeliveryTypeSelectionListAdapter(openDayDeliverySlots, this, type)
        deliveryTypeSelectionRecyclerView?.apply {
            addItemDecoration(object : RecyclerView.ItemDecoration() {})
            layoutManager = activity?.let { LinearLayoutManager(it) }
            checkoutDeliveryTypeSelectionListAdapter?.let { adapter = it }
        }
    }

    private fun initializeDeliveryFoodItems() {
        setupViewModel()
        getAvailableDeliverySlots()
        previousImgBtnFood.setOnClickListener(this)
        nextImgBtnFood.setOnClickListener(this)
        previousImgBtnOther.setOnClickListener(this)
        nextImgBtnOther.setOnClickListener(this)
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
                    /*if (it.data != null) {
                    // Keeping two diff response not to get merge while showing 2 diff slots.
                       selectedSlotResponseFood = it.data as? AvailableDeliverySlotsResponse
                       selectedSlotResponseOther = it.data as? AvailableDeliverySlotsResponse */

                    //use mock data from json file
                    val jsonFileString = Utils.getJsonDataFromAsset(
                        activity?.applicationContext,
                        "mocks/confirmDelivery_Response.json"
                    )
                    val mockDeliverySlotResponse: AvailableDeliverySlotsResponse = Gson().fromJson(
                        jsonFileString,
                        object : TypeToken<AvailableDeliverySlotsResponse>() {}.type
                    )
                    selectedSlotResponseFood = mockDeliverySlotResponse
                    selectedSlotResponseOther = mockDeliverySlotResponse
                    if (FOOD.type == selectedSlotResponseFood?.fulfillmentTypes?.join) {
                        //Only for Food
                        foodType = ONLY_FOOD
                        checkoutTimeSlotSelectionLayout.visibility = View.VISIBLE
                        expandableGrid.initialiseGridView(
                            selectedSlotResponseFood,
                            FIRST.week,
                            ONLY_FOOD
                        )
                    } else if (OTHER.type == selectedSlotResponseFood?.fulfillmentTypes?.join && OTHER.type == selectedSlotResponseFood?.fulfillmentTypes?.other) {
                        // For mix basket
                        foodType = MIXED_FOOD
                        checkoutTimeSlotSelectionLayout.visibility = View.VISIBLE
                        expandableGrid.initialiseGridView(
                            selectedSlotResponseFood,
                            FIRST.week,
                            MIXED_FOOD
                        )
                        initializeDeliveryTypeSelectionView(
                            selectedSlotResponseFood?.openDayDeliverySlots,
                            MIXED_OTHER
                        ) // Sending params MIXED_OTHER here to get mixed_other grid while click on timeslot radiobutton.
                    } else {
                        // for Other
                        initializeDeliveryTypeSelectionView(
                            selectedSlotResponseFood?.openDayDeliverySlots,
                            ONLY_OTHER
                        )
                    }
                    //}

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

    fun getSelectedSlotResponse(deliveryType: DeliveryType): AvailableDeliverySlotsResponse? {
        return if (deliveryType.equals(ONLY_FOOD) || deliveryType.equals(MIXED_FOOD)) selectedSlotResponseFood else selectedSlotResponseOther
    }

    fun setSelectedSlotResponse(
        availableDeliverySlotsResponse: AvailableDeliverySlotsResponse?,
        deliveryType: DeliveryType
    ) {
        if (deliveryType.equals(ONLY_FOOD) || deliveryType.equals(MIXED_FOOD))
            selectedSlotResponseFood = availableDeliverySlotsResponse
        else
            selectedSlotResponseOther = availableDeliverySlotsResponse
    }

    fun setSelectedFoodOrOtherSlot(selectedSlot: Slot, deliveryType: DeliveryType) {
        if (deliveryType.equals(ONLY_FOOD) || deliveryType.equals(MIXED_FOOD))
            selectedFoodSlot = selectedSlot
        else
            selectedOtherSlot = selectedSlot
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.previousImgBtnFood -> {
                expandableGrid.initialiseGridView(selectedSlotResponseFood, FIRST.week, foodType)
            }
            R.id.nextImgBtnFood -> {
                expandableGrid.initialiseGridView(selectedSlotResponseFood, SECOND.week, foodType)
            }
            R.id.previousImgBtnOther -> {
                expandableGrid.initialiseGridView(selectedSlotResponseOther, FIRST.week, otherType)
            }
            R.id.nextImgBtnOther -> {
                expandableGrid.initialiseGridView(selectedSlotResponseOther, SECOND.week, otherType)
            }
        }
    }

    override fun selectedDeliveryType(deliveryType: Any, type: DeliveryType) {
        if (((deliveryType as Map<Any, String>).getValue("deliveryType")).equals(
                DELIVERY_TYPE_TIMESLOT
            )
        ) {
            gridLayoutDeliveryOptions.visibility = View.VISIBLE
            otherType = type
            expandableGrid.initialiseGridView(selectedSlotResponseOther, FIRST.week, type)
        } else {
            gridLayoutDeliveryOptions.visibility = View.GONE
        }
    }
}