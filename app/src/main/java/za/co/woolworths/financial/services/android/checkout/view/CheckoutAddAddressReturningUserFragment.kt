package za.co.woolworths.financial.services.android.checkout.view

import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.checkout_add_address_new_user.*
import kotlinx.android.synthetic.main.checkout_add_address_retuning_user.*
import kotlinx.android.synthetic.main.checkout_delivery_time_slot_selection_fragment.*
import kotlinx.android.synthetic.main.checkout_grid_layout_other.*
import kotlinx.android.synthetic.main.checkout_how_would_you_delivered.*
import kotlinx.android.synthetic.main.edit_delivery_location_confirmation_fragment.view.*
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
import za.co.woolworths.financial.services.android.models.network.ConfirmDeliveryAddressBody
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.Utils
import java.util.regex.Pattern


/**
 * Created by Kunal Uttarwar on 27/05/21.
 */
class CheckoutAddAddressReturningUserFragment : CheckoutAddressManagementBaseFragment(),
    View.OnClickListener,
    CheckoutDeliveryTypeSelectionListAdapter.EventListner {

    companion object {
        const val REGEX_DELIVERY_INSTRUCTIONS = "^\$|^[a-zA-Z0-9\\s<!>@#\$&().+,-/\\\"']+\$"
    }

    private var selectedOpedDayDeliverySlot = OpenDayDeliverySlot()
    private var selectedFoodSubstitution = FoodSubstitution.SIMILAR_SUBSTITUTION
    private var oddSelectedPosition: Int = -1
    private var suburbId: String = ""

    private val deliveryInstructionsTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable?) {
            val text = s.toString()
            val length = text.length

            if (length > 0 && !Pattern.matches(REGEX_DELIVERY_INSTRUCTIONS, text)) {
                s!!.delete(length - 1, length)
            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }
    }
    private var confirmDeliveryAddressResponse: ConfirmDeliveryAddressResponse? = null
    private lateinit var checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel
    private val expandableGrid = ExpandableGrid(this)
    private var selectedSlotResponseFood: ConfirmDeliveryAddressResponse? = null
    private var selectedSlotResponseOther: ConfirmDeliveryAddressResponse? = null
    private var selectedFoodSlot = Slot()
    private var selectedOtherSlot = Slot()
    private var foodType = DEFAULT
    private var otherType = DEFAULT
    private var savedAddress = SavedAddressResponse()
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
        ONLY_OTHER("only_other"),
        DEFAULT("default")
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
        addFragmentListner()
        initializeDeliveringToView()
        initializeDeliveryFoodOtherItems()
        initializeFoodSubstitution()
        initializeDeliveryInstructions()
        validateContinueToPaymentButton()

        expandableGrid.apply {
            disablePreviousBtnFood()
            disablePreviousBtnOther()
        }

        when (confirmDeliveryAddressResponse) {
            null -> {
                getConfirmDeliveryAddressDetails()
            }
            else -> {
                initializeOrderSummary(confirmDeliveryAddressResponse?.orderSummary)
            }
        }

        txtContinueToPayment?.setOnClickListener(this)
        activity?.apply {
            view?.setOnClickListener {
                Utils.hideSoftKeyboard(this)
            }
        }
    }

    private fun addFragmentListner() {
        setFragmentResultListener(ErrorHandlerBottomSheetDialog.RESULT_ERROR_CODE_RETRY) { _, bundle ->
            when (bundle.getInt("bundle")) {
                ErrorHandlerBottomSheetDialog.ERROR_TYPE_CONFIRM_DELIVERY_ADDRESS -> {
                    getConfirmDeliveryAddressDetails()
                }
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
            if(loadingBar.visibility == View.VISIBLE){
                return@setOnCheckedChangeListener
            }
            edtTxtInputLayoutSpecialDeliveryInstruction?.visibility =
                if (isChecked) View.VISIBLE else View.GONE
            edtTxtInputLayoutSpecialDeliveryInstruction?.isCounterEnabled = isChecked
            edtTxtSpecialDeliveryInstruction?.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }

        switchGiftInstructions?.setOnCheckedChangeListener { _, isChecked ->
            if(loadingBar.visibility == View.VISIBLE){
                return@setOnCheckedChangeListener
            }
            edtTxtInputLayoutGiftInstructions?.visibility =
                if (isChecked) View.VISIBLE else View.GONE
            edtTxtInputLayoutGiftInstructions?.isCounterEnabled = isChecked
            edtTxtGiftInstructions?.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun initializeDeliveringToView() {
        if (arguments == null) {
            checkoutDeliveryDetailsLayout.visibility = View.GONE
            return
        }
        context?.let { context ->
            savedAddress = Utils.jsonStringToObject(
                baseFragBundle?.getString(SAVED_ADDRESS_KEY),
                SavedAddressResponse::class.java
            ) as? SavedAddressResponse
                ?: baseFragBundle?.getSerializable(SAVED_ADDRESS_KEY) as? SavedAddressResponse
                        ?: SavedAddressResponse()

            if (savedAddress == null || savedAddress.addresses.isNullOrEmpty()) {
                checkoutDeliveryDetailsLayout?.visibility = View.GONE
                return
            }
            savedAddress.let { savedAddresses ->

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
                        suburbId = address.suburbId ?: ""
                        val addressName = SpannableString(address.address1)
                        val typeface1 =
                            ResourcesCompat.getFont(context, R.font.myriad_pro_regular)
                        addressName.setSpan(
                            StyleSpan(typeface1!!.style),
                            0, addressName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        deliveringToAddress.append(addressName)
                        return@forEach
                    }
                    if (savedAddresses.defaultAddressNickname.isNullOrEmpty()) {
                        checkoutDeliveryDetailsLayout.visibility = View.GONE
                    }
                }
                tvNativeCheckoutDeliveringValue?.text = deliveringToAddress
                checkoutDeliveryDetailsLayout?.setOnClickListener(this@CheckoutAddAddressReturningUserFragment)

            }
        }
    }

    /**
     * Initializes food substitution view and Set by default selection to [FoodSubstitution.SIMILAR_SUBSTITUTION]
     *
     * @see [FoodSubstitution]
     */
    private fun initializeFoodSubstitution() {
        selectedFoodSubstitution = FoodSubstitution.SIMILAR_SUBSTITUTION
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
        confirmDeliveryAddressResponse: ConfirmDeliveryAddressResponse?,
        type: DeliveryType
    ) {
        // To show How would you like it to delivered.
        checkoutHowWouldYouDeliveredLayout.visibility = View.VISIBLE
        if (confirmDeliveryAddressResponse?.requiredToDisplayOnlyODD == false) {
            otherType = if (foodType == DEFAULT) ONLY_OTHER else MIXED_OTHER

            val timeSlotListItem = OpenDayDeliverySlot()
            timeSlotListItem.apply {
                deliveryType = DELIVERY_TYPE_TIMESLOT
                amount = selectedSlotResponseOther?.timedDeliveryCosts?.join?.toLong()
                val date = selectedSlotResponseOther?.timedDeliveryStartDates?.join ?: ""
                val deliveryText = getString(R.string.earliest_delivery_date_text)
                description = "$deliveryText <b>$date</b>"
            }
            confirmDeliveryAddressResponse.openDayDeliverySlots?.add(timeSlotListItem)
        }
        checkoutDeliveryTypeSelectionShimmerAdapter = null
        deliveryTypeSelectionRecyclerView.adapter = null
        checkoutDeliveryTypeSelectionListAdapter =
            CheckoutDeliveryTypeSelectionListAdapter(
                confirmDeliveryAddressResponse?.openDayDeliverySlots,
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
                    CheckoutAddAddressNewUserApiHelper()
                )
            )
        ).get(CheckoutAddAddressNewUserViewModel::class.java)
    }

    private fun getConfirmDeliveryAddressDetails() {

        if (TextUtils.isEmpty(suburbId)) {
            presentErrorDialog(
                getString(R.string.common_error_unfortunately_something_went_wrong),
                getString(R.string.common_error_message_without_contact_info),
                ErrorHandlerBottomSheetDialog.ERROR_TYPE_CONFIRM_DELIVERY_ADDRESS
            )
            return
        }

        loadingBar.visibility = View.VISIBLE
        expandableGrid.setUpShimmerView()
        expandableGrid.showDeliveryTypeShimmerView()
        showDeliverySubTypeShimmerView()
        val body = ConfirmDeliveryAddressBody(suburbId)
        checkoutAddAddressNewUserViewModel.getConfirmDeliveryAddressDetails(body)
            .observe(viewLifecycleOwner, { response ->
                loadingBar.visibility = View.GONE
                expandableGrid.hideDeliveryTypeShimmerView()
                when (response) {
                    is ConfirmDeliveryAddressResponse -> {
                        confirmDeliveryAddressResponse = response
                        // Keeping two diff response not to get merge while showing 2 diff slots.
                        selectedSlotResponseFood = response
                        selectedSlotResponseOther = response
                        showDeliverySlotSelectionView()
                        initializeOrderSummary(response.orderSummary)
                    }
                    is Throwable -> {
                        presentErrorDialog(
                            getString(R.string.common_error_unfortunately_something_went_wrong),
                            getString(R.string.no_internet_subtitle),
                            ErrorHandlerBottomSheetDialog.ERROR_TYPE_CONFIRM_DELIVERY_ADDRESS
                        )
                    }
                }
            })
    }

    private fun presentErrorDialog(title: String, subTitle: String, type: Int) {
        val bundle = Bundle()
        bundle.putString(
            ErrorHandlerBottomSheetDialog.ERROR_TITLE,
            title
        )
        bundle.putString(
            ErrorHandlerBottomSheetDialog.ERROR_DESCRIPTION,
            subTitle
        )
        bundle.putInt(ErrorHandlerBottomSheetDialog.ERROR_TYPE, type)
        view?.findNavController()?.navigate(
            R.id.action_CheckoutAddAddressReturningUserFragment_to_ErrorHandlerBottomSheetDialog,
            bundle
        )
    }

    private fun showDeliverySlotSelectionView() {

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

    fun getSelectedSlotResponse(deliveryType: DeliveryType): ConfirmDeliveryAddressResponse? {
        return if (deliveryType.equals(ONLY_FOOD) || deliveryType.equals(MIXED_FOOD)) selectedSlotResponseFood else selectedSlotResponseOther
    }

    fun setSelectedSlotResponse(
        confirmDeliveryAddressResponse: ConfirmDeliveryAddressResponse?,
        deliveryType: DeliveryType
    ) {
        if (deliveryType.equals(ONLY_FOOD) || deliveryType.equals(MIXED_FOOD))
            selectedSlotResponseFood = confirmDeliveryAddressResponse
        else
            selectedSlotResponseOther = confirmDeliveryAddressResponse
    }

    fun setSelectedFoodOrOtherSlot(selectedSlot: Slot, deliveryType: DeliveryType) {
        if (deliveryType.equals(ONLY_FOOD) || deliveryType.equals(MIXED_FOOD))
            selectedFoodSlot = selectedSlot
        else
            selectedOtherSlot = selectedSlot

        validateContinueToPaymentButton()
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
                    baseFragBundle
                )
            }
            R.id.txtContinueToPayment -> {
                onCheckoutPaymentClick(v)
            }
        }
    }

    private fun onCheckoutPaymentClick(view: View) {
        val body = getShipmentDetailsBody()
        if (TextUtils.isEmpty(body.oddDeliverySlotId) && TextUtils.isEmpty(body.foodDeliverySlotId)
            && TextUtils.isEmpty(body.otherDeliverySlotId)) {
            return
        }
        loadingBar?.visibility = View.VISIBLE
        setScreenClickEvents(false)
        checkoutAddAddressNewUserViewModel.getShippingDetails(body)
            .observe(viewLifecycleOwner, { response ->
                loadingBar.visibility = View.GONE
                setScreenClickEvents(true)
                when (response) {
                    is ShippingDetailsResponse -> {

                        if(TextUtils.isEmpty(response.jsessionId) || TextUtils.isEmpty(response.auth)){
                            presentErrorDialog(
                                getString(R.string.common_error_unfortunately_something_went_wrong),
                                getString(R.string.common_error_message_without_contact_info),
                                ErrorHandlerBottomSheetDialog.ERROR_TYPE_CONFIRM_DELIVERY_ADDRESS
                            )
                            return@observe
                        }
                        navigateToPaymentWebpage()
                    }
                    is Throwable -> {
                        presentErrorDialog(
                            getString(R.string.common_error_unfortunately_something_went_wrong),
                            getString(R.string.common_error_message_without_contact_info),
                            ErrorHandlerBottomSheetDialog.ERROR_TYPE_CONFIRM_DELIVERY_ADDRESS
                        )
                    }
                }
            })
    }

    private fun navigateToPaymentWebpage() {
        // TODO: Payment Web page integration.
    }

    private fun setScreenClickEvents(isClickable: Boolean) {
        radioGroupFoodSubstitution?.isClickable = isClickable
        checkoutDeliveryDetailsLayout?.isClickable = isClickable
        switchNeedBags?.isClickable = isClickable
        switchGiftInstructions?.isClickable = isClickable
        switchSpecialDeliveryInstruction?.isClickable = isClickable
    }

    private fun getShipmentDetailsBody(): ShippingDetailsBody {
        val body = ShippingDetailsBody()
        when {
            // Food Items Basket
            foodType == ONLY_FOOD -> {
                body.apply {
                    requestFrom = "express"
                    joinBasket = true
                    foodShipOnDate = selectedFoodSlot?.stringShipOnDate
                    otherShipOnDate = ""
                    foodDeliverySlotId = selectedFoodSlot?.slotId
                    otherDeliverySlotId = ""
                    oddDeliverySlotId = ""
                    foodDeliveryStartHour = selectedFoodSlot?.hourFrom?.toLong() ?: 0
                    otherDeliveryStartHour = 0
                }
            }
            // Other Items Basket
            otherType == ONLY_OTHER -> {

                body.apply {
                    joinBasket = true
                    foodShipOnDate = ""
                    foodDeliverySlotId = ""
                    foodDeliveryStartHour = 0
                    if (selectedOpedDayDeliverySlot.deliveryType != null && selectedOpedDayDeliverySlot.deliveryType != DELIVERY_TYPE_TIMESLOT) {
                        oddDeliverySlotId = selectedOpedDayDeliverySlot?.deliverySlotId ?: ""
                        otherShipOnDate = ""
                        otherDeliverySlotId = ""
                        otherDeliveryStartHour = 0
                    } else {
                        otherShipOnDate = selectedOtherSlot?.stringShipOnDate
                        otherDeliverySlotId = selectedOtherSlot?.slotId
                        otherDeliveryStartHour = selectedOtherSlot?.hourFrom?.toLong() ?: 0
                        oddDeliverySlotId = ""
                    }
                }
            }
            //Mixed Basket
            foodType == MIXED_FOOD || otherType == MIXED_OTHER -> {
                body.apply {
                    joinBasket = false
                    if (selectedOpedDayDeliverySlot.deliveryType != null && selectedOpedDayDeliverySlot.deliveryType == DELIVERY_TYPE_TIMESLOT) {
                        foodShipOnDate = selectedFoodSlot?.stringShipOnDate
                        otherShipOnDate = selectedOtherSlot?.stringShipOnDate
                        foodDeliverySlotId = selectedFoodSlot?.slotId
                        otherDeliverySlotId = selectedOtherSlot?.slotId
                        foodDeliveryStartHour = selectedFoodSlot?.hourFrom?.toLong() ?: 0
                        otherDeliveryStartHour = selectedOtherSlot?.hourFrom?.toLong() ?: 0
                        oddDeliverySlotId = ""
                    } else {
                        foodShipOnDate = selectedFoodSlot?.stringShipOnDate
                        otherShipOnDate = ""
                        foodDeliverySlotId = selectedFoodSlot?.slotId
                        otherDeliverySlotId = ""
                        foodDeliveryStartHour = selectedFoodSlot?.hourFrom?.toLong() ?: 0
                        otherDeliveryStartHour = 0
                        oddDeliverySlotId = selectedOtherSlot?.slotId
                    }
                }
            }
            else -> return body
        }

        // default body params
        body.apply {
            requestFrom = "express"
            shipToAddressName = savedAddress?.defaultAddressNickname
            substituesAllowed = selectedFoodSubstitution.rgb
            plasticBags = switchNeedBags?.isChecked ?: false
            giftNoteSelected = switchGiftInstructions?.isChecked ?: false
            deliverySpecialInstructions =
                if (switchSpecialDeliveryInstruction?.isChecked == true) edtTxtSpecialDeliveryInstruction?.text.toString() else ""
            giftMessage =
                if (switchGiftInstructions?.isChecked == true) edtTxtGiftInstructions?.text.toString() else ""
            suburbId = this@CheckoutAddAddressReturningUserFragment.suburbId
            storeId = ""
        }

        return body
    }


    private fun validateContinueToPaymentButton() {
        when {
            // Food Items Basket
            foodType == ONLY_FOOD -> {
                if (TextUtils.isEmpty(selectedFoodSlot.slotId)) {
                    disablePaymentButton()
                } else {
                    enablePaymentButton()
                }
            }
            // Other Items Basket
            otherType == ONLY_OTHER -> {
                if ((selectedOpedDayDeliverySlot?.deliveryType == DELIVERY_TYPE_TIMESLOT
                            && TextUtils.isEmpty(selectedOtherSlot.slotId))
                    || TextUtils.isEmpty(selectedOpedDayDeliverySlot?.deliveryType)
                ) {
                    disablePaymentButton()
                } else {
                    enablePaymentButton()
                }
            }
            //Mixed Basket
            foodType == MIXED_FOOD || otherType == MIXED_OTHER -> {
                if ((selectedOpedDayDeliverySlot?.deliveryType == DELIVERY_TYPE_TIMESLOT
                            && TextUtils.isEmpty(selectedOtherSlot.slotId))
                    || TextUtils.isEmpty(selectedOpedDayDeliverySlot?.deliveryType)
                ) {
                    disablePaymentButton()
                } else {
                    enablePaymentButton()
                }
            }
            else -> {
                disablePaymentButton()
            }
        }
    }

    private fun disablePaymentButton() {
        txtContinueToPayment?.isEnabled = false
        txtContinueToPayment?.isClickable = false
        txtContinueToPayment?.background =
            context?.let { ContextCompat.getDrawable(it, R.drawable.button_disable_color) }
    }

    private fun enablePaymentButton() {
        txtContinueToPayment?.isEnabled = true
        txtContinueToPayment?.isClickable = true
        txtContinueToPayment?.background =
            context?.let { ContextCompat.getDrawable(it, R.drawable.button_selector) }
    }

    override fun selectedDeliveryType(
        openDayDeliverySlot: OpenDayDeliverySlot,
        type: DeliveryType,
        position: Int
    ) {
        oddSelectedPosition = position
        selectedOpedDayDeliverySlot = openDayDeliverySlot
        validateContinueToPaymentButton()
        if (DELIVERY_TYPE_TIMESLOT == openDayDeliverySlot.deliveryType) {
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
