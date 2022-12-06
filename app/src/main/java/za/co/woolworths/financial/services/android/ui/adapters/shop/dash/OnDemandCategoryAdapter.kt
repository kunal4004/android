package za.co.woolworths.financial.services.android.ui.adapters.shop.dash

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.ItemDashCategoryBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.target.Target
import za.co.woolworths.financial.services.android.models.dto.RootCategory
import za.co.woolworths.financial.services.android.ui.views.shop.dash.OnDemandNavigationListener

class OnDemandCategoryAdapter(
    @NonNull val context: Context,
    val onDemandNavigationListener: OnDemandNavigationListener
) : RecyclerView.Adapter<OnDemandCategoryItemHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<RootCategory>() {

        override fun areItemsTheSame(oldItem: RootCategory, newItem: RootCategory): Boolean {
            return oldItem.categoryId == newItem.categoryId
        }

        override fun areContentsTheSame(oldItem: RootCategory, newItem: RootCategory): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }
    private val differ = AsyncListDiffer(this, diffCallback)

    private var categoryList: List<RootCategory>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnDemandCategoryItemHolder {
        return OnDemandCategoryItemHolder(
            ItemDashCategoryBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: OnDemandCategoryItemHolder, position: Int) {
        holder.bindItem(position, categoryList[position], onDemandNavigationListener)
    }

    override fun getItemCount() = categoryList.size

    fun setOnDemandCategoryList(list: List<RootCategory>) {
        categoryList = list
    }
}

class OnDemandCategoryItemHolder(val itemBinding: ItemDashCategoryBinding) : RecyclerView.ViewHolder(itemBinding.root) {
    fun bindItem(
        position: Int,
        categoryItem: RootCategory,
        onDemandNavigationListener: OnDemandNavigationListener
    ) {
        itemBinding.apply {
            root.setOnClickListener {
                onDemandNavigationListener.onDemandNavigationClicked(it, categoryItem)
            }
            categoryItem.imgUrl?.let{
                Glide.with(imgCategory)
                    .load(it)
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .dontAnimate()
                    .into(imgCategory)
            }
            txtCategoryName?.text = categoryItem.categoryName
        }
    }
}
