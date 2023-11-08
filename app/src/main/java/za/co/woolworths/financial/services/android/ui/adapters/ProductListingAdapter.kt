package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import com.awfs.coordination.R
import com.awfs.coordination.databinding.BottomProgressBarBinding
import com.awfs.coordination.databinding.ItemFoundLayoutBinding
import com.awfs.coordination.databinding.ProductListingPageRowBinding
import za.co.woolworths.financial.services.android.cart.view.SubstitutionChoice
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.isEnhanceSubstitutionFeatureAvailable
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.ui.adapters.holder.*
import za.co.woolworths.financial.services.android.util.Utils


class ProductListingAdapter(
    private val navigator: IProductListing?,
    private val mProductListItems: List<ProductList>?,
    val activity: FragmentActivity?,
    val mBannerLabel: String?,
    val mBannerImage: String?,
    val mIsComingFromBLP: Boolean,
    val promotionalRichText: String?,
    val listener: OnTapIcon,
) : RecyclerView.Adapter<RecyclerViewViewHolder>() {

    private var selectQuantityViewAdapter: SelectQuantityAdapter? = null
    private var addItemToCartData: AddItemToCart? = null
    private var lastSelectedItemPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewViewHolder {
        return when (ProductListingViewType.values()[viewType]) {
            ProductListingViewType.HEADER ->
                RecyclerViewViewHolderHeader(
                    ItemFoundLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )

            ProductListingViewType.FOOTER ->
                RecyclerViewViewHolderFooter(
                    BottomProgressBarBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )

            else ->
                RecyclerViewViewHolderItems(
                    ProductListingPageRowBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
        }
    }

    override fun onBindViewHolder(holder: RecyclerViewViewHolder, position: Int) {
        mProductListItems?.get(position)?.let { productList ->
            holder.itemView.invalidate()
            holder.itemView.requestLayout()
            when (productList.rowType) {
                ProductListingViewType.HEADER -> {
                    (holder as? RecyclerViewViewHolderHeader)?.setNumberOfItems(
                        activity, productList
                    )
                    (holder as? RecyclerViewViewHolderHeader)?.setChanelBanner(
                        mBannerLabel, mBannerImage, mIsComingFromBLP, navigator
                    )
                    (holder as? RecyclerViewViewHolderHeader)?.setPromotionalBanner(
                        promotionalRichText
                    )
                }

                ProductListingViewType.FOOTER -> (holder as? RecyclerViewViewHolderFooter)?.loadMoreProductProgressBarVisibility()
                else -> (holder as? RecyclerViewViewHolderItems)?.let { view ->
                    navigator?.let {
                        view.setProductItem(
                            productList,
                            it,
                            if (position % 2 != 0) mProductListItems.getOrNull(position + 1) else null,
                            if (position % 2 == 0) mProductListItems.getOrNull(position - 1) else null
                        )
                    }
                    view.itemBinding.includeProductListingPriceLayout.imQuickShopAddToCartIcon?.setOnClickListener {
                        when (lastSelectedItemPosition) {
                            position -> {
                                view.bindingAdapter?.notifyItemChanged(position)
                                lastSelectedItemPosition = -1 // reset the value.
                                return@setOnClickListener
                            }

                            else -> {
                                if (lastSelectedItemPosition != -1) {
                                    notifyItemChanged(lastSelectedItemPosition)
                                }
                                lastSelectedItemPosition = position

                                if (!productList.quickShopButtonWasTapped) {
                                    var fulfilmentTypeId = ""
                                    activity?.apply {
                                        Utils.triggerFireBaseEvents(
                                            FirebaseManagerAnalyticsProperties.SHOPQS_ADD_TO_CART,
                                            this
                                        )
                                    }
                                    activity?.apply {
                                        productList?.apply {
                                            when (productType) {
                                                getString(R.string.food_product_type) -> {
                                                    fulfilmentTypeId =
                                                        AppConfigSingleton.quickShopDefaultValues?.foodFulfilmentTypeId.toString()
                                                }

                                                getString(R.string.digital_product_type) -> {
                                                    fulfilmentTypeId =
                                                        AppConfigSingleton.quickShopDefaultValues?.digitalProductsFulfilmentTypeId.toString()
                                                }
                                            }
                                        }
                                    }

                                    fulfilmentTypeId?.let { id ->
                                        selectQuantityViewAdapter =
                                            SelectQuantityAdapter { selectedQuantity: Int ->
                                                quantityItemClicked(selectedQuantity)
                                            }
                                        addItemToCartData =
                                            if (isEnhanceSubstitutionFeatureAvailable()) {
                                                AddItemToCart(
                                                    productList.productId,
                                                    productList.sku,
                                                    0,
                                                    SubstitutionChoice.SHOPPER_CHOICE.name,
                                                    ""
                                                )
                                            } else {
                                                AddItemToCart(
                                                    productList.productId,
                                                    productList.sku,
                                                    0
                                                )
                                            }
                                        navigator?.setRecyclerViewHolderView(view)
                                        navigator?.queryInventoryForStore(
                                            id,
                                            addItemToCartData,
                                            productList
                                        )
                                    }
                                }
                            }
                        }
                    }
                    view.itemBinding.imAddToList?.setOnClickListener {
                        listener.onAddToListClicked(productList)
                    }
                }
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerViewViewHolder) {
        if (holder is RecyclerViewViewHolderItems) {
            holder.setIsRecyclable(false)
        }
        super.onViewAttachedToWindow(holder)
    }

    override fun getItemViewType(position: Int): Int =
        mProductListItems?.get(position)?.rowType?.ordinal
            ?: 0

    override fun getItemCount(): Int = mProductListItems?.size ?: 0

    override fun getItemId(position: Int): Long = position.toLong()

    fun resetQuickShopButton() {
        mProductListItems?.forEach { product ->
            product.quickShopButtonWasTapped = false
        }
        notifyDataSetChanged()
    }

    fun showQuantitySelector(
        recyclerViewViewHolderItems: RecyclerViewViewHolderItems?,
        addItemToCart: AddItemToCart?,
    ) {
        if (selectQuantityViewAdapter != null && addItemToCart != null) {
            addItemToCartData = addItemToCart
            val quantityInStock = addItemToCart.quantity
            if (quantityInStock > 0) {
                // replace quickshop button image to cross button image
                activity?.let {
                    recyclerViewViewHolderItems?.itemBinding?.includeProductListingPriceLayout?.imQuickShopAddToCartIcon?.setImageDrawable(
                        ContextCompat.getDrawable(
                            it,
                            R.drawable.cross_button_bg
                        )
                    )
                }
            }
            recyclerViewViewHolderItems?.itemBinding?.quantitySelectorView?.apply {
                layoutManager = activity?.let { activity ->
                    LinearLayoutManager(
                        activity,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                }
                val imageViewHeight = recyclerViewViewHolderItems.itemBinding.imProductImage.height
                if (quantityInStock >= 4) {
                    layoutParams?.height = imageViewHeight
                } else {
                    layoutParams?.height = LayoutParams.WRAP_CONTENT
                }
                adapter = selectQuantityViewAdapter


                val mScrollTouchListener: RecyclerView.OnItemTouchListener =
                    object : RecyclerView.OnItemTouchListener {
                        override fun onInterceptTouchEvent(
                            rv: RecyclerView,
                            e: MotionEvent,
                        ): Boolean {
                            val action = e.action
                            when (action) {
                                MotionEvent.ACTION_MOVE -> rv.parent.requestDisallowInterceptTouchEvent(
                                    true
                                )
                            }
                            return false
                        }

                        override fun onTouchEvent(
                            rv: RecyclerView,
                            e: MotionEvent,
                        ) {
                        }

                        override fun onRequestDisallowInterceptTouchEvent(
                            disallowIntercept: Boolean,
                        ) {
                        }
                    }

                addOnItemTouchListener(mScrollTouchListener)
            }
            selectQuantityViewAdapter?.setItem(quantityInStock)
        }
    }

    private fun quantityItemClicked(quantity: Int) {
        addItemToCartData?.apply {
            navigator?.addFoodProductTypeToCart(
                if (isEnhanceSubstitutionFeatureAvailable()) {
                    AddItemToCart(
                        productId,
                        catalogRefId,
                        quantity,
                        SubstitutionChoice.SHOPPER_CHOICE.name,
                        ""
                    )
                } else {
                    AddItemToCart(productId, catalogRefId, quantity)
                }
            )
        }
        notifyDataSetChanged()
    }

    interface OnTapIcon {
        fun onAddToListClicked(productList: ProductList)
    }
}