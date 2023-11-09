package za.co.woolworths.financial.services.android.recommendations.presentation.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.RecommendationCategoryListItemBinding
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.Action


class ProductCategoryAdapter(private val actionItemList: List<Action>?) :
    RecyclerView.Adapter<ProductCategoryAdapter.MyRecViewHolder>() {

    var row_index = 0
    var onItemClick: ((Int, List<ProductList>?) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRecViewHolder {
        return MyRecViewHolder(
            RecommendationCategoryListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: MyRecViewHolder,
        position: Int
    ) {
        actionItemList?.get(position)?.let { holder.bind(it) }
    }

    override fun getItemCount(): Int {
        return actionItemList?.size ?: 0
    }

    inner class MyRecViewHolder(
        private val recItemBinding: RecommendationCategoryListItemBinding
    ) :
        RecyclerView.ViewHolder(recItemBinding.root) {

        fun bind(actionItem: Action) {
            recItemBinding.recommendationsCategoryText?.text = actionItem.componentName
            if (row_index == this.position) {
                recItemBinding.recommendationsCategoryText?.setBackgroundResource(R.drawable.bg_geo_selected_tab)
                recItemBinding.recommendationsCategoryText?.setTextColor(Color.WHITE)
            } else {
                recItemBinding.recommendationsCategoryText?.setBackgroundResource(R.drawable.bg_reccomendations_selected_tab)
                recItemBinding.recommendationsCategoryText?.setTextColor(Color.GRAY)

            }
            itemView.setOnClickListener {
                onItemClick?.invoke(absoluteAdapterPosition, actionItem.products)
                row_index = position;
                notifyDataSetChanged();

            }
        }
    }
}

