package za.co.woolworths.financial.services.android.dash.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.item_dash_category.view.*
import za.co.woolworths.financial.services.android.models.dto.RootCategory
import za.co.woolworths.financial.services.android.util.ImageManager

class DashDeliveryAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<RootCategory>() {
        override fun areItemsTheSame(oldItem: RootCategory, newItem: RootCategory): Boolean {
            return oldItem.categoryId == newItem.categoryId
        }

        override fun areContentsTheSame(oldItem: RootCategory, newItem: RootCategory): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var categoryList: List<RootCategory>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CategoryItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_dash_category,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CategoryItemViewHolder -> {
                holder.bindView(position, categoryList.get(position))
            }
        }
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

}

class CategoryItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bindView(position: Int, categoryItem: RootCategory) {
        itemView.apply {
            ImageManager.loadImage(imgCategory, categoryItem.imgUrl)
            txtCategoryName?.text = categoryItem.categoryName
        }
    }
}
