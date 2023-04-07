package za.co.woolworths.financial.services.android.ui.fragments.product.shop

import android.content.Intent
import android.graphics.Typeface.BOLD
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.content.res.AppCompatResources
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Typeface
import androidx.fragment.app.viewModels
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentOrderConfirmationBinding
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.checkout.view.CheckoutActivity
import za.co.woolworths.financial.services.android.common.convertToTitleCase
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.cart.OrderItem
import za.co.woolworths.financial.services.android.models.dto.cart.OrderItems
import za.co.woolworths.financial.services.android.models.dto.cart.SubmittedOrderResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.viewmodel.OrderConfirmationViewModel
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.adapters.ItemsOrderListAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.communicator.WrewardsBottomSheetFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding
import za.co.woolworths.financial.services.android.util.voc.VoiceOfCustomerManager
import za.co.woolworths.financial.services.android.util.wenum.Delivery

@AndroidEntryPoint
class OrderConfirmationFragment :
    BaseFragmentBinding<FragmentOrderConfirmationBinding>(FragmentOrderConfirmationBinding::inflate) {

    private var itemsOrder: ArrayList<OrderItem>? = ArrayList(0)
    private var cncFoodItemsOrder: ArrayList<OrderItem>? = ArrayList(0)
    private var cncOtherItemsOrder: ArrayList<OrderItem>? = ArrayList(0)
    private var cncFoodItemsOrderListAdapter: ItemsOrderListAdapter? = null
    private var cncOtherItemsOrderListAdapter: ItemsOrderListAdapter? = null
    private var itemsOrderListAdapter: ItemsOrderListAdapter? = null
    private var isPurchaseEventTriggered: Boolean = false
    private val orderConfirmationViewModel: OrderConfirmationViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getOrderDetails()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_item, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    private fun getOrderDetails() {
        OneAppService().getSubmittedOrder()
            .enqueue(CompletionHandler(object : IResponseListener<SubmittedOrderResponse> {
                override fun onSuccess(response: SubmittedOrderResponse?) {
                    when (response) {
                        is SubmittedOrderResponse -> {
                            when (response.httpCode) {
                                AppConstant.HTTP_OK, AppConstant.HTTP_OK_201 -> {
                                    response.orderSummary?.orderId?.let { setToolbar(it) }
                                    setupDeliveryOrCollectionDetails(response)
                                    setupOrderTotalDetails(response)
                                    displayVocifNeeded(response)
                                    if (!isPurchaseEventTriggered)
                                    {
                                        showPurchaseEvent(response)
                                        isPurchaseEventTriggered = false
                                    }

                                    //Make this call to recommendation API after receiving the 200 or 201 from the order
                                    orderConfirmationViewModel.submitRecommendationsOnOrderResponse(response)
                                }
                                else -> {
                                    showErrorScreen(ErrorHandlerActivity.ERROR_TYPE_SUBMITTED_ORDER)
                                }
                            }
                        }
                        else -> {
                            showErrorScreen(ErrorHandlerActivity.ERROR_TYPE_SUBMITTED_ORDER)
                        }
                    }
                }

                override fun onFailure(error: Throwable?) {
                    showErrorScreen(ErrorHandlerActivity.ERROR_TYPE_SUBMITTED_ORDER)
                }
            }, SubmittedOrderResponse::class.java))
    }

    private fun showPurchaseEvent(response: SubmittedOrderResponse) {
        val purchaseItemParams = Bundle()
        purchaseItemParams.putString(
            FirebaseAnalytics.Param.CURRENCY,
            FirebaseManagerAnalyticsProperties.PropertyValues.CURRENCY_VALUE
        )
        purchaseItemParams.putString(
            FirebaseAnalytics.Param.AFFILIATION,
            FirebaseManagerAnalyticsProperties.PropertyValues.AFFILIATION_VALUE
        )
        purchaseItemParams.putString(
            FirebaseAnalytics.Param.TRANSACTION_ID,
            response.orderSummary?.orderId
        )
        response.orderSummary?.total?.let {
            purchaseItemParams.putDouble(
                FirebaseAnalytics.Param.VALUE,
                it
            )
        }
        purchaseItemParams.putString(
            FirebaseAnalytics.Param.SHIPPING,
            response.deliveryDetails?.shippingAmount.toString()
        )

        val purchaseItem = Bundle()
        purchaseItem.putString(
            FirebaseAnalytics.Param.ITEM_ID,
            response.items?.other?.get(0)?.commerceItemInfo?.productId
        )
        purchaseItem.putString(
            FirebaseAnalytics.Param.ITEM_NAME,
            response.items?.other?.get(0)?.commerceItemInfo?.productDisplayName
        )
        purchaseItem.putString(
            FirebaseAnalytics.Param.QUANTITY,
            response.items?.other?.get(0)?.commerceItemInfo?.quantity.toString()
        )
        response.items?.other?.get(0)?.priceInfo?.amount?.let {
            purchaseItem.putDouble(
                FirebaseAnalytics.Param.PRICE,
                it
            )
        }
        purchaseItem.putString(
            FirebaseAnalytics.Param.ITEM_VARIANT,
            response.items?.other?.get(0)?.color
        )
        purchaseItemParams.putParcelableArray(FirebaseAnalytics.Param.ITEMS, arrayOf(purchaseItem))

        AnalyticsManager.logEvent(FirebaseManagerAnalyticsProperties.PURCHASE, purchaseItemParams)
    }

    private fun displayVocifNeeded(response: SubmittedOrderResponse) {
        var deliveryType = response.orderSummary?.fulfillmentDetails?.deliveryType
        VoiceOfCustomerManager().showVocSurveyIfNeeded(
            activity,
            KotlinUtils.vocShoppingHandling(deliveryType)
        )
        if (Delivery.getType(deliveryType) == Delivery.CNC) {
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.SHOP_Click_Collect_CConfirm,
                activity
            )
        }
    }

    private fun showErrorScreen(errorType: Int) {
        activity?.apply {
            val intent = Intent(this, ErrorHandlerActivity::class.java)
            intent.putExtra(ErrorHandlerActivity.ERROR_TYPE, errorType)
            startActivityForResult(intent, ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE)
        }
    }

    private fun setToolbar(orderId: String) {
        binding.apply {
            orderIdText.text = bindString(R.string.order_details_toolbar_title, orderId)
            btnClose.setOnClickListener { requireActivity().onBackPressed() }
            helpTextView.setOnClickListener {
                findNavController()?.navigate(R.id.action_OrderConfirmationFragment_to_helpAndSupportFragment)
            }
        }
    }

    private fun setupDeliveryOrCollectionDetails(response: SubmittedOrderResponse?) {
        context?.let {
            when (Delivery.getType(response?.orderSummary?.fulfillmentDetails?.deliveryType)) {
                Delivery.CNC -> {
                    binding.deliveryCollectionDetailsConstraintLayout.root.visibility = VISIBLE
                    binding.dashOrderDetailsLayout.root.visibility = VISIBLE
                        binding.deliveryCollectionDetailsConstraintLayout.apply {
                            val other: Int = response?.items?.other?.size ?: 0
                            val food: Int = response?.items?.food?.size ?: 0
                            if (other > 0 && food == 0) {
                                deliveryTextView.text = it.getString(R.string.collection_semicolon)
                                infoDeliveryDateTimeTextView.visibility = VISIBLE
                                binding.dashOrderDetailsLayout.root.visibility = GONE
                            } else {
                                deliveryTextView.text = it.getString(R.string.food_items_semicolon)
                                infoDeliveryDateTimeTextView.visibility = GONE
                            }
                            optionImage.background =
                                AppCompatResources.getDrawable(it, R.drawable.ic_collection_bag)
                            optionLocation.text =
                                response?.orderSummary?.fulfillmentDetails?.storeName?.let {
                                    convertToTitleCase(it)
                                } ?: ""
                            standardEnroutetextView.text = it.getString(R.string.collection_status)
                            collectedOrDeliveredTextView.text =
                                it.getText(R.string.status_collected)
                            optionTitle.text = it.getString(R.string.collecting_from)
                            setUpCncOrderDetailsLayout(response)
                            continueBrowsingStandardLinearLayout.setOnClickListener {
                                requireActivity().setResult(CheckOutFragment.REQUEST_CHECKOUT_ON_CONTINUE_SHOPPING)
                                requireActivity().finish()
                            }
                        }
                }
                Delivery.STANDARD -> {
                    binding.deliveryCollectionDetailsConstraintLayout.root.visibility = VISIBLE
                    binding.dashOrderDetailsLayout.root.visibility = VISIBLE
                    binding.deliveryCollectionDetailsConstraintLayout.apply {
                        deliveryTextView.text = it.getString(R.string.delivery_semicolon)
                        optionImage.background =
                            AppCompatResources.getDrawable(
                                it,
                                R.drawable.ic_icon_standard_delivery_truck
                            )
                        val address =  response?.orderSummary?.fulfillmentDetails?.address?.address1?.let {
                            convertToTitleCase(it)
                        } ?: ""

                        val formattedNickNameWithAddress = KotlinUtils.getFormattedNickName(
                            response?.orderSummary?.fulfillmentDetails?.address?.nickname, address, activity
                        )

                        formattedNickNameWithAddress.append(address)

                        optionLocation.text = formattedNickNameWithAddress
                        optionTitle.text = it.getString(R.string.delivering_to)
                        continueBrowsingStandardLinearLayout.setOnClickListener {
                            requireActivity()?.setResult(CheckOutFragment.REQUEST_CHECKOUT_ON_CONTINUE_SHOPPING)
                            requireActivity()?.finish()
                        }
                        standardEnroutetextView.text = it.getText(R.string.dash_status_en_route)
                        collectedOrDeliveredTextView.text =
                            it.getText(R.string.dash_status_delivered)
                    }
                    setUpStandardOrderDetailsLayout(response)

                }
                Delivery.DASH -> {
                    binding.dashDeliveryConstraintLayout.root.visibility = VISIBLE
                    binding.dashOrderDetailsLayout.root.visibility = VISIBLE
                    binding.dashDeliveryConstraintLayout.apply {
                        optionImage.background =
                            AppCompatResources.getDrawable(
                                it,
                                R.drawable.icon_dash_delivery_scooter
                            )
                        optionTitle.text = it.getString(R.string.dashing_to)

                        val address =  response?.orderSummary?.fulfillmentDetails?.address?.address1?.let {
                            convertToTitleCase(it)
                        } ?: ""

                        val formattedNickNameWithAddress = KotlinUtils.getFormattedNickName(
                            response?.orderSummary?.fulfillmentDetails?.address?.nickname,
                            address,
                            activity
                        )

                        formattedNickNameWithAddress.append(address)

                        optionLocationTitle.text = formattedNickNameWithAddress

                        dashFoodDeliveryDateTimeTextView?.text = applyBoldBeforeComma(
                            response
                                ?.deliveryDetails?.deliveryInfos?.get(0)?.deliveryDateAndTime
                        )
                        continueBrowsingLinearLayout.setOnClickListener {
                            requireActivity()?.setResult(CheckOutFragment.REQUEST_CHECKOUT_ON_CONTINUE_SHOPPING)
                            requireActivity()?.finish()
                        }
                    }
                    setUpDashOrderDetailsLayout(response)
                }
                else -> {}
            }

            binding.deliveryCollectionDetailsConstraintLayout.apply {
                if (response?.deliveryDetails?.deliveryInfos?.size == 2) {
                    oneDeliveryLinearLayout.visibility = GONE
                    foodDeliveryLinearLayout.visibility = VISIBLE
                    otherDeliveryLinearLayout.visibility = VISIBLE
                    otherDeliveryDateTimeTextView.visibility = VISIBLE
                    foodDeliveryDateTimeTextView.text = applyBoldBeforeComma(
                        response
                            .deliveryDetails?.deliveryInfos?.getOrNull(0)?.deliveryDateAndTime
                    )
                    otherDeliveryDateTimeTextView.text = applyBoldBeforeComma(
                        response.deliveryDetails?.deliveryInfos?.getOrNull(1)?.deliveryDateAndTime
                    )
                    if(Delivery.getType(response?.orderSummary?.fulfillmentDetails?.deliveryType)==Delivery.CNC){
                        otherDeliveryDateTimeTextView.text = applyBoldBeforeComma(
                            response.deliveryDetails?.deliveryInfos?.getOrNull(1)?.time
                        )
                        otherDeliveryDateTimeTextView.setTypeface(otherDeliveryDateTimeTextView.typeface, Typeface.BOLD);
                        infoDeliveryDateTimeTextView.visibility = VISIBLE
                    }
                } else if (response?.deliveryDetails?.deliveryInfos?.size == 1) {
                    oneDeliveryLinearLayout.visibility = VISIBLE
                    foodDeliveryLinearLayout.visibility = GONE
                    otherDeliveryLinearLayout.visibility = GONE
                    deliveryDateTimeTextView.text = applyBoldBeforeComma(
                        response
                            .deliveryDetails?.deliveryInfos?.get(0)?.deliveryDateAndTime
                    )
                    val other: Int = response.items?.other?.size ?: 0
                    val food: Int = response.items?.food?.size ?: 0
                    if(other>0 && food==0){
                        deliveryDateTimeTextView.text = applyBoldBeforeComma(
                            response
                                .deliveryDetails?.deliveryInfos?.getOrNull(0)?.time
                        )
                        deliveryDateTimeTextView.setTypeface(deliveryDateTimeTextView.typeface, Typeface.BOLD);
                        infoDeliveryDateTimeTextView.visibility = VISIBLE
                    }
                }
            }
        }
    }

    private fun setupOrderTotalDetails(response: SubmittedOrderResponse?) {

        binding.otherOrderDetailsConstraintLayout.root.visibility = VISIBLE
        binding.otherOrderDetailsConstraintLayout.apply {
            orderTotalTextView.text = CurrencyFormatter
                .formatAmountToRandAndCentWithSpace(response?.orderSummary?.total)

            yourCartTextView.text = CurrencyFormatter
                .formatAmountToRandAndCentWithSpace(response?.orderSummary?.basketTotal)

            val otherDiscount = response?.orderSummary?.discountDetails?.otherDiscount
            if (otherDiscount != null && otherDiscount > 0) {
                discountsTextView.text = "- ".plus(
                    CurrencyFormatter
                        .formatAmountToRandAndCentWithSpace(otherDiscount)
                )
            } else {
                discountsLinearLayout.visibility = GONE
                discountsSeparator.visibility = GONE
            }

            val totalDiscount = response?.orderSummary?.discountDetails?.totalDiscount
            if (totalDiscount != null && totalDiscount > 0) {
                totalDiscountTextView?.text = "- ".plus(
                    CurrencyFormatter
                        .formatAmountToRandAndCentWithSpace(totalDiscount)
                )
            } else {
                totalDiscountLinearLayout.visibility = GONE
                totalDiscountSeparator.visibility = GONE
            }

            val cashVoucherApplied = response?.orderSummary?.cashVoucherApplied
            if (cashVoucherApplied != null && cashVoucherApplied > 0) {
                quarterlyVoucherText?.text = "- ".plus(
                    CurrencyFormatter
                        .formatAmountToRandAndCentWithSpace(cashVoucherApplied)
                )
            } else {
                quarterlyVoucherLinearLayout.visibility = GONE
                quarterlyVoucherSeparator.visibility = GONE
            }


            // Commenting this Till Jan-2022 Release as per WOP-13825
            /*if (response?.wfsCardDetails?.isWFSCardAvailable == false) {
                if (response.orderSummary?.discountDetails?.wrewardsDiscount!! > 0.0) {
                    setMissedRewardsSavings(response.orderSummary?.discountDetails?.wrewardsDiscount!!)
                } else if (response.orderSummary?.savedAmount!! > 10) {
                    setMissedRewardsSavings(response.orderSummary?.savedAmount!!.toDouble())
                }
            } else {*/
            missedRewardsLinearLayout.visibility = GONE
            //}

            when (Delivery.getType(response?.orderSummary?.fulfillmentDetails?.deliveryType)) {
                Delivery.STANDARD -> {
                    driverTipLinearLayout.visibility = GONE
                    driverTipSeparator.visibility = GONE
                    deliveryFeeLabel.text = context?.getString(R.string.delivery_fee)

                    val companyDiscount = response?.orderSummary?.discountDetails?.companyDiscount
                    if (companyDiscount != null && companyDiscount > 0) {
                        companyDiscountTextView.text =
                            "- ".plus(
                                CurrencyFormatter.formatAmountToRandAndCentWithSpace(
                                    companyDiscount
                                )
                            )
                    } else {
                        companyDiscountLinearLayout.visibility = GONE
                        companyDiscountSeparator.visibility = GONE
                    }
                    val wRewardsVouchers = response?.orderSummary?.discountDetails?.voucherDiscount
                        ?: 0.0
                    if (wRewardsVouchers > 0.0) {
                        wRewardsVouchersTextView.text = CurrencyFormatter
                            .formatAmountToRandAndCentWithSpace(response?.orderSummary?.discountDetails?.voucherDiscount)
                    } else {
                        wRewardsVouchersLinearLayout.visibility = GONE
                        wRewardsVouchersSeparator.visibility = GONE
                    }

                    deliveryFeeTextView.text = CurrencyFormatter
                        .formatAmountToRandAndCentWithSpace(response?.deliveryDetails?.shippingAmount)
                }
                Delivery.CNC -> {
                    driverTipLinearLayout.visibility = GONE
                    driverTipSeparator.visibility = GONE
                    deliveryFeeLabel.text = context?.getString(R.string.collection_fee)

                    val companyDiscount = response?.orderSummary?.discountDetails?.companyDiscount
                    if (companyDiscount != null && companyDiscount > 0) {
                        companyDiscountTextView.text =
                            "- ".plus(
                                CurrencyFormatter.formatAmountToRandAndCentWithSpace(
                                    companyDiscount
                                )
                            )
                    } else {
                        companyDiscountLinearLayout.visibility = GONE
                        companyDiscountSeparator.visibility = GONE
                    }

                    val wRewardsVouchers = response?.orderSummary?.discountDetails?.voucherDiscount
                        ?: 0.0
                    if (wRewardsVouchers > 0.0) {
                        wRewardsVouchersTextView.text = CurrencyFormatter
                            .formatAmountToRandAndCentWithSpace(response?.orderSummary?.discountDetails?.voucherDiscount)
                    } else {
                        wRewardsVouchersLinearLayout.visibility = GONE
                        wRewardsVouchersSeparator.visibility = GONE
                    }

                    val deliveryFee = response?.deliveryDetails?.shippingAmount
                    if (deliveryFee != null && deliveryFee > 0.0) {
                        deliveryFeeTextView.text = CurrencyFormatter
                            .formatAmountToRandAndCentWithSpace(deliveryFee)
                    } else {
                        deliveryFeeLinearLayout.visibility = GONE
                        deliveryFeeSeparator.visibility = GONE
                    }
                }
                Delivery.DASH -> {
                    companyDiscountLinearLayout.visibility = GONE
                    companyDiscountSeparator.visibility = GONE
                    wRewardsVouchersLinearLayout.visibility = GONE
                    wRewardsVouchersSeparator.visibility = GONE
                    deliveryFeeLabel.text = context?.getString(R.string.delivery_fee)
                    deliveryFeeTextView.text =
                        CurrencyFormatter.formatAmountToRandAndCentWithSpace(response?.deliveryDetails?.shippingAmount)

                    val driverTip = response?.orderSummary?.tip ?: 0.00
                    if (driverTip > 0) {
                        driverTipTextView.text =
                            CurrencyFormatter
                                .formatAmountToRandAndCentWithSpace(driverTip)
                    } else {
                        driverTipLinearLayout.visibility = GONE
                        driverTipSeparator.visibility = GONE
                    }
                }
                else -> {
                }
            }
        }
    }

    private fun setUpDashOrderDetailsLayout(response: SubmittedOrderResponse?) {
        setFoodItemCount(response?.items)

        initRecyclerView(response?.items)

        handleAddToShoppingListButton()
    }
    private fun setUpCncOrderDetailsLayout(response: SubmittedOrderResponse?) {
        setCncItemCount(response?.items)

        initCncRecyclerView(response?.items)

        initCncFoodRecyclerView()

        handleAddToShoppingListButtonFromCNC()
    }

    private fun setCncItemCount(items: OrderItems?) {
        val other: Int = items?.other?.size ?: 0
        val food: Int = items?.food?.size ?: 0
        binding.dashOrderDetailsLayout.foodNumberItemsTextView.text = if (food > 1)
            bindString(R.string.food_number_items, food.toString())
        else
            bindString(R.string.food_number_item, food.toString())

        binding.cncOrderDetailsLayout.foodNumberItemsTextView.text = if (other > 1)
            bindString(R.string.fashion_items, other.toString())
        else
            bindString(R.string.fashion_item, other.toString())
    }

    private fun setFoodItemCount(items: OrderItems?) {
        val other: Int = items?.other?.size ?: 0
        val food: Int = items?.food?.size ?: 0
        val number: Int = other.plus(food)
        binding.dashOrderDetailsLayout.foodNumberItemsTextView.text = if (number > 1)
            bindString(R.string.food_number_items, number.toString())
        else
            bindString(R.string.food_number_item, number.toString())
    }

    private fun handleAddToShoppingListButton() {
        if (itemsOrder.isNullOrEmpty()) {
            return
        }
        val listOfItems = ArrayList<AddToListRequest>()
        itemsOrder!!.forEach {
            val item = AddToListRequest()
            item.apply {
                quantity = it.quantity.toString()
                catalogRefId = it.catalogRefId
                giftListId = ""
                skuID = ""
            }
            listOfItems.add(item)
        }

        binding.dashOrderDetailsLayout.addShoppingListButton.setOnClickListener {
            NavigateToShoppingList.openShoppingList(activity, listOfItems, "", false)
        }
    }

    private fun initRecyclerView(items: OrderItems?) {
        initialiseItemsOrder(items)
        if (itemsOrder.isNullOrEmpty()) {
            return
        }
        context?.let {
            binding.dashOrderDetailsLayout.itemsRecyclerView.layoutManager =
                LinearLayoutManager(it, RecyclerView.VERTICAL, false)
            itemsOrderListAdapter = ItemsOrderListAdapter(itemsOrder!!)
        }
        binding.dashOrderDetailsLayout.itemsRecyclerView.adapter = itemsOrderListAdapter
    }

    private fun initCncRecyclerView(items: OrderItems?) {
        initialiseCncItemsOrder(items)
        if (cncOtherItemsOrder.isNullOrEmpty()) {
            return
        }
        binding.cncOrderDetailsLayout.root.visibility = VISIBLE
        context?.let {
            binding.cncOrderDetailsLayout.itemsRecyclerView.layoutManager =
                LinearLayoutManager(it, RecyclerView.VERTICAL, false)
            cncFoodItemsOrderListAdapter = ItemsOrderListAdapter(cncOtherItemsOrder!!)
        }
        binding.cncOrderDetailsLayout.itemsRecyclerView.adapter = cncFoodItemsOrderListAdapter
    }

    private fun initCncFoodRecyclerView() {
        if (cncFoodItemsOrder.isNullOrEmpty()) {
            return
        }
        context?.let {
            binding.dashOrderDetailsLayout.itemsRecyclerView.layoutManager =
                LinearLayoutManager(it, RecyclerView.VERTICAL, false)
            cncOtherItemsOrderListAdapter = ItemsOrderListAdapter(cncFoodItemsOrder!!)
        }
        binding.dashOrderDetailsLayout.itemsRecyclerView.adapter = cncOtherItemsOrderListAdapter
    }

    private fun initialiseItemsOrder(items: OrderItems?) {
        if (!items?.other.isNullOrEmpty()) {
            itemsOrder?.addAll(items?.other!!)
        }
        if (!items?.food.isNullOrEmpty()) {
            itemsOrder?.addAll(items?.food!!)
        }
    }

    private fun initialiseCncItemsOrder(items: OrderItems?) {
        if (!items?.other.isNullOrEmpty()) {
            cncOtherItemsOrder?.addAll(items?.other!!)
        }
        if (!items?.food.isNullOrEmpty()) {
            cncFoodItemsOrder?.addAll(items?.food!!)
        }
    }

    private fun setUpStandardOrderDetailsLayout(response: SubmittedOrderResponse?) {
        setNumberAndCostItemsBottomSheet(response?.items)

        initRecyclerView(response?.items)

        handleAddToShoppingListButton()
    }

    private fun setNumberAndCostItemsBottomSheet(items: OrderItems?) {
        val other: Int = items?.other?.size ?: 0
        val food: Int = items?.food?.size ?: 0
        val number: Int = other.plus(food)
        binding.dashOrderDetailsLayout.foodNumberItemsTextView.text = if (number > 1)
            bindString(R.string.number_items, number.toString())
        else
            bindString(R.string.number_item, number.toString())
    }

    private fun setMissedRewardsSavings(amount: Double) {
        binding.otherOrderDetailsConstraintLayout.apply {
            missedRewardsLinearLayout.visibility = VISIBLE
            missedRewardsTextView.text = CurrencyFormatter
                .formatAmountToRandAndCentWithSpace(amount)
            wrewardsIconImageView.setOnClickListener {
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.CHECKOUT_MISSED_WREWARD_SAVINGS,
                    hashMapOf(
                        FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_WREWARDS_SAVING
                    ),
                    activity
                )
                val bottomSheetFragment = WrewardsBottomSheetFragment(activity)

                val bundle = Bundle()
                bundle.putString(
                    WrewardsBottomSheetFragment.TAG,
                    missedRewardsTextView.text.toString()
                )
                bottomSheetFragment.arguments = bundle
                activity?.supportFragmentManager?.let { supportFragmentManager ->
                    bottomSheetFragment.show(
                        supportFragmentManager,
                        WrewardsBottomSheetFragment.TAG
                    )
                }
            }
        }
    }

    private fun applyBoldBeforeComma(deliveryDateAndTime: String?): Spannable {
        val splitDateTime = deliveryDateAndTime?.split(",", ignoreCase = false, limit = 2)
        val wordSpan: Spannable = SpannableString(deliveryDateAndTime)

        if (!splitDateTime.isNullOrEmpty() &&
            splitDateTime.size == 2
        ) {
            wordSpan.setSpan(
                StyleSpan(BOLD),
                0,
                splitDateTime[0].length + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return wordSpan
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE -> {
                when (resultCode) {
                    ErrorHandlerActivity.RESULT_RETRY -> {
                        getOrderDetails()
                    }
                    ErrorHandlerActivity.RESULT_CALL_CENTER -> {
                        (activity as? CheckoutActivity)?.onBackPressed()
                    }
                }
            }
        }
    }

    private fun handleAddToShoppingListButtonFromCNC() {
        handleCncFoodAddToList()
        handleCncOtherAddToList()

    }

    private fun handleCncFoodAddToList() {
        if (cncFoodItemsOrder.isNullOrEmpty()) {
            return
        }
        val listOfItems = ArrayList<AddToListRequest>()
        cncFoodItemsOrder!!.forEach {
            val item = AddToListRequest()
            item.apply {
                quantity = it.quantity.toString()
                catalogRefId = it.catalogRefId
                giftListId = ""
                skuID = ""
            }
            listOfItems.add(item)
        }

        binding.dashOrderDetailsLayout.addShoppingListButton.setOnClickListener {
            NavigateToShoppingList.openShoppingList(activity, listOfItems, "", false)
        }
    }

    private fun handleCncOtherAddToList() {
        if (cncOtherItemsOrder.isNullOrEmpty()) {
            return
        }
        val listOfItems = ArrayList<AddToListRequest>()
        cncOtherItemsOrder!!.forEach {
            val item = AddToListRequest()
            item.apply {
                quantity = it.quantity.toString()
                catalogRefId = it.catalogRefId
                giftListId = ""
                skuID = ""
            }
            listOfItems.add(item)
        }

        binding.cncOrderDetailsLayout.addShoppingListButton.setOnClickListener {
            NavigateToShoppingList.openShoppingList(activity, listOfItems, "", false)
        }
    }
}