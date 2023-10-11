package za.co.woolworths.financial.services.android.recommendations.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.RecommendationsProductListingPageRowBinding
import za.co.woolworths.financial.services.android.cart.view.SubstitutionChoice
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.isEnhanceSubstitutionFeatureAvailable
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.Promotions
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.Product
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.Promotion
import za.co.woolworths.financial.services.android.recommendations.presentation.RecommendationsProductListingListener
import za.co.woolworths.financial.services.android.recommendations.presentation.adapter.viewholder.MyRecycleViewHolder
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.toFloatOrZero
import za.co.woolworths.financial.services.android.util.Utils


class ProductListRecommendationAdapter(
    private val mProductsList: List<Product>,
    private val navigator: RecommendationsProductListingListener?,
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
                            productList.toProductList()
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

    private fun Product.toProductList(): ProductList {
        val productList = ProductList()
        productList.productId = productId
        productList.brandHeaderDescription = brandHeaderDescription
        productList.brandText = brandText
        productList.externalImageRefV2 = externalImageRefV2
        productList.isLiquor = isLiquor
        productList.isRnREnabled = isRnREnabled
        productList.kilogramPrice = kilogramPrice?.toFloatOrZero()
        productList.price = price?.toFloatOrZero()
        productList.priceType = priceType
        productList.productName = productName
        productList.productType = productType
        productList.productVariants = productVariants
        productList.promotions = promotions?.toArrayList()
        productList.saveText = saveText
        productList.sku = productId
        productList.wasPrice = wasPrice?.toFloatOrZero()
        productList.averageRating = averageRating
        productList.reviewCount = reviewCount

        return productList
    }

    private fun List<Promotion>?.toArrayList(): ArrayList<Promotions> {
        return this?.let {
            ArrayList(it.map{ promotion ->
                Promotions(promotion.promotionalText, promotion.searchTerm)
            })
        } ?: arrayListOf()
    }
}