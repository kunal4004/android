package za.co.woolworths.financial.services.android.ui.fragments.product.shop

import android.content.Intent
import android.graphics.Typeface.BOLD
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.*
import android.view.View.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.dash_order_details_layout.*
import kotlinx.android.synthetic.main.delivering_to_collection_from.*
import kotlinx.android.synthetic.main.delivering_to_collection_from.foodDeliveryDateTimeTextView
import kotlinx.android.synthetic.main.delivering_to_collection_from.foodDeliveryLinearLayout
import kotlinx.android.synthetic.main.delivering_to_collection_from.optionImage
import kotlinx.android.synthetic.main.delivering_to_collection_from.optionTitle
import kotlinx.android.synthetic.main.delivering_to_collection_from.otherDeliveryLinearLayout
import kotlinx.android.synthetic.main.delivering_to_dashing_from.*
import kotlinx.android.synthetic.main.fragment_order_confirmation.*
import kotlinx.android.synthetic.main.order_details_bottom_sheet.*
import kotlinx.android.synthetic.main.order_details_bottom_sheet.addShoppingListButton
import kotlinx.android.synthetic.main.order_details_bottom_sheet.itemsRecyclerView
import kotlinx.android.synthetic.main.other_order_details.*
import kotlinx.android.synthetic.main.product_details_fragment.*
import kotlinx.android.synthetic.main.product_details_size_and_color_layout.*
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
import za.co.woolworths.financial.services.android.ui.activities.CartCheckoutActivity
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.adapters.ItemsOrderListAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.communicator.WrewardsBottomSheetFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.wenum.Delivery


class OrderConfirmationFragment : Fragment() {

    private var itemsOrder: ArrayList<OrderItem>? = ArrayList(0)
    private var itemsOrderListAdapter: ItemsOrderListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_order_confirmation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getOrderDetails()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_item, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    private fun getOrderDetails() {
        OneAppService.getSubmittedOrder()
            .enqueue(CompletionHandler(object : IResponseListener<SubmittedOrderResponse> {
                override fun onSuccess(response: SubmittedOrderResponse?) {
                    when (response) {
                        is SubmittedOrderResponse -> {
                            when (response.httpCode) {
                                AppConstant.HTTP_OK, AppConstant.HTTP_OK_201 -> {
                                    response.orderSummary?.orderId?.let { setToolbar(it) }
                                    setupDeliveryOrCollectionDetails(response)
                                    setupOrderTotalDetails(response)
                                    setupOrderDetailsBottomSheet(response)
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

    private fun showErrorScreen(errorType: Int) {
        activity?.apply {
            val intent = Intent(this, ErrorHandlerActivity::class.java)
            intent.putExtra(ErrorHandlerActivity.ERROR_TYPE, errorType)
            startActivityForResult(intent, ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE)
        }
    }

    private fun setToolbar(orderId: String) {
        if (activity is CartCheckoutActivity) {
            (activity as? CartCheckoutActivity)?.apply {
                showTitleWithCrossButton(bindString(R.string.order_details_toolbar_title, orderId))
            }
        }
        (activity as? CheckoutActivity)?.apply {
            showTitleWithCrossButton(bindString(R.string.order_details_toolbar_title, orderId))
        }
    }

    private fun setupDeliveryOrCollectionDetails(response: SubmittedOrderResponse?) {
        context?.let {
            when (Delivery.getType(response?.orderSummary?.fulfillmentDetails?.deliveryType)) {
                Delivery.CNC -> {
                    deliveryCollectionDetailsConstraintLayout?.visibility = VISIBLE
                    deliveryOrderDetailsLayout.visibility = VISIBLE
                    optionImage.background =
                        AppCompatResources.getDrawable(it, R.drawable.icon_collection_grey_bg)
                    optionTitle?.text = it.getText(R.string.collecting_from)
                    deliveryTextView?.text = it.getText(R.string.collection_semicolon)
                    optionLocation?.text =
                        response?.orderSummary?.fulfillmentDetails?.storeName?.let { convertToTitleCase(it) } ?: ""
                }
                Delivery.STANDARD -> {
                    deliveryCollectionDetailsConstraintLayout?.visibility = VISIBLE
                    deliveryOrderDetailsLayout?.visibility = VISIBLE
                    optionImage?.background =
                        AppCompatResources.getDrawable(it, R.drawable.icon_delivery_grey_bg)
                    optionTitle?.text = it.getText(R.string.delivering_to)
                    deliveryTextView?.text = it.getText(R.string.delivery_semicolon)
                    optionLocation?.text =
                        response?.orderSummary?.fulfillmentDetails?.address?.address1?.let { convertToTitleCase(it) } ?: ""
                }
                Delivery.DASH -> {
                    dashDeliveryConstraintLayout?.visibility = VISIBLE
                    deliveryOrderDetailsLayout?.visibility = GONE
                    dashOrderDetailsLayout?.visibility = VISIBLE
                    val dashLocation = response?.orderSummary?.fulfillmentDetails?.address?.address1?.let { convertToTitleCase(it) }
                            ?: ""
                    val dashAddressName =
                            SpannableString(
                                    "$dashLocation " + requireContext().getString(R.string.bullet) + "" + " " + "$dashLocation "
                            )
                    optionLocationTitle?.text = dashAddressName

                    dashFoodDeliveryDateTimeTextView?.text = applyBoldBeforeComma(
                            response
                                    ?.deliveryDetails?.deliveryInfos?.get(0)?.deliveryDateAndTime
                    )
                    continueBrowsingLinearLayout.setOnClickListener {
                        (requireActivity() as? BottomNavigator)?.navigateToTabIndex(
                                BottomNavigationActivity.INDEX_PRODUCT,
                                null
                        )
                    }
                    setUpDashOrderDetailsLayout(response)
                }
            }

            if (response?.deliveryDetails?.deliveryInfos?.size == 2) {
                oneDeliveryLinearLayout?.visibility = GONE
                foodDeliveryLinearLayout?.visibility = VISIBLE
                otherDeliveryLinearLayout?.visibility = VISIBLE
                foodDeliveryDateTimeTextView?.text = applyBoldBeforeComma(
                    response
                        .deliveryDetails?.deliveryInfos?.get(0)?.deliveryDateAndTime
                )
                otherDeliveryDateTimeTextView?.text =
                    response.deliveryDetails?.deliveryInfos?.get(1)?.deliveryDateAndTime
            } else if (response?.deliveryDetails?.deliveryInfos?.size == 1) {
                oneDeliveryLinearLayout?.visibility = VISIBLE
                foodDeliveryLinearLayout?.visibility = GONE
                otherDeliveryLinearLayout?.visibility = GONE
                deliveryDateTimeTextView?.text =
                    response.deliveryDetails?.deliveryInfos?.get(0)?.deliveryDateAndTime
            }
        }
    }
    private fun setupOrderTotalDetails(response: SubmittedOrderResponse?) {

        otherOrderDetailsConstraintLayout?.visibility = VISIBLE

        orderTotalTextView?.text = CurrencyFormatter
                .formatAmountToRandAndCentWithSpace(response?.orderSummary?.total)

        yourCartTextView?.text = CurrencyFormatter
                .formatAmountToRandAndCentWithSpace(response?.orderSummary?.basketTotal)

        val otherDiscount = response?.orderSummary?.discountDetails?.otherDiscount
        if (otherDiscount != null && otherDiscount >= 0) {
            discountsTextView?.text = "- ".plus(
                    CurrencyFormatter
                            .formatAmountToRandAndCentWithSpace(otherDiscount)
            )
        }

        val totalDiscount = response?.orderSummary?.discountDetails?.totalDiscount
        if (totalDiscount != null && totalDiscount >= 0) {
            totalDiscountTextView?.text = "- ".plus(
                    CurrencyFormatter
                            .formatAmountToRandAndCentWithSpace(totalDiscount)
            )
        }

        // Commenting this Till Jan-2022 Release as per WOP-13825
        /*if (response?.wfsCardDetails?.isWFSCardAvailable == false) {
            if (response.orderSummary?.discountDetails?.wrewardsDiscount!! > 0.0) {
                setMissedRewardsSavings(response.orderSummary?.discountDetails?.wrewardsDiscount!!)
            } else if (response.orderSummary?.savedAmount!! > 10) {
                setMissedRewardsSavings(response.orderSummary?.savedAmount!!.toDouble())
            }
        } else {*/
        missedRewardsLinearLayout?.visibility = GONE
        //}

        when (Delivery.getType(response?.orderSummary?.fulfillmentDetails?.deliveryType)) {
            Delivery.STANDARD -> {
                driverTipLinearLayout.visibility = GONE
                driverTipSeparator.visibility = GONE

                val companyDiscount = response?.orderSummary?.discountDetails?.companyDiscount
                if (companyDiscount != null && companyDiscount > 0) {
                    companyDiscountTextView?.text = "- ".plus(CurrencyFormatter.formatAmountToRandAndCentWithSpace(companyDiscount))
                } else {
                    companyDiscountLinearLayout?.visibility = GONE
                    companyDiscountSeparator?.visibility = GONE
                }

                wRewardsVouchersLinearLayout?.visibility =
                        if ((response?.orderSummary?.discountDetails?.voucherDiscount
                                        ?: 0.0) > 0.0) VISIBLE else GONE
                wRewardsVouchersTextView?.text = CurrencyFormatter
                        .formatAmountToRandAndCentWithSpace(response?.orderSummary?.discountDetails?.voucherDiscount)
                deliveryFeeTextView?.text = CurrencyFormatter
                        .formatAmountToRandAndCentWithSpace(response?.deliveryDetails?.shippingAmount)
            }
            Delivery.CNC -> {
                driverTipLinearLayout.visibility = GONE
                driverTipSeparator.visibility = GONE

                val companyDiscount = response?.orderSummary?.discountDetails?.companyDiscount
                if (companyDiscount != null && companyDiscount > 0) {
                    companyDiscountTextView?.text = "- ".plus(CurrencyFormatter.formatAmountToRandAndCentWithSpace(companyDiscount))
                } else {
                    companyDiscountLinearLayout?.visibility = GONE
                    companyDiscountSeparator?.visibility = GONE
                }

                wRewardsVouchersLinearLayout?.visibility = if ((response?.orderSummary?.discountDetails?.voucherDiscount
                                ?: 0.0) > 0.0) VISIBLE else GONE
                wRewardsVouchersTextView?.text = CurrencyFormatter
                        .formatAmountToRandAndCentWithSpace(response?.orderSummary?.discountDetails?.voucherDiscount)
                deliveryFeeTextView?.text = CurrencyFormatter
                        .formatAmountToRandAndCentWithSpace(response?.deliveryDetails?.shippingAmount)

            }
            Delivery.DASH -> {
                companyDiscountLinearLayout.visibility = GONE
                companyDiscountSeparator?.visibility = GONE
                wRewardsVouchersLinearLayout.visibility = GONE
                wRewardsVouchersSeparator
                deliveryFeeTextView?.text = CurrencyFormatter.formatAmountToRandAndCentWithSpace(response?.deliveryDetails?.shippingAmount)
                driverTipTextView.text = CurrencyFormatter
                        .formatAmountToRandAndCentWithSpace(0.00)
            }
            else -> {
            }
        }
    }

    private fun setUpDashOrderDetailsLayout(response: SubmittedOrderResponse?) {
        setFoodItemCount(response?.items)

        initRecyclerView(response?.items)

        handleAddToShoppingListButton()
    }

    private fun setFoodItemCount(items: OrderItems?) {
        val food: Int = items?.food?.size ?: 0
        val number: Int = food
        foodNumberItemsTextView?.text = if (number > 1)
            bindString(R.string.food_number_items, number.toString())
        else
            bindString(R.string.food_number_item, number.toString())
    }

    private fun setupOrderDetailsBottomSheet(response: SubmittedOrderResponse?) {

        when (Delivery.getType(response?.orderSummary?.fulfillmentDetails?.deliveryType)) {
            Delivery.CNC -> {
                deliveryLocationText?.text =
                        context?.getText(R.string.collection_location_semicolon)
                deliveryOrderDetailsTextView?.text = context?.getText(R.string.collection_semicolon)
                setNumberAndCostItemsBottomSheet(response?.items)
                initRecyclerView(response?.items)
                handleAddToShoppingListButton()
            }
            Delivery.STANDARD -> {
                deliveryLocationText?.text = context?.getText(R.string.delivery_location_semicolon)
                deliveryOrderDetailsTextView?.text = context?.getText(R.string.delivery_semicolon)
                setNumberAndCostItemsBottomSheet(response?.items)
                initRecyclerView(response?.items)
                handleAddToShoppingListButton()
            }
            else -> {
            }
        }

        bottomSheetScrollView?.visibility = VISIBLE
        orderStatusTextView?.text = response?.orderSummary?.state
        deliveryLocationTextView?.text = optionLocation.text?.let { convertToTitleCase(it as String) }

        if (response?.deliveryDetails?.deliveryInfos?.size == 2) {
            oneDeliveryBottomSheetLinearLayout?.visibility = GONE
            foodDeliveryLinearLayout?.visibility = VISIBLE
            otherDeliveryBottomSheetLinearLayout?.visibility = VISIBLE
            foodDeliveryDateTimeBottomSheetTextView?.text = applyBoldBeforeComma(
                    response
                            .deliveryDetails?.deliveryInfos?.get(0)?.deliveryDateAndTime
            )
            otherDeliveryDateTimeBottomSheetTextView?.text =
                    response.deliveryDetails?.deliveryInfos?.get(1)?.deliveryDateAndTime
        } else if (response?.deliveryDetails?.deliveryInfos?.size == 1) {
            oneDeliveryBottomSheetLinearLayout?.visibility = VISIBLE
            foodDeliveryBottomSheetLinearLayout?.visibility = GONE
            otherDeliveryBottomSheetLinearLayout?.visibility = GONE
            deliveryDateTimeBottomSheetTextView?.text =
                    response.deliveryDetails?.deliveryInfos?.get(0)?.deliveryDateAndTime
        }


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

        addShoppingListButton.setOnClickListener {
            NavigateToShoppingList.openShoppingList(activity, listOfItems, "", false)
        }
    }

    private fun initRecyclerView(items: OrderItems?) {

        initialiseItemsOrder(items)

        if (itemsOrder.isNullOrEmpty()) {
            return
        }

        context?.let {
            itemsRecyclerView.layoutManager = LinearLayoutManager(it, RecyclerView.VERTICAL, false)
            itemsOrderListAdapter = ItemsOrderListAdapter(itemsOrder!!)
        }
        itemsRecyclerView.adapter = itemsOrderListAdapter
    }

    private fun initialiseItemsOrder(items: OrderItems?) {
        if (items?.other != null && items.other!!.isNotEmpty()) {
            itemsOrder?.addAll(items.other!!)
        }
        if (items?.food != null && items.food!!.isNotEmpty()) {
            itemsOrder?.addAll(items.food!!)
        }
    }

    private fun setNumberAndCostItemsBottomSheet(items: OrderItems?) {
        val other: Int = items?.other?.size ?: 0
        val food: Int = items?.food?.size ?: 0
        val number: Int = other.plus(food)
        numberItemsTextView?.text = if (number > 1)
            bindString(R.string.number_items, number.toString())
        else
            bindString(R.string.number_item, number.toString())
        costItemsTextView?.text = orderTotalTextView.text
    }

    private fun setMissedRewardsSavings(amount: Double) {
        missedRewardsLinearLayout?.visibility = VISIBLE
        missedRewardsTextView?.text = CurrencyFormatter
            .formatAmountToRandAndCentWithSpace(amount)

        wrewardsIconImageView?.setOnClickListener {
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.CHECKOUT_MISSED_WREWARD_SAVINGS,
                hashMapOf(
                    FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                            FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_WREWARDS_SAVING
                ),
                activity)
            val bottomSheetFragment = WrewardsBottomSheetFragment(activity)

            val bundle = Bundle()
            bundle.putString(WrewardsBottomSheetFragment.TAG, missedRewardsTextView.text.toString())
            bottomSheetFragment.arguments = bundle

            activity?.supportFragmentManager?.let { supportFragmentManager ->
                bottomSheetFragment.show(supportFragmentManager, WrewardsBottomSheetFragment.TAG)
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
}