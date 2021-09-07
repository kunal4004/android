package za.co.woolworths.financial.services.android.checkout.view

import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.checkout_add_address_new_user.*
import kotlinx.android.synthetic.main.checkout_add_address_retuning_user.*
import kotlinx.android.synthetic.main.checkout_delivery_time_slot_selection_fragment.*
import kotlinx.android.synthetic.main.checkout_grid_layout_other.*
import kotlinx.android.synthetic.main.checkout_how_would_you_delivered.*
import kotlinx.android.synthetic.main.layout_delivering_to_details.*
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_food_substitution.*
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_instructions.*
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_order_summary.*
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.DeliveryType.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.FulfillmentsType.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.WeekCounter.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.SAVED_ADDRESS_KEY
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutDeliveryTypeSelectionListAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutDeliveryTypeSelectionListAdapter.Companion.DELIVERY_TYPE_TIMESLOT
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutDeliveryTypeSelectionShimmerAdapter
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.models.dto.OrderSummary
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.Utils
import java.util.regex.Pattern


/**
 * Created by Kunal Uttarwar on 27/05/21.
 */
class CheckoutAddAddressReturningUserFragment : Fragment(), View.OnClickListener,
    CheckoutDeliveryTypeSelectionListAdapter.EventListner {

    companion object {
        const val REGEX_DELIVERY_INSTRUCTIONS = "^\$|^[a-zA-Z0-9\\s<!>@#\$&().+,-/\\\"']+\$"
    }

    private val deliveryInstructionsTextWatcher: TextWatcher = object : TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int){}
        override fun afterTextChanged(s: Editable?){
            val text = s.toString()
            val length = text.length

            if (length > 0 && !Pattern.matches(REGEX_DELIVERY_INSTRUCTIONS, text)) {
                s!!.delete(length - 1, length)
            }
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }
    }
    private lateinit var checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel
    private val expandableGrid = ExpandableGrid(this)
    private var selectedSlotResponseFood: AvailableDeliverySlotsResponse? = null
    private var selectedSlotResponseOther: AvailableDeliverySlotsResponse? = null
    private var selectedFoodSlot = Slot()
    private var selectedOtherSlot = Slot()
    private var foodType = ONLY_FOOD
    private var otherType = ONLY_OTHER
    private var checkoutDeliveryTypeSelectionListAdapter: CheckoutDeliveryTypeSelectionListAdapter? =
        null
    private var checkoutDeliveryTypeSelectionShimmerAdapter: CheckoutDeliveryTypeSelectionShimmerAdapter? =
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
        initializeDeliveringToView()
        initializeDeliveryFoodOtherItems()
        initializeFoodSubstitution()
        initializeDeliveryInstructions()

        expandableGrid.apply {
            disablePreviousBtnFood()
            disablePreviousBtnOther()
        }

        getConfirmDeliveryAddressDetails()

        activity?.apply {
            view?.setOnClickListener {
                Utils.hideSoftKeyboard(this)
            }
        }
    }

    private fun initializeDeliveryInstructions() {
        edtTxtSpecialDeliveryInstruction?.addTextChangedListener(deliveryInstructionsTextWatcher)
        edtTxtGiftInstructions?.addTextChangedListener(deliveryInstructionsTextWatcher)
        edtTxtInputLayoutSpecialDeliveryInstruction?.visibility = View.GONE
        edtTxtInputLayoutSpecialDeliveryInstruction?.isCounterEnabled = false
        edtTxtInputLayoutGiftInstructions?.visibility = View.GONE
        edtTxtInputLayoutGiftInstructions?.isCounterEnabled = false

        switchSpecialDeliveryInstruction?.setOnCheckedChangeListener { _, isChecked ->
            edtTxtInputLayoutSpecialDeliveryInstruction?.visibility =
                if (isChecked) View.VISIBLE else View.GONE
            edtTxtInputLayoutSpecialDeliveryInstruction?.isCounterEnabled = isChecked
            edtTxtSpecialDeliveryInstruction?.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }

        switchGiftInstructions?.setOnCheckedChangeListener { _, isChecked ->
            edtTxtInputLayoutGiftInstructions?.visibility =
                if (isChecked) View.VISIBLE else View.GONE
            edtTxtInputLayoutGiftInstructions?.isCounterEnabled = isChecked
            edtTxtGiftInstructions?.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun initializeDeliveringToView() {
        if(arguments == null) {
            checkoutDeliveryDetailsLayout.visibility = View.GONE
            return
        }
        arguments?.apply {
            context?.let { context ->
                val savedAddress = getSerializable(SAVED_ADDRESS_KEY) as? SavedAddressResponse
                if(savedAddress == null || savedAddress?.addresses.isNullOrEmpty()) {
                    checkoutDeliveryDetailsLayout?.visibility = View.GONE
                    return@apply
                }
                savedAddress?.let { savedAddresses ->

                    val deliveringToAddress = SpannableStringBuilder()
                    // default address nickname
                    val defaultAddressNickname =
                        SpannableString(
                            savedAddresses.defaultAddressNickname + " " + context.getString(
                                R.string.bullet
                            ) + " "
                        )
                    val typeface = ResourcesCompat.getFont(context, R.font.myriad_pro_semi_bold)
                    defaultAddressNickname.setSpan(
                        StyleSpan(typeface!!.style),
                        0, defaultAddressNickname.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    defaultAddressNickname.setSpan(
                        ForegroundColorSpan(Color.BLACK),
                        0,
                        defaultAddressNickname.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    deliveringToAddress.append(defaultAddressNickname)

                    // Extract default address display name
                    savedAddresses.addresses?.forEach { address ->
                        if (savedAddresses.defaultAddressNickname.equals(address.nickname)) {
                            val addressName = SpannableString(address.nickname)
                            val typeface1 =
                                ResourcesCompat.getFont(context, R.font.myriad_pro_regular)
                            addressName.setSpan(
                                StyleSpan(typeface1!!.style),
                                0, addressName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            deliveringToAddress.append(addressName)
                            return@forEach
                        }
                    }
                    tvNativeCheckoutDeliveringValue?.text = deliveringToAddress

                    checkoutDeliveryDetailsLayout?.setOnClickListener(this@CheckoutAddAddressReturningUserFragment)

                }
            }
        }
    }

    /**
     * Initializes food substitution view and Set by default selection to [FoodSubstitution.SIMILAR_SUBSTITUTION]
     *
     * @see [FoodSubstitution]
     */
    private fun initializeFoodSubstitution() {
        var selectedFoodSubstitution = FoodSubstitution.SIMILAR_SUBSTITUTION
        radioGroupFoodSubstitution?.setOnCheckedChangeListener { _, checkedId ->
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
        availableDeliverySlotsResponse: AvailableDeliverySlotsResponse?,
        type: DeliveryType
    ) {
        // To show How would you like it to delivered.
        checkoutHowWouldYouDeliveredLayout.visibility = View.VISIBLE
        if (availableDeliverySlotsResponse?.requiredToDisplayOnlyODD == false) {
            val timeSlotListItem: MutableMap<Any, Any> = HashMap()
            timeSlotListItem["deliveryType"] = DELIVERY_TYPE_TIMESLOT
            timeSlotListItem["amount"] = (selectedSlotResponseFood?.timedDeliveryCosts?.other!!)

            val date = selectedSlotResponseFood?.timedDeliveryStartDates?.other
            val deliveryText = getString(R.string.earliest_delivery_date_text)
            timeSlotListItem["description"] = "$deliveryText <b>$date</b>"

            (availableDeliverySlotsResponse.openDayDeliverySlots as ArrayList).add(timeSlotListItem)
        }
        checkoutDeliveryTypeSelectionShimmerAdapter = null
        deliveryTypeSelectionRecyclerView.adapter = null
        checkoutDeliveryTypeSelectionListAdapter =
            CheckoutDeliveryTypeSelectionListAdapter(
                availableDeliverySlotsResponse?.openDayDeliverySlots,
                this,
                type
            )
        deliveryTypeSelectionRecyclerView?.apply {
            addItemDecoration(object : RecyclerView.ItemDecoration() {})
            layoutManager = activity?.let { LinearLayoutManager(it) }
            checkoutDeliveryTypeSelectionListAdapter?.let { adapter = it }
        }
    }

    private fun initializeDeliveryFoodOtherItems() {
        setupViewModel()
        getAvailableDeliverySlots()
        previousImgBtnFood.setOnClickListener(this)
        nextImgBtnFood.setOnClickListener(this)
        previousImgBtnOther.setOnClickListener(this)
        nextImgBtnOther.setOnClickListener(this)
    }

    /**
     * Initializes Order Summary data from confirmDeliveryAddress or storePickUp API .
     */
    private fun initializeOrderSummary(orderSummary: OrderSummary?) {
        orderSummary?.let { it ->
            txtOrderSummaryYourCartValue?.text =
                CurrencyFormatter.formatAmountToRandAndCentWithSpace(it.basketTotal)
            it.discountDetails?.let { discountDetails ->
                groupOrderSummaryDiscount?.visibility =
                    if (discountDetails.otherDiscount == 0.0) View.GONE else View.VISIBLE
                groupPromoCodeDiscount?.visibility =
                    if (discountDetails.promoCodeDiscount == 0.0) View.GONE else View.VISIBLE
                groupWRewardsDiscount?.visibility =
                    if (discountDetails.voucherDiscount == 0.0) View.GONE else View.VISIBLE
                groupCompanyDiscount?.visibility =
                    if (discountDetails.companyDiscount == 0.0) View.GONE else View.VISIBLE
                groupTotalDiscount?.visibility =
                    if (discountDetails.totalDiscount == 0.0) View.GONE else View.VISIBLE

                txtOrderSummaryDiscountValue?.text =
                    "-" + CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.otherDiscount)
                txtOrderSummaryTotalDiscountValue?.text =
                    "-" + CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.totalDiscount)
                txtOrderSummaryWRewardsVouchersValue?.text =
                    "-" + CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.voucherDiscount)
                txtOrderSummaryCompanyDiscountValue?.text =
                    "-" + CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.companyDiscount)
                txtOrderSummaryPromoCodeDiscountValue?.text =
                    "-" + CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.promoCodeDiscount)

                txtOrderTotalValue.text =
                    CurrencyFormatter.formatAmountToRandAndCentWithSpace(it.total)
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


    private fun getConfirmDeliveryAddressDetails() {
        checkoutAddAddressNewUserViewModel.getConfirmDeliveryAddressDetails()
            .observe(viewLifecycleOwner, {
                when (it.responseStatus) {
                    ResponseStatus.SUCCESS -> {
                        loadingBar.visibility = View.GONE
                        /*if (it.data != null) {
                           confirmDeliveryAddressDetails = it.data as? ConfirmDeliveryAddressResponse
                            initializeOrderSummary(confirmDeliveryAddressDetails?.orderSummary)
                        }*/

                        //use mock data from json file
                        val jsonFileString = Utils.getJsonDataFromAsset(
                            activity?.applicationContext,
                            "mocks/confirmDelivery_Response.json"
                        )
                        val mockDeliverySlotResponse: ConfirmDeliveryAddressResponse =
                            Gson().fromJson(
                                jsonFileString,
                                object : TypeToken<ConfirmDeliveryAddressResponse>() {}.type
                            )

                        initializeOrderSummary(mockDeliverySlotResponse?.orderSummary)
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

    private fun getAvailableDeliverySlots() {
        expandableGrid.setUpShimmerView()
        expandableGrid.showDeliveryTypeShimmerView()
        showDeliverySubTypeShimmerView()
        checkoutAddAddressNewUserViewModel.getAvailableDeliverySlots().observe(viewLifecycleOwner, {
            when (it.responseStatus) {
                ResponseStatus.SUCCESS -> {
                    loadingBar.visibility = View.GONE
                    expandableGrid.hideDeliveryTypeShimmerView()
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
                        selectDeliveryTimeSlotTitle.text =
                            getString(R.string.slot_delivery_title_when)
                        selectDeliveryTimeSlotSubTitleFood.visibility = View.GONE
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
                        if (selectedSlotResponseFood?.requiredToDisplayODD == true) {
                            howWouldYouDeliveredTitle.text =
                                getString(R.string.delivery_timeslot_title_other_items)
                            initializeDeliveryTypeSelectionView(
                                selectedSlotResponseFood,
                                MIXED_OTHER
                            ) // Sending params MIXED_OTHER here to get mixed_other grid while click on timeslot radiobutton.
                        }
                    } else {
                        // for Other
                        if (selectedSlotResponseFood?.requiredToDisplayODD == true) {
                            initializeDeliveryTypeSelectionView(
                                selectedSlotResponseFood,
                                ONLY_OTHER
                            )
                        }
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

    private fun showDeliverySubTypeShimmerView() {
        checkoutDeliveryTypeSelectionShimmerAdapter =
            CheckoutDeliveryTypeSelectionShimmerAdapter(3)

        deliveryTypeSelectionRecyclerView?.apply {
            addItemDecoration(object : RecyclerView.ItemDecoration() {})
            layoutManager = activity?.let { LinearLayoutManager(it) }
            checkoutDeliveryTypeSelectionShimmerAdapter?.let { adapter = it }
        }
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
                expandableGrid.apply {
                    disablePreviousBtnFood()
                    enableNextBtnFood()
                    initialiseGridView(selectedSlotResponseFood, FIRST.week, foodType)
                }
            }
            R.id.nextImgBtnFood -> {
                expandableGrid.apply {
                    disableNextBtnFood()
                    enablePreviousBtnFood()
                    initialiseGridView(selectedSlotResponseFood, SECOND.week, foodType)
                }
            }
            R.id.previousImgBtnOther -> {
                expandableGrid.apply {
                    disablePreviousBtnOther()
                    enableNextBtnOther()
                    initialiseGridView(selectedSlotResponseOther, FIRST.week, otherType)
                }
            }
            R.id.nextImgBtnOther -> {
                expandableGrid.apply {
                    disableNextBtnOther()
                    enablePreviousBtnOther()
                    initialiseGridView(selectedSlotResponseOther, SECOND.week, otherType)
                }
            }
            R.id.checkoutDeliveryDetailsLayout -> {
                view?.findNavController()?.navigate(
                    R.id.action_CheckoutAddAddressReturningUserFragment_to_checkoutAddressConfirmationFragment,
                    arguments
                )
            }
        }
    }

    override fun selectedDeliveryType(deliveryType: Any, type: DeliveryType) {
        if (((deliveryType as? Map<Any, String>)?.getValue("deliveryType")).equals(
                DELIVERY_TYPE_TIMESLOT
            )
        ) {
            gridLayoutDeliveryOptions.visibility = View.VISIBLE
            otherType = type
            expandableGrid.apply {
                disablePreviousBtnOther()
                enableNextBtnOther()
                initialiseGridView(selectedSlotResponseOther, FIRST.week, type)
            }
        } else {
            gridLayoutDeliveryOptions.visibility = View.GONE
        }
    }
}
