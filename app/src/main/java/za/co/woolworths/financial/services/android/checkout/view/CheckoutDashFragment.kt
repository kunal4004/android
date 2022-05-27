package za.co.woolworths.financial.services.android.checkout.view

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.android.synthetic.main.checkout_add_address_retuning_user.*
import kotlinx.android.synthetic.main.checkout_add_address_retuning_user.loadingBar
import kotlinx.android.synthetic.main.fragment_checkout_returning_user_collection.*
import kotlinx.android.synthetic.main.layout_collection_time_details.*
import kotlinx.android.synthetic.main.layout_collection_time_details.view.*
import kotlinx.android.synthetic.main.layout_collection_user_information.*
import kotlinx.android.synthetic.main.layout_delivering_to_details.*
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_food_substitution.*
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_instructions.*
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_order_summary.*
import kotlinx.android.synthetic.main.layout_native_checkout_driver_tip.*
import kotlinx.android.synthetic.main.new_shopping_bags_layout.*
import kotlinx.android.synthetic.main.where_are_we_delivering_items.view.*
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.Companion.REGEX_DELIVERY_INSTRUCTIONS
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.FoodSubstitution
import za.co.woolworths.financial.services.android.checkout.view.CollectionDatesBottomSheetDialog.Companion.ARGS_KEY_COLLECTION_DATES
import za.co.woolworths.financial.services.android.checkout.view.CollectionDatesBottomSheetDialog.Companion.ARGS_KEY_SELECTED_POSITION
import za.co.woolworths.financial.services.android.checkout.view.CustomDriverTipBottomSheetDialog.Companion.MAX_TIP_VALUE
import za.co.woolworths.financial.services.android.checkout.view.CustomDriverTipBottomSheetDialog.Companion.MIN_TIP_VALUE
import za.co.woolworths.financial.services.android.checkout.view.ErrorHandlerBottomSheetDialog.Companion.ERROR_TYPE_CONFIRM_COLLECTION_ADDRESS
import za.co.woolworths.financial.services.android.checkout.view.ErrorHandlerBottomSheetDialog.Companion.ERROR_TYPE_SHIPPING_DETAILS_COLLECTION
import za.co.woolworths.financial.services.android.checkout.view.adapter.CollectionTimeSlotsAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.ShoppingBagsRadioGroupAdapter
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.response.ConfirmLocationAddress
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.OrderSummary
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.dto.app_config.native_checkout.ConfigShoppingBagsOptions
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.removeRandFromAmount
import za.co.woolworths.financial.services.android.util.WFormatter.DATE_FORMAT_EEEE_COMMA_dd_MMMM
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import java.util.regex.Pattern

class CheckoutDashFragment : Fragment(),
    ShoppingBagsRadioGroupAdapter.EventListner, View.OnClickListener, CollectionTimeSlotsListener,
    CustomDriverTipBottomSheetDialog.ClickListner {

    private var suburbId: String = ""
    private var placesId: String? = ""
    private var storeId: String? = ""
    private var nickName: String? = ""

    private var defaultAddress: Address? = null
    private var savedAddress = SavedAddressResponse()
    private var selectedTimeSlot: Slot? = null
    private var selectedPosition: Int = 0
    private var selectedShoppingBagType: Double? = null
    private lateinit var dashTimeSlotsAdapter: CollectionTimeSlotsAdapter
    private var confirmDeliveryAddressResponse: ConfirmDeliveryAddressResponse? = null
    private lateinit var checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel
    private var selectedFoodSubstitution = FoodSubstitution.SIMILAR_SUBSTITUTION
    private var shimmerComponentArray: List<Pair<ShimmerFrameLayout, View>> = ArrayList()
    private var navController: NavController? = null
    private var driverTipOptionsList: ArrayList<String>? = null
    private var selectedDriverTipValue: String? = null
    private var driverTipTextView: View? = null
    private val deliveryInstructionsTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable?) {
            val text = s?.toString() ?: ""
            val length = text.length

            if (length > 0 && !Pattern.matches(
                    REGEX_DELIVERY_INSTRUCTIONS,
                    text
                )
            ) {
                s?.delete(length - 1, length)
            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    companion object {
        const val REQUEST_KEY_SELECTED_COLLECTION_DATE: String = "SELECTED_COLLECTION_DATE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dashTimeSlotsAdapter = CollectionTimeSlotsAdapter(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.fragment_checkout_returning_user_dash,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (navController == null)
            navController = Navigation.findNavController(view)
        (activity as? CheckoutActivity)?.apply {
            showBackArrowWithTitle(bindString(R.string.checkout))
        }
        setupViewModel()
        initializeDashingToView()
        initializeDashTimeSlots()
        hideInstructionLayout()
        callConfirmLocationAPI()
        setFragmentResults()
        txtContinueToPaymentCollection?.setOnClickListener(this)
        checkoutCollectingFromLayout?.setOnClickListener(this)
    }

    private fun hideInstructionLayout() {
        txtNeedBags?.visibility = View.GONE
        switchNeedBags?.visibility = View.GONE
    }

    private fun setFragmentResults() {

        setFragmentResultListener(ErrorHandlerBottomSheetDialog.RESULT_ERROR_CODE_RETRY) { _, args ->
            when (args?.get(BUNDLE)) {
                ERROR_TYPE_CONFIRM_COLLECTION_ADDRESS -> {
                    callConfirmLocationAPI()
                }
                ERROR_TYPE_SHIPPING_DETAILS_COLLECTION -> {
                    onCheckoutPaymentClick()
                }
            }
        }
    }

    fun initShimmerView() {

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
                collectionTimeDetailsShimmerLayout,
                collectionTimeDetailsConstraintLayout
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
            Pair<ShimmerFrameLayout, View>(
                txtOrderTotalCollectionShimmerFrameLayout,
                txtOrderTotalTitleCollection
            ),
            Pair<ShimmerFrameLayout, View>(
                orderTotalValueCollectionShimmerFrameLayout,
                txtOrderTotalValueCollection
            ),
            Pair<ShimmerFrameLayout, View>(
                continuePaymentTxtCollectionShimmerFrameLayout,
                txtContinueToPaymentCollection
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
            ),
            Pair<ShimmerFrameLayout, View>(
                tipDashDriverTitleShimmerFrameLayout,
                tipDashDriverTitle
            ),
            Pair<ShimmerFrameLayout, View>(
                tipOptionScrollViewShimmerFrameLayout,
                tipOptionScrollView
            )
        )
        startShimmerView()
    }

    fun startShimmerView() {
        txtNeedBags?.visibility = View.GONE
        switchNeedBags?.visibility = View.GONE

        val shimmer = Shimmer.AlphaHighlightBuilder().build()
        shimmerComponentArray.forEach {
            it.first.setShimmer(shimmer)
            it.first.startShimmer()
            it.second.visibility = View.INVISIBLE
        }
    }

    fun stopShimmerView() {
        shimmerComponentArray.forEach {
            if (it.first.isShimmerStarted) {
                it.first.stopShimmer()
                it.first.setShimmer(null)
                it.second.visibility = View.VISIBLE
            }
        }

        txtNeedBags?.visibility = View.VISIBLE
        switchNeedBags?.visibility = View.VISIBLE
        initializeDeliveryInstructions()
        initializeDriverTipView()
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

    private fun callConfirmLocationAPI() {
        initShimmerView()
        val  confirmLocationAddress = ConfirmLocationAddress(defaultAddress?.placesId, defaultAddress?.nickname)
        var body = ConfirmLocationRequest(Delivery.DASH.type, confirmLocationAddress,
            defaultAddress?.nickname, "checkout")

        checkoutAddAddressNewUserViewModel?.getConfirmLocationDetails(body)
            .observe(viewLifecycleOwner) { response ->
                stopShimmerView()
                when (response) {
                    is ConfirmDeliveryAddressResponse -> {
                        when (response.httpCode ?: 400) {
                            AppConstant.HTTP_OK -> {
                                confirmDeliveryAddressResponse = response
                                if (!isAdded) {
                                    return@observe
                                }

                                if (response.orderSummary == null) {
                                    presentErrorDialog(
                                        getString(R.string.common_error_unfortunately_something_went_wrong),
                                        getString(R.string.no_internet_subtitle),
                                        ERROR_TYPE_CONFIRM_COLLECTION_ADDRESS
                                    )
                                    return@observe
                                }

                                if (response.orderSummary?.totalItemsCount ?: 0 <= 0) {
                                    showEmptyCart()
                                    return@observe
                                }
                                response.orderSummary?.fulfillmentDetails?.let {
                                    if (!it.deliveryType.isNullOrEmpty()) {
                                        Utils.savePreferredDeliveryLocation(ShoppingDeliveryLocation(
                                            it))
                                    }
                                }
                                initializeOrderSummary(response.orderSummary)
                                response.sortedJoinDeliverySlots?.apply {
                                    val firstAvailableDateSlot = getFirstAvailableSlot(this)
                                    initializeDatesAndTimeSlots(firstAvailableDateSlot)
                                    // Set default time slot selected
                                    dashTimeSlotsAdapter.setSelectedItem(0)
                                }
                            }
                            else -> {
                                presentErrorDialog(
                                    getString(R.string.common_error_unfortunately_something_went_wrong),
                                    getString(R.string.no_internet_subtitle),
                                    ERROR_TYPE_CONFIRM_COLLECTION_ADDRESS
                                )
                            }
                        }
                    }
                    is Throwable -> {
                        presentErrorDialog(
                            getString(R.string.common_error_unfortunately_something_went_wrong),
                            getString(R.string.no_internet_subtitle),
                            ERROR_TYPE_CONFIRM_COLLECTION_ADDRESS
                        )
                    }
                }
            }
    }

    private fun initializeDatesAndTimeSlots(selectedWeekSlot: Week?) {
        val slots = selectedWeekSlot?.slots?.filter { slot ->
            slot.available == true
        }

        if (slots.isNullOrEmpty()) {
            return
        }

        firstAvailableDateLayout?.titleTv?.text = selectedWeekSlot?.date ?: try {
            WFormatter.convertDateToFormat(
                slots[0].stringShipOnDate,
                DATE_FORMAT_EEEE_COMMA_dd_MMMM
            )
        } catch (e: Exception) {
            FirebaseManager.logException(e)
            ""
        }
        context?.let { context ->
            firstAvailableDateLayout?.titleTv?.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.white
                )
            )
            firstAvailableDateLayout?.titleTv?.background =
                ContextCompat.getDrawable(
                    context,
                    R.drawable.checkout_delivering_title_round_button_pressed
                )
            chooseDateLayout?.titleTv?.text = context.getString(R.string.choose_date)
        }

        setSelectedDateTimeSlots(slots)
        chooseDateLayout?.setOnClickListener(this@CheckoutDashFragment)
        firstAvailableDateLayout?.setOnClickListener(this@CheckoutDashFragment)
    }

    private fun setSelectedDateTimeSlots(slots: List<Slot>?) {
        // No Timeslots available
        if (slots.isNullOrEmpty()) {
            return
        }
        dashTimeSlotsAdapter.setCollectionTimeSlotData(ArrayList(slots))
    }

    fun getFirstAvailableSlot(list: List<SortedJoinDeliverySlot>): Week? {
        if (list.isNullOrEmpty()) {
            return null
        }
        list.forEach { sortedJoinDeliverySlot ->
            if (!sortedJoinDeliverySlot.week.isNullOrEmpty()) {
                sortedJoinDeliverySlot.week?.forEach { weekDay ->
                    if (!weekDay.slots.isNullOrEmpty()) {
                        return weekDay
                    }
                }
            }
        }
        return null
    }


    private fun showEmptyCart() {
        activity?.let {
            val intent = Intent(it, ErrorHandlerActivity::class.java)
            intent.putExtra(
                ErrorHandlerActivity.ERROR_TYPE,
                ErrorHandlerActivity.ERROR_TYPE_EMPTY_CART
            )
            it.startActivityForResult(intent, ErrorHandlerActivity.ERROR_EMPTY_REQUEST_CODE)
        }
    }

    private fun initializeDashTimeSlots() {

        checkoutCollectingTimeDetailsLayout?.tvCollectionTimeDetailsTitle?.text = getString(R.string.select_delivery_timeslot)
        checkoutCollectingTimeDetailsLayout?.tvCollectionTimeDetailsDate?.text = getString(R.string.dash_delivery_date)
        checkoutCollectingTimeDetailsLayout?.tvCollectionTimeDetailsTimeSlot?.text = getString(R.string.dash_delivery_timeslot)
        recyclerViewCollectionTimeSlots?.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = dashTimeSlotsAdapter
            dashTimeSlotsAdapter.setCollectionTimeSlotData(null)
        }

        /**
         * Returned value from Collection Date Bottom sheet dialog
         */
        setFragmentResultListener(REQUEST_KEY_SELECTED_COLLECTION_DATE) { _, bundle ->
            bundle.getSerializable(REQUEST_KEY_SELECTED_COLLECTION_DATE)?.apply {
                (this as? Week)?.let { selectedWeek ->
                    selectedPosition = bundle.getInt(ARGS_KEY_SELECTED_POSITION, 0)
                    initializeDatesAndTimeSlots(selectedWeek)
                    clearSelectedTimeSlot()
                }
            }
        }
    }

    private fun initializeDriverTipView() {
        //Todo This value will come from Config once it is available.
        driverTipOptionsList = ArrayList()
        driverTipOptionsList!!.add("R10")
        driverTipOptionsList!!.add("R20")
        driverTipOptionsList!!.add("R30")
        driverTipOptionsList!!.add("Own Amount")
        selectedDriverTipValue = null
        showDriverTipView()
    }

    private fun showDriverTipView() {
        if (!driverTipOptionsList.isNullOrEmpty()) {
            layoutDriverTip.visibility = View.VISIBLE
            for ((index, options) in driverTipOptionsList!!.withIndex()) {
                driverTipTextView =
                    View.inflate(context, R.layout.where_are_we_delivering_items, null)
                val titleTextView: TextView? = driverTipTextView?.findViewById(R.id.titleTv)
                titleTextView?.tag = index
                titleTextView?.text = options
                if (!selectedDriverTipValue.isNullOrEmpty() && selectedDriverTipValue.equals(
                        options
                    )
                ) {
                    titleTextView?.background =
                        bindDrawable(R.drawable.checkout_delivering_title_round_button_pressed)
                    titleTextView?.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    tipNoteTextView?.visibility = View.VISIBLE
                }
                titleTextView?.setOnClickListener {
                    var isSameSelection =
                        true // Because we want to change this view after the value entered from user.
                    selectedDriverTipValue = (it as TextView).text as? String

                    if (it.tag == driverTipOptionsList!!.lastIndex) {
                        val tipValue = if (titleTextView.text.toString()
                                .equals(driverTipOptionsList!!.lastOrNull())
                        ) getString(R.string.empty) else removeRandFromAmount(titleTextView.text.toString()
                            .trim())
                        val customDriverTipDialog = CustomDriverTipBottomSheetDialog.newInstance(
                            requireContext().getString(R.string.tip_your_dash_driver),
                            requireContext().getString(R.string.enter_your_own_amount, MIN_TIP_VALUE.toInt(), MAX_TIP_VALUE.toInt()), tipValue, this)
                        customDriverTipDialog.show(requireFragmentManager(),
                            CustomDriverTipBottomSheetDialog::class.java.simpleName)
                    } else {
                        isSameSelection = resetAllDriverTip(it.tag as Int)
                        if (isSameSelection) {
                            selectedDriverTipValue = null
                            tipNoteTextView?.visibility = View.GONE
                        }
                    }

                    if (!isSameSelection) {
                        // Change background of selected Tip as it's not unselection.
                        it.background =
                            bindDrawable(R.drawable.checkout_delivering_title_round_button_pressed)
                        it.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.white
                            )
                        )
                        tipNoteTextView?.visibility = View.VISIBLE
                    }
                }
                tip_options_layout?.addView(driverTipTextView)
            }
        }
    }

    private fun resetAllDriverTip(selectedTag: Int): Boolean {
        //change background of all unselected Tip
        var sameSelection = false
        for ((index) in driverTipOptionsList!!.withIndex()) {
            val titleTextView: TextView? = view?.findViewWithTag(index)
            if (titleTextView?.textColors?.defaultColor?.equals(ContextCompat.getColor(
                    requireContext(),
                    R.color.white)) == true && titleTextView.tag.equals(
                    selectedTag)
            ) {
                sameSelection = true
            }
            if (index == driverTipOptionsList?.size?.minus(1) ?: null) {
                titleTextView?.setText(driverTipOptionsList?.lastOrNull())
                titleTextView?.setCompoundDrawables(null, null, null, null)
            }
            titleTextView?.background =
                bindDrawable(R.drawable.checkout_delivering_title_round_button)
            titleTextView?.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.checkout_delivering_title
                )
            )
        }
        return sameSelection
    }

    private fun clearSelectedTimeSlot() {
        selectedTimeSlot = null
        dashTimeSlotsAdapter.clearSelection()
    }

    private fun initializeDashingToView() {
        tvNativeCheckoutDeliveringTitle?.text = getString(R.string.dashing_to)
        chooseDateLayout?.visibility = View.GONE
        if (arguments == null) {
            checkoutDeliveryDetailsLayout.visibility = View.GONE
            return
        }
        context?.let { context ->
            savedAddress = Utils.jsonStringToObject(
                CheckoutAddressManagementBaseFragment.baseFragBundle?.getString(
                    CheckoutAddressConfirmationFragment.SAVED_ADDRESS_KEY
                ),
                SavedAddressResponse::class.java
            ) as? SavedAddressResponse
                ?: CheckoutAddressManagementBaseFragment.baseFragBundle?.getSerializable(
                    CheckoutAddressConfirmationFragment.SAVED_ADDRESS_KEY
                ) as? SavedAddressResponse
                        ?: SavedAddressResponse()

            if (savedAddress?.addresses.isNullOrEmpty()) {
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
                        this.defaultAddress = address
                        suburbId = address.suburbId ?: ""
                        placesId = address?.placesId
                        storeId = address?.storeId
                        nickName = address?.nickname
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
                checkoutDeliveryDetailsLayout?.setOnClickListener(this)
            }
        }
    }


    fun initializeDeliveryInstructions() {
        edtTxtSpecialDeliveryInstruction?.addTextChangedListener(deliveryInstructionsTextWatcher)
        edtTxtGiftInstructions?.addTextChangedListener(deliveryInstructionsTextWatcher)
        edtTxtInputLayoutSpecialDeliveryInstruction?.visibility = View.GONE
        edtTxtInputLayoutSpecialDeliveryInstruction?.isCounterEnabled = false
        edtTxtInputLayoutGiftInstructions?.visibility = View.GONE
        edtTxtInputLayoutGiftInstructions?.isCounterEnabled = false

        switchSpecialDeliveryInstruction?.setOnCheckedChangeListener { _, isChecked ->
            if (loadingBar.visibility == View.VISIBLE) {
                return@setOnCheckedChangeListener
            }
            if (isChecked)
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.CHECKOUT_SPECIAL_COLLECTION_INSTRUCTION,
                    activity
                )
            edtTxtInputLayoutSpecialDeliveryInstruction?.visibility =
                if (isChecked) View.VISIBLE else View.GONE
            edtTxtInputLayoutSpecialDeliveryInstruction?.isCounterEnabled = isChecked
            edtTxtSpecialDeliveryInstruction?.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }

        switchGiftInstructions?.setOnCheckedChangeListener { _, isChecked ->
            if (loadingBar?.visibility == View.VISIBLE) {
                return@setOnCheckedChangeListener
            }
            if (isChecked)
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.CHECKOUT_IS_THIS_GIFT,
                    activity
                )
            edtTxtInputLayoutGiftInstructions?.visibility =
                if (isChecked) View.VISIBLE else View.GONE
            edtTxtInputLayoutGiftInstructions?.isCounterEnabled = isChecked
            edtTxtGiftInstructions?.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }
        if (AppConfigSingleton.nativeCheckout?.currentShoppingBag?.isEnabled == true) {
            switchNeedBags?.visibility = View.VISIBLE
            txtNeedBags?.visibility = View.VISIBLE
            newShoppingBagsLayout?.visibility = View.GONE
            switchNeedBags?.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    Utils.triggerFireBaseEvents(
                        FirebaseManagerAnalyticsProperties.CHECKOUT_SHOPPING_BAGS_INFO,
                        activity
                    )
                }
            }
        } else if (AppConfigSingleton.nativeCheckout?.newShoppingBag?.isEnabled == true) {
            switchNeedBags?.visibility = View.GONE
            txtNeedBags?.visibility = View.GONE
            newShoppingBagsLayout?.visibility = View.VISIBLE
            addShoppingBagsRadioButtons()
        }
    }

    private fun addShoppingBagsRadioButtons() {
        txtNewShoppingBagsSubDesc?.visibility = View.VISIBLE
        val newShoppingBags = AppConfigSingleton.nativeCheckout?.newShoppingBag
        txtNewShoppingBagsDesc?.text = newShoppingBags?.title
        txtNewShoppingBagsSubDesc?.text = newShoppingBags?.description

        val shoppingBagsAdapter = ShoppingBagsRadioGroupAdapter(newShoppingBags?.options, this)
        shoppingBagsRecyclerView.apply {
            layoutManager = activity?.let { LinearLayoutManager(it) }
            shoppingBagsAdapter.let { adapter = it }
        }
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

                txtOrderTotalValueCollection?.text =
                    CurrencyFormatter.formatAmountToRandAndCentWithSpace(it.total)
            }
        }
    }

    override fun selectedShoppingBagType(
        shoppingBagsOptionsList: ConfigShoppingBagsOptions,
        position: Int,
    ) {
        selectedShoppingBagType = shoppingBagsOptionsList.shoppingBagType
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.checkoutCollectingFromLayout -> {

                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.CHECKOUT_COLLECTION_USER_EDIT,
                    hashMapOf(
                        FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_COLLECTION_EDIT_USER_DETAILS
                    ),
                    activity
                )

                KotlinUtils.presentEditDeliveryGeoLocationActivity(
                    requireActivity(),
                    CheckoutAddAddressReturningUserFragment.SLOT_SELECTION_REQUEST_CODE,
                    KotlinUtils.getPreferredDeliveryType(),
                    placesId,
                    false,
                    true,
                    true,
                    savedAddress,
                    defaultAddress
                )
                activity?.finish()
            }
            R.id.chooseDateLayout -> {
                onChooseDateClicked()
            }
            R.id.txtContinueToPaymentCollection -> {
                onCheckoutPaymentClick()
            }
        }
    }

    fun onChooseDateClicked() {
        confirmDeliveryAddressResponse?.sortedJoinDeliverySlots?.apply {
            // No available dates to select
            if (this.isNullOrEmpty()) {
                return
            }
            val weekDaysList = ArrayList<Week>(0)
            this.forEach { sortedJoinDeliverySlot ->
                if (sortedJoinDeliverySlot == null || sortedJoinDeliverySlot.week.isNullOrEmpty()) {
                    return
                }
                sortedJoinDeliverySlot.week?.let {
                    weekDaysList.addAll(it)
                }
            }
            navigateToCollectionDateDialog(weekDaysList)
        }
    }

    fun navigateToCollectionDateDialog(weekDaysList: ArrayList<Week>) {
        navController?.navigate(
            R.id.action_checkoutReturningUserCollectionFragment_to_collectionDatesBottomSheetDialog,
            bundleOf(
                ARGS_KEY_COLLECTION_DATES to weekDaysList,
                ARGS_KEY_SELECTED_POSITION to selectedPosition
            )
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            ErrorHandlerActivity.ERROR_EMPTY_REQUEST_CODE -> {
                when (resultCode) {
                    // Comes from slot selection page.
                    // Cart is empty when removed unsellable items. go to cart and refresh cart screen.
                    Activity.RESULT_CANCELED, ErrorHandlerActivity.RESULT_RETRY -> {
                        (activity as? CheckoutActivity)?.apply {
                            setResult(CheckOutFragment.RESULT_EMPTY_CART)
                            closeActivity()
                        }
                    }
                }
            }
        }
    }

    override fun setSelectedTimeSlot(slot: Slot?) {
        selectedTimeSlot = slot
        isRequiredFieldsMissing()
    }

    private fun onCheckoutPaymentClick() {
        if (isRequiredFieldsMissing() || isInstructionsMissing() || isGiftMessage()) {
            return
        }

        val body = getShipmentDetailsBody()
        if (TextUtils.isEmpty(body.oddDeliverySlotId) && TextUtils.isEmpty(body.foodDeliverySlotId)
            && TextUtils.isEmpty(body.otherDeliverySlotId)
        ) {
            return
        }
        loadingBar?.visibility = View.VISIBLE
        setScreenClickEvents(false)
        checkoutAddAddressNewUserViewModel.getShippingDetails(body)
            .observe(viewLifecycleOwner) { response ->
                loadingBar.visibility = View.GONE
                setScreenClickEvents(true)
                when (response) {
                    is ShippingDetailsResponse -> {
                        if (TextUtils.isEmpty(response.jsessionId) || TextUtils.isEmpty(response.auth)) {
                            presentErrorDialog(
                                getString(R.string.common_error_unfortunately_something_went_wrong),
                                getString(R.string.common_error_message_without_contact_info),
                                ERROR_TYPE_SHIPPING_DETAILS_COLLECTION
                            )
                            return@observe
                        }
                        navigateToPaymentWebpage(response)
                    }
                    is Throwable -> {
                        presentErrorDialog(
                            getString(R.string.common_error_unfortunately_something_went_wrong),
                            getString(R.string.common_error_message_without_contact_info),
                            ERROR_TYPE_SHIPPING_DETAILS_COLLECTION
                        )
                    }
                }
            }
    }

    private fun presentErrorDialog(title: String, subTitle: String, errorType: Int) {
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
            errorType
        )
        view?.findNavController()?.navigate(
            R.id.action_checkoutDashFragment_to_errorHandlerBottomSheetDialog,
            bundle
        )
    }

    private fun setScreenClickEvents(isClickable: Boolean) {
        radioGroupFoodSubstitution?.isClickable = isClickable
        checkoutDeliveryDetailsLayout?.isClickable = isClickable
        switchNeedBags?.isClickable = isClickable
        switchGiftInstructions?.isClickable = isClickable
        switchSpecialDeliveryInstruction?.isClickable = isClickable
    }

    private fun getShipmentDetailsBody() = ShippingDetailsBody().apply {
        requestFrom = "express"
        joinBasket = true
        foodShipOnDate = selectedTimeSlot?.stringShipOnDate
        otherShipOnDate = ""
        foodDeliverySlotId = selectedTimeSlot?.slotId
        otherDeliverySlotId = ""
        oddDeliverySlotId = ""
        foodDeliveryStartHour = selectedTimeSlot?.intHourFrom?.toLong() ?: 0
        otherDeliveryStartHour = 0
        substituesAllowed = selectedFoodSubstitution.rgb
        plasticBags = switchNeedBags?.isChecked ?: false
        shoppingBagType = selectedShoppingBagType
        giftNoteSelected = switchGiftInstructions?.isChecked ?: false
        deliverySpecialInstructions =
            if (switchSpecialDeliveryInstruction?.isChecked == true) edtTxtSpecialDeliveryInstruction?.text.toString() else ""
        giftMessage =
            if (switchGiftInstructions?.isChecked == true) edtTxtGiftInstructions?.text.toString() else ""
        suburbId = ""
        storeId = Utils.getPreferredDeliveryLocation()?.let {
            it.fulfillmentDetails.storeId
        }
        deliveryType = Delivery.DASH.type
        address = ConfirmLocationAddress(Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId)
        driverTip = removeRandFromAmount(selectedDriverTipValue ?: "0.0").toDouble()
    }

    private fun isGiftMessage(): Boolean {
        return when (switchGiftInstructions?.isChecked) {
            true -> {
                if (TextUtils.isEmpty(edtTxtGiftInstructions?.text?.toString())) {

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

    private fun isInstructionsMissing(): Boolean {
        return when (switchSpecialDeliveryInstruction?.isChecked) {
            true -> {
                if (TextUtils.isEmpty(edtTxtSpecialDeliveryInstruction?.text.toString())) {
                    // scroll to instructions layout
                    checkoutReturningCollectionScrollView?.smoothScrollTo(
                        0,
                        layoutCollectionInstructions?.top ?: 0
                    )
                    true
                } else false
            }
            else -> false
        }
    }

    private fun isRequiredFieldsMissing(): Boolean {
        if (!TextUtils.isEmpty(selectedTimeSlot?.slotId)) {
            txtSelectCollectionTimeSlotFoodError?.visibility = View.GONE
            return false
        }
        // scroll to slot selection layout
        checkoutReturningCollectionScrollView?.smoothScrollTo(
            0,
            checkoutCollectingTimeDetailsLayout?.top ?: 0
        )
        txtSelectCollectionTimeSlotFoodError?.visibility = View.VISIBLE
        return true
    }

    private fun navigateToPaymentWebpage(webTokens: ShippingDetailsResponse) {
        view?.findNavController()?.navigate(
            R.id.action_checkoutDashFragment_to_checkoutPaymentWebFragment,
            bundleOf(CheckoutPaymentWebFragment.KEY_ARGS_WEB_TOKEN to webTokens)
        )
    }

    override fun onConfirmClick(tipValue: String) {
        val titleTextView: TextView? =
            driverTipTextView?.findViewWithTag(driverTipOptionsList?.lastIndex)
        driverTipOptionsList?.lastIndex?.let { resetAllDriverTip(it) }
        titleTextView?.text = "R$tipValue "
        val image = AppCompatResources.getDrawable(requireContext(), R.drawable.edit_icon_white)
        titleTextView?.setCompoundDrawablesWithIntrinsicBounds(null, null, image, null)
        titleTextView?.compoundDrawablePadding = resources.getDimension(R.dimen.five_dp).toInt()

        titleTextView?.background =
            bindDrawable(R.drawable.checkout_delivering_title_round_button_pressed)
        titleTextView?.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        tipNoteTextView?.visibility = View.VISIBLE
        selectedDriverTipValue = tipValue
    }
}