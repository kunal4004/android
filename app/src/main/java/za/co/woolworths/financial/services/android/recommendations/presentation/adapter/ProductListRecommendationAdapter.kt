package za.co.woolworths.financial.services.android.recommendations.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.RecommendationsProductListingPageRowBinding
import za.co.woolworths.financial.services.android.cart.view.SubstitutionChoice
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.isEnhanceSubstitutionFeatureAvailable
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.recommendations.presentation.adapter.viewholder.MyRecycleViewHolder
import za.co.woolworths.financial.services.android.util.Utils


class ProductListRecommendationAdapter(
    private val mProductsList: List<ProductList>,
    private val navigator: IProductListing?,
    val activity: FragmentActivity?
) : RecyclerView.Adapter<MyRecycleViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRecycleViewHolder {
        return MyRecycleViewHolder(
            RecommendationsProductListingPageRowBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MyRecycleViewHolder, position: Int) {
        mProductsList.get(position)?.let { productList ->
            if (holder is MyRecycleViewHolder) {
                navigator?.let {
                    holder.setProductItem(
                        productList,
                        it,
                        if (position % 2 != 0) mProductsList.getOrNull(position + 1) else null,
                        if (position % 2 == 0) mProductsList.getOrNull(position - 1) else null
                    )
                }

                holder.mProductListingPageRowBinding.includeProductListingPriceLayout.imQuickShopAddToCartIcon.setOnClickListener {
                    activity?.apply {
                        Utils.triggerFireBaseEvents(
                            FirebaseManagerAnalyticsProperties.SHOPQS_ADD_TO_CART, this
                        )
                    }
                    val fulfilmentTypeId = AppConfigSingleton.quickShopDefaultValues?.foodFulfilmentTypeId
                    fulfilmentTypeId?.let { id ->
                        navigator?.queryInventoryForStore(
                            id,
                            if (isEnhanceSubstitutionFeatureAvailable()) {
                                AddItemToCart(productList.productId, productList.productId, 0, SubstitutionChoice.SHOPPER_CHOICE.name, "")
                            } else {
                                AddItemToCart(productList.productId, productList.productId, 0)
                            },
                            productList
                        )
                    }
                }
            }
            if (position >= mProductsList.size || position < 0) {
                return
            }
        }
    }

    override fun getItemCount(): Int {
        return mProductsList?.size ?: 0
    }
}