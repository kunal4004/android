package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.checkout_add_address_retuning_user.*
import kotlinx.android.synthetic.main.checkout_add_address_retuning_user.loadingBar
import kotlinx.android.synthetic.main.fragment_checkout_returning_user_collection.*
import kotlinx.android.synthetic.main.layout_collection_user_information.*
import kotlinx.android.synthetic.main.layout_delivering_to_details.*
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_food_substitution.*
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_instructions.*
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_order_summary.*
import kotlinx.android.synthetic.main.new_shopping_bags_layout.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.Companion.REGEX_DELIVERY_INSTRUCTIONS
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressReturningUserFragment.FoodSubstitution
import za.co.woolworths.financial.services.android.checkout.view.adapter.ShoppingBagsRadioGroupAdapter
import za.co.woolworths.financial.services.android.checkout.viewmodel.WhoIsCollectingDetails
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.OrderSummary
import za.co.woolworths.financial.services.android.models.dto.ShoppingBagsOptions
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.Utils
import java.util.regex.Pattern

class CheckoutReturningUserCollectionFragment : Fragment(),
    ShoppingBagsRadioGroupAdapter.EventListner, View.OnClickListener {

    private var selectedFoodSubstitution = FoodSubstitution.SIMILAR_SUBSTITUTION
    private var whoIsCollectingDetails: WhoIsCollectingDetails? = null
    private var shimmerComponentArray: List<Pair<ShimmerFrameLayout, View>> = ArrayList()
    private var navController: NavController? = null
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
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
        initializeCollectingFromView()
        initializeCollectingDetailsView()
        startShimmerView()
        stopShimmerView()
    }

    private fun startShimmerView() {

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

        txtNeedBags.visibility = View.GONE
        switchNeedBags.visibility = View.GONE

        val shimmer = Shimmer.AlphaHighlightBuilder().build()
        shimmerComponentArray.forEach {
            it.first.setShimmer(shimmer)
            it.first.startShimmer()
            it.second.visibility = View.INVISIBLE
        }
    }

    private fun stopShimmerView() {
        shimmerComponentArray.forEach {
            if (it.first.isShimmerStarted) {
                it.first.stopShimmer()
                it.first.setShimmer(null)
                it.second.visibility = View.VISIBLE
            }
        }

        txtNeedBags.visibility = View.VISIBLE
        switchNeedBags.visibility = View.VISIBLE

        initializeFoodSubstitution()
        initializeDeliveryInstructions()
    }

    private fun initializeCollectingFromView() {
        val location = Utils.getPreferredDeliveryLocation()
        checkoutCollectingFromLayout.setOnClickListener(this)
        if (location != null) {
            val selectedStore = if (location.storePickup) location.store.name else ""
            if (!selectedStore.isNullOrEmpty()) {
                tvNativeCheckoutDeliveringTitle?.text =
                    context?.getString(R.string.native_checkout_collecting_from)
                tvNativeCheckoutDeliveringValue.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                tvNativeCheckoutDeliveringValue?.text = selectedStore
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

    private fun initializeDeliveryInstructions() {
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
        if (WoolworthsApplication.getNativeCheckout()?.currentShoppingBag?.isEnabled == true) {
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
        } else if (WoolworthsApplication.getNativeCheckout()?.newShoppingBag?.isEnabled == true) {
            switchNeedBags?.visibility = View.GONE
            txtNeedBags?.visibility = View.GONE
            newShoppingBagsLayout?.visibility = View.VISIBLE
            addShoppingBagsRadioButtons()
        }
    }

    private fun addShoppingBagsRadioButtons() {
        txtNewShoppingBagsSubDesc?.visibility = View.VISIBLE
        val newShoppingBags = WoolworthsApplication.getNativeCheckout()?.newShoppingBag
        txtNewShoppingBagsDesc?.text = newShoppingBags?.title
        txtNewShoppingBagsSubDesc?.text = newShoppingBags?.description

        val shoppingBagsAdapter = ShoppingBagsRadioGroupAdapter(newShoppingBags?.options, this)
        shoppingBagsRecyclerView.apply {
            layoutManager = activity?.let { LinearLayoutManager(it) }
            shoppingBagsAdapter?.let { adapter = it }
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

    override fun selectedShoppingBagType(
        shoppingBagsOptionsList: ShoppingBagsOptions,
        position: Int
    ) {
//        selectedShoppingBagType = shoppingBagsOptionsList.shoppingBagType
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.checkoutCollectingFromLayout -> {
                val bundle = Bundle()
                bundle.putBoolean(KEY_IS_WHO_IS_COLLECTING, true)
                navController?.navigate(
                    R.id.action_checkoutReturningUserCollectionFragment_to_checkoutAddressConfirmationFragment,
                    bundle
                )
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
        }
    }
}