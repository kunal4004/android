package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.DepartmentRowBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidatePlace
import za.co.woolworths.financial.services.android.models.dto.RootCategory
import za.co.woolworths.financial.services.android.ui.adapters.holder.DepartmentsBaseViewHolder

class DepartmentAdapter(var mlRootCategories: MutableList<RootCategory>?,
                        private val clickListener: (RootCategory) -> Unit,
                        var validatePlace: ValidatePlace? = null)
    : RecyclerView.Adapter<DepartmentsBaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartmentsBaseViewHolder {
        return DepartmentViewHolder(
            DepartmentRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: DepartmentsBaseViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return mlRootCategories?.size ?: 0
    }

    fun setRootCategories(rootCategories: MutableList<RootCategory>?) {
        mlRootCategories = rootCategories
    }

    inner class DepartmentViewHolder(val itemBinding: DepartmentRowBinding) : DepartmentsBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            val rootCategory: RootCategory? = mlRootCategories?.get(position)
            bindText(rootCategory!!, clickListener)
            loadImage(rootCategory)
        }

        private fun bindText(rootCategory: RootCategory, clickListener: (RootCategory) -> Unit) {
            itemBinding.tvDepartmentTitle.text = rootCategory.categoryName
            itemBinding.root.setOnClickListener { clickListener(rootCategory) }
        }

        private fun loadImage(rootCategory: RootCategory) {
            itemBinding.imProductCategory.visibility = if (rootCategory.imgUrl.isEmpty()) View.GONE else View.VISIBLE
            itemBinding.imProductCategory.context?.apply {
                Glide.with(this)
                    .load(rootCategory.imgUrl)
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .fitCenter()
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(6)))
                    .dontAnimate()
                    .into(itemBinding.imProductCategory)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return mlRootCategories?.get(position)?.viewType!!.value
    }

}