package za.co.woolworths.financial.services.android.checkout.view

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentCheckoutReturningUserDashBinding
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.JsonElement
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.checkout.service.network.Address
import za.co.woolworths.financial.services.android.checkout.service.network.ConfirmDeliveryAddressResponse
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.service.network.ShippingDetailsBody
import za.co.woolworths.financial.services.android.checkout.service.network.ShippingDetailsResponse
import za.co.woolworths.financial.services.android.checkout.service.network.Slot
import za.co.woolworths.financial.services.android.checkout.service.network.SortedJoinDeliverySlot
import za.co.woolworths.financial.services.android.checkout.service.network.Week
import za.co.woolworths.financial.services.android.checkout.utils.AddShippingInfoEventsAnalytics
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.Companion.REGEX_DELIVERY_INSTRUCTIONS
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.FoodSubstitution
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressManagementBaseFragment.Companion.CART_ITEM_LIST
import za.co.woolworths.financial.services.android.checkout.view.CollectionDatesBottomSheetDialog.Companion.ARGS_KEY_COLLECTION_DATES
import za.co.woolworths.financial.services.android.checkout.view.CollectionDatesBottomSheetDialog.Companion.ARGS_KEY_SELECTED_POSITION
import za.co.woolworths.financial.services.android.checkout.view.CustomDriverTipBottomSheetDialog.Companion.MAX_TIP_VALUE
import za.co.woolworths.financial.services.android.checkout.view.CustomDriverTipBottomSheetDialog.Companion.MIN_TIP_VALUE
import za.co.woolworths.financial.services.android.checkout.view.ErrorHandlerBottomSheetDialog.Companion.ERROR_TYPE_CONFIRM_COLLECTION_ADDRESS
import za.co.woolworths.financial.services.android.checkout.view.ErrorHandlerBottomSheetDialog.Companion.ERROR_TYPE_SHIPPING_DETAILS_COLLECTION
import za.co.woolworths.financial.services.android.checkout.view.adapter.CollectionTimeSlotsAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.ShoppingBagsRadioGroupAdapter
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyValues.Companion.SHIPPING_TIER_VALUE_DASH
import za.co.woolworths.financial.services.android.contracts.IToastInterface
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.isEnhanceSubstitutionFeatureEnable
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.response.ConfirmLocationAddress
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.models.dto.LiquorCompliance
import za.co.woolworths.financial.services.android.models.dto.OrderSummary
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.dto.app_config.native_checkout.ConfigShoppingBagsOptions
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment
import za.co.woolworths.financial.services.android.ui.views.ToastFactory.Companion.buildPushNotificationAlertToast
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.Constant
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.ImageManager
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.removeRandFromAmount
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter
import za.co.woolworths.financial.services.android.util.WFormatter.DATE_FORMAT_EEEE_COMMA_dd_MMMM
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.pushnotification.NotificationUtils
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class CheckoutDashFragment : Fragment(R.layout.fragment_checkout_returning_user_dash),
    ShoppingBagsRadioGroupAdapter.EventListner, View.OnClickListener, CollectionTimeSlotsListener,
    CustomDriverTipBottomSheetDialog.ClickListner, CompoundButton.OnCheckedChangeListener,
    IToastInterface {

    private var isItemLimitExceeded: Boolean = false
    private var isTimeSlotsNotAvailable: Boolean = false
    private lateinit var binding: FragmentCheckoutReturningUserDashBinding

    private var orderTotalValue: Double = -1.0
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
    private val checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel by activityViewModels()
    private var selectedFoodSubstitution = FoodSubstitution.SIMILAR_SUBSTITUTION
    private var shimmerComponentArray: List<Pair<ShimmerFrameLayout, View>> = ArrayList()
    private var navController: NavController? = null
    private var driverTipOptionsList: ArrayList<String>? = null
    private var selectedDriverTipValue: String = "R10"
    private var driverTipTextView: View? = null

    private var liquorImageUrl: String? = ""
    private var liquorOrder: Boolean? = false
    private var cartItemList: ArrayList<CommerceItem>? = null

    @Inject
    lateinit var addShippingInfoEventsAnalytics: AddShippingInfoEventsAnalytics

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
        private const val REQUEST_KEY_SELECTED_COLLECTION_DATE: String = "SELECTED_COLLECTION_DATE"
        private const val DEFAULT_AMOUNT: String = "0.0"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dashTimeSlotsAdapter = CollectionTimeSlotsAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCheckoutReturningUserDashBinding.bind(view)

        if (navController == null)
            navController = Navigation.findNavController(view)
        (activity as? CheckoutActivity)?.apply {
            showBackArrowWithTitle(bindString(R.string.checkout))
        }
        initializeDashingToView()
        initializeDashTimeSlots()
        loadShoppingCart()
        getLiquorComplianceDetails()
        hideGiftOption()
        hideInstructionLayout()
        initShimmerView()
        initializeDriverTipList()
        callConfirmLocationAPI()
        setFragmentResults()
        binding.txtContinueToPayment?.setOnClickListener(this)
        binding.checkoutCollectingFromLayout?.root?.setOnClickListener(this)
    }



    private fun hideInstructionLayout() {
        binding.layoutDeliveryInstructions.txtNeedBags?.visibility = GONE
        binding.layoutDeliveryInstructions.switchNeedBags?.visibility = GONE
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

    //LiquorCompliance
    private fun getLiquorComplianceDetails() {
        binding.ageConfirmationLayout.radioBtnAgeConfirmation.setOnCheckedChangeListener(this)
        CheckoutAddressManagementBaseFragment.baseFragBundle?.apply {
            if (containsKey(Constant.LIQUOR_ORDER)) {
                liquorOrder = getBoolean(Constant.LIQUOR_ORDER)
                if (liquorOrder == true && containsKey(Constant.NO_LIQUOR_IMAGE_URL)) {
                    liquorImageUrl = getString(Constant.NO_LIQUOR_IMAGE_URL)
                    binding.ageConfirmationLayout?.root?.visibility = View.VISIBLE
                    binding.ageConfirmationLayout.liquorComplianceBannerLayout?.root?.visibility =
                        View.VISIBLE
                    ImageManager.setPicture(
                        binding.ageConfirmationLayout.liquorComplianceBannerLayout.imgLiquorBanner,
                        liquorImageUrl
                    )

                    binding.ageConfirmationLayout.root.visibility = View.VISIBLE
                    binding.ageConfirmationLayout.liquorComplianceBannerSeparator.visibility =
                        View.VISIBLE
                    binding.ageConfirmationLayout.liquorComplianceBannerLayout.root.visibility =
                        View.VISIBLE

                    if (!binding.ageConfirmationLayout.radioBtnAgeConfirmation.isChecked) {
                        Utils.fadeInFadeOutAnimation(binding.txtContinueToPayment, true)
                        binding.ageConfirmationLayout.radioBtnAgeConfirmation?.isChecked = false
                    } else {
                        Utils.fadeInFadeOutAnimation(binding.txtContinueToPayment, false)
                        binding.ageConfirmationLayout.radioBtnAgeConfirmation?.isChecked = true
                    }
                }
            } else if (containsKey(CART_ITEM_LIST)) {
                cartItemList =
                    getSerializable(CART_ITEM_LIST) as ArrayList<CommerceItem>?
            } else {
                binding.ageConfirmationLayout?.root?.visibility = GONE
                binding.ageConfirmationLayout.liquorComplianceBannerLayout?.root?.visibility = GONE
            }
        }
    }

    private fun initShimmerView() {

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
                binding.nativeCheckoutFoodSubstitutionLayout.foodSubstitutionDescShimmerFrameLayout,
                binding.nativeCheckoutFoodSubstitutionLayout.txtFoodSubstitutionDesc
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
                binding.ageConfirmationLayout.ageConfirmationTitleShimmerFrameLayout,
                binding.ageConfirmationLayout.txtAgeConfirmationTitle
            ),

            Pair<ShimmerFrameLayout, View>(
                binding.ageConfirmationLayout.ageConfirmationDescShimmerFrameLayout,
                binding.ageConfirmationLayout.txtAgeConfirmationDesc
            ),

            Pair<ShimmerFrameLayout, View>(
                binding.ageConfirmationLayout.ageConfirmationDescNoteShimmerFrameLayout,
                binding.ageConfirmationLayout.txtAgeConfirmationDescNote
            ),

            Pair<ShimmerFrameLayout, View>(
                binding.ageConfirmationLayout.radioGroupAgeConfirmationShimmerFrameLayout,
                binding.ageConfirmationLayout.radioBtnAgeConfirmation
            ),

            Pair<ShimmerFrameLayout, View>(
                binding.ageConfirmationLayout.ageConfirmationTitleShimmerFrameLayout,
                binding.ageConfirmationLayout.txtAgeConfirmationTitle
            ),

            Pair<ShimmerFrameLayout, View>(
                binding.ageConfirmationLayout.ageConfirmationDescShimmerFrameLayout,
                binding.ageConfirmationLayout.txtAgeConfirmationDesc
            ),

            Pair<ShimmerFrameLayout, View>(
                binding.ageConfirmationLayout.ageConfirmationDescNoteShimmerFrameLayout,
                binding.ageConfirmationLayout.txtAgeConfirmationDescNote
            ),

            Pair<ShimmerFrameLayout, View>(
                binding.ageConfirmationLayout.radioGroupAgeConfirmationShimmerFrameLayout,
                binding.ageConfirmationLayout.radioBtnAgeConfirmation
            ),

            Pair<ShimmerFrameLayout, View>(
                binding.ageConfirmationLayout.liquorComplianceBannerShimmerFrameLayout,
                binding.ageConfirmationLayout.liquorComplianceBannerLayout.root
            ),

            Pair<ShimmerFrameLayout, View>(
                binding.layoutDeliveryInstructions.instructionTxtShimmerFrameLayout,
                binding.layoutDeliveryInstructions.txtSpecialDeliveryInstruction
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.layoutDeliveryInstructions.specialInstructionSwitchShimmerFrameLayout,
                binding.layoutDeliveryInstructions.switchSpecialDeliveryInstruction
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
                binding.continuePaymentTxtShimmerFrameLayout,
                binding.txtContinueToPayment
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.checkoutCollectingTimeDetailsLayout.collectionTimeDetailsShimmerLayout,
                binding.checkoutCollectingTimeDetailsLayout.collectionTimeDetailsConstraintLayout
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.layoutDeliveryInstructions.newShoppingBagsLayout.newShoppingBagsTitleShimmerFrameLayout,
                binding.layoutDeliveryInstructions.newShoppingBagsLayout.newShoppingBagsTitle
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.layoutDeliveryInstructions.newShoppingBagsLayout.newShoppingBagsDescShimmerFrameLayout,
                binding.layoutDeliveryInstructions.newShoppingBagsLayout.txtNewShoppingBagsDesc
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.layoutDeliveryInstructions.newShoppingBagsLayout.radioGroupShoppingBagsShimmerFrameLayout,
                binding.layoutDeliveryInstructions.newShoppingBagsLayout.radioGroupShoppingBags
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.layoutDriverTip.tipDashDriverTitleShimmerFrameLayout,
                binding.layoutDriverTip.tipDashDriverTitle
            ),
            Pair<ShimmerFrameLayout, View>(
                binding.layoutDriverTip.tipOptionScrollViewShimmerFrameLayout,
                binding.layoutDriverTip.tipOptionScrollView
            )
        )
    }

    private fun startShimmerView() {
        binding.layoutDeliveryInstructions.txtNeedBags?.visibility = GONE
        binding.layoutDeliveryInstructions.switchNeedBags?.visibility = GONE
        binding.layoutDeliveryInstructions.edtTxtSpecialDeliveryInstruction?.visibility = GONE
        binding.layoutDeliveryInstructions.edtTxtGiftInstructions?.visibility = GONE
        binding.layoutDeliveryInstructions.switchSpecialDeliveryInstruction?.isChecked = false
        binding.layoutDeliveryInstructions.switchGiftInstructions?.isChecked = false

        val shimmer = Shimmer.AlphaHighlightBuilder().build()
        shimmerComponentArray.forEach {
            it.first.setShimmer(shimmer)
            it.first.startShimmer()
            it.second.visibility = View.INVISIBLE
        }
    }

    private fun stopShimmerView() {
        shimmerComponentArray.forEach {
            it.first.stopShimmer()
            it.first.setShimmer(null)
            it.second.visibility = View.VISIBLE
        }

        binding.layoutDeliveryInstructions.txtNeedBags?.visibility = View.VISIBLE
        binding.layoutDeliveryInstructions.switchNeedBags?.visibility = View.VISIBLE
        initializeDeliveryInstructions()
        showDriverTipView()
    }

    private fun callConfirmLocationAPI() {
        startShimmerView()
        val confirmLocationAddress =
            ConfirmLocationAddress(defaultAddress?.placesId, defaultAddress?.nickname)
        val body = ConfirmLocationRequest(
            Delivery.DASH.type, confirmLocationAddress,
            KotlinUtils.getDeliveryType()?.storeId, "checkout"
        )

        isItemLimitExceeded = false
        checkoutAddAddressNewUserViewModel?.getConfirmLocationDetails(body)
            ?.observe(viewLifecycleOwner) { response ->
                stopShimmerView()
                when (response) {
                    is ConfirmDeliveryAddressResponse -> {
                        when (response.httpCode ?: AppConstant.HTTP_SESSION_TIMEOUT_400) {
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

                                if (response.orderSummary != null && (response.orderSummary?.totalItemsCount
                                                ?: 0) <= 0) {
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

                                isItemLimitExceeded =
                                    response.orderSummary?.fulfillmentDetails?.allowsCheckout == false
                                if (isItemLimitExceeded) {
                                    showMaxItemView()
                                }

                                if (response.orderSummary?.hasMinimumBasketAmount == false) {
                                    KotlinUtils.showMinCartValueError(
                                        requireActivity() as AppCompatActivity,
                                        response.orderSummary?.minimumBasketAmount
                                    )
                                }

                                initializeFoodSubstitution()
                                initializeOrderSummary(response.orderSummary)
                                response.sortedJoinDeliverySlots?.apply {
                                    val firstAvailableDateSlot = getFirstAvailableSlot(this)
                                    initializeDatesAndTimeSlots(firstAvailableDateSlot)
                                    // Set default time slot selected
                                    var selectedSlotIndex = 0
                                    firstAvailableDateSlot?.slots?.let {
                                        ArrayList(it).forEachIndexed { index, slot ->
                                            if (slot.slotId.equals(selectedTimeSlot?.slotId)) {
                                                selectedSlotIndex = index
                                            }
                                        }
                                    }
                                    dashTimeSlotsAdapter.setSelectedItem(selectedSlotIndex)
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

        if (selectedWeekSlot == null) {
            binding.checkoutCollectingTimeDetailsLayout?.root?.visibility = GONE
            showNoTimeSlotsView()
            return
        }

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
        binding.checkoutCollectingTimeDetailsLayout.chooseDateLayout?.root?.setOnClickListener(this@CheckoutDashFragment)
        binding.checkoutCollectingTimeDetailsLayout.firstAvailableDateLayout?.root?.setOnClickListener(
            this@CheckoutDashFragment
        )
    }

    private fun setSelectedDateTimeSlots(slots: List<Slot>?) {
        // No Timeslots available
        if (slots.isNullOrEmpty()) {
            return
        }
        dashTimeSlotsAdapter.setCollectionTimeSlotData(ArrayList(slots))
    }

    private fun getFirstAvailableSlot(list: List<SortedJoinDeliverySlot>): Week? {
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

        binding.checkoutCollectingTimeDetailsLayout?.tvCollectionTimeDetailsTitle?.text =
            getString(R.string.select_delivery_timeslot)
        binding.checkoutCollectingTimeDetailsLayout?.tvCollectionTimeDetailsDate?.text =
            getString(R.string.dash_delivery_date)
        binding.checkoutCollectingTimeDetailsLayout?.tvCollectionTimeDetailsTimeSlot?.text =
            getString(R.string.dash_delivery_timeslot)
        binding.checkoutCollectingTimeDetailsLayout.recyclerViewCollectionTimeSlots?.apply {
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

    private fun initializeDriverTipList() {
        //Todo This value will come from Config once it is available.
        driverTipOptionsList = ArrayList()
        driverTipOptionsList!!.add("R10")
        driverTipOptionsList!!.add("R20")
        driverTipOptionsList!!.add("R30")
        driverTipOptionsList!!.add("Own Amount")
    }

    private fun showDriverTipView() {
        if (!driverTipOptionsList.isNullOrEmpty()) {
            binding.layoutDriverTip?.root?.visibility = View.VISIBLE
            binding.layoutDriverTip.tipOptionsLayout?.removeAllViews()
            for ((index, options) in driverTipOptionsList!!.withIndex()) {
                driverTipTextView =
                    View.inflate(context, R.layout.where_are_we_delivering_items, null)
                val titleTextView: TextView? = driverTipTextView?.findViewById(R.id.titleTv)
                titleTextView?.tag = index
                titleTextView?.text = options

                if (selectedDriverTipValue.isNotEmpty()) {
                    if (selectedDriverTipValue == options) {
                        titleTextView?.background =
                            bindDrawable(R.drawable.checkout_delivering_title_round_button_pressed)
                        titleTextView?.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.white
                            )
                        )
                        binding.layoutDriverTip.tipNoteTextView?.visibility = View.VISIBLE
                    } else if (
                        removeRandFromAmount(
                            selectedDriverTipValue
                                ?: DEFAULT_AMOUNT
                        ).toDouble() != 0.0
                        && driverTipOptionsList?.contains(selectedDriverTipValue) == false
                        && index == driverTipOptionsList?.size?.minus(1)
                    ) {
                        /*this is for custom driver tip*/
                        titleTextView?.background =
                            bindDrawable(R.drawable.checkout_delivering_title_round_button_pressed)
                        titleTextView?.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.white
                            )
                        )
                        binding.layoutDriverTip.tipNoteTextView?.visibility = View.VISIBLE
                        titleTextView?.text =
                            "R${removeRandFromAmount(selectedDriverTipValue ?: DEFAULT_AMOUNT).toDouble()}"
                        val image = AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.edit_icon_white
                        )
                        titleTextView?.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            image,
                            null
                        )
                        titleTextView?.compoundDrawablePadding =
                            resources.getDimension(R.dimen.five_dp).toInt()
                    }
                }
                titleTextView?.setOnClickListener {
                    val amountString = (it as TextView).text as? String

                    val isSameSelection = resetAllDriverTip(it.tag as? Int ?: 0)

                    if (it.tag == driverTipOptionsList?.lastIndex) {
                        val tipValue =
                            if (titleTextView.text.toString() == driverTipOptionsList?.lastOrNull())
                                getString(R.string.empty)
                            else removeRandFromAmount(
                                titleTextView.text.toString().trim()
                            )
                        val customDriverTipDialog = CustomDriverTipBottomSheetDialog.newInstance(
                            requireContext().getString(R.string.tip_your_dash_driver),
                            requireContext().getString(
                                R.string.enter_your_own_amount,
                                AppConfigSingleton.dashConfig?.driverTip?.minAmount?.toInt()
                                    ?: MIN_TIP_VALUE.toInt(),
                                AppConfigSingleton.dashConfig?.driverTip?.maxAmount?.toInt()
                                    ?: MAX_TIP_VALUE.toInt()
                            ), tipValue, this
                        )
                        customDriverTipDialog.show(
                            requireActivity().supportFragmentManager,
                            CustomDriverTipBottomSheetDialog::class.java.simpleName
                        )
                    } else if (!isSameSelection) {

                        selectedDriverTipValue = amountString ?: "R10"

                        // Change background of selected Tip as it's not unselection.
                        it.background =
                            bindDrawable(R.drawable.checkout_delivering_title_round_button_pressed)
                        it.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.white
                            )
                        )
                        binding.layoutDriverTip.tipNoteTextView?.visibility = View.VISIBLE
                    } else {
                        selectedDriverTipValue = DEFAULT_AMOUNT

                        // Change background of selected Tip to unselect.
                        it.background =
                            bindDrawable(R.drawable.checkout_delivering_title_round_button)
                        it.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.checkout_delivering_title
                            )
                        )
                        binding.layoutDriverTip.tipNoteTextView?.visibility = View.VISIBLE
                    }
                }
                binding.layoutDriverTip.tipOptionsLayout?.addView(driverTipTextView)
            }
        }
    }

    private fun resetAllDriverTip(selectedTag: Int): Boolean {
        //change background of all unselected Tip
        var sameSelection = false
        for ((index) in driverTipOptionsList!!.withIndex()) {
            val titleTextView: TextView? = view?.findViewWithTag(index)
            if (titleTextView?.textColors?.defaultColor?.equals(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                ) == true && titleTextView.tag.equals(
                    selectedTag
                )
            ) {
                sameSelection = true
            }
            if (index == driverTipOptionsList?.size?.minus(1) ?: null) {
                titleTextView?.text = driverTipOptionsList?.lastOrNull()
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
        binding.checkoutCollectingFromLayout.tvNativeCheckoutDeliveringTitle?.text =
            getString(R.string.dashing_to)
        binding.checkoutCollectingTimeDetailsLayout.chooseDateLayout?.root?.visibility = GONE
        if (arguments == null) {
            binding.checkoutCollectingFromLayout.root.visibility = GONE
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
                binding.checkoutCollectingFromLayout?.root?.visibility = GONE
                return
            }
            savedAddress.let { savedAddresses ->

                val deliveringToAddress = SpannableStringBuilder()
                // default address nickname
                val defaultAddressNickname =
                    SpannableString(
                        savedAddresses.defaultAddressNickname + "  " + context.getString(
                            R.string.bullet
                        ) + "  "
                    )
                val typeface = ResourcesCompat.getFont(context, R.font.opensans_semi_bold)
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

                run list@{
                    savedAddresses.addresses?.forEach { address ->
                        if (savedAddresses.defaultAddressNickname.equals(address.nickname)) {
                            this.defaultAddress = address
                            suburbId = address.suburbId ?: ""
                            placesId = address?.placesId
                            storeId = address?.storeId
                            nickName = address?.nickname
                            val addressName = SpannableString(address.address1)
                            val typeface1 =
                                ResourcesCompat.getFont(context, R.font.opensans_regular)
                            addressName.setSpan(
                                StyleSpan(typeface1!!.style),
                                0, addressName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            deliveringToAddress.append(addressName)
                            return@list
                        }
                    }
                }

                if (savedAddresses.defaultAddressNickname.isNullOrEmpty()) {
                    binding.checkoutCollectingFromLayout?.root?.visibility = GONE
                }
                binding.checkoutCollectingFromLayout.tvNativeCheckoutDeliveringValue?.text =
                    deliveringToAddress
                binding.checkoutCollectingFromLayout?.root?.setOnClickListener(this)
            }
        }
    }


    private fun initializeDeliveryInstructions() {
        binding.layoutDeliveryInstructions.edtTxtSpecialDeliveryInstruction?.addTextChangedListener(
            deliveryInstructionsTextWatcher
        )
        //Special delivery instructions by default enabled for dash delivery and for other delivery
        // type(standard and click and collect)  by default it is disabled
        binding.layoutDeliveryInstructions.edtTxtInputLayoutSpecialDeliveryInstruction?.visibility = VISIBLE
        binding.layoutDeliveryInstructions.edtTxtSpecialDeliveryInstruction?.visibility = VISIBLE
        binding.layoutDeliveryInstructions.switchSpecialDeliveryInstruction?.isChecked = true
        binding.layoutDeliveryInstructions.edtTxtInputLayoutSpecialDeliveryInstruction?.isCounterEnabled =
            true
        deliveryInstructionClickListener(binding.layoutDeliveryInstructions.switchSpecialDeliveryInstruction.isChecked)

        binding.layoutDeliveryInstructions.switchSpecialDeliveryInstruction?.setOnCheckedChangeListener { _, isChecked ->
            if (binding.loadingBar.visibility == View.VISIBLE) {
                return@setOnCheckedChangeListener
            }
            deliveryInstructionClickListener(isChecked)
        }
        if (AppConfigSingleton.nativeCheckout?.currentShoppingBag?.isEnabled == true) {
            binding.layoutDeliveryInstructions.switchNeedBags?.visibility = View.VISIBLE
            binding.layoutDeliveryInstructions.txtNeedBags?.visibility = View.VISIBLE
            binding.layoutDeliveryInstructions.txtNeedBags?.text =
                AppConfigSingleton.nativeCheckout?.currentShoppingBag?.title.plus(
                    AppConfigSingleton.nativeCheckout?.currentShoppingBag?.description
                )
            binding.layoutDeliveryInstructions.viewHorizontalSeparator?.visibility = GONE
            binding.layoutDeliveryInstructions.newShoppingBagsLayout?.root?.visibility = GONE
            binding.layoutDeliveryInstructions.switchNeedBags?.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    Utils.triggerFireBaseEvents(
                            FirebaseManagerAnalyticsProperties.CHECKOUT,
                            hashMapOf(
                                    FirebaseManagerAnalyticsProperties.PropertyNames.STEP to
                                            FirebaseManagerAnalyticsProperties.PropertyValues.DELIVERY_PAGE,
                                    FirebaseManagerAnalyticsProperties.PropertyNames.DELIVERY_TYPE to
                                            KotlinUtils.getPreferredDeliveryType().toString(),
                                    FirebaseManagerAnalyticsProperties.PropertyNames.TOGGLE_SELECTED to
                                            FirebaseManagerAnalyticsProperties.PropertyValues.NEED_SHOPPING_BAG
                            ),
                            activity
                    )
                }
            }
        } else if (AppConfigSingleton.nativeCheckout?.newShoppingBag?.isEnabled == true) {
            binding.layoutDeliveryInstructions.switchNeedBags?.visibility = GONE
            binding.layoutDeliveryInstructions.viewHorizontalSeparator?.visibility = View.VISIBLE
            binding.layoutDeliveryInstructions.txtNeedBags?.visibility = GONE
            binding.layoutDeliveryInstructions.newShoppingBagsLayout?.root?.visibility =
                View.VISIBLE
            addShoppingBagsRadioButtons()
        }
    }

    private fun deliveryInstructionClickListener(isChecked: Boolean) {
        if (isChecked)
            Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.CHECKOUT,
                    hashMapOf(
                            FirebaseManagerAnalyticsProperties.PropertyNames.STEP to
                                    FirebaseManagerAnalyticsProperties.PropertyValues.DELIVERY_PAGE,
                            FirebaseManagerAnalyticsProperties.PropertyNames.DELIVERY_TYPE to
                                    KotlinUtils.getPreferredDeliveryType().toString(),
                            FirebaseManagerAnalyticsProperties.PropertyNames.TOGGLE_SELECTED to
                                    FirebaseManagerAnalyticsProperties.PropertyValues.SPECIAL_DELIVERY_INSTRUCTION
                    ),
                    activity
            )
        binding.layoutDeliveryInstructions.edtTxtInputLayoutSpecialDeliveryInstruction?.visibility =
            if (isChecked) View.VISIBLE else GONE
        binding.layoutDeliveryInstructions.edtTxtInputLayoutSpecialDeliveryInstruction?.isCounterEnabled =
            isChecked
        binding.layoutDeliveryInstructions.edtTxtSpecialDeliveryInstruction?.visibility =
            if (isChecked) View.VISIBLE else GONE
    }

    private fun addShoppingBagsRadioButtons() {
        binding.layoutDeliveryInstructions.newShoppingBagsLayout.txtNewShoppingBagsSubDesc?.visibility =
            View.VISIBLE
        val newShoppingBags = AppConfigSingleton.nativeCheckout?.newShoppingBag
        binding.layoutDeliveryInstructions.newShoppingBagsLayout.txtNewShoppingBagsDesc?.text =
            newShoppingBags?.title
        binding.layoutDeliveryInstructions.newShoppingBagsLayout.txtNewShoppingBagsSubDesc?.text =
            newShoppingBags?.description

        val shoppingBagsAdapter =
            ShoppingBagsRadioGroupAdapter(newShoppingBags?.options, this, selectedShoppingBagType)
        binding.layoutDeliveryInstructions.newShoppingBagsLayout.shoppingBagsRecyclerView.apply {
            layoutManager = activity?.let { LinearLayoutManager(it) }
            shoppingBagsAdapter.let { adapter = it }
        }
    }

    /**
     * Initializes food substitution view and Set by default selection to [FoodSubstitution.SIMILAR_SUBSTITUTION]
     *
     * @see [FoodSubstitution]
     */
    private fun initializeFoodSubstitution() {
        /* if feature flag is disabled then only show food substitution layout */
        if (isEnhanceSubstitutionFeatureEnable() == true) {
            binding.nativeCheckoutFoodSubstitutionLayout.apply {
                this.radioGroupFoodSubstitution.visibility = GONE
                this.txtFoodSubstitutionDesc.text = getString(R.string.food_substitution_message)
            }
        } else {
            binding.nativeCheckoutFoodSubstitutionLayout.apply {
                this.radioGroupFoodSubstitution.visibility = VISIBLE
                this.txtFoodSubstitutionDesc.text = getString(R.string.native_checkout_delivery_food_substitution_desc)
            }
        }
        binding.nativeCheckoutFoodSubstitutionLayout.radioBtnPhoneConfirmation?.text =
            requireContext().getString(R.string.native_checkout_delivery_food_substitution_chat)

        selectedFoodSubstitution = FoodSubstitution.SIMILAR_SUBSTITUTION
        binding.nativeCheckoutFoodSubstitutionLayout.radioGroupFoodSubstitution?.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioBtnPhoneConfirmation -> {
                    Utils.triggerFireBaseEvents(
                            FirebaseManagerAnalyticsProperties.CHECKOUT,
                            hashMapOf(
                                    FirebaseManagerAnalyticsProperties.PropertyNames.STEP to
                                            FirebaseManagerAnalyticsProperties.PropertyValues.DELIVERY_PAGE,
                                    FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                            FirebaseManagerAnalyticsProperties.PropertyValues.FOOD_SUBSTITUTION,
                                    FirebaseManagerAnalyticsProperties.PropertyNames.OPTION to
                                            FirebaseManagerAnalyticsProperties.PropertyValues.CHAT_WITH_SHOPPER,
                                    FirebaseManagerAnalyticsProperties.PropertyNames.DELIVERY_TYPE to
                                            KotlinUtils.getPreferredDeliveryType().toString()
                            ),
                            activity
                    )

                    selectedFoodSubstitution = FoodSubstitution.CHAT
                }

                R.id.radioBtnSimilarSubst -> {
                    selectedFoodSubstitution = FoodSubstitution.SIMILAR_SUBSTITUTION
                    Utils.triggerFireBaseEvents(
                            FirebaseManagerAnalyticsProperties.CHECKOUT,
                            hashMapOf(
                                    FirebaseManagerAnalyticsProperties.PropertyNames.STEP to
                                            FirebaseManagerAnalyticsProperties.PropertyValues.DELIVERY_PAGE,
                                    FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                            FirebaseManagerAnalyticsProperties.PropertyValues.FOOD_SUBSTITUTION,
                                    FirebaseManagerAnalyticsProperties.PropertyNames.OPTION to
                                            FirebaseManagerAnalyticsProperties.PropertyValues.SUBSTITUTE,
                                    FirebaseManagerAnalyticsProperties.PropertyNames.DELIVERY_TYPE to
                                            KotlinUtils.getPreferredDeliveryType().toString()
                            ),
                            activity
                    )
                }

                R.id.radioBtnNoThanks -> {
                    Utils.triggerFireBaseEvents(
                            FirebaseManagerAnalyticsProperties.CHECKOUT,
                            hashMapOf(
                                    FirebaseManagerAnalyticsProperties.PropertyNames.STEP to
                                            FirebaseManagerAnalyticsProperties.PropertyValues.DELIVERY_PAGE,
                                    FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                            FirebaseManagerAnalyticsProperties.PropertyValues.FOOD_SUBSTITUTION,
                                    FirebaseManagerAnalyticsProperties.PropertyNames.OPTION to
                                            FirebaseManagerAnalyticsProperties.PropertyValues.NO_THANKS,
                                    FirebaseManagerAnalyticsProperties.PropertyNames.DELIVERY_TYPE to
                                            KotlinUtils.getPreferredDeliveryType().toString()
                            ),
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
        orderSummary?.let {
            binding.layoutCheckoutDeliveryOrderSummary.txtOrderSummaryYourCartValue?.text =
                CurrencyFormatter.formatAmountToRandAndCentWithSpace(it.basketTotal)
            it.discountDetails?.let { discountDetails ->
                binding.layoutCheckoutDeliveryOrderSummary.groupOrderSummaryDiscount?.visibility =
                    if (discountDetails.otherDiscount == 0.0) GONE else View.VISIBLE
                binding.layoutCheckoutDeliveryOrderSummary.groupPromoCodeDiscount?.visibility =
                    if (discountDetails.promoCodeDiscount == 0.0) GONE else View.VISIBLE
                binding.layoutCheckoutDeliveryOrderSummary.groupWRewardsDiscount?.visibility =
                    if (discountDetails.voucherDiscount == 0.0) GONE else View.VISIBLE
                binding.layoutCheckoutDeliveryOrderSummary.groupCompanyDiscount?.visibility =
                    if (discountDetails.companyDiscount == 0.0) GONE else View.VISIBLE
                binding.layoutCheckoutDeliveryOrderSummary.groupTotalDiscount?.visibility =
                    if (discountDetails.totalDiscount == 0.0) GONE else View.VISIBLE

                binding.layoutCheckoutDeliveryOrderSummary.txtOrderSummaryDiscountValue?.text =
                    "-" + CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.otherDiscount)
                binding.layoutCheckoutDeliveryOrderSummary.txtOrderSummaryTotalDiscountValue?.text =
                    "-" + CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.totalDiscount)
                binding.layoutCheckoutDeliveryOrderSummary.txtOrderSummaryWRewardsVouchersValue?.text =
                    "-" + CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.voucherDiscount)
                binding.layoutCheckoutDeliveryOrderSummary.txtOrderSummaryCompanyDiscountValue?.text =
                    "-" + CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.companyDiscount)
                binding.layoutCheckoutDeliveryOrderSummary.txtOrderSummaryPromoCodeDiscountValue?.text =
                    "-" + CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.promoCodeDiscount)

                binding.layoutCheckoutDeliveryOrderSummary.txtOrderTotalValue?.text =
                    CurrencyFormatter.formatAmountToRandAndCentWithSpace(it.total)
                orderTotalValue = it.total
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
                    isFromDashTab = false,
                    isComingFromCheckout = true,
                    isMixedBasket = false,
                    isFBHOnly = false,
                    isComingFromSlotSelection = true,
                    savedAddressResponse = savedAddress,
                    defaultAddress = defaultAddress,
                    whoISCollecting = "",
                    liquorCompliance = liquorOrder?.let { liquorOrder ->
                        liquorImageUrl?.let { liquorImageUrl ->
                            LiquorCompliance(liquorOrder, liquorImageUrl)
                        }
                    }
                )
            }

            R.id.chooseDateLayout -> {
                onChooseDateClicked()
            }

            R.id.txtContinueToPayment -> {
                onCheckoutPaymentClick()
                cartItemList?.let {
                    addShippingInfoEventsAnalytics.sendEventData(
                        it,
                        SHIPPING_TIER_VALUE_DASH, orderTotalValue
                    )
                }
            }
        }
    }

    private fun onChooseDateClicked() {
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

    private fun navigateToCollectionDateDialog(weekDaysList: ArrayList<Week>) {
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

        Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.CHECKOUT,
                hashMapOf(
                        FirebaseManagerAnalyticsProperties.PropertyNames.STEP to
                                FirebaseManagerAnalyticsProperties.PropertyValues.DELIVERY_PAGE,
                        FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                FirebaseManagerAnalyticsProperties.PropertyValues.SELECT_TIMESLOT,
                        FirebaseManagerAnalyticsProperties.PropertyNames.DELIVERY_TYPE to
                                KotlinUtils.getPreferredDeliveryType().toString(),
                        FirebaseManagerAnalyticsProperties.PropertyNames.TIME_SELECTED to
                                selectedTimeSlot?.stringShipOnDate + " " + selectedTimeSlot?.hourSlot
                ),
                activity
        )
    }

    private fun onCheckoutPaymentClick() {
        if (isItemLimitExceeded) {
            showMaxItemView()
            return
        }

        if (isTimeSlotsNotAvailable) {
            showNoTimeSlotsView()
            return
        }

        if (isRequiredFieldsMissing() || isAgeConfirmationLiquorCompliance()) {
            return
        }
        val body = getShipmentDetailsBody()
        if (TextUtils.isEmpty(body.oddDeliverySlotId) && TextUtils.isEmpty(body.foodDeliverySlotId)
            && TextUtils.isEmpty(body.otherDeliverySlotId)
        ) {
            return
        }
        setEventForDriverTip()
        binding.loadingBar?.visibility = View.VISIBLE
        setScreenClickEvents(false)
        checkoutAddAddressNewUserViewModel.getShippingDetails(body)
            .observe(viewLifecycleOwner) { response ->
                binding.loadingBar.visibility = GONE
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
        if (liquorOrder == true && !binding.ageConfirmationLayout.radioBtnAgeConfirmation.isChecked) {
            binding.ageConfirmationLayout.root.visibility = View.VISIBLE
            binding.ageConfirmationLayout.liquorComplianceBannerSeparator.visibility = View.VISIBLE
            binding.ageConfirmationLayout.liquorComplianceBannerLayout.root.visibility =
                View.VISIBLE

            Utils.fadeInFadeOutAnimation(binding.txtContinueToPayment, false)
        } else {
            Utils.fadeInFadeOutAnimation(binding.txtContinueToPayment, true)
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

    private fun showNoTimeSlotsView() {
        isTimeSlotsNotAvailable = true
        binding.txtContinueToPayment?.background = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.grey_background_with_corner_6
        )
        KotlinUtils.showGeneralInfoDialog(
            requireActivity().supportFragmentManager,
            getString(R.string.timeslot_desc),
            getString(R.string.timeslot_title),
            getString(R.string.got_it),
            R.drawable.icon_dash_delivery_scooter,
            false
        )
    }

    private fun setEventForDriverTip() {
        if (orderTotalValue == -1.0) {
            return
        }

        val driverTipItemParams = Bundle()
        driverTipItemParams.putString(
            FirebaseAnalytics.Param.CURRENCY,
            FirebaseManagerAnalyticsProperties.PropertyValues.CURRENCY_VALUE
        )

        driverTipItemParams.putDouble(FirebaseAnalytics.Param.VALUE, orderTotalValue)

        driverTipItemParams.putString(
            FirebaseManagerAnalyticsProperties.PropertyNames.DASH_TIP,
            removeRandFromAmount(selectedDriverTipValue)
        )

        AnalyticsManager.logEvent(
            FirebaseManagerAnalyticsProperties.DASH_DRIVER_TIP,
            driverTipItemParams
        )
    }

    private fun presentErrorDialog(title: String, subTitle: String, errorType: Int) {
        val bundle = Bundle()
        bundle.apply {
            putString(ErrorHandlerBottomSheetDialog.ERROR_TITLE, title)
            putString(ErrorHandlerBottomSheetDialog.ERROR_DESCRIPTION, subTitle)
            putInt(ErrorHandlerBottomSheetDialog.ERROR_TYPE, errorType)
        }
        if (navController?.currentDestination?.id == R.id.checkoutDashFragment) {
            view?.findNavController()?.navigate(
                R.id.action_checkoutDashFragment_to_errorHandlerBottomSheetDialog,
                bundle
            )
        }
    }

    private fun setScreenClickEvents(isClickable: Boolean) {
        binding.nativeCheckoutFoodSubstitutionLayout.radioGroupFoodSubstitution?.isClickable =
            isClickable
        binding.checkoutCollectingFromLayout?.root?.isClickable = isClickable
        binding.layoutDeliveryInstructions.switchNeedBags?.isClickable = isClickable
        binding.layoutDeliveryInstructions.switchSpecialDeliveryInstruction?.isClickable =
            isClickable
    }

    private fun getShipmentDetailsBody() = ShippingDetailsBody().apply {
        requestFrom = "express"
        shipToAddressName = savedAddress?.defaultAddressNickname
        joinBasket = true
        foodShipOnDate = selectedTimeSlot?.stringShipOnDate
        otherShipOnDate = ""
        foodDeliverySlotId = selectedTimeSlot?.slotId
        otherDeliverySlotId = ""
        oddDeliverySlotId = ""
        foodDeliveryStartHour = selectedTimeSlot?.intHourFrom?.toLong() ?: 0
        otherDeliveryStartHour = 0
        if (isEnhanceSubstitutionFeatureEnable() == false) {
          substituesAllowed = selectedFoodSubstitution.rgb
        } else {
            substituesAllowed = FoodSubstitution.NO_THANKS.rgb
        }
        plasticBags = binding.layoutDeliveryInstructions.switchNeedBags?.isChecked ?: false
        shoppingBagType = selectedShoppingBagType
        deliverySpecialInstructions =
            if (binding.layoutDeliveryInstructions.switchSpecialDeliveryInstruction?.isChecked == true) binding.layoutDeliveryInstructions.edtTxtSpecialDeliveryInstruction?.text.toString() else ""
        giftMessage = ""
        suburbId = ""
        storeId = Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.storeId
        deliveryType = Delivery.DASH.type
        address =
            ConfirmLocationAddress(Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId)
        driverTip = removeRandFromAmount(selectedDriverTipValue ?: "R10.0").toDouble()
        if (liquorOrder == true) {
            ageConsentConfirmed = true
        }
        KotlinUtils.getUniqueDeviceID {
            pushNotificationToken = Utils.getToken()
            appInstanceId = it
            tokenProvider =
                if (Utils.isGooglePlayServicesAvailable()) NotificationUtils.TOKEN_PROVIDER_FIREBASE else NotificationUtils.TOKEN_PROVIDER_HMS
        }
    }

    private fun hideGiftOption() {
        binding.layoutDeliveryInstructions.viewGiftHorizontalSeparator?.visibility = GONE
        binding.layoutDeliveryInstructions.giftInstructionTxtShimmerFrameLayout.visibility = GONE
        binding.layoutDeliveryInstructions.giftInstructionSwitchShimmerFrameLayout.visibility = GONE
        binding.layoutDeliveryInstructions.edtTxtInputLayoutGiftInstructions.visibility = GONE
    }

    private fun isAgeConfirmationLiquorCompliance(): Boolean {
        binding.ageConfirmationLayout.txtAgeConfirmationTitle.parent.requestChildFocus(
            binding.ageConfirmationLayout.txtAgeConfirmationTitle,
            binding.ageConfirmationLayout.txtAgeConfirmationTitle
        )
        binding.ageConfirmationLayout.radioBtnAgeConfirmation.parent.requestChildFocus(
            binding.ageConfirmationLayout.radioBtnAgeConfirmation,
            binding.ageConfirmationLayout.radioBtnAgeConfirmation
        )
        return liquorOrder == true &&
                !binding.ageConfirmationLayout.radioBtnAgeConfirmation.isChecked &&
                binding.ageConfirmationLayout?.root?.visibility == View.VISIBLE
    }

    private fun isRequiredFieldsMissing(): Boolean {
        if (!TextUtils.isEmpty(selectedTimeSlot?.slotId)) {
            binding.checkoutCollectingTimeDetailsLayout.txtSelectCollectionTimeSlotFoodError?.visibility =
                GONE
            return false
        }
        // scroll to slot selection layout
        binding.deliverySummaryScrollView?.smoothScrollTo(
            0,
            binding.checkoutCollectingTimeDetailsLayout?.root?.top ?: 0
        )
        binding.checkoutCollectingTimeDetailsLayout.txtSelectCollectionTimeSlotFoodError?.visibility =
            View.VISIBLE
        return true
    }

    private fun navigateToPaymentWebpage(webTokens: ShippingDetailsResponse) {
        view?.findNavController()?.navigate(
            R.id.action_checkoutDashFragment_to_checkoutPaymentWebFragment,

            bundleOf(
                CheckoutPaymentWebFragment.KEY_ARGS_WEB_TOKEN to webTokens,
                CART_ITEM_LIST to cartItemList
            )
        )
    }

    private fun areNotificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!manager.areNotificationsEnabled()) {
                return false
            }
            val channels = manager.notificationChannels
            for (channel in channels) {
                if (channel.importance == NotificationManager.IMPORTANCE_NONE) {
                    return false
                }
            }
            true
        } else {
            if (requireContext() != null)
                NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
            else false
        }
    }

    private fun openAppNotificationSettings(context: Context) {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra("app_package", context.packageName)
                    putExtra("app_uid", context.applicationInfo.uid)
                }

                else -> {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    addCategory(Intent.CATEGORY_DEFAULT)
                    data = Uri.parse("package:" + context.packageName)
                }
            }
        }
        context.startActivity(intent)
    }

    override fun onConfirmClick(tipValue: String) {
        val titleTextView: TextView? =
            driverTipTextView?.findViewWithTag(driverTipOptionsList?.lastIndex)
        driverTipOptionsList?.lastIndex?.let { resetAllDriverTip(it) }
        titleTextView?.text = "R${removeRandFromAmount(tipValue ?: DEFAULT_AMOUNT).toDouble()}"
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
        binding.layoutDriverTip.tipNoteTextView?.visibility = View.VISIBLE
        selectedDriverTipValue = tipValue
    }

    override fun onCancelDialog(previousTipValue: String) {

        var index = driverTipOptionsList?.indexOf(selectedDriverTipValue) ?: -1
        if (index < 0 && removeRandFromAmount(
                selectedDriverTipValue
                    ?: DEFAULT_AMOUNT
            ).toDouble() > 0.0
        ) {
            index = driverTipOptionsList?.lastIndex ?: -1
        }
        val titleTextView: TextView? = view?.findViewWithTag(index)
        titleTextView?.apply {
            if (index == driverTipOptionsList?.lastIndex) {
                text =
                    "R${removeRandFromAmount(selectedDriverTipValue ?: DEFAULT_AMOUNT).toDouble()}"
                val image =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.edit_icon_white)
                setCompoundDrawablesWithIntrinsicBounds(null, null, image, null)
                compoundDrawablePadding = resources.getDimension(R.dimen.five_dp).toInt()
            }
            background = bindDrawable(
                R.drawable.checkout_delivering_title_round_button_pressed
            )
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.layoutDriverTip.tipNoteTextView?.visibility = View.VISIBLE
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        //single checkbox age confirmation
        if (!isChecked) {
            Utils.fadeInFadeOutAnimation(binding.txtContinueToPayment, true)
            binding.ageConfirmationLayout.radioBtnAgeConfirmation?.isChecked = false
        } else {
            Utils.fadeInFadeOutAnimation(binding.txtContinueToPayment, false)
            binding.ageConfirmationLayout.radioBtnAgeConfirmation?.isChecked = true
        }
    }

    override fun onResume() {
        super.onResume()
        if (!areNotificationsEnabled()) {
            buildPushNotificationAlertToast(
                requireActivity(),
                binding.deliverySummaryScrollView,
                this
            )
        }
    }

    override fun onToastButtonClicked(jsonElement: JsonElement?) {
        // Open settings screen for turning on push notification.
        openAppNotificationSettings(requireActivity())
    }

    private fun loadShoppingCart() {
        checkoutAddAddressNewUserViewModel.shoppingCartData.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        val isNoLiquorOrder = resource.data?.data?.getOrNull(0)?.liquorOrder
                        if(isNoLiquorOrder == false) {
                            updateAgeConfirmationUI(isNoLiquorOrder)
                        }
                    }
                    Status.ERROR -> {
                        //Do Nothing
                    }
                    else -> {
                        //Do Nothing
                    }
                }
            }
        }
    }

    private fun updateAgeConfirmationUI(isNoLiquorOrder: Boolean?) {
        binding.ageConfirmationLayout?.root?.visibility = View.GONE
        binding.ageConfirmationLayout.liquorComplianceBannerLayout?.root?.visibility =
                View.GONE
        Utils.fadeInFadeOutAnimation(binding.txtContinueToPayment, false)
        liquorOrder = isNoLiquorOrder
        CheckoutAddressManagementBaseFragment.baseFragBundle?.apply {
            remove(Constant.LIQUOR_ORDER)
            remove(Constant.NO_LIQUOR_IMAGE_URL)
        }
    }
}