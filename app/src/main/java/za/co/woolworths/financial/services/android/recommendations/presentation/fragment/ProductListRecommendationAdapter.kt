package za.co.woolworths.financial.services.android.recommendations.presentation.fragment

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.RecommendationsProductListingPageRowBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.Product
import za.co.woolworths.financial.services.android.util.Utils


class ProductListRecommendationAdapter(
    private val mProductsList: List<Product>,
    private val navigator: RecommendationsProductListingListener?,
    val activity: Context
) : RecyclerView.Adapter<MyRecycleViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRecycleViewHolder {
        return MyRecycleViewHolder(
            RecommendationsProductListingPageRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyRecycleViewHolder, position: Int) {
        mProductsList.get(position)?.let { productList ->
          //  holder.bind(it)

        if (holder is MyRecycleViewHolder) {
            navigator?.let {
                holder.setProductItem(
                    productList, it,
                    if (position % 2 != 0) mProductsList.getOrNull(position + 1) else null,
                    if (position % 2 == 0) mProductsList.getOrNull(position - 1) else null
                )
            }


        /*    holder.mProductListingPageRowBinding.includeProductListingPriceLayout.imQuickShopAddToCartIcon?.setOnClickListener {
                // if (!productList.quickShopButtonWasTapped) {
                activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOPQS_ADD_TO_CART,
                    this as Activity?
                ) }
                val fulfilmentTypeId = AppConfigSingleton.quickShopDefaultValues?.foodFulfilmentTypeId
               // val storeId = fulfilmentTypeId?.let { it1 -> RecyclerViewViewHolderItems.getFulFillmentStoreId(it1) }
                fulfilmentTypeId?.let { id ->
                    navigator?.queryInventoryForStore(
                        id,
                        AddItemToCart(productList.productId, productList.sku, 0),
                        productList
                    )
                }
                //  }
            }*/
        }
            if(position >= mProductsList.size || position < 0){
                return
            }
        }
    }

    override fun getItemCount(): Int {
        return mProductsList?.size ?: 0
    }

   }