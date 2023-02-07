package za.co.woolworths.financial.services.android.checkout.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.*
import android.text.style.StyleSpan
import android.view.View
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
import com.awfs.coordination.databinding.FragmentCheckoutReturningUserCollectionBinding
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
import za.co.woolworths.financial.services.android.util.StoreUtils

class CheckoutReturningUserCollectionFragment :
    Fragment(R.layout.fragment_checkout_returning_user_collection),
    ShoppingBagsRadioGroupAdapter.EventListner, View.OnClickListener, CollectionTimeSlotsListener,
    CompoundButton.OnCheckedChangeListener {

    private var isItemLimitExceeded: Boolean = false
    private lateinit var binding: FragmentCheckoutReturningUserCollectionBinding
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCheckoutReturningUserCollectionBinding.bind(view)

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
        binding.txtContinueToPaymentCollection?.setOnClickListener(this)
        binding.ageConfirmationLayoutCollection.radioBtnAgeConfirmation?.setOnCheckedChangeListener(
            this
        )
        setFragmentResults()
    }

    private fun isUnSellableLiquorItemRemoved() {
        ShoppingCartLiveData.observe(viewLifecycleOwner) { isLiquorOrder ->
            if (isLiquorOrder == false) {
                binding.ageConfirmationLayoutCollection?.root?.visibility = View.GONE
                binding.ageConfirmationLayoutCollection.liquorComplianceBannerLayout?.root?.visibility =
                    View.GONE
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
                binding.checkoutCollectingFromLayout.deliveringTitleShimmerFrameLayout,
                binding.checkoutCollectingFromLayout.tvNativeCheckoutDeliveringTitle
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.checkoutCollectingFromLayout.deliveringTitleValueShimmerFrameLayout,
                binding.checkoutCollectingFromLayout.tvNativeCheckoutDeliveringValue
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.checkoutCollectingFromLayout.forwardImgViewShimmerFrameLayout,
                binding.checkoutCollectingFromLayout.imageViewCaretForward
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.nativeCheckoutFoodSubstitutionLayout.foodSubstitutionTitleShimmerFrameLayout,
                binding.nativeCheckoutFoodSubstitutionLayout.txtFoodSubstitutionTitle
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.nativeCheckoutFoodSubstitutionLayout.radioGroupFoodSubstitutionShimmerFrameLayout,
                binding.nativeCheckoutFoodSubstitutionLayout.radioGroupFoodSubstitution
            ),

            Pair<ShimmerFrameLayout, View>(
                binding.layoutCollectionInstructions.checkoutCollectionDetailsInfoLayout.collectionDetailsTitleShimmerFrameLayout,
                binding.layoutCollectionInstructions.checkoutCollectionDetailsInfoLayout.tvCollectionDetailsTitle
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.layoutCollectionInstructions.checkoutCollectionDetailsInfoLayout.collectionDetailsTextShimmerFrameLayout,
                binding.layoutCollectionInstructions.checkoutCollectionDetailsInfoLayout.tvCollectionDetailsText
            ),

            Pair<ShimmerFrameLayout, View>(
                binding.ageConfirmationLayoutCollection.ageConfirmationTitleShimmerFrameLayout,
                binding.ageConfirmationLayoutCollection.txtAgeConfirmationTitle
            ),

            Pair<ShimmerFrameLayout, View>(
                binding.ageConfirmationLayoutCollection.ageConfirmationDescShimmerFrameLayout,
                binding.ageConfirmationLayoutCollection.txtAgeConfirmationDesc
            ),

            Pair<ShimmerFrameLayout, View>(
                binding.ageConfirmationLayoutCollection.ageConfirmationDescNoteShimmerFrameLayout,
                binding.ageConfirmationLayoutCollection.txtAgeConfirmationDescNote
            ),

            Pair<ShimmerFrameLayout, View>(
                binding.ageConfirmationLayoutCollection.radioGroupAgeConfirmationShimmerFrameLayout,
                binding.ageConfirmationLayoutCollection.radioBtnAgeConfirmation
            ),

            Pair<ShimmerFrameLayout, View>(
                binding.ageConfirmationLayoutCollection.ageConfirmationTitleShimmerFrameLayout,
                binding.ageConfirmationLayoutCollection.txtAgeConfirmationTitle
            ),

            Pair<ShimmerFrameLayout, View>(
                binding.ageConfirmationLayoutCollection.ageConfirmationDescShimmerFrameLayout,
                binding.ageConfirmationLayoutCollection.txtAgeConfirmationDesc
            ),

            Pair<ShimmerFrameLayout, View>(
                binding.ageConfirmationLayoutCollection.ageConfirmationDescNoteShimmerFrameLayout,
                binding.ageConfirmationLayoutCollection.txtAgeConfirmationDescNote
            ),

            Pair<ShimmerFrameLayout, View>(
                binding.ageConfirmationLayoutCollection.radioGroupAgeConfirmationShimmerFrameLayout,
                binding.ageConfirmationLayoutCollection.radioBtnAgeConfirmation
            ),

            Pair<ShimmerFrameLayout, View>(
                binding.checkoutCollectingTimeDetailsLayout.collectionTimeDetailsShimmerLayout,
                binding.checkoutCollectingTimeDetailsLayout.collectionTimeDetailsConstraintLayout
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.nativeCheckoutFoodSubstitutionLayout.foodSubstitutionDescShimmerFrameLayout,
                binding.nativeCheckoutFoodSubstitutionLayout.txtFoodSubstitutionDesc
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.nativeCheckoutFoodSubstitutionLayout.radioGroupFoodSubstitutionShimmerFrameLayout,
                binding.nativeCheckoutFoodSubstitutionLayout.radioGroupFoodSubstitution
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.ageConfirmationLayoutCollection.liquorComplianceBannerShimmerFrameLayout,
                binding.ageConfirmationLayoutCollection.liquorComplianceBannerLayout.root
            ),

            Pair<ShimmerFrameLayout, View>(
                binding.layoutCollectionInstructions.instructionTxtShimmerFrameLayout,
                binding.layoutCollectionInstructions.txtSpecialDeliveryInstruction
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.layoutCollectionInstructions.specialInstructionSwitchShimmerFrameLayout,
                binding.layoutCollectionInstructions.switchSpecialDeliveryInstruction
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.layoutCollectionInstructions.giftInstructionTxtShimmerFrameLayout,
                binding.layoutCollectionInstructions.txtGiftInstructions
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.layoutCollectionInstructions.giftInstructionSwitchShimmerFrameLayout,
                binding.layoutCollectionInstructions.switchGiftInstructions
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.layoutCheckoutDeliveryOrderSummary.txtYourCartShimmerFrameLayout,
                binding.layoutCheckoutDeliveryOrderSummary.txtOrderSummaryYourCart
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.layoutCheckoutDeliveryOrderSummary.yourCartValueShimmerFrameLayout,
                binding.layoutCheckoutDeliveryOrderSummary.txtOrderSummaryYourCartValue
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.layoutCheckoutDeliveryOrderSummary.deliveryFeeTxtShimmerFrameLayout,
                binding.layoutCheckoutDeliveryOrderSummary.txtOrderSummaryDeliveryFee
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.layoutCheckoutDeliveryOrderSummary.deliveryFeeValueShimmerFrameLayout,
                binding.layoutCheckoutDeliveryOrderSummary.txtOrderSummaryDeliveryFeeValue
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.layoutCheckoutDeliveryOrderSummary.summaryNoteShimmerFrameLayout,
                binding.layoutCheckoutDeliveryOrderSummary.txtOrderSummaryNote
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.layoutCheckoutDeliveryOrderSummary.txtOrderTotalShimmerFrameLayout,
                binding.layoutCheckoutDeliveryOrderSummary.txtOrderTotalTitle
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.layoutCheckoutDeliveryOrderSummary.orderTotalValueShimmerFrameLayout,
                binding.layoutCheckoutDeliveryOrderSummary.txtOrderTotalValue
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.continuePaymentTxtCollectionShimmerFrameLayout,
                binding.txtContinueToPaymentCollection
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.layoutCollectionInstructions.newShoppingBagsLayout.newShoppingBagsTitleShimmerFrameLayout,
                binding.layoutCollectionInstructions.newShoppingBagsLayout.newShoppingBagsTitle
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.layoutCollectionInstructions.newShoppingBagsLayout.newShoppingBagsDescShimmerFrameLayout,
                binding.layoutCollectionInstructions.newShoppingBagsLayout.txtNewShoppingBagsDesc
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.layoutCollectionInstructions.newShoppingBagsLayout.radioGroupShoppingBagsShimmerFrameLayout,
                binding.layoutCollectionInstructions.newShoppingBagsLayout.radioGroupShoppingBags
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.checkoutCollectingUserInfoLayout.imgUserProfileShimmerFrameLayout,
                binding.checkoutCollectingUserInfoLayout.imgUserProfile
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.checkoutCollectingUserInfoLayout.tvCollectionUserNameShimmerFrameLayout,
                binding.checkoutCollectingUserInfoLayout.tvCollectionUserName
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.checkoutCollectingUserInfoLayout.tvCollectionUserPhoneNumberShimmerFrameLayout,
                binding.checkoutCollectingUserInfoLayout.tvCollectionUserPhoneNumber
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.checkoutCollectingUserInfoLayout.imageViewCaretForwardCollectionShimmerFrameLayout,
                binding.checkoutCollectingUserInfoLayout.imageViewCaretForwardCollection
            )
        )
        startShimmerView()
    }

    fun startShimmerView() {
        binding.layoutCollectionInstructions.txtNeedBags?.visibility = View.GONE
        binding.layoutCollectionInstructions.switchNeedBags?.visibility = View.GONE
        binding.layoutCollectionInstructions.edtTxtSpecialDeliveryInstruction?.visibility =
            View.GONE
        binding.layoutCollectionInstructions.edtTxtGiftInstructions?.visibility = View.GONE
        binding.layoutCollectionInstructions.switchSpecialDeliveryInstruction?.isChecked = false
        binding.layoutCollectionInstructions.switchGiftInstructions?.isChecked = false

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

        binding.layoutCollectionInstructions.txtNeedBags?.visibility = View.VISIBLE
        binding.layoutCollectionInstructions.switchNeedBags?.visibility = View.VISIBLE

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

        isItemLimitExceeded = false
        checkoutAddAddressNewUserViewModel?.getStorePickupInfo(getStorePickupInfoBody())
            ?.observe(viewLifecycleOwner) { response ->
                stopShimmerView()
                when (response) {
                    is ConfirmDeliveryAddressResponse -> {
                        when (response.httpCode ?: AppConstant.HTTP_SESSION_TIMEOUT_400) {
                            AppConstant.HTTP_OK -> {
                                storePickupInfoResponse = response
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
                                var maxItemLimit = -1
                                response.orderSummary?.fulfillmentDetails?.let {
                                    maxItemLimit = it.foodMaximumQuantity ?: -1
                                    if (!it.deliveryType.isNullOrEmpty()) {
                                        Utils.savePreferredDeliveryLocation(
                                            ShoppingDeliveryLocation(
                                                it
                                            )
                                        )
                                    }
                                }

                                isItemLimitExceeded =
                                    (response.orderSummary?.totalItemsCount ?: 0) > maxItemLimit
                                if (isItemLimitExceeded && !isFBHOnly()) {
                                    showMaxItemView()
                                }

                                initializeOrderSummary(response.orderSummary)
                                updateCollectionItemsForCheckout()
                                if (response.orderSummary?.hasMinimumBasketAmount == false) {
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

        binding.checkoutCollectingTimeDetailsLayout.firstAvailableDateLayout?.titleTv?.text =
            selectedWeekSlot?.date ?: try {
                WFormatter.convertDateToFormat(
                    slots[0].stringShipOnDate,
                    DATE_FORMAT_EEEE_COMMA_dd_MMMM
                )
            } catch (e: Exception) {
                FirebaseManager.logException(e)
                ""
            }
        context?.let { context ->
            binding.checkoutCollectingTimeDetailsLayout.firstAvailableDateLayout?.titleTv?.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.white
                )
            )
            binding.checkoutCollectingTimeDetailsLayout.firstAvailableDateLayout?.titleTv?.background =
                ContextCompat.getDrawable(
                    context,
                    R.drawable.checkout_delivering_title_round_button_pressed
                )
            binding.checkoutCollectingTimeDetailsLayout.chooseDateLayout?.titleTv?.text =
                context.getString(R.string.choose_date)
        }

        setSelectedDateTimeSlots(slots)
        binding.checkoutCollectingTimeDetailsLayout.chooseDateLayout?.root?.setOnClickListener(this@CheckoutReturningUserCollectionFragment)
        binding.checkoutCollectingTimeDetailsLayout.firstAvailableDateLayout?.root?.setOnClickListener(
            this@CheckoutReturningUserCollectionFragment
        )
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
        binding.checkoutCollectingTimeDetailsLayout.recyclerViewCollectionTimeSlots?.apply {
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
                    binding.ageConfirmationLayoutCollection?.root?.visibility = View.VISIBLE
                    binding.ageConfirmationLayoutCollection.liquorComplianceBannerLayout?.root?.visibility =
                        View.VISIBLE
                    ImageManager.setPicture(
                        binding.ageConfirmationLayoutCollection.liquorComplianceBannerLayout.imgLiquorBanner,
                        liquorImageUrl
                    )

                    binding.ageConfirmationLayoutCollection?.root?.visibility = View.VISIBLE
                    binding.ageConfirmationLayoutCollection.liquorComplianceBannerSeparator?.visibility =
                        View.VISIBLE
                    binding.ageConfirmationLayoutCollection.liquorComplianceBannerLayout?.root?.visibility =
                        View.VISIBLE

                    if (!binding.ageConfirmationLayoutCollection.radioBtnAgeConfirmation.isChecked) {
                        Utils.fadeInFadeOutAnimation(binding.txtContinueToPaymentCollection, true)
                        binding.ageConfirmationLayoutCollection.radioBtnAgeConfirmation?.isChecked =
                            false
                        binding.txtContinueToPaymentCollection?.isClickable = false
                    } else {
                        Utils.fadeInFadeOutAnimation(binding.txtContinueToPaymentCollection, false)
                        binding.txtContinueToPaymentCollection?.isClickable = true
                        binding.ageConfirmationLayoutCollection.radioBtnAgeConfirmation?.isChecked =
                            true
                    }
                }
            } else {
                binding.ageConfirmationLayoutCollection?.root?.visibility = View.GONE
                binding.ageConfirmationLayoutCollection.liquorComplianceBannerLayout?.root?.visibility =
                    View.GONE
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        //single checkbox age confirmation
        if (!isChecked) {
            Utils.fadeInFadeOutAnimation(binding.txtContinueToPaymentCollection, true)
            binding.ageConfirmationLayoutCollection.radioBtnAgeConfirmation?.isChecked = false
        } else {
            Utils.fadeInFadeOutAnimation(binding.txtContinueToPaymentCollection, false)
            binding.ageConfirmationLayoutCollection.radioBtnAgeConfirmation?.isChecked = true
        }
    }

    private fun clearSelectedTimeSlot() {
        selectedTimeSlot = null
        collectionTimeSlotsAdapter.clearSelection()
    }

    private fun initializeCollectingFromView() {
        val location = Utils.getPreferredDeliveryLocation()
        binding.checkoutCollectingFromLayout.root.setOnClickListener(this)
        if (location != null) {
            val selectedStore =
                if (KotlinUtils.getPreferredDeliveryType() == Delivery.CNC) location.fulfillmentDetails?.storeName else ""
            if (!selectedStore.isNullOrEmpty()) {
                binding.checkoutCollectingFromLayout.tvNativeCheckoutDeliveringTitle?.text =
                    context?.getString(R.string.native_checkout_collecting_from)
                binding.checkoutCollectingFromLayout.tvNativeCheckoutDeliveringValue.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                binding.checkoutCollectingFromLayout.tvNativeCheckoutDeliveringValue?.text =
                    convertToTitleCase(selectedStore)
            } else
                binding.checkoutCollectingFromLayout.root.visibility = View.GONE
        } else
            binding.checkoutCollectingFromLayout.root.visibility = View.GONE
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
            binding.checkoutCollectingUserInfoLayout.tvCollectionUserName.text =
                whoIsCollectingDetails?.recipientName
            val star = "***"
            val phoneNo = whoIsCollectingDetails?.phoneNumber
            val beforeStar =
                phoneNo?.substring(0, if (phoneNo.length > 3) 3 else phoneNo.length) ?: ""
            val afterStar = phoneNo?.substring(
                if (beforeStar.length + star.length < phoneNo.length) beforeStar.length + star.length else beforeStar.length,
                phoneNo.length
            )
            binding.checkoutCollectingUserInfoLayout.tvCollectionUserPhoneNumber.text =
                beforeStar.plus(star).plus(afterStar)
        } else {
            binding.checkoutCollectingUserInfoLayout.root.visibility = View.GONE
        }
        binding.checkoutCollectingUserInfoLayout.root.setOnClickListener(this)
    }

    /**
     * collection label message for FBH item into cart
     */
    private fun collectionMessageForFBHItem() {
        val deliveryInDays = storePickupInfoResponse?.openDayDeliverySlots?.get(0)?.deliveryInDays
        binding.layoutCollectionInstructions?.checkoutCollectionDetailsInfoLayout?.root?.visibility = View.VISIBLE

        val collectionDetailsTextString  = context?.resources?.getString(R.string.collection_details_text).toString() +
                " " + deliveryInDays?.lowercase() + ". " + (context?.resources?.getString(R.string.notify_text_label))
        val spannableStringBuilder = SpannableStringBuilder(collectionDetailsTextString)
        val styleSpam = StyleSpan(android.graphics.Typeface.BOLD)
        spannableStringBuilder.setSpan(styleSpam, (collectionDetailsTextString.length - 41),
                collectionDetailsTextString.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        binding.layoutCollectionInstructions.checkoutCollectionDetailsInfoLayout?.tvCollectionDetailsText?.text = spannableStringBuilder
    }
    /**
     * check if cart items have only FBH products
     */
    private fun isFBHOnly() : Boolean {
         storePickupInfoResponse?.let {
           return it.fulfillmentTypes?.join == StoreUtils.Companion.FulfillmentType.CLOTHING_ITEMS?.type
                    && it.openDayDeliverySlots?.isNullOrEmpty() == false
                    && it.fulfillmentTypes?.join != StoreUtils.Companion.FulfillmentType.FOOD_ITEMS?.type
                    && it.fulfillmentTypes?.food != StoreUtils.Companion.FulfillmentType.FOOD_ITEMS?.type
            }
            return false
        }
    /**
     * Update collection item view according to Food, FBH and mixed item with title on checkout
     * screen
     */
    private fun updateCollectionItemsForCheckout() {
        //FBH only
        if(isFBHOnly()) {
            collectionMessageForFBHItem()
            binding.apply {
                checkoutCollectingTimeDetailsLayout?.root?.visibility = View.GONE
                with(layoutCollectionInstructions) {
                    txtNeedBags?.visibility = View.GONE
                    nativeCheckoutFoodSubstitutionLayout?.root?.visibility = View.GONE
                    viewHorizontalCollectionBottomSeparator?.visibility = View.VISIBLE
                    instructionTxtShimmerFrameLayout?.visibility = View.GONE
                    switchNeedBags?.visibility = View.GONE
                    specialInstructionSwitchShimmerFrameLayout?.visibility = View.GONE
                    shoppingBagSeparator?.visibility = View.GONE
                    viewGiftHorizontalSeparator?.visibility = View.GONE
                }
            }
        } //mixed cart - FBH + Food
        else if((storePickupInfoResponse?.fulfillmentTypes?.other == StoreUtils.Companion.FulfillmentType.CLOTHING_ITEMS?.type
                        && storePickupInfoResponse?.openDayDeliverySlots?.isNullOrEmpty() == false
                        && storePickupInfoResponse?.sortedFoodDeliverySlots?.isNullOrEmpty() == false
                        && storePickupInfoResponse?.fulfillmentTypes?.food == StoreUtils.Companion.FulfillmentType.FOOD_ITEMS?.type
                        && storePickupInfoResponse?.fulfillmentTypes?.join == StoreUtils.Companion.FulfillmentType.CLOTHING_ITEMS?.type)) {
            collectionMessageForFBHItem()
            binding?.apply {
                checkoutCollectingTimeDetailsLayout.tvCollectionTimeDetailsTitle?.text = bindString(R.string.mixed_cart_food_item_title)
                with(layoutCollectionInstructions) {
                    checkoutCollectionDetailsInfoLayout?.tvCollectionDetailsTitle?.text = bindString(R.string.mixed_cart_other_item_title)

                    specialInstructionSwitchShimmerFrameLayout?.visibility = View.GONE
                    viewGiftHorizontalSeparator?.visibility = View.GONE
                    instructionTxtShimmerFrameLayout?.visibility = View.GONE
                    shoppingBagSeparator?.visibility = View.GONE
                    switchNeedBags?.visibility = View.GONE
                    txtNeedBags?.visibility = View.GONE

                    checkoutCNCShoppingBagsInfoLayout?.root?.visibility = View.GONE

                    viewHorizontalCollectionSeparator?.visibility = View.GONE
                    shoppingBagsSeparator?.visibility = View.GONE
                    viewHorizontalCollectionBottomSeparator?.visibility = View.GONE
                }
            }
            storePickupInfoResponse?.sortedFoodDeliverySlots?.apply {
                val firstAvailableDateSlot = getFirstAvailableFoodSlot(this)
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

        }   //Food only
        else if(storePickupInfoResponse?.sortedJoinDeliverySlots?.isNullOrEmpty() == false
                && storePickupInfoResponse?.fulfillmentTypes?.other != StoreUtils.Companion.FulfillmentType.CLOTHING_ITEMS?.type
                && storePickupInfoResponse?.fulfillmentTypes?.join == StoreUtils.Companion.FulfillmentType.FOOD_ITEMS?.type) {

            binding.apply {
                with(layoutCollectionInstructions) {
                    txtNeedBags?.visibility = View.GONE
                    instructionTxtShimmerFrameLayout?.visibility = View.GONE
                    switchNeedBags?.visibility = View.GONE
                    specialInstructionSwitchShimmerFrameLayout?.visibility = View.GONE
                    shoppingBagSeparator?.visibility = View.GONE
                    viewGiftHorizontalSeparator?.visibility = View.GONE

                    checkoutCNCShoppingBagsInfoLayout?.root?.visibility = View.GONE

                    viewHorizontalCollectionSeparator?.visibility = View.GONE
                    viewHorizontalCollectionBottomSeparator?.visibility = View.GONE
                }
            }
            storePickupInfoResponse?.sortedJoinDeliverySlots?.apply {
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
        }
    }
    fun initializeDeliveryInstructions() {
        with(binding.layoutCollectionInstructions) {
            edtTxtSpecialDeliveryInstruction?.addTextChangedListener(deliveryInstructionsTextWatcher)
            edtTxtGiftInstructions?.addTextChangedListener(deliveryInstructionsTextWatcher)
            edtTxtInputLayoutSpecialDeliveryInstruction?.visibility = View.GONE
            edtTxtInputLayoutSpecialDeliveryInstruction?.isCounterEnabled = false
            edtTxtInputLayoutGiftInstructions?.visibility = View.GONE
            edtTxtInputLayoutGiftInstructions?.isCounterEnabled = false
            deliveryInstructionClickListener(switchSpecialDeliveryInstruction.isChecked)
            giftClickListener(switchGiftInstructions.isChecked)

            switchSpecialDeliveryInstruction?.setOnCheckedChangeListener { _, isChecked ->
                if (binding.loadingBar.visibility == View.VISIBLE) {
                    return@setOnCheckedChangeListener
                }
                deliveryInstructionClickListener(isChecked)
            }

            switchGiftInstructions?.setOnCheckedChangeListener { _, isChecked ->
                if (binding.loadingBar?.visibility == View.VISIBLE) {
                    return@setOnCheckedChangeListener
                }
                giftClickListener(isChecked)
            }
            if (AppConfigSingleton.nativeCheckout?.currentShoppingBag?.isEnabled == true) {
                switchNeedBags?.visibility = View.VISIBLE
                txtNeedBags?.text =
                    AppConfigSingleton.nativeCheckout?.currentShoppingBag?.title.plus(
                        AppConfigSingleton.nativeCheckout?.currentShoppingBag?.description
                    )
                txtNeedBags?.visibility = View.VISIBLE
                viewHorizontalSeparator?.visibility = View.GONE
                newShoppingBagsLayout?.root?.visibility = View.GONE
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
                newShoppingBagsLayout?.root?.visibility = View.VISIBLE
                addShoppingBagsRadioButtons()
            }
        }
    }

    private fun deliveryInstructionClickListener(isChecked: Boolean) {
        if (isChecked)
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.CHECKOUT_SPECIAL_COLLECTION_INSTRUCTION,
                activity
            )
        with(binding.layoutCollectionInstructions) {
            edtTxtInputLayoutSpecialDeliveryInstruction?.visibility =
                if (isChecked) View.VISIBLE else View.GONE
            edtTxtInputLayoutSpecialDeliveryInstruction?.isCounterEnabled = isChecked
            edtTxtSpecialDeliveryInstruction?.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun giftClickListener(isChecked: Boolean) {
        if (isChecked)
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.CHECKOUT_IS_THIS_GIFT,
                activity
            )
        with(binding.layoutCollectionInstructions) {
            edtTxtInputLayoutGiftInstructions?.visibility =
                if (isChecked) View.VISIBLE else View.GONE
            edtTxtInputLayoutGiftInstructions?.isCounterEnabled = isChecked
            edtTxtGiftInstructions?.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun addShoppingBagsRadioButtons() {

        val newShoppingBags = AppConfigSingleton.nativeCheckout?.newShoppingBag
        binding.layoutCollectionInstructions.newShoppingBagsLayout.newShoppingBagsTitle.text =
            newShoppingBags?.title

        binding.layoutCollectionInstructions.newShoppingBagsLayout.txtNewShoppingBagsDesc?.text =
            newShoppingBags?.description

        val shoppingBagsAdapter =
            ShoppingBagsRadioGroupAdapter(newShoppingBags?.options, this, selectedShoppingBagType)
        binding.layoutCollectionInstructions.newShoppingBagsLayout.shoppingBagsRecyclerView.apply {
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
        binding.nativeCheckoutFoodSubstitutionLayout.radioGroupFoodSubstitution?.setOnCheckedChangeListener { _, checkedId ->
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
            binding.layoutCheckoutDeliveryOrderSummary.txtOrderSummaryYourCartValue?.text =
                CurrencyFormatter.formatAmountToRandAndCentWithSpace(it.basketTotal)
            it.discountDetails?.let { discountDetails ->
                with(binding.layoutCheckoutDeliveryOrderSummary) {
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
                    if (KotlinUtils.getPreferredDeliveryType() == Delivery.CNC)
                    {
                        txtOrderSummaryDeliveryFee?.text=context?.getString(R.string.collection_fee)
                    }

                }
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
                        false,
                        false,
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
                            LocalBroadcastManager.getInstance(this)
                                .sendBroadcast(Intent(CheckOutFragment.TAG_CART_BROADCAST_RECEIVER))
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
        binding.layoutCollectionInstructions.root.parent.requestChildFocus(
            binding.layoutCollectionInstructions.root,
            binding.layoutCollectionInstructions.root
        )
        return liquorOrder == true && !binding.ageConfirmationLayoutCollection.radioBtnAgeConfirmation.isChecked
    }

    private fun onCheckoutPaymentClick() {

        if (isItemLimitExceeded && !isFBHOnly()) {
            showMaxItemView()
            return
        }

        if (isRequiredFieldsMissing() || isGiftMessage()) {
            return
        }
        if (isAgeConfirmationLiquorCompliance()) {
            return
        }

        val body = getShipmentDetailsBody()
        if (TextUtils.isEmpty(body.oddDeliverySlotId) && TextUtils.isEmpty(body.foodDeliverySlotId)
            && TextUtils.isEmpty(body.otherDeliverySlotId) && !isFBHOnly()
        ) {
            return
        }
        binding.loadingBar?.visibility = View.VISIBLE
        setScreenClickEvents(false)
        checkoutAddAddressNewUserViewModel.getShippingDetails(body)
            .observe(viewLifecycleOwner) { response ->
                binding.loadingBar.visibility = View.GONE
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
        if (liquorOrder == true && !binding.ageConfirmationLayoutCollection.radioBtnAgeConfirmation.isChecked) {
            binding.ageConfirmationLayoutCollection?.root?.visibility = View.VISIBLE
            binding.ageConfirmationLayoutCollection.liquorComplianceBannerSeparator?.visibility =
                View.VISIBLE
            binding.ageConfirmationLayoutCollection.liquorComplianceBannerLayout?.root?.visibility =
                View.VISIBLE

            Utils.fadeInFadeOutAnimation(binding.txtContinueToPaymentCollection, false)
        } else {
            Utils.fadeInFadeOutAnimation(binding.txtContinueToPaymentCollection, true)
        }
    }

    private fun showMaxItemView() {
        KotlinUtils.showGeneralInfoDialog(
            requireActivity().supportFragmentManager,
            getString(R.string.unable_process_checkout_desc),
            getString(R.string.unable_process_checkout_title),
            getString(R.string.got_it),
            R.drawable.payment_overdue_icon,
            isFromCheckoutScreen = true
        )
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
        binding.nativeCheckoutFoodSubstitutionLayout.radioGroupFoodSubstitution?.isClickable =
            isClickable
        binding.checkoutCollectingFromLayout?.root?.isClickable = isClickable
        with(binding.layoutCollectionInstructions) {
            switchNeedBags?.isClickable = isClickable
            switchGiftInstructions?.isClickable = isClickable
            switchSpecialDeliveryInstruction?.isClickable = isClickable
        }
    }

    private fun getShipmentDetailsBody() = ShippingDetailsBody().apply {
        requestFrom = "express"
        joinBasket = true
        if (liquorOrder == true) {
            ageConsentConfirmed = true
        }
        foodShipOnDate = if(selectedTimeSlot?.stringShipOnDate != null) selectedTimeSlot?.stringShipOnDate else ""
        otherShipOnDate = ""
        foodDeliverySlotId = if(selectedTimeSlot?.slotId !=null) selectedTimeSlot?.slotId else ""
        otherDeliverySlotId = ""
        oddDeliverySlotId = if(storePickupInfoResponse?.openDayDeliverySlots?.size!! > 0 && storePickupInfoResponse?.openDayDeliverySlots?.get(0)?.deliverySlotId != null) storePickupInfoResponse?.openDayDeliverySlots?.get(0)?.deliverySlotId else ""
        foodDeliveryStartHour = selectedTimeSlot?.intHourFrom?.toLong() ?: 0
        otherDeliveryStartHour = 0
        substituesAllowed = selectedFoodSubstitution.rgb
        plasticBags = binding.layoutCollectionInstructions.switchNeedBags?.isChecked ?: false
        shoppingBagType = selectedShoppingBagType
        giftNoteSelected =
            binding.layoutCollectionInstructions.switchGiftInstructions?.isChecked ?: false
        deliverySpecialInstructions =
            if (binding.layoutCollectionInstructions.switchSpecialDeliveryInstruction?.isChecked == true) binding.layoutCollectionInstructions.edtTxtSpecialDeliveryInstruction?.text.toString() else ""
        giftMessage =
            if (binding.layoutCollectionInstructions.switchGiftInstructions?.isChecked == true) binding.layoutCollectionInstructions.edtTxtGiftInstructions?.text.toString() else ""
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
            tokenProvider =
                if (Utils.isGooglePlayServicesAvailable()) NotificationUtils.TOKEN_PROVIDER_FIREBASE else NotificationUtils.TOKEN_PROVIDER_HMS
        }
    }

    private fun isGiftMessage(): Boolean {
        return when (binding.layoutCollectionInstructions.switchGiftInstructions?.isChecked) {
            true -> {
                if (TextUtils.isEmpty(binding.layoutCollectionInstructions.edtTxtGiftInstructions?.text?.toString())) {

                    binding.checkoutReturningCollectionScrollView?.smoothScrollTo(
                        0,
                        binding.layoutCollectionInstructions?.root?.top ?: 0
                    )
                    true
                } else false
            }
            else -> false
        }
    }

    private fun isInstructionsMissing(): Boolean {
        return when (binding.layoutCollectionInstructions.switchSpecialDeliveryInstruction?.isChecked) {
            true -> {
                if (TextUtils.isEmpty(binding.layoutCollectionInstructions.edtTxtSpecialDeliveryInstruction?.text.toString())) {
                    // scroll to instructions layout
                    binding.checkoutReturningCollectionScrollView?.smoothScrollTo(
                        0,
                        binding.layoutCollectionInstructions?.root?.top ?: 0
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
        if (!TextUtils.isEmpty(selectedTimeSlot?.slotId) || isFBHOnly()) {
            binding.checkoutCollectingTimeDetailsLayout.txtSelectCollectionTimeSlotFoodError?.visibility = View.GONE
            return false
        }
        // scroll to slot selection layout
        if(!isFBHOnly())
        binding.checkoutReturningCollectionScrollView?.smoothScrollTo(
            0,
            binding.checkoutCollectingTimeDetailsLayout?.root?.top ?: 0
        )
        binding.checkoutCollectingTimeDetailsLayout.txtSelectCollectionTimeSlotFoodError?.visibility =
            View.VISIBLE
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
    fun getFirstAvailableFoodSlot(list: List<SortedFoodDeliverySlot>): Week? {
        if (list.isNullOrEmpty()) {
            return null
        }
        list.forEach { sortedFoodDeliverySlot ->
            if (!sortedFoodDeliverySlot.week.isNullOrEmpty()) {
                sortedFoodDeliverySlot.week?.forEach { weekDay ->
                    if (!weekDay.slots.isNullOrEmpty()) {
                        weekDay.slots?.forEach { slot ->
                            if(slot.available == true) {
                                return weekDay
                            }
                        }
                    }
                }
            }
        }
        return null
    }
}