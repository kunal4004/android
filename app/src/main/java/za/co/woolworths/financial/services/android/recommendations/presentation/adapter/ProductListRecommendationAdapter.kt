package za.co.woolworths.financial.services.android.recommendations.presentation.adapter

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.RecommendationsProductListingPageRowBinding
import za.co.woolworths.financial.services.android.cart.view.SubstitutionChoice
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.isEnhanceSubstitutionFeatureAvailable
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.recommendations.presentation.adapter.viewholder.MyRecycleViewHolder
import za.co.woolworths.financial.services.android.recommendations.presentation.viewmodel.RecommendationViewModel
import za.co.woolworths.financial.services.android.ui.adapters.SelectQuantityAdapter
import za.co.woolworths.financial.services.android.util.Utils


class ProductListRecommendationAdapter(
    private val mProductsList: List<ProductList>,
    private val navigator: IProductListing?,
    val activity: FragmentActivity?,
    private val recommendationViewModel: RecommendationViewModel,
) : RecyclerView.Adapter<MyRecycleViewHolder>() {

    companion object {
        private const val MAX_QUANTITY = 6
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRecycleViewHolder {
        return MyRecycleViewHolder(
            RecommendationsProductListingPageRowBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onViewAttachedToWindow(holder: MyRecycleViewHolder) {
        holder.setIsRecyclable(false)
        super.onViewAttachedToWindow(holder)
    }

    override fun onBindViewHolder(holder: MyRecycleViewHolder, position: Int) {
        mProductsList[position]?.let { productList ->
            navigator?.let {
                holder.setProductItem(
                    productList,
                    it,
                    if (position % 2 != 0) mProductsList.getOrNull(position + 1) else null,
                    if (position % 2 == 0) mProductsList.getOrNull(position - 1) else null
                )
            }

            holder.mProductListingPageRowBinding.includeProductListingPriceLayout.imQuickShopAddToCartIcon.setOnClickListener {
                if (recommendationViewModel.getQuickShopButtonPressed()) {
                    updateRecyclerView()
                    return@setOnClickListener
                }

                activity?.apply {
                    Utils.triggerFireBaseEvents(
                        FirebaseManagerAnalyticsProperties.SHOPQS_ADD_TO_CART, this
                    )
                }
                val fulfilmentTypeId =
                    AppConfigSingleton.quickShopDefaultValues?.foodFulfilmentTypeId
                fulfilmentTypeId?.let { id ->
                    navigator?.setMyRecycleViewHolder(holder)
                    val addItemToCart = if (isEnhanceSubstitutionFeatureAvailable()) {
                        AddItemToCart(
                            productList.productId,
                            productList.productId,
                            0,
                            SubstitutionChoice.SHOPPER_CHOICE.name,
                            ""
                        )
                    } else {
                        AddItemToCart(
                            productList.productId,
                            productList.productId,
                            0
                        )
                    }
                    navigator?.queryInventoryForStore(
                        id,
                        addItemToCart,
                        productList
                    )
                }
            }
            if (position >= mProductsList.size || position < 0) {
                return
            }
        }
    }

    fun showQuantitySelector(
        recyclerViewViewHolderItems: MyRecycleViewHolder?,
        addItemToCart: AddItemToCart?,
    ) {
        recommendationViewModel.setQuickShopButtonPressed(true)
        if (addItemToCart != null) {
            val quantityInStock = addItemToCart.quantity
            val selectQuantityViewAdapter =
                SelectQuantityAdapter { selectedQuantity: Int ->
                    quantityItemClicked(selectedQuantity, addItemToCart)
                }
            if (quantityInStock > 0) {
                // replace quick shop button image to cross button image
                activity?.let {
                    recyclerViewViewHolderItems?.mProductListingPageRowBinding?.includeProductListingPriceLayout?.imQuickShopAddToCartIcon?.setImageDrawable(
                        ContextCompat.getDrawable(
                            it,
                            R.drawable.cross_button_bg
                        )
                    )
                }
            }
            recyclerViewViewHolderItems?.mProductListingPageRowBinding?.quantitySelectorView?.apply {
                visibility = View.VISIBLE
                layoutManager = activity?.let { activity ->
                    LinearLayoutManager(
                        activity,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                }
                val imageViewHeight =
                    recyclerViewViewHolderItems.mProductListingPageRowBinding.imProductImage.height + recyclerViewViewHolderItems.mProductListingPageRowBinding.tvProductName.height + 50
                if (quantityInStock >= MAX_QUANTITY) {
                    layoutParams?.height = imageViewHeight
                } else {
                    layoutParams?.height = RecyclerView.LayoutParams.WRAP_CONTENT
                }
                adapter = selectQuantityViewAdapter

                val mScrollTouchListener: RecyclerView.OnItemTouchListener =
                    object : RecyclerView.OnItemTouchListener {
                        override fun onInterceptTouchEvent(
                            rv: RecyclerView,
                            e: MotionEvent,
                        ): Boolean {
                            when (e.action) {
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

    private fun quantityItemClicked(quantity: Int, addItemToCart: AddItemToCart) {
        addItemToCart?.apply {
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
        updateRecyclerView()
    }

    private fun updateRecyclerView() {
        recommendationViewModel.setQuickShopButtonPressed(false)
        navigator?.updateMainRecyclerView()
    }

    override fun getItemCount(): Int {
        return mProductsList?.size ?: 0
    }
}