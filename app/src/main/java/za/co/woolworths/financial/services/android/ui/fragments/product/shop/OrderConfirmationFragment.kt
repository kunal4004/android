package za.co.woolworths.financial.services.android.ui.fragments.product.shop

import android.graphics.Typeface.BOLD
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.delivering_to_collection_from.*
import kotlinx.android.synthetic.main.fragment_order_confirmation.*
import kotlinx.android.synthetic.main.order_details_bottom_sheet.*
import kotlinx.android.synthetic.main.other_order_details.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutActivity
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.cart.OrderItem
import za.co.woolworths.financial.services.android.models.dto.cart.OrderItems
import za.co.woolworths.financial.services.android.models.dto.cart.SubmittedOrderResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.adapters.ItemsOrderListAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.communicator.WrewardsBottomSheetFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList
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

    private fun getOrderDetails() {
        /*OneAppService.getSubmittedOrder()
            .enqueue(CompletionHandler(object : IResponseListener<SubmittedOrderResponse> {
                override fun onSuccess(response: SubmittedOrderResponse?) {
                    response?.orderSummary?.orderId?.let { setToolbar(it) }
                    setupDeliveryOrCollectionDetails(response)
                    setupOrderTotalDetails(response)
                    setupOrderDetailsBottomSheet(response)
                }

                override fun onFailure(error: Throwable?) {
                    //TODO: handle error
                }
            }, SubmittedOrderResponse::class.java))*/


        //use mock json file.
        val jsonFileString = Utils.getJsonDataFromAsset(
            activity?.applicationContext,
            "mocks/submittedOrder.json"
        )
        val mockSubmittedOrder: SubmittedOrderResponse = Gson().fromJson(
            jsonFileString,
            object : TypeToken<SubmittedOrderResponse>() {}.type
        )
        mockSubmittedOrder?.orderSummary?.orderId?.let { setToolbar(it) }
        setupDeliveryOrCollectionDetails(mockSubmittedOrder)
        setupOrderTotalDetails(mockSubmittedOrder)
        setupOrderDetailsBottomSheet(mockSubmittedOrder)
    }

    private fun setToolbar(orderId: String) {
        if (activity is CheckoutActivity) {
            (activity as? CheckoutActivity)?.apply {
                showTitleWithCrossButton(bindString(R.string.order_details_toolbar_title, orderId))
            }
        }
    }

    private fun setupDeliveryOrCollectionDetails(response: SubmittedOrderResponse?) {
        context?.let {
            deliveryCollectionDetailsConstraintLayout.visibility = View.VISIBLE
            if (response?.orderSummary?.store?.name != null) {
                optionImage.background =
                    AppCompatResources.getDrawable(it, R.drawable.icon_collection_grey_bg)
                optionTitle.text = it.getText(R.string.collecting_from)
                optionLocation.text = response.orderSummary?.store?.name

            } else {
                optionImage.background =
                    AppCompatResources.getDrawable(it, R.drawable.icon_delivery_grey_bg)
                optionTitle.text = it.getText(R.string.delivering_to)
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
                    applyBoldBeforeComma(response.deliveryDetails?.deliveryInfos?.get(0)?.deliveryDateAndTime)
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
                applyBoldBeforeComma(response.deliveryDetails?.deliveryInfos?.get(0)?.deliveryDateAndTime)
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
        if (items?.other?.size!! > 0) {
            itemsOrder?.addAll(items.other!!)
        }
        if (items.food?.size!! > 0) {
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
            val bottomSheetFragment = WrewardsBottomSheetFragment()

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
}