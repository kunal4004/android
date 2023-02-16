package za.co.woolworths.financial.services.android.recommendations.presentation.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.RecommendationsProductListingPageRowBinding
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.Product


class ProductListRecommendationAdapter(
    private val mProductsList: List<Product>,
    private val navigator: RecommendationsProductListingListener?,
    val activity: Context
) : RecyclerView.Adapter<MyRecycleViewHolder>() {


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
            if (holder is MyRecycleViewHolder) {
                navigator?.let {
                    holder.setProductItem(
                        productList, it,
                        if (position % 2 != 0) mProductsList.getOrNull(position + 1) else null,
                        if (position % 2 == 0) mProductsList.getOrNull(position - 1) else null
                    )
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