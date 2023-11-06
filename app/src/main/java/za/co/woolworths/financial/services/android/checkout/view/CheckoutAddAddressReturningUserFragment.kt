package za.co.woolworths.financial.services.android.checkout.view

import android.app.Activity.RESULT_CANCELED
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.View.FOCUS_UP
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CheckoutAddAddressRetuningUserBinding
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.checkout.service.network.Address
import za.co.woolworths.financial.services.android.checkout.service.network.ConfirmDeliveryAddressResponse
import za.co.woolworths.financial.services.android.checkout.service.network.OpenDayDeliverySlot
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.service.network.ShippingDetailsBody
import za.co.woolworths.financial.services.android.checkout.service.network.ShippingDetailsResponse
import za.co.woolworths.financial.services.android.checkout.service.network.Slot
import za.co.woolworths.financial.services.android.checkout.utils.AddShippingInfoEventsAnalytics
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.DeliveryType.DEFAULT
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.DeliveryType.MIXED_FOOD
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.DeliveryType.MIXED_OTHER
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.DeliveryType.ONLY_FOOD
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.DeliveryType.ONLY_OTHER
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.FulfillmentsType.FOOD
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.FulfillmentsType.OTHER
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.WeekCounter.FIRST
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.WeekCounter.SECOND
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.SAVED_ADDRESS_KEY
import za.co.woolworths.financial.services.android.checkout.view.CheckoutPaymentWebFragment.Companion.KEY_ARGS_WEB_TOKEN
import za.co.woolworths.financial.services.android.checkout.view.CheckoutPaymentWebFragment.Companion.REQUEST_KEY_PAYMENT_STATUS
import za.co.woolworths.financial.services.android.checkout.view.ExpandableGrid.Companion.DEFAULT_POSITION
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutDeliveryTypeSelectionListAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutDeliveryTypeSelectionListAdapter.Companion.DELIVERY_TYPE_TIMESLOT
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutDeliveryTypeSelectionShimmerAdapter
import za.co.woolworths.financial.services.android.checkout.view.adapter.ShoppingBagsRadioGroupAdapter
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.endlessaisle.service.network.UserLocationData
import za.co.woolworths.financial.services.android.endlessaisle.service.network.UserLocationResponse
import za.co.woolworths.financial.services.android.endlessaisle.utils.isEndlessAisleAvailable
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.response.ConfirmLocationAddress
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.models.dto.LiquorCompliance
import za.co.woolworths.financial.services.android.models.dto.OrderSummary
import za.co.woolworths.financial.services.android.models.dto.app_config.native_checkout.ConfigShoppingBagsOptions
import za.co.woolworths.financial.services.android.models.network.AppContextProviderImpl
import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity.Companion.ERROR_TYPE_EMPTY_CART
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.*
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.response.DyHomePageViewModel
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment.RESULT_EMPTY_CART
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment.RESULT_RELOAD_CART
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DEFAULT_ADDRESS
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_ENDLESS_AISLE_JOURNEY
import za.co.woolworths.financial.services.android.util.Constant.Companion.LIQUOR_ORDER
import za.co.woolworths.financial.services.android.util.Constant.Companion.NO_LIQUOR_IMAGE_URL
import za.co.woolworths.financial.services.android.util.ImageManager.Companion.setPicture
import za.co.woolworths.financial.services.android.util.Utils.*
import za.co.woolworths.financial.services.android.util.location.Event
import za.co.woolworths.financial.services.android.util.location.EventType
import za.co.woolworths.financial.services.android.util.location.Locator
import za.co.woolworths.financial.services.android.util.pushnotification.NotificationUtils
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Created by Kunal Uttarwar on 27/05/21.
 */
@AndroidEntryPoint
class CheckoutAddAddressReturningUserFragment :
    CheckoutAddressManagementBaseFragment(R.layout.checkout_add_address_retuning_user),
    OnClickListener,
    CheckoutDeliveryTypeSelectionListAdapter.EventListner,
    ShoppingBagsRadioGroupAdapter.EventListner, CompoundButton.OnCheckedChangeListener {

    companion object {
        const val REGEX_DELIVERY_INSTRUCTIONS = "^\$|^[a-zA-Z0-9\\s<!>@#\$&().+,-/\\\"']+\$"
        const val SLOT_SELECTION_REQUEST_CODE = 9876
    }

    private lateinit var binding: CheckoutAddAddressRetuningUserBinding
    private var selectedOpenDayDeliverySlot = OpenDayDeliverySlot()
    private var selectedFoodSubstitution = FoodSubstitution.SIMILAR_SUBSTITUTION
    private var oddSelectedPosition: Int = -1
    private var suburbId: String = ""
    private var selectedShoppingBagType: Double? = null
    private var placesId: String? = ""
    private var storeId: String? = ""
    private var nickName: String? = ""
    private var liquorImageUrl: String? = ""
    private var liquorOrder: Boolean? = false
    private var orderTotalValue: Double = -1.0
    private lateinit var locator: Locator

    @Inject
    lateinit var addShippingInfoEventsAnalytics: AddShippingInfoEventsAnalytics

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
    private val checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel by activityViewModels()
    private lateinit var expandableGrid: ExpandableGrid
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

    private var defaultAddress: Address? = null
    private var cartItemList: ArrayList<CommerceItem>? = null
    private var dyServerId: String? = null
    private var dySessionId: String? = null
    private var config: NetworkConfig? = null
    private var isMixedBasket: Boolean? = false
    private val dyChooseVariationViewModel: DyHomePageViewModel by viewModels()

    enum class FoodSubstitution(val rgb: String) {
        PHONE_CONFIRM("YES_CALL_CONFIRM"),
        SIMILAR_SUBSTITUTION("YES"),
        NO_THANKS("NO"),
        CHAT("CHAT")
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CheckoutAddAddressRetuningUserBinding.bind(view)
        expandableGrid = ExpandableGrid(this, binding)
        locator = Locator(activity as AppCompatActivity)

        // Hide keyboard in case it was visible from a previous screen
        KeyboardUtils.hideKeyboardIfVisible(activity)

        (activity as? CheckoutActivity)?.apply {
            showBackArrowWithTitle(bindString(R.string.checkout))
        }

        cartItemList = arguments?.getSerializable(CART_ITEM_LIST) as ArrayList<CommerceItem>?
        isMixedBasket = arguments?.getBoolean(Constant.IS_MIXED_BASKET, false)

        initViews()
    }

    private fun initViews() {
        initializeVariables()
        addFragmentListner()
        initializeDeliveringToView()
        initializeDeliveryFoodOtherItems()
        loadShoppingCart()
        getLiquorComplianceDetails()
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

        binding.txtContinueToPayment?.setOnClickListener(this)
        activity?.apply {
            view?.setOnClickListener {
                Utils.hideSoftKeyboard(this)
            }
        }
    }


    //LiquorCompliance
    private fun getLiquorComplianceDetails() {
        baseFragBundle?.apply {
            binding.apply {
                if (containsKey(LIQUOR_ORDER)) {
                    liquorOrder = getBoolean(LIQUOR_ORDER)
                    if (liquorOrder == true && containsKey(NO_LIQUOR_IMAGE_URL)) {
                        liquorImageUrl = getString(NO_LIQUOR_IMAGE_URL)
                        ageConfirmationLayout?.root?.visibility = VISIBLE
                        ageConfirmationLayout.liquorComplianceBannerLayout?.root?.visibility =
                            VISIBLE
                        setPicture(
                            ageConfirmationLayout.liquorComplianceBannerLayout.imgLiquorBanner,
                            liquorImageUrl
                        )

                        ageConfirmationLayout.root.visibility = VISIBLE
                        ageConfirmationLayout.liquorComplianceBannerSeparator.visibility =
                            VISIBLE
                        ageConfirmationLayout.liquorComplianceBannerLayout.root.visibility =
                            VISIBLE

                        if (!ageConfirmationLayout.radioBtnAgeConfirmation.isChecked) {
                            Utils.fadeInFadeOutAnimation(txtContinueToPayment, true)
                            ageConfirmationLayout.radioBtnAgeConfirmation?.isChecked = false
                            txtContinueToPayment?.isClickable = false
                        } else {
                            Utils.fadeInFadeOutAnimation(txtContinueToPayment, false)
                            txtContinueToPayment?.isClickable = true
                            ageConfirmationLayout.radioBtnAgeConfirmation?.isChecked = true
                        }
                    }
                } else {
                    ageConfirmationLayout?.root?.visibility = GONE
                    ageConfirmationLayout.liquorComplianceBannerLayout?.root?.visibility =
                        GONE
                }
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
            when (bundle.getInt(BUNDLE)) {
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
        binding.apply {
            layoutDeliveryInstructions.edtTxtSpecialDeliveryInstruction?.addTextChangedListener(
                deliveryInstructionsTextWatcher
            )
            layoutDeliveryInstructions.edtTxtGiftInstructions?.addTextChangedListener(
                deliveryInstructionsTextWatcher
            )
            layoutDeliveryInstructions.edtTxtInputLayoutSpecialDeliveryInstruction?.visibility =
                GONE
            layoutDeliveryInstructions.edtTxtInputLayoutSpecialDeliveryInstruction?.isCounterEnabled =
                false
            layoutDeliveryInstructions.edtTxtInputLayoutGiftInstructions?.visibility = GONE
            layoutDeliveryInstructions.edtTxtInputLayoutGiftInstructions?.isCounterEnabled =
                false

            layoutDeliveryInstructions.switchSpecialDeliveryInstruction?.setOnCheckedChangeListener { _, isChecked ->
                if (loadingBar.visibility == VISIBLE) {
                    return@setOnCheckedChangeListener
                }
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
                layoutDeliveryInstructions.edtTxtInputLayoutSpecialDeliveryInstruction?.visibility =
                    if (isChecked) VISIBLE else GONE
                layoutDeliveryInstructions.edtTxtInputLayoutSpecialDeliveryInstruction?.isCounterEnabled =
                    isChecked
                layoutDeliveryInstructions.edtTxtSpecialDeliveryInstruction?.visibility =
                    if (isChecked) VISIBLE else GONE
            }

            layoutDeliveryInstructions.switchGiftInstructions?.setOnCheckedChangeListener { _, isChecked ->
                if (loadingBar.visibility == VISIBLE) {
                    return@setOnCheckedChangeListener
                }
                if (isChecked)
                    Utils.triggerFireBaseEvents(
                        FirebaseManagerAnalyticsProperties.CHECKOUT,
                        hashMapOf(
                            FirebaseManagerAnalyticsProperties.PropertyNames.STEP to
                                        FirebaseManagerAnalyticsProperties.PropertyValues.DELIVERY_PAGE,
                                FirebaseManagerAnalyticsProperties.PropertyNames.DELIVERY_TYPE to
                                        KotlinUtils.getPreferredDeliveryType().toString(),
                                FirebaseManagerAnalyticsProperties.PropertyNames.TOGGLE_SELECTED to
                                    FirebaseManagerAnalyticsProperties.PropertyValues.IS_THIS_GIFT
                        ),
                        activity
                    )
                layoutDeliveryInstructions.edtTxtInputLayoutGiftInstructions?.visibility =
                    if (isChecked) VISIBLE else GONE
                layoutDeliveryInstructions.edtTxtInputLayoutGiftInstructions?.isCounterEnabled =
                    isChecked
                layoutDeliveryInstructions.edtTxtGiftInstructions?.visibility =
                    if (isChecked) VISIBLE else GONE
            }
            if (AppConfigSingleton.nativeCheckout?.currentShoppingBag?.isEnabled == true) {
                layoutDeliveryInstructions.switchNeedBags.visibility = VISIBLE
                layoutDeliveryInstructions.txtNeedBags?.text =
                    AppConfigSingleton.nativeCheckout?.currentShoppingBag?.title.plus(
                        AppConfigSingleton.nativeCheckout?.currentShoppingBag?.description
                    )
                layoutDeliveryInstructions.txtNeedBags.visibility = VISIBLE
                layoutDeliveryInstructions.viewHorizontalSeparator?.visibility = GONE
                layoutDeliveryInstructions.newShoppingBagsLayout.root.visibility = GONE
                layoutDeliveryInstructions.switchNeedBags?.setOnCheckedChangeListener { _, isChecked ->
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
                layoutDeliveryInstructions.switchNeedBags?.visibility = GONE
                layoutDeliveryInstructions.shoppingBagSeparator?.visibility = GONE
                layoutDeliveryInstructions.txtNeedBags?.visibility = GONE
                layoutDeliveryInstructions.newShoppingBagsLayout?.root?.visibility = VISIBLE
                addShoppingBagsRadioButtons()
            }
        }
    }

    private fun addShoppingBagsRadioButtons() {
        binding.layoutDeliveryInstructions.newShoppingBagsLayout.txtNewShoppingBagsSubDesc.visibility =
            VISIBLE
        val newShoppingBags = AppConfigSingleton.nativeCheckout?.newShoppingBag
        binding.layoutDeliveryInstructions.newShoppingBagsLayout.txtNewShoppingBagsDesc.text =
            newShoppingBags?.title
        binding.layoutDeliveryInstructions.newShoppingBagsLayout.txtNewShoppingBagsSubDesc.text =
            newShoppingBags?.description

        val shoppingBagsAdapter =
            ShoppingBagsRadioGroupAdapter(newShoppingBags?.options, this, selectedShoppingBagType)
        binding.layoutDeliveryInstructions.newShoppingBagsLayout.shoppingBagsRecyclerView.apply {
            layoutManager = activity?.let { LinearLayoutManager(it) }
            shoppingBagsAdapter?.let { adapter = it }
        }
    }

    private fun initializeDeliveringToView() {
        if (arguments == null) {
            binding.checkoutDeliveryDetailsLayout.root.visibility = GONE
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
                binding.checkoutDeliveryDetailsLayout?.root?.visibility = GONE
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
                        if (savedAddresses.defaultAddressNickname.isNullOrEmpty()) {
                            binding.checkoutDeliveryDetailsLayout.root.visibility = GONE
                        }
                    }
                }
                binding.checkoutDeliveryDetailsLayout.tvNativeCheckoutDeliveringTitle.text =
                    requireContext().getString(R.string.standard_delivery)
                binding.checkoutDeliveryDetailsLayout.tvNativeCheckoutDeliveringValue?.text =
                    deliveringToAddress
                binding.checkoutDeliveryDetailsLayout?.root?.setOnClickListener(this@CheckoutAddAddressReturningUserFragment)
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
                                            FirebaseManagerAnalyticsProperties.PropertyValues.PHONE_ME,
                                    FirebaseManagerAnalyticsProperties.PropertyNames.DELIVERY_TYPE to
                                            KotlinUtils.getPreferredDeliveryType().toString()
                            ),
                            activity
                    )

                    selectedFoodSubstitution = FoodSubstitution.PHONE_CONFIRM
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

    private fun initializeDeliveryTypeSelectionView(
        confirmDeliveryAddressResponse: ConfirmDeliveryAddressResponse?,
        type: DeliveryType,
    ) {
        // To show How would you like it to delivered.
        binding.checkoutHowWouldYouDeliveredLayout.root.visibility = VISIBLE
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
        binding.checkoutHowWouldYouDeliveredLayout.deliveryTypeSelectionRecyclerView.adapter = null
        checkoutDeliveryTypeSelectionListAdapter =
            CheckoutDeliveryTypeSelectionListAdapter(
                localOpenDayDeliverySlots,
                this,
                type,
                selectedOpenDayDeliverySlot
            )
        binding.checkoutHowWouldYouDeliveredLayout.deliveryTypeSelectionRecyclerView?.apply {
            addItemDecoration(object : RecyclerView.ItemDecoration() {})
            layoutManager = activity?.let { LinearLayoutManager(it) }
            checkoutDeliveryTypeSelectionListAdapter?.let { adapter = it }
        }
    }

    private fun initializeDeliveryFoodOtherItems() {
        binding.checkoutTimeSlotSelectionLayout.previousImgBtnFood.setOnClickListener(this)
        binding.checkoutTimeSlotSelectionLayout.nextImgBtnFood.setOnClickListener(this)
        binding.checkoutHowWouldYouDeliveredLayout.gridLayoutDeliveryOptions.previousImgBtnOther.setOnClickListener(
            this
        )
        binding.checkoutHowWouldYouDeliveredLayout.gridLayoutDeliveryOptions.nextImgBtnOther.setOnClickListener(
            this
        )
        binding.ageConfirmationLayout.radioBtnAgeConfirmation.setOnCheckedChangeListener(this)
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
                    if (discountDetails.otherDiscount == 0.0) GONE else VISIBLE
                binding.layoutCheckoutDeliveryOrderSummary.groupPromoCodeDiscount?.visibility =
                    if (discountDetails.promoCodeDiscount == 0.0) GONE else VISIBLE
                binding.layoutCheckoutDeliveryOrderSummary.groupWRewardsDiscount?.visibility =
                    if (discountDetails.voucherDiscount == 0.0) GONE else VISIBLE
                binding.layoutCheckoutDeliveryOrderSummary.groupCompanyDiscount?.visibility =
                    if (discountDetails.companyDiscount == 0.0) GONE else VISIBLE
                binding.layoutCheckoutDeliveryOrderSummary.groupTotalDiscount?.visibility =
                    if (discountDetails.totalDiscount == 0.0) GONE else VISIBLE

                binding.layoutCheckoutDeliveryOrderSummary.txtOrderSummaryDiscountValue?.text =
                    "-".plus(CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.otherDiscount))
                binding.layoutCheckoutDeliveryOrderSummary.txtOrderSummaryTotalDiscountValue?.text =
                    "-".plus(CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.totalDiscount))
                binding.layoutCheckoutDeliveryOrderSummary.txtOrderSummaryWRewardsVouchersValue?.text =
                    "-".plus(CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.voucherDiscount))
                binding.layoutCheckoutDeliveryOrderSummary.txtOrderSummaryCompanyDiscountValue?.text =
                    "-".plus(CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.companyDiscount))
                binding.layoutCheckoutDeliveryOrderSummary.txtOrderSummaryPromoCodeDiscountValue?.text =
                    "-".plus(CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.promoCodeDiscount))

                binding.layoutCheckoutDeliveryOrderSummary.txtOrderTotalValue.text =
                    CurrencyFormatter.formatAmountToRandAndCentWithSpace(it.total)
                orderTotalValue = it.total

            }
        }
    }

    private fun startShimmerView() {
        expandableGrid.setUpShimmerView()
        expandableGrid.showDeliveryTypeShimmerView()
        showDeliverySubTypeShimmerView()
        binding.apply {
            layoutDeliveryInstructions.edtTxtSpecialDeliveryInstruction?.visibility = GONE
            layoutDeliveryInstructions.edtTxtGiftInstructions?.visibility = GONE
            layoutDeliveryInstructions.switchSpecialDeliveryInstruction?.isChecked = false
            layoutDeliveryInstructions.switchGiftInstructions?.isChecked = false

            shimmerComponentArray = listOf(
                Pair<ShimmerFrameLayout, View>(
                    checkoutDeliveryDetailsLayout.deliveringTitleShimmerFrameLayout,
                    checkoutDeliveryDetailsLayout.tvNativeCheckoutDeliveringTitle
                ),
                Pair<ShimmerFrameLayout, View>(
                    checkoutDeliveryDetailsLayout.deliveringTitleValueShimmerFrameLayout,
                    checkoutDeliveryDetailsLayout.tvNativeCheckoutDeliveringValue
                ),
                Pair<ShimmerFrameLayout, View>(
                    checkoutDeliveryDetailsLayout.forwardImgViewShimmerFrameLayout,
                    checkoutDeliveryDetailsLayout.imageViewCaretForward
                ),
                Pair<ShimmerFrameLayout, View>(
                    nativeCheckoutFoodSubstitutionLayout.foodSubstitutionTitleShimmerFrameLayout,
                    nativeCheckoutFoodSubstitutionLayout.txtFoodSubstitutionTitle
                ),
                Pair<ShimmerFrameLayout, View>(
                    nativeCheckoutFoodSubstitutionLayout.foodSubstitutionDescShimmerFrameLayout,
                    nativeCheckoutFoodSubstitutionLayout.txtFoodSubstitutionDesc
                ),
                Pair<ShimmerFrameLayout, View>(
                    nativeCheckoutFoodSubstitutionLayout.radioGroupFoodSubstitutionShimmerFrameLayout,
                    nativeCheckoutFoodSubstitutionLayout.radioGroupFoodSubstitution
                ),

                Pair<ShimmerFrameLayout, View>(
                    ageConfirmationLayout.ageConfirmationTitleShimmerFrameLayout,
                    ageConfirmationLayout.txtAgeConfirmationTitle
                ),

                Pair<ShimmerFrameLayout, View>(
                    ageConfirmationLayout.ageConfirmationDescShimmerFrameLayout,
                    ageConfirmationLayout.txtAgeConfirmationDesc
                ),

                Pair<ShimmerFrameLayout, View>(
                    ageConfirmationLayout.ageConfirmationDescNoteShimmerFrameLayout,
                    ageConfirmationLayout.txtAgeConfirmationDescNote
                ),

                Pair<ShimmerFrameLayout, View>(
                    ageConfirmationLayout.radioGroupAgeConfirmationShimmerFrameLayout,
                    ageConfirmationLayout.radioBtnAgeConfirmation
                ),

                Pair<ShimmerFrameLayout, View>(
                    ageConfirmationLayout.ageConfirmationTitleShimmerFrameLayout,
                    ageConfirmationLayout.txtAgeConfirmationTitle
                ),

                Pair<ShimmerFrameLayout, View>(
                    ageConfirmationLayout.ageConfirmationDescShimmerFrameLayout,
                    ageConfirmationLayout.txtAgeConfirmationDesc
                ),

                Pair<ShimmerFrameLayout, View>(
                    ageConfirmationLayout.ageConfirmationDescNoteShimmerFrameLayout,
                    ageConfirmationLayout.txtAgeConfirmationDescNote
                ),

                Pair<ShimmerFrameLayout, View>(
                    ageConfirmationLayout.radioGroupAgeConfirmationShimmerFrameLayout,
                    ageConfirmationLayout.radioBtnAgeConfirmation
                ),

                Pair<ShimmerFrameLayout, View>(
                    ageConfirmationLayout.liquorComplianceBannerShimmerFrameLayout,
                    ageConfirmationLayout.liquorComplianceBannerLayout.root
                ),

                Pair<ShimmerFrameLayout, View>(
                    layoutDeliveryInstructions.instructionTxtShimmerFrameLayout,
                    layoutDeliveryInstructions.txtSpecialDeliveryInstruction
                ),
                Pair<ShimmerFrameLayout, View>(
                    layoutDeliveryInstructions.specialInstructionSwitchShimmerFrameLayout,
                    layoutDeliveryInstructions.switchSpecialDeliveryInstruction
                ),
                Pair<ShimmerFrameLayout, View>(
                    layoutDeliveryInstructions.giftInstructionTxtShimmerFrameLayout,
                    layoutDeliveryInstructions.txtGiftInstructions
                ),
                Pair<ShimmerFrameLayout, View>(
                    layoutDeliveryInstructions.giftInstructionSwitchShimmerFrameLayout,
                    layoutDeliveryInstructions.switchGiftInstructions
                ),
                Pair<ShimmerFrameLayout, View>(
                    layoutCheckoutDeliveryOrderSummary.txtYourCartShimmerFrameLayout,
                    layoutCheckoutDeliveryOrderSummary.txtOrderSummaryYourCart
                ),
                Pair<ShimmerFrameLayout, View>(
                    layoutCheckoutDeliveryOrderSummary.yourCartValueShimmerFrameLayout,
                    layoutCheckoutDeliveryOrderSummary.txtOrderSummaryYourCartValue
                ),
                Pair<ShimmerFrameLayout, View>(
                    layoutCheckoutDeliveryOrderSummary.deliveryFeeTxtShimmerFrameLayout,
                    layoutCheckoutDeliveryOrderSummary.txtOrderSummaryDeliveryFee
                ),
                Pair<ShimmerFrameLayout, View>(
                    layoutCheckoutDeliveryOrderSummary.deliveryFeeValueShimmerFrameLayout,
                    layoutCheckoutDeliveryOrderSummary.txtOrderSummaryDeliveryFeeValue
                ),
                Pair<ShimmerFrameLayout, View>(
                    layoutCheckoutDeliveryOrderSummary.summaryNoteShimmerFrameLayout,
                    layoutCheckoutDeliveryOrderSummary.txtOrderSummaryNote
                ),
                Pair<ShimmerFrameLayout, View>(
                    layoutCheckoutDeliveryOrderSummary.txtOrderTotalShimmerFrameLayout,
                    layoutCheckoutDeliveryOrderSummary.txtOrderTotalTitle
                ),
                Pair<ShimmerFrameLayout, View>(
                    layoutCheckoutDeliveryOrderSummary.orderTotalValueShimmerFrameLayout,
                    layoutCheckoutDeliveryOrderSummary.txtOrderTotalValue
                ),
                Pair<ShimmerFrameLayout, View>(
                    continuePaymentTxtShimmerFrameLayout,
                    txtContinueToPayment
                ),
                Pair<ShimmerFrameLayout, View>(
                    layoutDeliveryInstructions.newShoppingBagsLayout.newShoppingBagsTitleShimmerFrameLayout,
                    layoutDeliveryInstructions.newShoppingBagsLayout.newShoppingBagsTitle
                ),
                Pair<ShimmerFrameLayout, View>(
                    layoutDeliveryInstructions.newShoppingBagsLayout.newShoppingBagsDescShimmerFrameLayout,
                    layoutDeliveryInstructions.newShoppingBagsLayout.txtNewShoppingBagsDesc
                ),
                Pair<ShimmerFrameLayout, View>(
                    layoutDeliveryInstructions.newShoppingBagsLayout.radioGroupShoppingBagsShimmerFrameLayout,
                    layoutDeliveryInstructions.newShoppingBagsLayout.radioGroupShoppingBags
                )
            )

            layoutDeliveryInstructions.txtNeedBags.visibility = GONE
            layoutDeliveryInstructions.switchNeedBags.visibility = GONE

            val shimmer = Shimmer.AlphaHighlightBuilder().build()
            shimmerComponentArray.forEach {
                it.first.setShimmer(shimmer)
                it.first.startShimmer()
                it.second.visibility = INVISIBLE
            }
        }
    }

    private fun stopShimmerView() {
        expandableGrid.hideDeliveryTypeShimmerView()

        shimmerComponentArray.forEach {
            it.first.stopShimmer()
            it.first.setShimmer(null)
            it.second.visibility = VISIBLE
        }

        binding.layoutDeliveryInstructions.txtNeedBags.visibility = VISIBLE
        binding.layoutDeliveryInstructions.switchNeedBags.visibility = VISIBLE

        initializeFoodSubstitution()
        initializeDeliveryInstructions()
    }

    private fun getConfirmDeliveryAddressDetails() {
        binding.deliverySummaryScrollView?.fullScroll(FOCUS_UP)
        startShimmerView()

        val confirmLocationAddress =
            ConfirmLocationAddress(defaultAddress?.placesId, defaultAddress?.nickname)

        if (confirmLocationAddress.placeId?.isNullOrEmpty() == true) {
            return
        }

        val body =
            ConfirmLocationRequest(Delivery.STANDARD.name, confirmLocationAddress, "", "checkout")

        checkoutAddAddressNewUserViewModel.getConfirmLocationDetails(body)
            .observe(viewLifecycleOwner) { response ->
                stopShimmerView()
                when (response) {
                    is ConfirmDeliveryAddressResponse -> {
                        confirmDeliveryAddressResponse = response

                        if (response.orderSummary != null && (response.orderSummary?.totalItemsCount
                                        ?: 0) <= 0) {
                            showEmptyCart()
                            return@observe
                        }

                        // Keeping two diff response not to get merge while showing 2 diff slots.
                        selectedSlotResponseFood = response
                        selectedSlotResponseOther = response
                        showDeliverySlotSelectionView()
                        initializeOrderSummary(response.orderSummary)

                        if (response.orderSummary?.hasMinimumBasketAmount == false) {
                            KotlinUtils.showMinCartValueError(
                                requireActivity() as AppCompatActivity,
                                response.orderSummary?.minimumBasketAmount
                            )
                        }
                    }

                    is Throwable -> {
                        presentErrorDialog(
                            getString(R.string.common_error_unfortunately_something_went_wrong),
                            getString(R.string.no_internet_subtitle)
                        )
                    }
                }
            }
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
        binding.nativeCheckoutFoodSubstitutionLayout.root.visibility =
            VISIBLE // by default it is visible.
        if (FOOD.type == selectedSlotResponseFood?.fulfillmentTypes?.join) {
            //Only for Food
            foodType = ONLY_FOOD
            binding.checkoutTimeSlotSelectionLayout.root.visibility = VISIBLE
            binding.checkoutTimeSlotSelectionLayout.selectDeliveryTimeSlotTitle.text =
                getString(R.string.slot_delivery_title_when)
            binding.checkoutTimeSlotSelectionLayout.selectDeliveryTimeSlotSubTitleFood.visibility =
                GONE
            binding.checkoutTimeSlotSelectionLayout.txtSelectDeliveryTimeSlotFoodError?.visibility =
                GONE
            expandableGrid.initialiseGridView(
                selectedSlotResponseFood,
                FIRST.week,
                ONLY_FOOD
            )

        } else if (OTHER.type == selectedSlotResponseFood?.fulfillmentTypes?.join && OTHER.type == selectedSlotResponseFood?.fulfillmentTypes?.other) {
            // For mix basket
            foodType = MIXED_FOOD
            binding.checkoutTimeSlotSelectionLayout.root.visibility = VISIBLE
            expandableGrid.initialiseGridView(
                selectedSlotResponseFood,
                FIRST.week,
                MIXED_FOOD
            )
            if (selectedSlotResponseFood?.requiredToDisplayODD == true) {
                binding.checkoutHowWouldYouDeliveredLayout.howWouldYouDeliveredTitle.text =
                    getString(R.string.delivery_timeslot_title_other_items)
                initializeDeliveryTypeSelectionView(
                    selectedSlotResponseFood,
                    MIXED_OTHER
                ) // Sending params MIXED_OTHER here to get mixed_other grid while click on timeslot radiobutton.
            }
        } else {
            // for Other
            if (selectedSlotResponseFood?.requiredToDisplayODD == true) {
                binding.nativeCheckoutFoodSubstitutionLayout.root.visibility =
                    GONE // if FBH then hide this layout.
                initializeDeliveryTypeSelectionView(
                    selectedSlotResponseFood,
                    ONLY_OTHER
                )
            }
            // When the basket is FBH then no need to show shopping/plastic bags.
            binding.layoutDeliveryInstructions.switchNeedBags.visibility = GONE
            binding.layoutDeliveryInstructions.txtNeedBags.visibility = GONE
            binding.layoutDeliveryInstructions.newShoppingBagsLayout.root.visibility = GONE
        }

    }

    private fun showDeliverySubTypeShimmerView() {
        checkoutDeliveryTypeSelectionShimmerAdapter =
            CheckoutDeliveryTypeSelectionShimmerAdapter(3)

        binding.checkoutHowWouldYouDeliveredLayout.deliveryTypeSelectionRecyclerView?.apply {
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
        deliveryType: DeliveryType,
    ) {
        if (deliveryType == ONLY_FOOD || deliveryType == MIXED_FOOD)
            selectedSlotResponseFood = confirmDeliveryAddressResponse
        else
            selectedSlotResponseOther = confirmDeliveryAddressResponse
    }

    fun setSelectedFoodOrOtherSlot(selectedSlot: Slot, deliveryType: DeliveryType) {
        if (deliveryType == ONLY_FOOD || deliveryType == MIXED_FOOD) {
            selectedFoodSlot = selectedSlot
            binding.checkoutTimeSlotSelectionLayout.txtSelectDeliveryTimeSlotFoodError.visibility =
                GONE
        } else {
            selectedOtherSlot = selectedSlot
            binding.checkoutHowWouldYouDeliveredLayout.txtSelectDeliveryTimeSlotOtherError.visibility =
                GONE
        }
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
                                selectedSlot.stringShipOnDate + " " + selectedSlot.hourFrom
                ),
                activity
        )
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
                KotlinUtils.presentEditDeliveryGeoLocationActivity(
                    requireActivity(),
                    SLOT_SELECTION_REQUEST_CODE,
                    KotlinUtils.getPreferredDeliveryType(),
                    placesId,
                    false,
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
                activity?.finish()
            }

            R.id.txtContinueToPayment -> {
                cartItemList?.let {
                    addShippingInfoEventsAnalytics.sendEventData(
                        it,
                        FirebaseManagerAnalyticsProperties.PropertyValues.SHIPPING_TIER_VALUE_STD,
                        orderTotalValue
                    )
                }
                onCheckoutPaymentClick()
                preparePaymentPageViewRequest(orderTotalValue)
            }
        }
    }

    private fun preparePaymentPageViewRequest(orderTotalValue: Double) {
        config = NetworkConfig(AppContextProviderImpl())
        if (Utils.getSessionDaoDyServerId(SessionDao.KEY.DY_SERVER_ID) != null)
            dyServerId = Utils.getSessionDaoDyServerId(SessionDao.KEY.DY_SERVER_ID)
        if (Utils.getSessionDaoDySessionId(SessionDao.KEY.DY_SESSION_ID) != null)
            dySessionId = Utils.getSessionDaoDySessionId(SessionDao.KEY.DY_SESSION_ID)
        val user = User(dyServerId,dyServerId)
        val session = Session(dySessionId)
        val device = Device(Utils.IPAddress, config?.getDeviceModel())
        val dataOther = DataOther(null,null,ZAR,"",orderTotalValue)
        val dataOtherArray: ArrayList<DataOther>? = ArrayList<DataOther>()
        dataOtherArray?.add(dataOther)
        val page = Page(null, PAYMENT_PAGE, Utils.OTHER, null, dataOtherArray)
        val context = Context(device, page, Utils.DY_CHANNEL)
        val options = Options(true)
        val homePageRequestEvent = HomePageRequestEvent(user, session, context, options)
        dyChooseVariationViewModel.createDyRequest(homePageRequestEvent)
    }

    private fun onCheckoutPaymentClick() {
        if ((isRequiredFieldsMissing() || isGiftMessage())) {
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
        binding.loadingBar?.visibility = VISIBLE
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
                                getString(R.string.common_error_message_without_contact_info)
                            )
                            return@observe
                        }
                        // TODO: Verify user in store by API call
                        if(isEndlessAisleAvailable() && isMixedBasket == false) {
                            locator.getCurrentLocationSilently { event ->
                                when (event) {
                                    is Event.Location -> {
                                        handleLocationEvent(event, response)
                                    }
                                    is Event.Permission -> {
                                        handleLocationPermissionEvent(event, response)
                                    }
                                }
                            }
                        } else {
                            navigateToPaymentWebpage(response)
                        }
                    }

                    is Throwable -> {
                        presentErrorDialog(
                            getString(R.string.common_error_unfortunately_something_went_wrong),
                            getString(R.string.common_error_message_without_contact_info)
                        )
                    }
                }
            }
        //liquor compliance: age confirmation
        if (liquorOrder == true && !binding.ageConfirmationLayout.radioBtnAgeConfirmation.isChecked) {
            binding.ageConfirmationLayout.root.visibility = VISIBLE
            binding.ageConfirmationLayout.liquorComplianceBannerSeparator.visibility = VISIBLE
            binding.ageConfirmationLayout.liquorComplianceBannerLayout.root.visibility = VISIBLE

            Utils.fadeInFadeOutAnimation(binding.txtContinueToPayment, false)
        } else {
            Utils.fadeInFadeOutAnimation(binding.txtContinueToPayment, true)
        }
    }

    private fun isGiftMessage(): Boolean {
        return when (binding.layoutDeliveryInstructions.switchGiftInstructions?.isChecked) {
            true -> {
                if (TextUtils.isEmpty(binding.layoutDeliveryInstructions.edtTxtGiftInstructions?.text?.toString())) {

                    binding.deliverySummaryScrollView?.smoothScrollTo(
                        0,
                        binding.layoutDeliveryInstructions?.root?.top ?: 0
                    )
                    true
                } else false
            }

            else -> false
        }
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
        when {
            // Food Items Basket
            foodType == ONLY_FOOD -> {
                if (!TextUtils.isEmpty(selectedFoodSlot?.slotId)) {
                    binding.checkoutTimeSlotSelectionLayout.txtSelectDeliveryTimeSlotFoodError?.visibility =
                        GONE
                    return false
                }
                // scroll to slot selection layout
                binding.deliverySummaryScrollView?.smoothScrollTo(
                    0,
                    binding.checkoutTimeSlotSelectionLayout?.root?.top ?: 0
                )
                binding.checkoutTimeSlotSelectionLayout.txtSelectDeliveryTimeSlotFoodError?.visibility =
                    VISIBLE
            }
            // Other Items Basket
            otherType == ONLY_OTHER -> {
                when {
                    (selectedOpenDayDeliverySlot.deliveryType != null
                            && selectedOpenDayDeliverySlot.deliveryType != DELIVERY_TYPE_TIMESLOT) -> {
                        if (!TextUtils.isEmpty(selectedOpenDayDeliverySlot?.deliverySlotId)) {
                            binding.checkoutHowWouldYouDeliveredLayout.txtSelectDeliveryTimeSlotOtherError?.visibility =
                                GONE
                            return false
                        }
                    }

                    else -> {
                        if (!TextUtils.isEmpty(selectedOtherSlot?.slotId)) {
                            binding.checkoutHowWouldYouDeliveredLayout.txtSelectDeliveryTimeSlotOtherError?.visibility =
                                GONE
                            return false
                        }
                        binding.checkoutHowWouldYouDeliveredLayout.txtSelectDeliveryTimeSlotOtherError?.visibility =
                            VISIBLE
                    }
                }
                // scroll to other slot selection layout
                binding.checkoutHowWouldYouDeliveredLayout?.selectDeliveryTimeSlotSubTitle?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_D0021B
                    )
                )
                binding.deliverySummaryScrollView?.smoothScrollTo(
                    0,
                    binding.checkoutHowWouldYouDeliveredLayout?.root?.top ?: 0
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
                            binding.checkoutTimeSlotSelectionLayout.txtSelectDeliveryTimeSlotFoodError?.visibility =
                                GONE
                            binding.checkoutHowWouldYouDeliveredLayout.txtSelectDeliveryTimeSlotOtherError?.visibility =
                                GONE
                            return false
                        }

                        (TextUtils.isEmpty(selectedFoodSlot?.slotId)) -> {
                            // scroll to slot selection layout
                            binding.deliverySummaryScrollView?.smoothScrollTo(
                                0,
                                binding.checkoutTimeSlotSelectionLayout?.root?.top ?: 0
                            )
                            binding.checkoutTimeSlotSelectionLayout.txtSelectDeliveryTimeSlotFoodError?.visibility =
                                VISIBLE
                        }

                        else -> {
                            if (TextUtils.isEmpty(selectedOtherSlot?.slotId)) {
                                binding.checkoutHowWouldYouDeliveredLayout?.selectDeliveryTimeSlotSubTitle?.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.color_D0021B
                                    )
                                )
                                // scroll to other slot selection layout
                                binding.deliverySummaryScrollView?.smoothScrollTo(
                                    0,
                                    binding.checkoutHowWouldYouDeliveredLayout?.root?.top ?: 0
                                )
                                binding.checkoutHowWouldYouDeliveredLayout.txtSelectDeliveryTimeSlotOtherError?.visibility =
                                    VISIBLE
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
                        binding.deliverySummaryScrollView?.smoothScrollTo(
                            0,
                            binding.checkoutTimeSlotSelectionLayout?.root?.top ?: 0
                        )
                        binding.checkoutTimeSlotSelectionLayout.txtSelectDeliveryTimeSlotFoodError?.visibility =
                            VISIBLE
                    } else if (TextUtils.isEmpty(selectedOtherSlot?.slotId)) {
                        // scroll to other slot selection layout
                        binding.checkoutHowWouldYouDeliveredLayout?.selectDeliveryTimeSlotSubTitle?.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.color_D0021B
                            )
                        )
                        binding.deliverySummaryScrollView?.smoothScrollTo(
                            0,
                            binding.checkoutHowWouldYouDeliveredLayout?.root?.top ?: 0
                        )
                    }
                }
            }
            //Default
            otherType == DEFAULT -> {
                // scroll to other slot selection layout
                binding.checkoutHowWouldYouDeliveredLayout?.selectDeliveryTimeSlotSubTitle?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_D0021B
                    )
                )
                binding.deliverySummaryScrollView?.smoothScrollTo(
                    0,
                    binding.checkoutHowWouldYouDeliveredLayout?.root?.top ?: 0
                )
            }

            else -> return true
        }
        return true
    }

    private fun navigateToPaymentWebpage(webTokens: ShippingDetailsResponse, isEndlessAisle: Boolean = false) {
        view?.findNavController()?.navigate(
            R.id.action_CheckoutAddAddressReturningUserFragment_to_checkoutPaymentWebFragment,
            bundleOf(
                KEY_ARGS_WEB_TOKEN to webTokens,
                CART_ITEM_LIST to cartItemList,
                IS_ENDLESS_AISLE_JOURNEY to isEndlessAisle
            )
        )
    }

    private fun setScreenClickEvents(isClickable: Boolean) {
        binding.nativeCheckoutFoodSubstitutionLayout.radioGroupFoodSubstitution?.isClickable =
            isClickable
        binding.checkoutDeliveryDetailsLayout?.root?.isClickable = isClickable
        binding.layoutDeliveryInstructions.switchNeedBags?.isClickable = isClickable
        binding.layoutDeliveryInstructions.switchGiftInstructions?.isClickable = isClickable
        binding.layoutDeliveryInstructions.switchSpecialDeliveryInstruction?.isClickable =
            isClickable
    }

    private fun getShipmentDetailsBody(): ShippingDetailsBody {
        val body = ShippingDetailsBody()
        KotlinUtils.getUniqueDeviceID {
            body.apply {
                pushNotificationToken = Utils.getToken()
                appInstanceId = it
                tokenProvider =
                    if (Utils.isGooglePlayServicesAvailable()) NotificationUtils.TOKEN_PROVIDER_FIREBASE else NotificationUtils.TOKEN_PROVIDER_HMS
            }
        }
        when {
            // Food Items Basket
            foodType == ONLY_FOOD -> {
                body.apply {
                    requestFrom = "express"
                    joinBasket = true
                    if (liquorOrder == true) {
                        ageConsentConfirmed = true
                    }
                    foodShipOnDate = selectedFoodSlot?.stringShipOnDate
                    otherShipOnDate = ""
                    foodDeliverySlotId = selectedFoodSlot?.slotId
                    otherDeliverySlotId = ""
                    oddDeliverySlotId = ""
                    foodDeliveryStartHour = selectedFoodSlot?.intHourFrom?.toLong() ?: 0
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
                        otherDeliveryStartHour = selectedOtherSlot?.intHourFrom?.toLong() ?: 0
                        oddDeliverySlotId = ""
                    }
                }
            }
            //Mixed Basket
            foodType == MIXED_FOOD || otherType == MIXED_OTHER -> {
                body.apply {
                    joinBasket = false
                    if (liquorOrder == true) {
                        ageConsentConfirmed = true
                    }
                    if (selectedOpenDayDeliverySlot.deliveryType != null && selectedOpenDayDeliverySlot.deliveryType == DELIVERY_TYPE_TIMESLOT) {
                        foodShipOnDate = selectedFoodSlot?.stringShipOnDate
                        otherShipOnDate = selectedOtherSlot?.stringShipOnDate
                        foodDeliverySlotId = selectedFoodSlot?.slotId
                        otherDeliverySlotId = selectedOtherSlot?.slotId
                        foodDeliveryStartHour = selectedFoodSlot?.intHourFrom?.toLong() ?: 0
                        otherDeliveryStartHour = selectedOtherSlot?.intHourFrom?.toLong() ?: 0
                        oddDeliverySlotId = ""
                    } else {
                        foodShipOnDate = selectedFoodSlot?.stringShipOnDate
                        otherShipOnDate = ""
                        foodDeliverySlotId = selectedFoodSlot?.slotId
                        otherDeliverySlotId = ""
                        foodDeliveryStartHour = selectedFoodSlot?.intHourFrom?.toLong() ?: 0
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
            substituesAllowed =
                if (binding.nativeCheckoutFoodSubstitutionLayout.root.visibility == VISIBLE) selectedFoodSubstitution.rgb else null
            plasticBags = binding.layoutDeliveryInstructions.switchNeedBags?.isChecked ?: false
            shoppingBagType = selectedShoppingBagType
            giftNoteSelected =
                binding.layoutDeliveryInstructions.switchGiftInstructions?.isChecked ?: false
            deliverySpecialInstructions =
                if (binding.layoutDeliveryInstructions.switchSpecialDeliveryInstruction?.isChecked == true) binding.layoutDeliveryInstructions.edtTxtSpecialDeliveryInstruction?.text.toString() else ""
            giftMessage =
                if (binding.layoutDeliveryInstructions.switchGiftInstructions?.isChecked == true) binding.layoutDeliveryInstructions.edtTxtGiftInstructions?.text.toString() else ""
            suburbId = this@CheckoutAddAddressReturningUserFragment.suburbId
            storeId = ""
            deliveryType = Delivery.STANDARD.type
            address = ConfirmLocationAddress(placesId)
        }

        return body
    }

    override fun selectedDeliveryType(
        openDayDeliverySlot: OpenDayDeliverySlot,
        type: DeliveryType,
        position: Int,
    ) {
        oddSelectedPosition = position
        selectedOpenDayDeliverySlot = openDayDeliverySlot

        binding.checkoutHowWouldYouDeliveredLayout?.selectDeliveryTimeSlotSubTitle?.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.checkout_delivering_title
            )
        )

        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.CHECKOUT_DELIVERY_OPTION_.plus(openDayDeliverySlot.deliveryType),
            hashMapOf(
                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                        FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_DELIVERY_OPTION_PRE_VALUE1
                            .plus(openDayDeliverySlot.deliveryType)
                            .plus(FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_DELIVERY_OPTION_PRE_VALUE2)
            ),
            activity
        )
        when (openDayDeliverySlot.deliveryType) {
            DELIVERY_TYPE_TIMESLOT -> {
                binding.checkoutHowWouldYouDeliveredLayout.gridLayoutDeliveryOptions.root.visibility =
                    VISIBLE
                otherType = type
                expandableGrid.apply {
                    disablePreviousBtnOther()
                    enableNextBtnOther()
                    initialiseGridView(selectedSlotResponseOther, FIRST.week, type)
                }
            }

            else -> {
                otherType = type
                binding.checkoutHowWouldYouDeliveredLayout.gridLayoutDeliveryOptions.root.visibility =
                    GONE
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
        shoppingBagsOptionsList: ConfigShoppingBagsOptions,
        position: Int,
    ) {
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.CHECKOUT_SHOPPING_BAGS_INFO,
            hashMapOf(
                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                        FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_BAGS_INFO
            ),
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

            SLOT_SELECTION_REQUEST_CODE -> {
                if (data?.hasExtra(DEFAULT_ADDRESS) == true) {
                    this.defaultAddress = data?.getSerializableExtra(DEFAULT_ADDRESS) as Address
                }
            }


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

    private fun handleLocationEvent(locationEvent: Event.Location, response: ShippingDetailsResponse) {
        if (locationEvent.locationData == null) {
            navigateToPaymentWebpage(response)
        } else {
            val latitude = locationEvent.locationData.latitude
            val longitude = locationEvent.locationData.longitude
            checkoutAddAddressNewUserViewModel.verifyUserIsInStore(latitude, longitude).observeForever {
                when (it) {
                    is UserLocationResponse -> {
                        if (it.httpCode == 200) {
                            val store = it.data.firstOrNull { data -> data.payInStore }
                            if (store != null) {
                                navigateToPaymentWebpage(response, true)
                            } else {
                                navigateToPaymentWebpage(response)
                            }
                        } else {
                            navigateToPaymentWebpage(response)
                        }
                    }
                    is Throwable -> {
                        navigateToPaymentWebpage(response)
                    }
                    null -> {
                        navigateToPaymentWebpage(response)
                    }
                }
            }
        }
    }

    private fun handleLocationPermissionEvent(event: Event.Permission, response: ShippingDetailsResponse) {
        when (event.event) {
            EventType.LOCATION_PERMISSION_GRANTED -> {
                // do nothing
            }
            EventType.LOCATION_DISABLED_ON_DEVICE -> {
                navigateToPaymentWebpage(response)
            }
            EventType.LOCATION_PERMISSION_NOT_GRANTED -> {
                navigateToPaymentWebpage(response)
            }
            EventType.LOCATION_SERVICE_DISCONNECTED -> {
                navigateToPaymentWebpage(response)
            }
        }
    }
}
