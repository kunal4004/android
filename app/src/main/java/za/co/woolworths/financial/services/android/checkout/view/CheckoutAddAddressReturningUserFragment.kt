package za.co.woolworths.financial.services.android.checkout.view

import android.app.Activity.RESULT_CANCELED
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
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
import kotlinx.android.synthetic.main.new_shopping_bags_layout.*
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.DeliveryType.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.FulfillmentsType.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.WeekCounter.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.CONFIRM_DELIVERY_ADDRESS_RESPONSE_KEY
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.SAVED_ADDRESS_KEY
import za.co.woolworths.financial.services.android.checkout.view.CheckoutPaymentWebFragment.Companion.KEY_ARGS_WEB_TOKEN
import za.co.woolworths.financial.services.android.checkout.view.CheckoutPaymentWebFragment.Companion.REQUEST_KEY_PAYMENT_STATUS
import za.co.woolworths.financial.services.android.checkout.view.ExpandableGrid.Companion.DEFAULT_POSITION
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutDeliveryTypeSelectionListAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutDeliveryTypeSelectionListAdapter.Companion.DELIVERY_TYPE_TIMESLOT
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutDeliveryTypeSelectionShimmerAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.ShoppingBagsRadioGroupAdapter
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.OrderSummary
import za.co.woolworths.financial.services.android.models.dto.ShoppingBagsOptions
import za.co.woolworths.financial.services.android.models.network.ConfirmDeliveryAddressBody
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity.Companion.ERROR_TYPE_EMPTY_CART
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment.RESULT_EMPTY_CART
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment.RESULT_RELOAD_CART
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.KeyboardUtils
import za.co.woolworths.financial.services.android.util.Utils
import java.util.regex.Pattern


/**
 * Created by Kunal Uttarwar on 27/05/21.
 */
class CheckoutAddAddressReturningUserFragment : CheckoutAddressManagementBaseFragment(),
    OnClickListener,
    CheckoutDeliveryTypeSelectionListAdapter.EventListner,
    ShoppingBagsRadioGroupAdapter.EventListner {

    companion object {
        const val REGEX_DELIVERY_INSTRUCTIONS = "^\$|^[a-zA-Z0-9\\s<!>@#\$&().+,-/\\\"']+\$"
    }

    private var selectedOpenDayDeliverySlot = OpenDayDeliverySlot()
    private var selectedFoodSubstitution = FoodSubstitution.SIMILAR_SUBSTITUTION
    private var oddSelectedPosition: Int = -1
    private var suburbId: String = ""
    private var selectedShoppingBagType: Double? = null

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
    private var shimmerComponentArray: List<Pair<ShimmerFrameLayout, View>> = ArrayList()

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
        // Hide keyboard in case it was visible from a previous screen
        KeyboardUtils.hideKeyboardIfVisible(activity)
        return inflater.inflate(R.layout.checkout_add_address_retuning_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? CheckoutActivity)?.apply {
            showBackArrowWithTitle(bindString(R.string.checkout))
        }
        initViews()
    }

    private fun initViews() {
        initializeVariables()
        addFragmentListner()
        initializeDeliveringToView()
        initializeDeliveryFoodOtherItems()

        expandableGrid.apply {
            disablePreviousBtnFood()
            disablePreviousBtnOther()
        }

        when (confirmDeliveryAddressResponse) {
            null -> {
                getConfirmDeliveryAddressDetails()
            }
            else -> {
                startShimmerView()
                stopShimmerView()
                showDeliverySlotSelectionView()
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

    private fun initializeVariables() {
        if (selectedFoodSlot.slotId.isNullOrEmpty())
            selectedFoodSlot = Slot()
        if (selectedOtherSlot.slotId.isNullOrEmpty())
            selectedOtherSlot = Slot()
        if (selectedOpenDayDeliverySlot.deliveryType.isNullOrEmpty())
            selectedOpenDayDeliverySlot = OpenDayDeliverySlot()
        foodType = DEFAULT
        otherType = DEFAULT
    }

    private fun addFragmentListner() {
        setFragmentResultListener(ErrorHandlerBottomSheetDialog.RESULT_ERROR_CODE_RETRY) { _, bundle ->
            when (bundle.getInt("bundle")) {
                ErrorHandlerBottomSheetDialog.ERROR_TYPE_CONFIRM_DELIVERY_ADDRESS -> {
                    getConfirmDeliveryAddressDetails()
                }
                ErrorHandlerBottomSheetDialog.ERROR_TYPE_PAYMENT_STATUS -> {
                    onCheckoutPaymentClick()
                }
            }
        }
        setFragmentResultListener(REQUEST_KEY_PAYMENT_STATUS) { _, bundle ->
            when (bundle?.get(CheckoutPaymentWebFragment.KEY_STATUS)) {
                CheckoutPaymentWebFragment.PaymentStatus.PAYMENT_ERROR -> {
                    view?.findNavController()?.navigate(
                        R.id.action_CheckoutAddAddressReturningUserFragment_to_ErrorHandlerBottomSheetDialog,
                        bundleOf(
                            ErrorHandlerBottomSheetDialog.ERROR_TITLE to context?.getString(R.string.common_error_unfortunately_something_went_wrong),
                            ErrorHandlerBottomSheetDialog.ERROR_DESCRIPTION to context?.getString(R.string.please_try_again),
                            ErrorHandlerBottomSheetDialog.ERROR_TYPE to
                                    ErrorHandlerBottomSheetDialog.ERROR_TYPE_PAYMENT_STATUS
                        )
                    )
                }
            }
        }
    }

    private fun initializeDeliveryInstructions() {
        edtTxtSpecialDeliveryInstruction?.addTextChangedListener(deliveryInstructionsTextWatcher)
        edtTxtGiftInstructions?.addTextChangedListener(deliveryInstructionsTextWatcher)
        edtTxtInputLayoutSpecialDeliveryInstruction?.visibility = GONE
        edtTxtInputLayoutSpecialDeliveryInstruction?.isCounterEnabled = false
        edtTxtInputLayoutGiftInstructions?.visibility = GONE
        edtTxtInputLayoutGiftInstructions?.isCounterEnabled = false

        switchSpecialDeliveryInstruction?.setOnCheckedChangeListener { _, isChecked ->
            if (loadingBar.visibility == VISIBLE) {
                return@setOnCheckedChangeListener
            }
            if (isChecked)
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.CHECKOUT_SPECIAL_COLLECTION_INSTRUCTION,
                    activity
                )
            edtTxtInputLayoutSpecialDeliveryInstruction?.visibility =
                if (isChecked) VISIBLE else GONE
            edtTxtInputLayoutSpecialDeliveryInstruction?.isCounterEnabled = isChecked
            edtTxtSpecialDeliveryInstruction?.visibility =
                if (isChecked) VISIBLE else GONE
        }

        switchGiftInstructions?.setOnCheckedChangeListener { _, isChecked ->
            if (loadingBar.visibility == VISIBLE) {
                return@setOnCheckedChangeListener
            }
            if (isChecked)
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.CHECKOUT_IS_THIS_GIFT,
                    activity
                )
            edtTxtInputLayoutGiftInstructions?.visibility =
                if (isChecked) VISIBLE else GONE
            edtTxtInputLayoutGiftInstructions?.isCounterEnabled = isChecked
            edtTxtGiftInstructions?.visibility =
                if (isChecked) VISIBLE else GONE
        }
        if (WoolworthsApplication.getNativeCheckout()?.currentShoppingBag?.isEnabled == true) {
            switchNeedBags.visibility = VISIBLE
            txtNeedBags.visibility = VISIBLE
            newShoppingBagsLayout.visibility = GONE
            switchNeedBags?.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    Utils.triggerFireBaseEvents(
                        FirebaseManagerAnalyticsProperties.CHECKOUT_SHOPPING_BAGS_INFO,
                        activity
                    )
                }
            }
        } else if (WoolworthsApplication.getNativeCheckout()?.newShoppingBag?.isEnabled == true) {
            switchNeedBags.visibility = GONE
            txtNeedBags.visibility = GONE
            newShoppingBagsLayout.visibility = VISIBLE
            addShoppingBagsRadioButtons()
        }
    }

    private fun addShoppingBagsRadioButtons() {
        txtNewShoppingBagsSubDesc.visibility = VISIBLE
        val newShoppingBags = WoolworthsApplication.getNativeCheckout()?.newShoppingBag
        txtNewShoppingBagsDesc.text = newShoppingBags?.title
        txtNewShoppingBagsSubDesc.text = newShoppingBags?.description

        val shoppingBagsAdapter = ShoppingBagsRadioGroupAdapter(newShoppingBags?.options, this)
        shoppingBagsRecyclerView.apply {
            layoutManager = activity?.let { LinearLayoutManager(it) }
            shoppingBagsAdapter?.let { adapter = it }
        }
    }

    private fun initializeDeliveringToView() {
        if (arguments == null) {
            checkoutDeliveryDetailsLayout.visibility = GONE
            return
        }
        context?.let { context ->
            savedAddress = Utils.jsonStringToObject(
                baseFragBundle?.getString(SAVED_ADDRESS_KEY),
                SavedAddressResponse::class.java
            ) as? SavedAddressResponse
                ?: baseFragBundle?.getSerializable(SAVED_ADDRESS_KEY) as? SavedAddressResponse
                        ?: SavedAddressResponse()

            if (savedAddress?.addresses.isNullOrEmpty()) {
                checkoutDeliveryDetailsLayout?.visibility = GONE
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
                        checkoutDeliveryDetailsLayout.visibility = GONE
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
                    Utils.triggerFireBaseEvents(
                        FirebaseManagerAnalyticsProperties.CHECKOUT_FOOD_SUBSTITUTE_PHONE_ME,
                        activity
                    )
                    selectedFoodSubstitution = FoodSubstitution.PHONE_CONFIRM
                }
                R.id.radioBtnSimilarSubst -> {
                    selectedFoodSubstitution = FoodSubstitution.SIMILAR_SUBSTITUTION
                }
                R.id.radioBtnNoThanks -> {
                    Utils.triggerFireBaseEvents(
                        FirebaseManagerAnalyticsProperties.CHECKOUT_FOOD_SUBSTITUTE_NO_THANKS,
                        activity
                    )
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
        checkoutHowWouldYouDeliveredLayout.visibility = VISIBLE
        val localOpenDayDeliverySlots = confirmDeliveryAddressResponse?.openDayDeliverySlots
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
            var isTimeSlotAvailable = false
            if (!localOpenDayDeliverySlots.isNullOrEmpty()) {
                for (openDaySlot in localOpenDayDeliverySlots) {
                    // check if timeslot already exist then don't add it again.
                    if (openDaySlot.deliveryType == DELIVERY_TYPE_TIMESLOT)
                        isTimeSlotAvailable = true
                }
            }
            if (!isTimeSlotAvailable) {
                if ((type == MIXED_OTHER && confirmDeliveryAddressResponse?.sortedOtherDeliverySlots?.isNotEmpty() == true) || (type == ONLY_OTHER && confirmDeliveryAddressResponse?.sortedJoinDeliverySlots?.isNotEmpty() == true))
                    localOpenDayDeliverySlots?.add(timeSlotListItem)
            }
        }
        checkoutDeliveryTypeSelectionShimmerAdapter = null
        deliveryTypeSelectionRecyclerView.adapter = null
        checkoutDeliveryTypeSelectionListAdapter =
            CheckoutDeliveryTypeSelectionListAdapter(
                localOpenDayDeliverySlots,
                this,
                type,
                selectedOpenDayDeliverySlot
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
                    if (discountDetails.otherDiscount == 0.0) GONE else VISIBLE
                groupPromoCodeDiscount?.visibility =
                    if (discountDetails.promoCodeDiscount == 0.0) GONE else VISIBLE
                groupWRewardsDiscount?.visibility =
                    if (discountDetails.voucherDiscount == 0.0) GONE else VISIBLE
                groupCompanyDiscount?.visibility =
                    if (discountDetails.companyDiscount == 0.0) GONE else VISIBLE
                groupTotalDiscount?.visibility =
                    if (discountDetails.totalDiscount == 0.0) GONE else VISIBLE

                txtOrderSummaryDiscountValue?.text =
                    "-".plus(CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.otherDiscount))
                txtOrderSummaryTotalDiscountValue?.text =
                    "-".plus(CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.totalDiscount))
                txtOrderSummaryWRewardsVouchersValue?.text =
                    "-".plus(CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.voucherDiscount))
                txtOrderSummaryCompanyDiscountValue?.text =
                    "-".plus(CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.companyDiscount))
                txtOrderSummaryPromoCodeDiscountValue?.text =
                    "-".plus(CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.promoCodeDiscount))

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

    private fun startShimmerView() {
        expandableGrid.setUpShimmerView()
        expandableGrid.showDeliveryTypeShimmerView()
        showDeliverySubTypeShimmerView()

        shimmerComponentArray = listOf(
            Pair<ShimmerFrameLayout, View>(
                deliveringTitleShimmerFrameLayout,
                tvNativeCheckoutDeliveringTitle
            ),
            Pair<ShimmerFrameLayout, View>(
                deliveringTitleValueShimmerFrameLayout,
                tvNativeCheckoutDeliveringValue
            ),
            Pair<ShimmerFrameLayout, View>(forwardImgViewShimmerFrameLayout, imageViewCaretForward),
            Pair<ShimmerFrameLayout, View>(
                foodSubstitutionTitleShimmerFrameLayout,
                txtFoodSubstitutionTitle
            ),
            Pair<ShimmerFrameLayout, View>(
                foodSubstitutionDescShimmerFrameLayout,
                txtFoodSubstitutionDesc
            ),
            Pair<ShimmerFrameLayout, View>(
                radioGroupFoodSubstitutionShimmerFrameLayout,
                radioGroupFoodSubstitution
            ),
            Pair<ShimmerFrameLayout, View>(
                instructionTxtShimmerFrameLayout,
                txtSpecialDeliveryInstruction
            ),
            Pair<ShimmerFrameLayout, View>(
                specialInstructionSwitchShimmerFrameLayout,
                switchSpecialDeliveryInstruction
            ),
            Pair<ShimmerFrameLayout, View>(
                giftInstructionTxtShimmerFrameLayout,
                txtGiftInstructions
            ),
            Pair<ShimmerFrameLayout, View>(
                giftInstructionSwitchShimmerFrameLayout,
                switchGiftInstructions
            ),
            Pair<ShimmerFrameLayout, View>(txtYourCartShimmerFrameLayout, txtOrderSummaryYourCart),
            Pair<ShimmerFrameLayout, View>(
                yourCartValueShimmerFrameLayout,
                txtOrderSummaryYourCartValue
            ),
            Pair<ShimmerFrameLayout, View>(
                deliveryFeeTxtShimmerFrameLayout,
                txtOrderSummaryDeliveryFee
            ),
            Pair<ShimmerFrameLayout, View>(
                deliveryFeeValueShimmerFrameLayout,
                txtOrderSummaryDeliveryFeeValue
            ),
            Pair<ShimmerFrameLayout, View>(summaryNoteShimmerFrameLayout, txtOrderSummaryNote),
            Pair<ShimmerFrameLayout, View>(txtOrderTotalShimmerFrameLayout, txtOrderTotalTitle),
            Pair<ShimmerFrameLayout, View>(orderTotalValueShimmerFrameLayout, txtOrderTotalValue),
            Pair<ShimmerFrameLayout, View>(
                continuePaymentTxtShimmerFrameLayout,
                txtContinueToPayment
            ),
            Pair<ShimmerFrameLayout, View>(
                newShoppingBagsTitleShimmerFrameLayout,
                newShoppingBagsTitle
            ),
            Pair<ShimmerFrameLayout, View>(
                newShoppingBagsDescShimmerFrameLayout,
                txtNewShoppingBagsDesc
            ),
            Pair<ShimmerFrameLayout, View>(
                radioGroupShoppingBagsShimmerFrameLayout,
                radioGroupShoppingBags
            )
        )

        txtNeedBags.visibility = GONE
        switchNeedBags.visibility = GONE

        val shimmer = Shimmer.AlphaHighlightBuilder().build()
        shimmerComponentArray.forEach {
            it.first.setShimmer(shimmer)
            it.first.startShimmer()
            it.second.visibility = INVISIBLE
        }
    }

    private fun stopShimmerView() {
        expandableGrid.hideDeliveryTypeShimmerView()

        shimmerComponentArray.forEach {
            if (it.first.isShimmerStarted) {
                it.first.stopShimmer()
                it.first.setShimmer(null)
                it.second.visibility = VISIBLE
            }
        }

        txtNeedBags.visibility = VISIBLE
        switchNeedBags.visibility = VISIBLE

        initializeFoodSubstitution()
        initializeDeliveryInstructions()
    }

    private fun getConfirmDeliveryAddressDetails() {

        if (TextUtils.isEmpty(suburbId)) {
            presentErrorDialog(
                getString(R.string.common_error_unfortunately_something_went_wrong),
                getString(R.string.common_error_message_without_contact_info)
            )
            return
        }
        deliverySummaryScrollView?.fullScroll(FOCUS_UP)
        startShimmerView()
        val body = ConfirmDeliveryAddressBody(suburbId)
        checkoutAddAddressNewUserViewModel.getConfirmDeliveryAddressDetails(body)
            .observe(viewLifecycleOwner, { response ->
                stopShimmerView()
                when (response) {
                    is ConfirmDeliveryAddressResponse -> {
                        confirmDeliveryAddressResponse = response

                        if (response.orderSummary?.totalItemsCount ?: 0 <= 0) {
                            showEmptyCart()
                            return@observe
                        }

                        // Keeping two diff response not to get merge while showing 2 diff slots.
                        selectedSlotResponseFood = response
                        selectedSlotResponseOther = response
                        showDeliverySlotSelectionView()
                        initializeOrderSummary(response.orderSummary)
                    }
                    is Throwable -> {
                        presentErrorDialog(
                            getString(R.string.common_error_unfortunately_something_went_wrong),
                            getString(R.string.no_internet_subtitle)
                        )
                    }
                }
            })
    }

    private fun showEmptyCart() {
        activity?.let {
            val intent = Intent(it, ErrorHandlerActivity::class.java)
            intent.putExtra(ErrorHandlerActivity.ERROR_TYPE, ERROR_TYPE_EMPTY_CART)
            it.startActivityForResult(intent, ErrorHandlerActivity.ERROR_EMPTY_REQUEST_CODE)
        }
    }

    private fun presentErrorDialog(title: String, subTitle: String) {
        val bundle = Bundle()
        bundle.putString(
            ErrorHandlerBottomSheetDialog.ERROR_TITLE,
            title
        )
        bundle.putString(
            ErrorHandlerBottomSheetDialog.ERROR_DESCRIPTION,
            subTitle
        )
        bundle.putInt(
            ErrorHandlerBottomSheetDialog.ERROR_TYPE,
            ErrorHandlerBottomSheetDialog.ERROR_TYPE_CONFIRM_DELIVERY_ADDRESS
        )
        view?.findNavController()?.navigate(
            R.id.action_CheckoutAddAddressReturningUserFragment_to_ErrorHandlerBottomSheetDialog,
            bundle
        )
    }

    private fun showDeliverySlotSelectionView() {

        if (FOOD.type == selectedSlotResponseFood?.fulfillmentTypes?.join) {
            //Only for Food
            foodType = ONLY_FOOD
            checkoutTimeSlotSelectionLayout.visibility = VISIBLE
            selectDeliveryTimeSlotTitle.text =
                getString(R.string.slot_delivery_title_when)
            selectDeliveryTimeSlotSubTitleFood.visibility = GONE
            txtSelectDeliveryTimeSlotFoodError?.visibility = GONE
            expandableGrid.initialiseGridView(
                selectedSlotResponseFood,
                FIRST.week,
                ONLY_FOOD
            )
        } else if (OTHER.type == selectedSlotResponseFood?.fulfillmentTypes?.join && OTHER.type == selectedSlotResponseFood?.fulfillmentTypes?.other) {
            // For mix basket
            foodType = MIXED_FOOD
            checkoutTimeSlotSelectionLayout.visibility = VISIBLE
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
        return if (deliveryType == ONLY_FOOD || deliveryType == MIXED_FOOD) selectedSlotResponseFood else selectedSlotResponseOther
    }

    fun setSelectedSlotResponse(
        confirmDeliveryAddressResponse: ConfirmDeliveryAddressResponse?,
        deliveryType: DeliveryType
    ) {
        if (deliveryType == ONLY_FOOD || deliveryType == MIXED_FOOD)
            selectedSlotResponseFood = confirmDeliveryAddressResponse
        else
            selectedSlotResponseOther = confirmDeliveryAddressResponse
    }

    fun setSelectedFoodOrOtherSlot(selectedSlot: Slot, deliveryType: DeliveryType) {
        if (deliveryType == ONLY_FOOD || deliveryType == MIXED_FOOD) {
            selectedFoodSlot = selectedSlot
            txtSelectDeliveryTimeSlotFoodError.visibility = GONE
        } else {
            selectedOtherSlot = selectedSlot
            txtSelectDeliveryTimeSlotOtherError.visibility = GONE
        }
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
                baseFragBundle?.putString(
                    CONFIRM_DELIVERY_ADDRESS_RESPONSE_KEY,
                    Utils.toJson(confirmDeliveryAddressResponse)
                )
                baseFragBundle?.putBoolean(
                    IS_DELIVERY,
                    (tvNativeCheckoutDeliveringTitle.text == getString(R.string.native_checkout_delivering_to_title))
                )
                view?.findNavController()?.navigate(
                    R.id.action_CheckoutAddAddressReturningUserFragment_to_checkoutAddressConfirmationFragment,
                    baseFragBundle
                )
            }
            R.id.txtContinueToPayment -> {
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.CHECKOUT_CONTINUE_TO_PAYMENT,
                    activity
                )
                onCheckoutPaymentClick()
            }
        }
    }

    private fun onCheckoutPaymentClick() {
        if (isRequiredFieldsMissing() || isInstructionsMissing()) {
            return
        }

        val body = getShipmentDetailsBody()
        if (TextUtils.isEmpty(body.oddDeliverySlotId) && TextUtils.isEmpty(body.foodDeliverySlotId)
            && TextUtils.isEmpty(body.otherDeliverySlotId)
        ) {
            return
        }
        loadingBar?.visibility = VISIBLE
        setScreenClickEvents(false)
        checkoutAddAddressNewUserViewModel.getShippingDetails(body)
            .observe(viewLifecycleOwner, { response ->
                loadingBar.visibility = GONE
                setScreenClickEvents(true)
                when (response) {
                    is ShippingDetailsResponse -> {

                        if (TextUtils.isEmpty(response.jsessionId) || TextUtils.isEmpty(response.auth)) {
                            presentErrorDialog(
                                getString(R.string.common_error_unfortunately_something_went_wrong),
                                getString(R.string.common_error_message_without_contact_info)
                            )
                            return@observe
                        }
                        navigateToPaymentWebpage(response)
                    }
                    is Throwable -> {
                        presentErrorDialog(
                            getString(R.string.common_error_unfortunately_something_went_wrong),
                            getString(R.string.common_error_message_without_contact_info)
                        )
                    }
                }
            })
    }

    private fun isInstructionsMissing(): Boolean {
        return when {
            switchSpecialDeliveryInstruction?.isChecked == true -> {
                if (TextUtils.isEmpty(edtTxtSpecialDeliveryInstruction?.text.toString())) {
                    // scroll to instructions layout
                    deliverySummaryScrollView?.smoothScrollTo(
                        0,
                        layoutDeliveryInstructions?.top ?: 0
                    )
                    true
                } else false
            }
            switchGiftInstructions?.isChecked == true -> {
                if (TextUtils.isEmpty(edtTxtGiftInstructions?.text?.toString())) {
                    // scroll to instructions layout
                    deliverySummaryScrollView?.smoothScrollTo(
                        0,
                        layoutDeliveryInstructions?.top ?: 0
                    )
                    true
                } else false
            }
            else -> false
        }
    }

    private fun isRequiredFieldsMissing(): Boolean {
        when {
            // Food Items Basket
            foodType == ONLY_FOOD -> {
                if (!TextUtils.isEmpty(selectedFoodSlot?.slotId)) {
                    txtSelectDeliveryTimeSlotFoodError?.visibility = GONE
                    return false
                }
                // scroll to slot selection layout
                deliverySummaryScrollView?.smoothScrollTo(
                    0,
                    checkoutTimeSlotSelectionLayout?.top ?: 0
                )
                txtSelectDeliveryTimeSlotFoodError?.visibility = VISIBLE
            }
            // Other Items Basket
            otherType == ONLY_OTHER -> {
                when {
                    (selectedOpenDayDeliverySlot.deliveryType != null
                            && selectedOpenDayDeliverySlot.deliveryType != DELIVERY_TYPE_TIMESLOT) -> {
                        if (!TextUtils.isEmpty(selectedOpenDayDeliverySlot?.deliverySlotId)) {
                            txtSelectDeliveryTimeSlotOtherError?.visibility = GONE
                            return false
                        }
                    }
                    else -> {
                        if (!TextUtils.isEmpty(selectedOtherSlot?.slotId)) {
                            txtSelectDeliveryTimeSlotOtherError?.visibility = GONE
                            return false
                        }
                        txtSelectDeliveryTimeSlotOtherError?.visibility = VISIBLE
                    }
                }
                // scroll to other slot selection layout
                deliverySummaryScrollView?.smoothScrollTo(
                    0,
                    checkoutHowWouldYouDeliveredLayout?.top ?: 0
                )
            }
            //Mixed Basket
            foodType == MIXED_FOOD || otherType == MIXED_OTHER -> {
                if (selectedOpenDayDeliverySlot.deliveryType != null
                    && selectedOpenDayDeliverySlot.deliveryType == DELIVERY_TYPE_TIMESLOT
                ) {
                    when {
                        (!TextUtils.isEmpty(selectedFoodSlot?.slotId)
                                && !TextUtils.isEmpty(selectedOtherSlot?.slotId)
                                ) -> {
                            txtSelectDeliveryTimeSlotFoodError?.visibility = GONE
                            txtSelectDeliveryTimeSlotOtherError?.visibility = GONE
                            return false
                        }
                        (TextUtils.isEmpty(selectedFoodSlot?.slotId)) -> {
                            // scroll to slot selection layout
                            deliverySummaryScrollView?.smoothScrollTo(
                                0,
                                checkoutTimeSlotSelectionLayout?.top ?: 0
                            )
                            txtSelectDeliveryTimeSlotFoodError?.visibility = VISIBLE
                        }
                        else -> {
                            if (TextUtils.isEmpty(selectedOtherSlot?.slotId)) {
                                // scroll to other slot selection layout
                                deliverySummaryScrollView?.smoothScrollTo(
                                    0,
                                    checkoutHowWouldYouDeliveredLayout?.top ?: 0
                                )
                                txtSelectDeliveryTimeSlotOtherError?.visibility = VISIBLE
                            }
                        }
                    }
                } else {
                    if (!TextUtils.isEmpty(selectedFoodSlot?.slotId) &&
                        !TextUtils.isEmpty(selectedOpenDayDeliverySlot?.deliverySlotId)
                    ) {
                        return false
                    }
                    if (TextUtils.isEmpty(selectedFoodSlot?.slotId)) {
                        // scroll to food slot selection layout
                        deliverySummaryScrollView?.smoothScrollTo(
                            0,
                            checkoutTimeSlotSelectionLayout?.top ?: 0
                        )
                        txtSelectDeliveryTimeSlotFoodError?.visibility = VISIBLE
                    } else if (TextUtils.isEmpty(selectedOtherSlot?.slotId)) {
                        // scroll to other slot selection layout
                        deliverySummaryScrollView?.smoothScrollTo(
                            0,
                            checkoutHowWouldYouDeliveredLayout?.top ?: 0
                        )
                    }
                }
            }
            else -> return true
        }
        return true
    }

    private fun navigateToPaymentWebpage(webTokens: ShippingDetailsResponse) {
        view?.findNavController()?.navigate(
            R.id.action_CheckoutAddAddressReturningUserFragment_to_checkoutPaymentWebFragment,
            bundleOf(KEY_ARGS_WEB_TOKEN to webTokens)
        )
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
                    if (selectedOpenDayDeliverySlot.deliveryType != null && selectedOpenDayDeliverySlot.deliveryType != DELIVERY_TYPE_TIMESLOT) {
                        oddDeliverySlotId = selectedOpenDayDeliverySlot?.deliverySlotId ?: ""
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
                    if (selectedOpenDayDeliverySlot.deliveryType != null && selectedOpenDayDeliverySlot.deliveryType == DELIVERY_TYPE_TIMESLOT) {
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
                        oddDeliverySlotId = selectedOpenDayDeliverySlot?.deliverySlotId
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
            shoppingBagType = selectedShoppingBagType
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

    override fun selectedDeliveryType(
        openDayDeliverySlot: OpenDayDeliverySlot,
        type: DeliveryType,
        position: Int
    ) {
        oddSelectedPosition = position
        selectedOpenDayDeliverySlot = openDayDeliverySlot
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.CHECKOUT_DELIVERY_OPTION_.plus(openDayDeliverySlot.deliveryType),
            activity
        )
        when (openDayDeliverySlot.deliveryType) {
            DELIVERY_TYPE_TIMESLOT -> {
                gridLayoutDeliveryOptions.visibility = VISIBLE
                otherType = type
                expandableGrid.apply {
                    disablePreviousBtnOther()
                    enableNextBtnOther()
                    initialiseGridView(selectedSlotResponseOther, FIRST.week, type)
                }
            }
            else -> {
                gridLayoutDeliveryOptions.visibility = GONE
                expandableGrid.gridOnClickListner(
                    type,
                    DEFAULT_POSITION,
                    FIRST.week,
                    expandableGrid.deliverySlotsGridViewAdapter
                )
                selectedOtherSlot = Slot()
            }
        }
    }

    override fun selectedShoppingBagType(
        shoppingBagsOptionsList: ShoppingBagsOptions,
        position: Int
    ) {
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.CHECKOUT_SHOPPING_BAGS_INFO,
            activity
        )
        selectedShoppingBagType = shoppingBagsOptionsList.shoppingBagType
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            ErrorHandlerActivity.ERROR_EMPTY_REQUEST_CODE -> {
                when (resultCode) {
                    // Comes from slot selection page.
                    // Cart is empty when removed unsellable items. go to cart and refresh cart screen.
                    RESULT_CANCELED, ErrorHandlerActivity.RESULT_RETRY -> {
                        (activity as? CheckoutActivity)?.apply {
                            setResult(RESULT_EMPTY_CART)
                            closeActivity()
                        }
                    }
                }
            }
            ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE -> {
                when (resultCode) {
                    // Comes from slot selection page.
                    // Cart is empty when removed unsellable items. go to cart and refresh cart screen.
                    RESULT_CANCELED, ErrorHandlerActivity.RESULT_RETRY -> {
                        (activity as? CheckoutActivity)?.apply {
                            setResult(RESULT_RELOAD_CART)
                            closeActivity()
                        }
                    }
                }
            }
        }
    }
}
