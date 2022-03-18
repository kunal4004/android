package za.co.woolworths.financial.services.android.ui.adapters.shop.dash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.RootCategory
import za.co.woolworths.financial.services.android.models.dto.shop.ProductCatalogue
import java.util.*

class DashDeliveryAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ON_DEMAND_CATEGORIES = 0
        private const val TYPE_DASH_CATEGORIES = 1
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Any?>() {

        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is ProductCatalogue && newItem is ProductCatalogue -> {
                    oldItem.headerText == newItem.headerText
                }
                oldItem is List<*> && newItem is List<*> -> {
                    true
                }
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is ProductCatalogue && newItem is ProductCatalogue -> {
                    (oldItem as ProductCatalogue).hashCode() == (newItem as ProductCatalogue).hashCode()
                }
                oldItem is List<*> && newItem is List<*> -> {
                    (oldItem as List<RootCategory>).hashCode() == (newItem as List<RootCategory>).hashCode()
                }
                else -> false
            }
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    private var categoryList: List<Any?>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OnDemandCategoryItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_dash_category,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is OnDemandCategoryItemViewHolder -> {
//                holder.bindView(position, categoryList[position] as List<RootCategory>)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            categoryList[position] is List<*> -> {
                TYPE_ON_DEMAND_CATEGORIES
            }
            else -> -1
        }
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    fun setData(
        onDemandCategories: List<RootCategory>?,
        dashCategories: ArrayList<ProductCatalogue>?
    ) {
        val list = ArrayList<Any?>(0)
        list.apply {
            add(onDemandCategories)
            dashCategories?.let {
                addAll(it)
            }
        }
        categoryList = list
    }
}

class OnDemandCategoryItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bindView(position: Int, onDemandCategories: List<RootCategory>?) {
        /*itemView.apply {
            ImageManager.loadImage(imgCategory, categoryItem.imgUrl)
            txtCategoryName?.text = categoryItem.categoryName
        }*/
    }
}
