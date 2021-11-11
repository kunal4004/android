package za.co.woolworths.financial.services.android.ui.fragments.product.shop

import android.content.Intent
import android.graphics.Typeface.BOLD
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.delivering_to_collection_from.*
import kotlinx.android.synthetic.main.fragment_order_confirmation.*
import kotlinx.android.synthetic.main.order_details_bottom_sheet.*
import kotlinx.android.synthetic.main.other_order_details.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutActivity
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
import za.co.woolworths.financial.services.android.ui.adapters.ItemsOrderListAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.communicator.WrewardsBottomSheetFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.Utils


class OrderConfirmationFragment : Fragment() {

    private var itemsOrder: ArrayList<OrderItem>? = ArrayList(0)
    private var itemsOrderListAdapter: ItemsOrderListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
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
            deliveryCollectionDetailsConstraintLayout.visibility = View.VISIBLE
            if (response?.orderSummary?.store?.name != null) {
                optionImage.background =
                    AppCompatResources.getDrawable(it, R.drawable.icon_collection_grey_bg)
                optionTitle.text = it.getText(R.string.collecting_from)
                deliveryTextView.text = it.getText(R.string.collection_semicolon)
                optionLocation.text = response.orderSummary?.store?.name

            } else {
                optionImage.background =
                    AppCompatResources.getDrawable(it, R.drawable.icon_delivery_grey_bg)
                optionTitle.text = it.getText(R.string.delivering_to)
                deliveryTextView.text = it.getText(R.string.delivery_semicolon)
                optionLocation.text = response?.deliveryDetails?.shippingAddress?.address1
            }

            if (response?.deliveryDetails?.deliveryInfos?.size == 2) {
                oneDeliveryLinearLayout.visibility = View.GONE
                foodDeliveryLinearLayout.visibility = View.VISIBLE
                otherDeliveryLinearLayout.visibility = View.VISIBLE
                foodDeliveryDateTimeTextView.text = applyBoldBeforeComma(
                    response
                        .deliveryDetails?.deliveryInfos?.get(0)?.deliveryDateAndTime
                )
                otherDeliveryDateTimeTextView.text =
                    response.deliveryDetails?.deliveryInfos?.get(1)?.deliveryDateAndTime
            } else if (response?.deliveryDetails?.deliveryInfos?.size == 1) {
                oneDeliveryLinearLayout.visibility = View.VISIBLE
                foodDeliveryLinearLayout.visibility = View.GONE
                otherDeliveryLinearLayout.visibility = View.GONE
                deliveryDateTimeTextView.text =
                    response.deliveryDetails?.deliveryInfos?.get(0)?.deliveryDateAndTime
            }
        }
    }

    private fun setupOrderTotalDetails(response: SubmittedOrderResponse?) {
        otherOrderDetailsConstraintLayout.visibility = View.VISIBLE

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
            discountsLinearLayout.visibility = View.GONE
            discountsSeparator.visibility = View.GONE
        }

        val companyDiscount = response?.orderSummary?.discountDetails?.companyDiscount
        if (companyDiscount != null && companyDiscount > 0) {
            companyDiscountTextView.text = "- ".plus(
                CurrencyFormatter
                    .formatAmountToRandAndCentWithSpace(companyDiscount)
            )
        } else {
            companyDiscountLinearLayout.visibility = View.GONE
            companyDiscountSeparator.visibility = View.GONE
        }

        wRewardsVouchersLinearLayout?.visibility =
            if ((response?.orderSummary?.discountDetails?.voucherDiscount
                    ?: 0.0) > 0.0
            ) View.VISIBLE else View.GONE
        wRewardsVouchersTextView.text = CurrencyFormatter
            .formatAmountToRandAndCentWithSpace(response?.orderSummary?.discountDetails?.voucherDiscount)

        val totalDiscount = response?.orderSummary?.discountDetails?.totalDiscount
        if (totalDiscount != null && totalDiscount > 0) {
            totalDiscountTextView.text = "- ".plus(
                CurrencyFormatter
                    .formatAmountToRandAndCentWithSpace(totalDiscount)
            )
        } else {
            totalDiscountLinearLayout.visibility = View.GONE
            totalDiscountSeparator.visibility = View.GONE
        }

        deliveryFeeTextView.text = CurrencyFormatter
            .formatAmountToRandAndCentWithSpace(response?.deliveryDetails?.shippingAmount)

        if (response?.wfsCardDetails?.isWFSCardAvailable == false) {
            if (response.orderSummary?.discountDetails?.wrewardsDiscount!! > 0.0) {
                setMissedRewardsSavings(response.orderSummary?.discountDetails?.wrewardsDiscount!!)
            } else if (response.orderSummary?.savedAmount!! > 10) {
                setMissedRewardsSavings(response.orderSummary?.savedAmount!!.toDouble())
            }
        } else {
            missedRewardsLinearLayout.visibility = View.GONE
        }
    }

    private fun setupOrderDetailsBottomSheet(response: SubmittedOrderResponse?) {
        if (response?.orderSummary?.store?.name != null) {
            deliveryLocationText.text = context?.getText(R.string.collection_location_semicolon)
            deliveryOrderDetailsTextView.text = context?.getText(R.string.collection_semicolon)
        } else {
            deliveryLocationText.text = context?.getText(R.string.delivery_location_semicolon)
            deliveryOrderDetailsTextView.text = context?.getText(R.string.delivery_semicolon)
        }
        bottomSheetScrollView.visibility = View.VISIBLE
        orderStatusTextView.text = response?.orderSummary?.state
        deliveryLocationTextView.text = optionLocation.text

        if (response?.deliveryDetails?.deliveryInfos?.size == 2) {
            oneDeliveryBottomSheetLinearLayout.visibility = View.GONE
            foodDeliveryLinearLayout.visibility = View.VISIBLE
            otherDeliveryBottomSheetLinearLayout.visibility = View.VISIBLE
            foodDeliveryDateTimeBottomSheetTextView.text = applyBoldBeforeComma(
                response
                    .deliveryDetails?.deliveryInfos?.get(0)?.deliveryDateAndTime
            )
            otherDeliveryDateTimeBottomSheetTextView.text =
                response.deliveryDetails?.deliveryInfos?.get(1)?.deliveryDateAndTime
        } else if (response?.deliveryDetails?.deliveryInfos?.size == 1) {
            oneDeliveryBottomSheetLinearLayout.visibility = View.VISIBLE
            foodDeliveryBottomSheetLinearLayout.visibility = View.GONE
            otherDeliveryBottomSheetLinearLayout.visibility = View.GONE
            deliveryDateTimeBottomSheetTextView.text =
                response.deliveryDetails?.deliveryInfos?.get(0)?.deliveryDateAndTime
        }

        setNumberAndCostItemsBottomSheet(response?.items)

        initRecyclerView(response?.items)

        handleAddToShoppingListButton()
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
        numberItemsTextView.text = if (number > 1)
            bindString(R.string.number_items, number.toString())
        else
            bindString(R.string.number_item, number.toString())
        costItemsTextView.text = orderTotalTextView.text
    }

    private fun setMissedRewardsSavings(amount: Double) {
        missedRewardsLinearLayout.visibility = View.VISIBLE
        missedRewardsTextView.text = CurrencyFormatter
            .formatAmountToRandAndCentWithSpace(amount)

        wrewardsIconImageView.setOnClickListener {
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.CHECKOUT_MISSED_WREWARD_SAVINGS,
                activity
            )
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