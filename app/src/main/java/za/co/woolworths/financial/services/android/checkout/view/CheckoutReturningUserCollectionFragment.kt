package za.co.woolworths.financial.services.android.checkout.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.checkout_add_address_retuning_user.*
import kotlinx.android.synthetic.main.checkout_add_address_retuning_user.loadingBar
import kotlinx.android.synthetic.main.fragment_cart.*
import kotlinx.android.synthetic.main.fragment_checkout_returning_user_collection.*
import kotlinx.android.synthetic.main.layout_collection_details.*
import kotlinx.android.synthetic.main.layout_collection_time_details.*
import kotlinx.android.synthetic.main.layout_collection_user_information.*
import kotlinx.android.synthetic.main.layout_delivering_to_details.*
import kotlinx.android.synthetic.main.layout_native_checkout_age_confirmation.*
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_food_substitution.*
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_instructions.*
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_order_summary.*
import kotlinx.android.synthetic.main.liquor_compliance_banner.*
import kotlinx.android.synthetic.main.new_shopping_bags_layout.*
import kotlinx.android.synthetic.main.where_are_we_delivering_items.view.*
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.Companion.REGEX_DELIVERY_INSTRUCTIONS
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.FoodSubstitution
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.SAVED_ADDRESS_KEY
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressManagementBaseFragment.Companion.baseFragBundle
import za.co.woolworths.financial.services.android.checkout.view.CollectionDatesBottomSheetDialog.Companion.ARGS_KEY_COLLECTION_DATES
import za.co.woolworths.financial.services.android.checkout.view.CollectionDatesBottomSheetDialog.Companion.ARGS_KEY_SELECTED_POSITION
import za.co.woolworths.financial.services.android.checkout.view.ErrorHandlerBottomSheetDialog.Companion.ERROR_TYPE_CONFIRM_COLLECTION_ADDRESS
import za.co.woolworths.financial.services.android.checkout.view.ErrorHandlerBottomSheetDialog.Companion.ERROR_TYPE_SHIPPING_DETAILS_COLLECTION
import za.co.woolworths.financial.services.android.checkout.view.adapter.CollectionTimeSlotsAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.ShoppingBagsRadioGroupAdapter
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.checkout.viewmodel.WhoIsCollectingDetails
import za.co.woolworths.financial.services.android.common.convertToTitleCase
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.geolocation.GeoUtils
import za.co.woolworths.financial.services.android.geolocation.model.response.ConfirmLocationAddress
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.LiquorCompliance
import za.co.woolworths.financial.services.android.models.dto.OrderSummary
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.dto.app_config.native_checkout.ConfigShoppingBagsOptions
import za.co.woolworths.financial.services.android.models.network.StorePickupInfoBody
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.WFormatter.DATE_FORMAT_EEEE_COMMA_dd_MMMM
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.pushnotification.NotificationUtils
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import za.co.woolworths.financial.services.android.viewmodels.ShoppingCartLiveData
import java.util.regex.Pattern

class CheckoutReturningUserCollectionFragment : Fragment(),
    ShoppingBagsRadioGroupAdapter.EventListner, View.OnClickListener, CollectionTimeSlotsListener,
    CompoundButton.OnCheckedChangeListener {

    private var selectedTimeSlot: Slot? = null
    private var selectedPosition: Int = 0
    private var selectedShoppingBagType: Double? = null
    private lateinit var collectionTimeSlotsAdapter: CollectionTimeSlotsAdapter
    private var storePickupInfoResponse: ConfirmDeliveryAddressResponse? = null
    private lateinit var checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel
    private var selectedFoodSubstitution = FoodSubstitution.SIMILAR_SUBSTITUTION
    private var whoIsCollectingDetails: WhoIsCollectingDetails? = null
    private var savedAddressResponse = SavedAddressResponse()
    private var shimmerComponentArray: List<Pair<ShimmerFrameLayout, View>> = ArrayList()
    private var navController: NavController? = null
    private var liquorImageUrl: String? = ""
    private var liquorOrder: Boolean? = false
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
        const val KEY_COLLECTING_DETAILS = "key_collecting_details"
        const val KEY_IS_WHO_IS_COLLECTING = "key_is_WhoIsCollecting"
        const val REQUEST_KEY_SELECTED_COLLECTION_DATE: String = "SELECTED_COLLECTION_DATE"
        var COLLECTION_SLOT_SLECTION_REQUEST_CODE = 6789
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectionTimeSlotsAdapter = CollectionTimeSlotsAdapter(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.fragment_checkout_returning_user_collection,
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
        initializeCollectingFromView()
        initializeCollectingDetailsView()
        initializeCollectionTimeSlots()
        isUnSellableLiquorItemRemoved()
        getLiquorComplianceDetails()
        callStorePickupInfoAPI()
        txtContinueToPaymentCollection?.setOnClickListener(this)
        radioBtnAgeConfirmation?.setOnCheckedChangeListener(this)
        setFragmentResults()
    }

    private fun isUnSellableLiquorItemRemoved() {
        ShoppingCartLiveData.observe(viewLifecycleOwner) { isLiquorOrder ->
            if (isLiquorOrder == false) {
                ageConfirmationLayout?.visibility = View.GONE
                liquorComplianceBannerLayout?.visibility = View.GONE
                ShoppingCartLiveData.value = true
            }
        }
    }
    private fun setFragmentResults() {

        setFragmentResultListener(ErrorHandlerBottomSheetDialog.RESULT_ERROR_CODE_RETRY) { _, args ->
            when (args?.get(BUNDLE)) {
                ERROR_TYPE_CONFIRM_COLLECTION_ADDRESS -> {
                    callStorePickupInfoAPI()
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
                foodSubstitutionTitleShimmerFrameLayout,
                txtFoodSubstitutionTitle
            ),
            Pair<ShimmerFrameLayout, View>(
                radioGroupFoodSubstitutionShimmerFrameLayout,
                radioGroupFoodSubstitution
            ),

            Pair<ShimmerFrameLayout, View>(
                 collectionDetailsTitleShimmerFrameLayout,
                 tvCollectionDetailsTitle
            ),

            Pair<ShimmerFrameLayout, View>(
                collectionDetailsTextShimmerFrameLayout,
                tvCollectionDetailsText
            ),
            Pair<ShimmerFrameLayout, View>(
                ageConfirmationTitleShimmerFrameLayout,
                txtAgeConfirmationTitle
            ),

            Pair<ShimmerFrameLayout, View>(
                ageConfirmationDescShimmerFrameLayout,
                txtAgeConfirmationDesc
            ),

            Pair<ShimmerFrameLayout, View>(
                ageConfirmationDescNoteShimmerFrameLayout,
                txtAgeConfirmationDescNote
            ),

            Pair<ShimmerFrameLayout, View>(
                radioGroupAgeConfirmationShimmerFrameLayout,
                radioBtnAgeConfirmation
            ),

            Pair<ShimmerFrameLayout, View>(
                ageConfirmationTitleShimmerFrameLayout,
                txtAgeConfirmationTitle
            ),

            Pair<ShimmerFrameLayout, View>(
                ageConfirmationDescShimmerFrameLayout,
                txtAgeConfirmationDesc
            ),

            Pair<ShimmerFrameLayout, View>(
                ageConfirmationDescNoteShimmerFrameLayout,
                txtAgeConfirmationDescNote
            ),

            Pair<ShimmerFrameLayout, View>(
                radioGroupAgeConfirmationShimmerFrameLayout,
                radioBtnAgeConfirmation
            ),

            Pair<ShimmerFrameLayout, View>(
                collectionTimeDetailsShimmerLayout,
                collectionTimeDetailsConstraintLayout
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
                liquorComplianceBannerShimmerFrameLayout,
                liquorComplianceBannerLayout
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
                txtOrderTotalShimmerFrameLayout,
                txtOrderTotalTitle
            ),
            Pair<ShimmerFrameLayout, View>(
                orderTotalValueShimmerFrameLayout,
                txtOrderTotalValue
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
                imgUserProfileShimmerFrameLayout,
                imgUserProfile
            ),
            Pair<ShimmerFrameLayout, View>(
                tvCollectionUserNameShimmerFrameLayout,
                tvCollectionUserName
            ),
            Pair<ShimmerFrameLayout, View>(
                tvCollectionUserPhoneNumberShimmerFrameLayout,
                tvCollectionUserPhoneNumber
            ),
            Pair<ShimmerFrameLayout, View>(
                imageViewCaretForwardCollectionShimmerFrameLayout,
                imageViewCaretForwardCollection
            )
        )
        startShimmerView()
    }

    fun startShimmerView() {
        txtNeedBags?.visibility = View.GONE
        switchNeedBags?.visibility = View.GONE
        edtTxtSpecialDeliveryInstruction?.visibility = View.GONE
        edtTxtGiftInstructions?.visibility = View.GONE
        switchSpecialDeliveryInstruction?.isChecked = false
        switchGiftInstructions?.isChecked = false

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

        initializeFoodSubstitution()
        initializeDeliveryInstructions()
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

    private fun callStorePickupInfoAPI() {
        initShimmerView()

        checkoutAddAddressNewUserViewModel?.getStorePickupInfo(getStorePickupInfoBody())
            ?.observe(viewLifecycleOwner) { response ->
                stopShimmerView()
                when (response) {
                    is ConfirmDeliveryAddressResponse -> {
                        when (response.httpCode ?: AppConstant.HTTP_SESSION_TIMEOUT_400) {
                            AppConstant.HTTP_OK -> {
                                storePickupInfoResponse = response
                                collectionDetails()
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
                                        Utils.savePreferredDeliveryLocation(
                                            ShoppingDeliveryLocation(
                                                it
                                            )
                                        )
                                    }
                                }
                                initializeOrderSummary(response.orderSummary)
                                response.sortedJoinDeliverySlots?.apply {
                                    val firstAvailableDateSlot = getFirstAvailableSlot(this)
                                    initializeDatesAndTimeSlots(firstAvailableDateSlot)
                                    // Set default time slot selected
                                    var selectedSlotIndex = 0
                                    firstAvailableDateSlot?.let { week ->
                                        ArrayList(week?.slots).forEachIndexed { index, slot ->
                                            if (slot?.slotId.equals(selectedTimeSlot?.slotId)) {
                                                selectedSlotIndex = index
                                            }
                                        }
                                        collectionTimeSlotsAdapter.setSelectedItem(selectedSlotIndex)
                                    }
                                }
                                if(response.orderSummary?.hasMinimumBasketAmount == false) {
                                   KotlinUtils.showMinCartValueError(
                                       requireActivity() as AppCompatActivity,
                                       response.orderSummary?.minimumBasketAmount
                                   )
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
        chooseDateLayout?.setOnClickListener(this@CheckoutReturningUserCollectionFragment)
        firstAvailableDateLayout?.setOnClickListener(this@CheckoutReturningUserCollectionFragment)
    }

    private fun setSelectedDateTimeSlots(slots: List<Slot>?) {
        // No Timeslots available
        if (slots.isNullOrEmpty()) {
            return
        }
        collectionTimeSlotsAdapter.setCollectionTimeSlotData(ArrayList(slots))
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

    private fun getStorePickupInfoBody() = StorePickupInfoBody().apply {
        firstName = whoIsCollectingDetails?.recipientName
        primaryContactNo = whoIsCollectingDetails?.phoneNumber
        storeId = Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.storeId ?: ""
        vehicleModel = whoIsCollectingDetails?.vehicleModel ?: ""
        vehicleColour = whoIsCollectingDetails?.vehicleColor ?: ""
        vehicleRegistration = whoIsCollectingDetails?.vehicleRegistration ?: ""
        taxiOpted = whoIsCollectingDetails?.isMyVehicle != true
        deliveryType = Delivery.CNC.name
        address =
            ConfirmLocationAddress(Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId)
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

    private fun initializeCollectionTimeSlots() {
        recyclerViewCollectionTimeSlots?.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = collectionTimeSlotsAdapter
            collectionTimeSlotsAdapter.setCollectionTimeSlotData(null)
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

    //LiquorCompliance
    private fun getLiquorComplianceDetails() {
        baseFragBundle?.apply {
            if (containsKey(Constant.LIQUOR_ORDER)) {
                liquorOrder = getBoolean(Constant.LIQUOR_ORDER)
                if (liquorOrder == true && containsKey(Constant.NO_LIQUOR_IMAGE_URL)) {
                    liquorImageUrl = getString(Constant.NO_LIQUOR_IMAGE_URL)
                    ageConfirmationLayoutCollection?.visibility = View.VISIBLE
                    liquorComplianceBannerLayout?.visibility = View.VISIBLE
                    ImageManager.setPicture(imgLiquorBanner, liquorImageUrl)

                    ageConfirmationLayoutCollection?.visibility = View.VISIBLE
                    liquorComplianceBannerSeparator?.visibility = View.VISIBLE
                    liquorComplianceBannerLayout?.visibility = View.VISIBLE

                    if (!radioBtnAgeConfirmation.isChecked) {
                        Utils.fadeInFadeOutAnimation(txtContinueToPaymentCollection, true)
                        radioBtnAgeConfirmation?.isChecked = false
                        txtContinueToPaymentCollection?.isClickable = false
                    } else {
                        Utils.fadeInFadeOutAnimation(txtContinueToPaymentCollection, false)
                        txtContinueToPaymentCollection?.isClickable = true
                        radioBtnAgeConfirmation?.isChecked = true
                    }
                }
            } else {
                ageConfirmationLayoutCollection?.visibility = View.GONE
                liquorComplianceBannerLayout?.visibility = View.GONE
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        //single checkbox age confirmation
        if (!isChecked) {
            Utils.fadeInFadeOutAnimation(txtContinueToPaymentCollection, true)
            radioBtnAgeConfirmation?.isChecked = false
        } else {
            Utils.fadeInFadeOutAnimation(txtContinueToPaymentCollection, false)
            radioBtnAgeConfirmation?.isChecked = true
        }
    }

    private fun clearSelectedTimeSlot() {
        selectedTimeSlot = null
        collectionTimeSlotsAdapter.clearSelection()
    }

    private fun initializeCollectingFromView() {
        val location = Utils.getPreferredDeliveryLocation()
        checkoutCollectingFromLayout.setOnClickListener(this)
        if (location != null) {
            val selectedStore =
                if (KotlinUtils.getPreferredDeliveryType() == Delivery.CNC) location.fulfillmentDetails?.storeName else ""
            if (!selectedStore.isNullOrEmpty()) {
                tvNativeCheckoutDeliveringTitle?.text =
                    context?.getString(R.string.native_checkout_collecting_from)
                tvNativeCheckoutDeliveringValue.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                tvNativeCheckoutDeliveringValue?.text = convertToTitleCase(selectedStore)
            } else
                checkoutCollectingFromLayout.visibility = View.GONE
        } else
            checkoutCollectingFromLayout.visibility = View.GONE
    }

    private fun initializeCollectingDetailsView() {
        arguments?.apply {
            getString(KEY_COLLECTING_DETAILS)?.let {
                whoIsCollectingDetails =
                    Gson().fromJson(it, object : TypeToken<WhoIsCollectingDetails>() {}.type)
            }
            savedAddressResponse = Utils.jsonStringToObject(
                getString(SAVED_ADDRESS_KEY),
                SavedAddressResponse::class.java
            ) as? SavedAddressResponse ?: getSerializable(
                SAVED_ADDRESS_KEY
            ) as? SavedAddressResponse ?: SavedAddressResponse()
        }
        if (whoIsCollectingDetails != null) {
            tvCollectionUserName.text = whoIsCollectingDetails?.recipientName
            val star = "***"
            val phoneNo = whoIsCollectingDetails?.phoneNumber
            val beforeStar =
                phoneNo?.substring(0, if (phoneNo.length > 3) 3 else phoneNo.length) ?: ""
            val afterStar = phoneNo?.substring(
                if (beforeStar.length + star.length < phoneNo.length) beforeStar.length + star.length else beforeStar.length,
                phoneNo.length
            )
            tvCollectionUserPhoneNumber.text = beforeStar.plus(star).plus(afterStar)
        } else {
            checkoutCollectingUserInfoLayout.visibility = View.GONE
        }
        checkoutCollectingUserInfoLayout.setOnClickListener(this)
    }

    private fun collectionDetails() {
        if (storePickupInfoResponse?.openDayDeliverySlots?.isNullOrEmpty() == false) {
            val deliveryInDays = storePickupInfoResponse?.openDayDeliverySlots?.get(0)?.deliveryInDays
            checkoutCollectionDetailsInfoLayout?.visibility = View.VISIBLE
            tvCollectionDetailsText.text = context?.resources?.getString(R.string.collection_details_text) + " " + deliveryInDays?.lowercase() + " " + context?.resources?.getString(R.string.notify_text_label)
        }
    }
    fun initializeDeliveryInstructions() {
        edtTxtSpecialDeliveryInstruction?.addTextChangedListener(deliveryInstructionsTextWatcher)
        edtTxtGiftInstructions?.addTextChangedListener(deliveryInstructionsTextWatcher)
        edtTxtInputLayoutSpecialDeliveryInstruction?.visibility = View.GONE
        edtTxtInputLayoutSpecialDeliveryInstruction?.isCounterEnabled = false
        edtTxtInputLayoutGiftInstructions?.visibility = View.GONE
        edtTxtInputLayoutGiftInstructions?.isCounterEnabled = false
        deliveryInstructionClickListener(switchSpecialDeliveryInstruction.isChecked)
        giftClickListener(switchGiftInstructions.isChecked)

        switchSpecialDeliveryInstruction?.setOnCheckedChangeListener { _, isChecked ->
            if (loadingBar.visibility == View.VISIBLE) {
                return@setOnCheckedChangeListener
            }
            deliveryInstructionClickListener(isChecked)
        }

        switchGiftInstructions?.setOnCheckedChangeListener { _, isChecked ->
            if (loadingBar?.visibility == View.VISIBLE) {
                return@setOnCheckedChangeListener
            }
            giftClickListener(isChecked)
        }
        if (AppConfigSingleton.nativeCheckout?.currentShoppingBag?.isEnabled == true) {
            switchNeedBags?.visibility = View.VISIBLE
            txtNeedBags?.text = AppConfigSingleton.nativeCheckout?.currentShoppingBag?.title.plus(
                AppConfigSingleton.nativeCheckout?.currentShoppingBag?.description
            )
            txtNeedBags?.visibility = View.VISIBLE
            viewHorizontalSeparator?.visibility = View.GONE
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
            viewHorizontalSeparator?.visibility = View.VISIBLE
            newShoppingBagsLayout?.visibility = View.VISIBLE
            addShoppingBagsRadioButtons()
        }
    }

    private fun deliveryInstructionClickListener(isChecked: Boolean) {
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

    private fun giftClickListener(isChecked: Boolean) {
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

    private fun addShoppingBagsRadioButtons() {
        txtNewShoppingBagsSubDesc?.visibility = View.VISIBLE
        val newShoppingBags = AppConfigSingleton.nativeCheckout?.newShoppingBag
        txtNewShoppingBagsDesc?.text = newShoppingBags?.title
        txtNewShoppingBagsSubDesc?.text = newShoppingBags?.description

        val shoppingBagsAdapter =
            ShoppingBagsRadioGroupAdapter(newShoppingBags?.options, this, selectedShoppingBagType)
        shoppingBagsRecyclerView.apply {
            layoutManager = activity?.let { LinearLayoutManager(it) }
            shoppingBagsAdapter.let { adapter = it }
        }
    }

    /**
     * Initializes food substitution view and Set by default selection to [FoodSubstitution.SIMILAR_SUBSTITUTION]
     *
     * @see [FoodSubstitution]
     */
    fun initializeFoodSubstitution() {
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

                txtOrderTotalValue?.text =
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
                var defaultAddress = Address()
                savedAddressResponse.addresses?.forEach { address ->
                    if (savedAddressResponse.defaultAddressNickname.equals(address.nickname)) {
                        defaultAddress = address
                    }
                }

                KotlinUtils.presentEditDeliveryGeoLocationActivity(
                        requireActivity(),
                        COLLECTION_SLOT_SLECTION_REQUEST_CODE,
                        GeoUtils.getDelivertyType(),
                        GeoUtils.getPlaceId(),
                        false,
                        true,
                        true,
                        savedAddressResponse,
                        defaultAddress,
                        Utils.toJson(whoIsCollectingDetails),
                        liquorOrder?.let { liquorOrder ->
                            liquorImageUrl?.let { liquorImageUrl ->
                                LiquorCompliance(liquorOrder, liquorImageUrl)
                            }
                        }
                )
                activity?.finish()
            }
            R.id.checkoutCollectingUserInfoLayout -> {
                val bundle = Bundle()
                bundle.apply {
                    putString(
                        KEY_COLLECTING_DETAILS,
                        Utils.toJson(whoIsCollectingDetails)
                    )
                }
                navController?.navigate(
                    R.id.action_checkoutReturningUserCollectionFragment_checkoutWhoIsCollectingFragment,
                    bundle
                )
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
        storePickupInfoResponse?.sortedJoinDeliverySlots?.apply {
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
                            //set BR to update cart fragment in CNC flow
                            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(CheckOutFragment.TAG_CART_BROADCAST_RECEIVER))
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

    private fun isAgeConfirmationLiquorCompliance(): Boolean {
        layoutCollectionInstructions.parent.requestChildFocus(
            layoutCollectionInstructions,
            layoutCollectionInstructions
        )
        return liquorOrder == true && !radioBtnAgeConfirmation.isChecked
    }

    private fun onCheckoutPaymentClick() {
        if (isRequiredFieldsMissing() || isGiftMessage()) {
            return
        }
        if (isAgeConfirmationLiquorCompliance()) {
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
        //liquor compliance: age confirmation
        if (liquorOrder == true && !radioBtnAgeConfirmation.isChecked) {
            ageConfirmationLayout?.visibility = View.VISIBLE
            liquorComplianceBannerSeparator?.visibility = View.VISIBLE
            liquorComplianceBannerLayout?.visibility = View.VISIBLE

            Utils.fadeInFadeOutAnimation(txtContinueToPaymentCollection, false)
        } else {
            Utils.fadeInFadeOutAnimation(txtContinueToPaymentCollection, true)
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
            R.id.action_checkoutReturningUserCollectionFragment_to_errorHandlerBottomSheetDialog,
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
        if (liquorOrder == true) {
            ageConsentConfirmed = true
        }
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
        deliveryType = Delivery.CNC.type
        address =
            ConfirmLocationAddress(Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId)
        KotlinUtils.getUniqueDeviceID {
            pushNotificationToken = Utils.getToken()
            appInstanceId = it
            tokenProvider = if (Utils.isGooglePlayServicesAvailable()) NotificationUtils.TOKEN_PROVIDER_FIREBASE else NotificationUtils.TOKEN_PROVIDER_HMS
        }
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
                    /**
                     * New requirement to have instructions optional
                     */
//                    true
                    false
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
            R.id.action_checkoutReturningUserCollectionFragment_to_checkoutPaymentWebFragment,
            bundleOf(CheckoutPaymentWebFragment.KEY_ARGS_WEB_TOKEN to webTokens)
        )
    }

    @VisibleForTesting
    fun testSetShimmerArray(mockedArray: List<Pair<ShimmerFrameLayout, View>>) {
        shimmerComponentArray = mockedArray
    }

    @VisibleForTesting
    fun testSetViewModelInstance(viewModel: CheckoutAddAddressNewUserViewModel) {
        checkoutAddAddressNewUserViewModel = viewModel
    }

    @VisibleForTesting
    fun testSetStorePickupInfoResponse(mockStorePickupInfoResponse: ConfirmDeliveryAddressResponse) {
        storePickupInfoResponse = mockStorePickupInfoResponse
    }

}