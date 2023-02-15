package za.co.woolworths.financial.services.android.cart.view

import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter
import za.co.woolworths.financial.services.android.cart.service.network.CartItemGroup
import za.co.woolworths.financial.services.android.cart.viewmodel.CartUtils.Companion.getAppliedVouchersCount
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton.liquor
import za.co.woolworths.financial.services.android.models.AppConfigSingleton.lowStock
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.VoucherDetails
import za.co.woolworths.financial.services.android.models.service.event.ProductState
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList.Companion.openShoppingList
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.util.CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.ImageManager.Companion.setPicture
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.capitaliseFirstLetter
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import java.util.*


class CartProductAdapter(
    var cartItems: ArrayList<CartItemGroup>?,
    private val onItemClick: OnItemClick,
    orderSummary: OrderSummary?,
    context: Activity?,
    voucherDetails: VoucherDetails?,
    liquorCompliance: LiquorCompliance?,
) : RecyclerSwipeAdapter<RecyclerView.ViewHolder>() {
    private val DISABLE_VIEW_VALUE = 0.5f
    private val GIFT_ITEM = "GIFT"
    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.swipe
    }

    enum class CartRowType(val value: Int) {
        HEADER(0), PRODUCT(1), PRICES(2), GIFT(3);
    }

    interface OnItemClick {
        fun onItemDeleteClickInEditMode(commerceId: CommerceItem)
        fun onChangeQuantity(commerceId: CommerceItem, quantity: Int)
        fun totalItemInBasket(total: Int)
        fun onOpenProductDetail(commerceItem: CommerceItem)
        fun onViewVouchers()
        fun onViewCashBackVouchers()
        fun updateOrderTotal()
        fun onGiftItemClicked(commerceItem: CommerceItem)
        fun onEnterPromoCode()
        fun onRemovePromoCode(promoCode: String)
        fun onPromoDiscountInfo()
        fun onItemDeleteClick(commerceId: CommerceItem)
        fun onCheckBoxChange(isChecked: Boolean, commerceItem: CommerceItem)
    }

    private var editMode = false
    private var firstLoadCompleted = false
    private var orderSummary: OrderSummary? = null
    private var liquorComplianceInfo: LiquorCompliance?
    private val mContext: Activity?
    private var voucherDetails: VoucherDetails?
    private var isQuantityUploading = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            CartRowType.HEADER.value -> {
                CartHeaderViewHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.cart_product_header_item, parent, false))
            }
            CartRowType.PRODUCT.value -> {
                ProductHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.layout_cart_list_product_item, parent, false))
            }
            CartRowType.GIFT.value -> {
                GiftProductHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.cart_gift_item, parent, false))
            }
            else -> {
                CartPricesViewHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.cart_product_basket_prices, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemRow = getItemTypeAtPosition(position)
        when (itemRow.rowType) {
            CartRowType.HEADER -> {
                val headerHolder = holder as CartHeaderViewHolder
                val commerceItems = itemRow.commerceItems
                headerHolder.tvHeaderTitle.setText(mContext?.resources?.getQuantityString(
                    R.plurals.category_item,
                    commerceItems?.size ?: 0,
                    commerceItems?.size ?: 0,
                    capitaliseFirstLetter(itemRow.category)))
                headerHolder.addToListListener(commerceItems)
                if (itemRow.category?.uppercase(Locale.getDefault())
                        .equals(GIFT_ITEM, ignoreCase = true)
                ) {
                    headerHolder.tvAddToList.visibility = GONE
                } else {
                    headerHolder.tvAddToList.visibility = VISIBLE
                    headerHolder.tvAddToList.visibility =
                        if (editMode) INVISIBLE else VISIBLE
                }
            }
            CartRowType.PRODUCT -> {
                val productHolder = holder as ProductHolder
                val commerceItem = itemRow.commerceItem ?: return
                productHolder.swipeLayout.apply {
                    isRightSwipeEnabled = !editMode
                    addDrag(SwipeLayout.DragEdge.Right,
                        productHolder.swipeRight)
                    if (!editMode) close(true, true)
                }
                val param = productHolder.clCartItems.layoutParams as ViewGroup.MarginLayoutParams
                param.marginStart =
                    if (editMode) mContext?.resources?.getDimension(R.dimen.sixty_dp)?.toInt()
                        ?: 0 else mContext?.resources?.getDimension(R.dimen.twenty_four_dp)?.toInt()
                        ?: 0
                productHolder.clCartItems.layoutParams = param
                val commerceItemInfo: CommerceItemInfo? = commerceItem.commerceItemInfo
                //setListCheckBoxVisibility(editMode, productHolder)
                productHolder.tvTitle.setText(if (commerceItemInfo == null) "" else commerceItemInfo.getProductDisplayName())
                Utils.truncateMaxLine(productHolder.tvTitle)
                productHolder.quantity.setText(commerceItemInfo?.getQuantity()?.toString() ?: "")
                productHolder.price.setText(formatAmountToRandAndCentWithSpace(commerceItem.getPriceInfo()
                    .getAmount()))
                val productImageUrl =
                    if (commerceItemInfo == null) "" else commerceItemInfo.externalImageRefV2
                setPicture(productHolder.productImage, productImageUrl)
                productHolder.btnDeleteRow.visibility = if (editMode) VISIBLE else GONE
                productHolder.rlDeleteButton.visibility = if (editMode) VISIBLE else GONE
                val quantityIsLoading = commerceItem.quantityUploading

                // prevent triggering animation on first load
                if (firstLoadWasCompleted()) animateOnDeleteButtonVisibility(productHolder.clCartItems,
                    editMode)

                productHolder.pbQuantity.visibility =
                    if (quantityIsLoading) VISIBLE else GONE
                productHolder.quantity.visibility =
                    if (quantityIsLoading) GONE else VISIBLE

                //Set Promotion Text START
                if (commerceItem.getPriceInfo().discountedAmount > 0) {
                    productHolder.promotionalText.setText(" " + formatAmountToRandAndCentWithSpace(
                        commerceItem.getPriceInfo().discountedAmount))
                    productHolder.llPromotionalText.visibility = VISIBLE
                    mContext?.let {
                        productHolder.promotionalText.setTextColor(
                            ContextCompat.getColor(it, R.color.promotional_text_red)
                        )
                        productHolder.price.setTextColor(ContextCompat.getColor(it,
                            R.color.black))
                    }
                } else {
                    productHolder.llPromotionalText.visibility = GONE
                    mContext?.let {
                        productHolder.price.setTextColor(ContextCompat.getColor(it,
                            R.color.black))
                    }
                }
                //Set Promotion Text END

                // Set Color and Size START
                if (itemRow.category.equals("FOOD", ignoreCase = true)) {
                    productHolder.tvColorSize.visibility = INVISIBLE
                } else {
                    val sizeColor = getSizeColor(commerceItemInfo)
                    productHolder.tvColorSize.setText(sizeColor)
                    productHolder.tvColorSize.visibility = VISIBLE
                }
                // Set Color and Size END
                productHolder.pbQuantity.indeterminateDrawable.setColorFilter(Color.BLACK,
                    PorterDuff.Mode.MULTIPLY)
                productHolder.llQuantity.alpha =
                    if (commerceItem.isStockChecked) 1.0f else DISABLE_VIEW_VALUE
                if (commerceItem.isStockChecked) {
                    productHolder.llQuantity.alpha =
                        if (commerceItem.quantityInStock == 0) 0.0f else 1.0f
                    productHolder.tvProductAvailability.visibility =
                        if (commerceItem.quantityInStock == 0) VISIBLE else GONE
                    Utils.setBackgroundColor(productHolder.tvProductAvailability,
                        R.drawable.round_amber_corner,
                        R.string.out_of_stock)
                    when (commerceItem.quantityInStock) {
                        0 -> {
                            productHolder.llPromotionalText.visibility = GONE
                            productHolder.price.visibility = VISIBLE
                        }
                        -1 -> {
                            productHolder.llQuantity.alpha = DISABLE_VIEW_VALUE
                            productHolder.price.visibility = VISIBLE
                            productHolder.llQuantity.isEnabled = false
                            productHolder.quantity.alpha = DISABLE_VIEW_VALUE
                        }
                        else -> {
                            productHolder.price.visibility = VISIBLE
                            showMinusOrDeleteButton(productHolder, commerceItem)
                        }
                    }
                } else {
                    productHolder.llQuantity.visibility = VISIBLE
                    productHolder.tvProductAvailability.visibility = GONE
                }
                productHolder.btnDeleteRow.setOnClickListener {
                    setFirstLoadCompleted(false)
                    commerceItem.commerceItemDeletedId(commerceItem)
                    commerceItem.setDeleteIconWasPressed(true)
                    onRemoveSingleItemInEditMode(productHolder, commerceItem)
                }
                productHolder.cbShoppingList.setOnCheckedChangeListener { button, isChecked ->
                    commerceItem.isDeletePressed = isChecked
                    onItemClick.onCheckBoxChange(isChecked, commerceItem)
                }
                productHolder.addCountImageLayout.setOnClickListener {
                    if (commerceItem.quantityInStock == 0) return@setOnClickListener
                    if (!NetworkManager.getInstance().isConnectedToNetwork(mContext)) {
                        ErrorHandlerView(mContext).showToast()
                        return@setOnClickListener
                    }
                    val userQuantity = commerceItem.commerceItemInfo?.getQuantity() ?: 0
                    if (userQuantity < commerceItem.quantityInStock) {
                        commerceItem.quantityUploading = true
                        setFirstLoadCompleted(false)
                        onItemClick.onChangeQuantity(commerceItem, userQuantity + 1)
                    }
                }
                productHolder.minusDeleteCountImageLayout.setOnClickListener {
                    if (commerceItem.quantityInStock == 0) return@setOnClickListener
                    val userQuantity = commerceItem.commerceItemInfo?.getQuantity() ?: 0
                    if (userQuantity > 1) {
                        // This will reduce the product quantity.
                        commerceItem.quantityUploading = true
                        setFirstLoadCompleted(false)
                        onItemClick.onChangeQuantity(commerceItem, userQuantity - 1)
                    } else if (userQuantity == 1) {
                        // This will remove the product
                        commerceItem.commerceItemDeletedId(commerceItem)
                        commerceItem.isDeletePressed = true
                        onRemoveSingleItem(productHolder, commerceItem)
                    }
                }
                productHolder.swipeRight.setOnClickListener {
                    commerceItem.commerceItemDeletedId(commerceItem)
                    commerceItem.isDeletePressed = true
                    onRemoveSingleItem(productHolder, commerceItem)
                }
                productHolder.productImage.setOnClickListener {
                    onItemClick.onOpenProductDetail(commerceItem)
                }
                productHolder.tvTitle.setOnClickListener {
                    onItemClick.onOpenProductDetail(commerceItem)
                }
                if (commerceItem.lowStockThreshold > commerceItem.quantityInStock && commerceItem.quantityInStock > 0 && lowStock?.isEnabled == true) {
                    showLowStockIndicator(productHolder)
                }
                mItemManger.bindView(productHolder.itemView, position)

                //enable/disable change quantity click
                if (editMode) {
                    productHolder.llQuantity.isEnabled = !editMode
                    Utils.fadeInFadeOutAnimation(productHolder.llQuantity, editMode)
                    disableItemClickListener(productHolder)
                } else if (quantityIsLoading) {
                    productHolder.llQuantity.isEnabled = false
                    Utils.fadeInFadeOutAnimation(productHolder.llQuantity, true)
                    disableItemClickListener(productHolder)
                } else if (isQuantityUploading) {
                    disableItemClickListener(productHolder)
                } else {
                    productHolder.llQuantity.isEnabled = true
                    enableItemClickListener(productHolder)
                }
            }
            CartRowType.GIFT -> {
                val giftProductHolder = holder as GiftProductHolder
                val giftCommerceItem = itemRow.commerceItem ?: return
                val giftCommerceItemInfo = giftCommerceItem.commerceItemInfo
                val imageUrl =
                    if (giftCommerceItemInfo == null) "" else giftCommerceItemInfo.externalImageRefV2
                setPicture(giftProductHolder.giftItemImageView, imageUrl)
                giftProductHolder.productNameTextView.text =
                    giftCommerceItemInfo?.getProductDisplayName()
                Utils.truncateMaxLine(giftProductHolder.productNameTextView)
                val sizeColor = getSizeColor(giftCommerceItemInfo)
                giftProductHolder.brandProductDescriptionTextView.text = sizeColor
                giftProductHolder.giftRootContainerConstraintLayout.setOnClickListener {
                    onItemClick.onGiftItemClicked(giftCommerceItem)
                }
            }
            CartRowType.PRICES -> {
                val priceHolder = holder as CartPricesViewHolder
                if (orderSummary != null) {
                    priceHolder.orderSummeryLayout.visibility = VISIBLE
                    orderSummary?.basketTotal?.let {
                        setPriceValue(priceHolder.txtYourCartPrice, it)
                    }
                    priceHolder.orderTotal.text = formatAmountToRandAndCentWithSpace(
                        orderSummary?.total)
                    val discountDetails = orderSummary?.discountDetails
                    if (discountDetails != null) {

                        if (discountDetails.companyDiscount > 0) {
                            setDiscountPriceValue(priceHolder.txtCompanyDiscount,
                                discountDetails.companyDiscount)
                            priceHolder.rlCompanyDiscount.visibility = VISIBLE
                        } else {
                            priceHolder.rlCompanyDiscount.visibility = GONE
                        }
                        if (discountDetails.totalOrderDiscount > 0) {
                            setDiscountPriceValue(priceHolder.txtTotalDiscount,
                                discountDetails.totalOrderDiscount)
                            priceHolder.rlTotalDiscount.visibility = VISIBLE
                        } else {
                            priceHolder.rlTotalDiscount.visibility = GONE
                        }
                        if (discountDetails.otherDiscount > 0) {
                            setDiscountPriceValue(priceHolder.txtDiscount,
                                discountDetails.otherDiscount)
                            priceHolder.rlDiscount.visibility = VISIBLE
                        } else {
                            priceHolder.rlDiscount.visibility = GONE
                        }
                        if (discountDetails.voucherDiscount > 0) {
                            setDiscountPriceValue(priceHolder.txtWrewardsDiscount,
                                discountDetails.voucherDiscount)
                            priceHolder.rlWrewardsDiscount.visibility = VISIBLE
                        } else {
                            priceHolder.rlWrewardsDiscount.visibility = GONE
                        }
                        if (discountDetails.promoCodeDiscount > 0) {
                            setDiscountPriceValue(priceHolder.txtPromoCodeDiscount,
                                discountDetails.promoCodeDiscount)
                            priceHolder.rlPromoCodeDiscount.visibility = VISIBLE
                        } else {
                            priceHolder.rlPromoCodeDiscount.visibility = GONE
                        }
                    }
                } else {
                    priceHolder.orderSummeryLayout.visibility = GONE
                }
                priceHolder.rlAvailableWRewardsVouchers.setOnClickListener {
                    onItemClick.onViewVouchers()
                    Utils.triggerFireBaseEvents(if (appliedVouchersCount > 0) FirebaseManagerAnalyticsProperties.Cart_ovr_edit else FirebaseManagerAnalyticsProperties.Cart_ovr_view,
                        mContext)
                }
                priceHolder.rlAvailableCashVouchers?.setOnClickListener {
                    onItemClick.onViewCashBackVouchers()
                    Utils.triggerFireBaseEvents(
                        if (appliedVouchersCount > 0) FirebaseManagerAnalyticsProperties.Cart_ovr_edit else FirebaseManagerAnalyticsProperties.Cart_ovr_view,
                        mContext)
                }

                if (voucherDetails == null) {
                    return
                }
                val activeCashVouchersCount = voucherDetails?.let {
                    it.activeCashVouchersCount
                }
                if (activeCashVouchersCount!=null && activeCashVouchersCount > 0) {
                    val availableVouchersLabel =
                        mContext?.resources?.getQuantityString(R.plurals.available_cash_vouchers_message,
                            activeCashVouchersCount,
                            activeCashVouchersCount)
                    priceHolder.availableCashVouchersCount.text = availableVouchersLabel
                    priceHolder.viewCashVouchers.isEnabled = true
                    priceHolder.rlAvailableCashVouchers.isClickable = true
                } else {
                    priceHolder.availableCashVouchersCount.text =
                        mContext?.getString(R.string.zero_cash_vouchers_available)
                    priceHolder.viewCashVouchers.isEnabled = false
                    priceHolder.rlAvailableCashVouchers.isClickable = false
                }

                val activeVouchersCount = voucherDetails?.let {
                    it.activeVouchersCount
                }
                if (activeVouchersCount != null && activeVouchersCount > 0) {
                    if (appliedVouchersCount > 0) {
                        val availableVouchersLabel =
                            mContext?.resources?.getQuantityString(R.plurals._rewards_vouchers_message_applied,
                                appliedVouchersCount,
                                appliedVouchersCount)
                        priceHolder.availableVouchersCount.text = availableVouchersLabel
                        priceHolder.viewVouchers.text = mContext?.getString(R.string.edit)
                        priceHolder.viewVouchers.isEnabled = true
                        priceHolder.rlAvailableWRewardsVouchers.isClickable = true
                    } else {
                        val availableVouchersLabel =
                            mContext?.resources?.getQuantityString(R.plurals.available_rewards_vouchers_message,
                                activeVouchersCount,
                                activeVouchersCount)
                        priceHolder.availableVouchersCount.text = availableVouchersLabel
                        priceHolder.viewVouchers.text = mContext?.getString(R.string.view)
                        priceHolder.viewVouchers.isEnabled = true
                        priceHolder.rlAvailableWRewardsVouchers.isClickable = true
                    }
                } else {
                    priceHolder.availableVouchersCount.text =
                        mContext?.getString(R.string.zero_wrewards_vouchers_available)
                    priceHolder.viewVouchers.text = mContext?.getString(R.string.view)
                    priceHolder.viewVouchers.isEnabled = false
                    priceHolder.rlAvailableWRewardsVouchers.isClickable = false

                }
                priceHolder.promoCodeAction.text =
                    mContext?.getString(if (voucherDetails?.promoCodes != null && voucherDetails!!.promoCodes.size > 0) R.string.remove else R.string.enter)
                if (voucherDetails!!.promoCodes != null && voucherDetails!!.promoCodes.size > 0) {
                    val appliedPromoCodeText =
                        mContext?.getString(R.string.promo_code_applied) + voucherDetails!!.promoCodes[0].promoCode
                    priceHolder.promoCodeLabel.text = appliedPromoCodeText
                } else {
                    priceHolder.promoCodeLabel.text =
                        mContext?.getString(R.string.do_you_have_a_promo_code)
                }
                priceHolder.rlPromoCode.setOnClickListener {
                    if (voucherDetails!!.promoCodes != null && voucherDetails!!.promoCodes.size > 0) onItemClick.onRemovePromoCode(
                        voucherDetails!!.promoCodes[0].promoCode) else onItemClick.onEnterPromoCode()
                }
                priceHolder.promoDiscountInfo.setOnClickListener { onItemClick.onPromoDiscountInfo() }
                if (liquorComplianceInfo != null && liquorComplianceInfo!!.isLiquorOrder) {
                    priceHolder.liquorBannerRootConstraintLayout.visibility = VISIBLE
                    if (!liquor?.noLiquorImgUrl.isNullOrEmpty()) setPicture(priceHolder.imgLiBanner,
                        liquor?.noLiquorImgUrl)
                } else {
                    priceHolder.liquorBannerRootConstraintLayout.visibility = GONE
                }
                if (KotlinUtils.getPreferredDeliveryType() == Delivery.CNC) {
                    priceHolder.deliveryFee.text = mContext?.getString(R.string.collection_fee)
                }
            }
        }
    }

    /**
     * This method used to show low stock indicator
     * When lowStockThreshold > quantity
     *
     * @param productHolder
     */
    private fun showLowStockIndicator(productHolder: ProductHolder) {
        productHolder.cartLowStock.visibility = VISIBLE
        productHolder.txtCartLowStock.text = lowStock?.lowStockCopy
    }

    private fun showMinusOrDeleteButton(productHolder: ProductHolder, commerceItem: CommerceItem) {
        val userQuantity = commerceItem.commerceItemInfo?.getQuantity() ?: 0
        productHolder.llQuantity.visibility =
            if (commerceItem.quantityInStock == 0) GONE else VISIBLE
        productHolder.minusDeleteCountImage.setImageResource(
            if (userQuantity == 1
            ) R.drawable.delete_24 else R.drawable.ic_minus_black
        )
        val padding = productHolder.minusDeleteCountImage.context
            .resources.getDimension(if (userQuantity == 1) R.dimen.six_dp else R.dimen.ten_dp)
            .toInt()
        productHolder.minusDeleteCountImage.setPadding(padding, padding, padding, padding)
        productHolder.addCountImageLayout.visibility = if (commerceItem.quantityInStock == 1 ||
            userQuantity == commerceItem.quantityInStock
        ) INVISIBLE else VISIBLE
    }

    private fun disableItemClickListener(productHolder: ProductHolder) {
        productHolder.clCartItems.isClickable = false
        productHolder.minusDeleteCountImageLayout.isClickable = false
        productHolder.addCountImageLayout.isClickable = false
    }

    private fun enableItemClickListener(productHolder: ProductHolder) {
        productHolder.clCartItems.isClickable = true
        productHolder.minusDeleteCountImageLayout.isClickable = true
        productHolder.addCountImageLayout.isClickable = true
    }

    private fun getSizeColor(commerceItemInfo: CommerceItemInfo?): String? {
        var sizeColor = if (commerceItemInfo == null) "" else commerceItemInfo.color
        if (sizeColor == null) sizeColor = ""
        if (commerceItemInfo != null) {
            if (sizeColor.isEmpty() && commerceItemInfo.size.isNotEmpty() && !commerceItemInfo.size.equals(
                    "NO SZ",
                    ignoreCase = true)
            ) sizeColor =
                commerceItemInfo.size else if (sizeColor.isNotEmpty() && commerceItemInfo.size.isNotEmpty() && !commerceItemInfo.size.equals(
                    "NO SZ",
                    ignoreCase = true)
            ) sizeColor = sizeColor + ", " + commerceItemInfo.size
        }
        return sizeColor
    }

    private fun setListCheckBoxVisibility(visibility: Boolean, productHolder: ProductHolder) {
        val param = productHolder.clCartItems.layoutParams as ViewGroup.MarginLayoutParams
        if (visibility) {
            productHolder.cbShoppingList.visibility = VISIBLE
            param.marginStart = mContext?.resources?.getDimension(R.dimen.fifty_dp)?.toInt() ?: 0
            productHolder.clCartItems.layoutParams = param
        } else {
            productHolder.cbShoppingList.visibility = GONE
            param.marginStart = mContext?.resources?.getDimension(R.dimen.eighteen_dp)?.toInt() ?: 0
            productHolder.clCartItems.layoutParams = param
        }
    }

    private fun onRemoveSingleItemInEditMode(
        productHolder: ProductHolder,
        commerceItem: CommerceItem,
    ) {
        if (editMode) {
            if (commerceItem.deleteIconWasPressed()) {
                val animateRowToDelete =
                    AnimationUtils.loadAnimation(mContext, R.anim.animate_layout_delete)
                animateRowToDelete.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {
                        //setListCheckBoxVisibility(!commerceItem.deleteIconWasPressed(), productHolder)
                        onItemClick.onItemDeleteClickInEditMode(commerceItem.deletedCommerceItemId)
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })
                productHolder.clCartItems.startAnimation(animateRowToDelete)
            }
        }
    }

    private fun onRemoveSingleItem(productHolder: ProductHolder, commerceItem: CommerceItem) {
        if (commerceItem.isDeletePressed) {
            val animateRowToDelete =
                AnimationUtils.loadAnimation(mContext, R.anim.animate_layout_delete)
            animateRowToDelete.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    onItemClick.onItemDeleteClick(commerceItem.deletedCommerceItemId)
                }

                override fun onAnimationEnd(animation: Animation) {
                }

                override fun onAnimationRepeat(animation: Animation) {
                }
            })
            productHolder.clCartItems.startAnimation(animateRowToDelete)
        }
    }

    private fun setPriceValue(textView: WTextView, value: Double) {
        textView.setText(formatAmountToRandAndCentWithSpace(value))
    }

    private fun setDiscountPriceValue(textView: WTextView, value: Double) {
        textView.setText("- " + formatAmountToRandAndCentWithSpace(value))
    }

    override fun getItemCount(): Int {
        var size = cartItems?.size ?: 0
        if (!cartItems.isNullOrEmpty()) {
            for (collection in cartItems!!) {
                size += collection.getCommerceItems().size
            }
        }
        return if (editMode) {
            // returns sum of headers + product items
            size
        } else {
            // returns sum of headers + product items + last row for prices
            size + 1
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItemTypeAtPosition(position).rowType.value
    }

    private fun getItemTypeAtPosition(position: Int): CartCommerceItemRow {
        var currentPosition = 0
        if (!cartItems.isNullOrEmpty()) {
            for (entry in cartItems!!) {
                if (currentPosition == position) {
                    return CartCommerceItemRow(
                        CartRowType.HEADER,
                        entry.type,
                        null,
                        entry.getCommerceItems())
                }

                // increment position for header
                currentPosition++
                val productCollection = entry.commerceItems
                currentPosition += if (position > currentPosition + productCollection.size - 1) {
                    productCollection.size
                } else {
                    return if (entry.type.equals(GIFT_ITEM, ignoreCase = true)) CartCommerceItemRow(
                        CartRowType.GIFT,
                        entry.type,
                        productCollection[position - currentPosition],
                        null) else CartCommerceItemRow(
                        CartRowType.PRODUCT,
                        entry.type,
                        productCollection[position - currentPosition],
                        null)
                }
            }
        }
        // last row is for prices
        return CartCommerceItemRow(CartRowType.PRICES, null, null, null)
    }

    fun toggleEditMode(): Boolean {
        editMode = !editMode
        toggleFirstLoad()
        notifyDataSetChanged()
        return editMode
    }

    private fun toggleFirstLoad(): Boolean {
        setFirstLoadCompleted(true)
        return firstLoadCompleted
    }

    fun clear() {
        cartItems?.clear()
        orderSummary = null
        notifyDataSetChanged()
    }

    private inner class CartHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHeaderTitle: WTextView
        val tvAddToList: WTextView
        fun addToListListener(commerceItems: ArrayList<CommerceItem>?) {
            tvAddToList.setOnClickListener {
                val woolworthsApplication = WoolworthsApplication.getInstance()
                if (woolworthsApplication != null) {
                    woolworthsApplication.wGlobalState.selectedSKUId = null
                }
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYCARTADDTOLIST,
                    mContext)
                val addToListRequests = ArrayList<AddToListRequest>()
                if (!commerceItems.isNullOrEmpty()) {
                    for (commerceItem in commerceItems!!) {
                        val listItem = AddToListRequest()
                        val commerceItemInfo = commerceItem.commerceItemInfo
                        listItem.catalogRefId = commerceItemInfo.catalogRefId
                        listItem.skuID = commerceItemInfo.catalogRefId
                        listItem.giftListId = commerceItemInfo.catalogRefId
                        listItem.quantity = "1"
                        addToListRequests.add(listItem)
                    }
                }
                openShoppingList(mContext, addToListRequests, "", false)
            }
        }

        init {
            tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle)
            tvAddToList = view.findViewById(R.id.tvAddToList)
        }
    }

    inner class ProductHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: WTextView
        val tvColorSize: WTextView
        val quantity: WTextView
        val price: WTextView
        val promotionalText: WTextView
        val btnDeleteRow: ImageView
        val llQuantity: LinearLayout
        val productImage: ImageView
        val clCartItems: ConstraintLayout
        val llPromotionalText: LinearLayout
        private val tvDelete: WTextView
        val pbQuantity: ProgressBar
        val rlDeleteButton: RelativeLayout
        val tvProductAvailability: TextView
        val swipeLayout: SwipeLayout
        val cartLowStock: View
        val txtCartLowStock: TextView
        val minusDeleteCountImage: ImageView
        val minusDeleteCountImageLayout: RelativeLayout
        val addCountImageLayout: RelativeLayout
        val cbShoppingList: CheckBox
        val pbLoadProduct: ProgressBar
        val swipeRight: RelativeLayout

        init {
            tvTitle = view.findViewById(R.id.tvTitle)
            tvColorSize = view.findViewById(R.id.tvColorSize)
            quantity = view.findViewById(R.id.tvQuantity)
            price = view.findViewById(R.id.tvPrice)
            btnDeleteRow = view.findViewById(R.id.btnDeleteRow)
            productImage = view.findViewById(R.id.cartProductImage)
            llQuantity = view.findViewById(R.id.llQuantity)
            pbQuantity = view.findViewById(R.id.pbQuantityLoader)
            clCartItems = view.findViewById(R.id.clCartItems)
            tvDelete = view.findViewById(R.id.tvDelete)
            promotionalText = view.findViewById(R.id.promotionalText)
            llPromotionalText = view.findViewById(R.id.promotionalTextLayout)
            rlDeleteButton = view.findViewById(R.id.rlDeleteButton)
            tvProductAvailability = view.findViewById(R.id.tvProductAvailability)
            swipeLayout = view.findViewById(R.id.swipe)
            minusDeleteCountImage = view.findViewById(R.id.minusDeleteCountImage)
            minusDeleteCountImageLayout = view.findViewById(R.id.minusDeleteCountImageLayout)
            addCountImageLayout = view.findViewById(R.id.addCountImageLayout)
            cartLowStock = view.findViewById(R.id.cartLowStock)
            txtCartLowStock = view.findViewById(R.id.txtCartLowStock)
            cbShoppingList = view.findViewById(R.id.cbShoppingList)
            pbLoadProduct = view.findViewById(R.id.pbLoadProduct)
            swipeRight = view.findViewById(R.id.swipeRight)
        }
    }

    private inner class CartPricesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtYourCartPrice: WTextView
        val txtDiscount: WTextView
        val txtCompanyDiscount: WTextView
        val txtWrewardsDiscount: WTextView
        val txtTotalDiscount: WTextView
        val txtPromoCodeDiscount: WTextView
        val orderSummeryLayout: LinearLayout
        val rlDiscount: RelativeLayout
        var rlCompanyDiscount: RelativeLayout
        val rlWrewardsDiscount: RelativeLayout
        val rlTotalDiscount: RelativeLayout
        val rlPromoCodeDiscount: RelativeLayout
        val availableVouchersCount: TextView
        val viewVouchers: TextView
        val promoCodeAction: TextView
        val promoCodeLabel: TextView
        val orderTotal: TextView
        val promoDiscountInfo: ImageView
        val liquorBannerRootConstraintLayout: ConstraintLayout
        val imgLiBanner: ImageView
        val deliveryFee: TextView
        val txtPriceEstimatedDelivery:TextView

        val availableCashVouchersCount: TextView
        val viewCashVouchers: TextView
        val rlAvailableCashVouchers: RelativeLayout
        val rlAvailableWRewardsVouchers: RelativeLayout
        val rlPromoCode: RelativeLayout

        init {
            txtYourCartPrice = view.findViewById(R.id.txtYourCartPrice)
            orderSummeryLayout = view.findViewById(R.id.orderSummeryLayout)
            rlCompanyDiscount = view.findViewById(R.id.rlCompanyDiscount)
            availableVouchersCount = view.findViewById(R.id.availableVouchersCount)
            viewVouchers = view.findViewById(R.id.viewVouchers)
            txtDiscount = view.findViewById(R.id.txtDiscount)
            txtCompanyDiscount = view.findViewById(R.id.txtCompanyDiscount)
            txtWrewardsDiscount = view.findViewById(R.id.txtWrewardsDiscount)
            txtTotalDiscount = view.findViewById(R.id.txtTotalDiscount)
            rlDiscount = view.findViewById(R.id.rlDiscount)
            rlCompanyDiscount = view.findViewById(R.id.rlCompanyDiscount)
            rlWrewardsDiscount = view.findViewById(R.id.rlWrewardsDiscount)
            rlTotalDiscount = view.findViewById(R.id.rlTotalDiscount)
            promoCodeAction = view.findViewById(R.id.promoCodeAction)
            promoCodeLabel = view.findViewById(R.id.promoCodeLabel)
            txtPromoCodeDiscount = view.findViewById(R.id.txtPromoCodeDiscount)
            rlPromoCodeDiscount = view.findViewById(R.id.rlPromoCodeDiscount)
            promoDiscountInfo = view.findViewById(R.id.promoDiscountInfo)
            liquorBannerRootConstraintLayout =
                view.findViewById(R.id.liquorBannerRootConstraintLayout)
            imgLiBanner = view.findViewById(R.id.imgLiquorBanner)
            orderTotal = view.findViewById(R.id.orderTotal)
            availableCashVouchersCount = view.findViewById(R.id.availableCashVouchersCount)
            viewCashVouchers = view.findViewById(R.id.viewCashVouchers)
            rlAvailableCashVouchers = view.findViewById(R.id.rlAvailableCashVouchers);
            rlAvailableWRewardsVouchers = view.findViewById(R.id.rlAvailableWRewardsVouchers);
            rlPromoCode = view.findViewById(R.id.rlPromoCode);
            deliveryFee = view.findViewById(R.id.delivery_fee_label)
            txtPriceEstimatedDelivery = view.findViewById(R.id.txtPriceEstimatedDelivery)
        }
    }

    inner class GiftProductHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        val giftItemImageView: ImageView
        val productNameTextView: TextView
        val brandProductDescriptionTextView: TextView
        val giftRootContainerConstraintLayout: ConstraintLayout

        init {
            giftItemImageView = view.findViewById(R.id.giftItemImageView)
            productNameTextView = view.findViewById(R.id.productNameTextView)
            brandProductDescriptionTextView =
                view.findViewById(R.id.brandProductDescriptionTextView)
            giftRootContainerConstraintLayout =
                view.findViewById(R.id.giftRootContainerConstraintLayout)
        }
    }

    inner class CartCommerceItemRow internal constructor(
        val rowType: CartRowType,
        val category: String?,
        val commerceItem: CommerceItem?,
        val commerceItems: ArrayList<CommerceItem>?,
    )

    fun notifyAdapter(
        cartItems: ArrayList<CartItemGroup>?,
        orderSummary: OrderSummary?, voucherDetails: VoucherDetails?,
        liquorCompliance: LiquorCompliance?,
    ) {
        this.cartItems = cartItems
        this.orderSummary = orderSummary
        this.voucherDetails = voucherDetails
        liquorComplianceInfo = liquorCompliance
        resetQuantityState(false)
        notifyDataSetChanged()
        onItemClick.updateOrderTotal()
    }

    fun onChangeQuantityComplete() {
        isQuantityUploading = false
        resetQuantityState(false)
        notifyDataSetChanged()
    }

    fun onChangeQuantityLoad(mCommerceItem: CommerceItem) {
        if (!cartItems.isNullOrEmpty()) {
            for (cartItemGroup in cartItems!!) {
                val commerceItemList = cartItemGroup.commerceItems
                if (commerceItemList != null) {
                    for (cm in commerceItemList) {
                        if (cm === mCommerceItem) {
                            cm.quantityUploading = true
                            isQuantityUploading = true
                        }
                    }
                }
            }
            notifyDataSetChanged()
        }
    }

    private fun isQuantityUploading(): Boolean {
        var isUploading = false
        if (!cartItems.isNullOrEmpty()) {
            for (cartItemGroup in cartItems!!) {
                val commerceItemList = cartItemGroup.commerceItems
                if (commerceItemList != null) {
                    for (cm in commerceItemList) {
                        if (cm.quantityUploading) {
                            isUploading = true
                            break
                        }
                    }
                }
            }
        }
        return isUploading
    }

    fun onChangeQuantityError() {
        resetQuantityState(true)
        notifyDataSetChanged()
    }

    fun onChangeQuantityLoad() {
        isQuantityUploading = isQuantityUploading()
        notifyDataSetChanged()
    }

    private fun animateOnDeleteButtonVisibility(view: View, animate: Boolean) {
        if (mContext != null) {
            val width = getWidthAndHeight(mContext)
            val animator: ObjectAnimator = ObjectAnimator.ofFloat(view,
                "translationX",
                if (animate) -width.toFloat() else width.toFloat(),
                1f)
            animator.interpolator = DecelerateInterpolator()
            animator.duration = 300
            animator.start()
        }
    }

    private fun getWidthAndHeight(activity: Activity): Int {
        val dm = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(dm)
        return dm.widthPixels / 10
    }

    fun onPopUpCancel(status: String?) {
        when (status) {
            ProductState.CANCEL_DIALOG_TAPPED -> {
                resetQuantityState(true)
                notifyDataSetChanged()
            }
            else -> {}
        }
    }

    private fun resetQuantityState(refreshQuantity: Boolean) {
        if (!cartItems.isNullOrEmpty()) {
            for (cartItemGroup in cartItems!!) {
                val commerceItemList = cartItemGroup.commerceItems
                if (commerceItemList != null) {
                    for (cm in commerceItemList) {
                        if (refreshQuantity) cm.quantityUploading = false
                        setFirstLoadCompleted(false)
                    }
                }
            }
        }
    }

    private fun setFirstLoadCompleted(firstLoadCompleted: Boolean) {
        this.firstLoadCompleted = firstLoadCompleted
    }

    private fun firstLoadWasCompleted(): Boolean {
        return firstLoadCompleted
    }

    fun setEditMode(editMode: Boolean) {
        this.editMode = editMode
        if (cartItems != null) notifyItemRangeChanged(0, cartItems!!.size)
    }

    fun updateStockAvailability(cartItems: ArrayList<CartItemGroup>?) {
        this.cartItems = cartItems
        notifyDataSetChanged()
    }

    private val appliedVouchersCount: Int
        get() = if (voucherDetails == null) {
            -1
        } else getAppliedVouchersCount(voucherDetails!!.vouchers)

    init {
        this.orderSummary = orderSummary
        mContext = context
        this.voucherDetails = voucherDetails
        liquorComplianceInfo = liquorCompliance
    }
}