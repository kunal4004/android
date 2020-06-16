package za.co.woolworths.financial.services.android.ui.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.department_header_delivery_location.view.*
import kotlinx.android.synthetic.main.department_row.view.*
import za.co.woolworths.financial.services.android.models.dto.RootCategory
import za.co.woolworths.financial.services.android.ui.adapters.holder.DepartmentsBaseViewHolder
import za.co.woolworths.financial.services.android.ui.adapters.holder.RootCategoryViewType
import za.co.woolworths.financial.services.android.util.ImageManager
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils


internal class DepartmentAdapter(private var mlRootCategories: MutableList<RootCategory>?, private val clickListener: (RootCategory) -> Unit, private val onEditDeliveryLocation: () -> Unit)
    : RecyclerView.Adapter<DepartmentsBaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartmentsBaseViewHolder {
        return when (viewType) {
            RootCategoryViewType.HEADER.value -> {
                HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.department_header_delivery_location, parent, false))
            }
            else -> DepartmentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.department_row, parent, false))
        }
    }

    override fun onBindViewHolder(holder: DepartmentsBaseViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return mlRootCategories?.size ?: 0
    }

    fun setRootCategories(rootCategories: MutableList<RootCategory>?) {
        if (!listContainHeader(rootCategories)) {
            val header = RootCategory()
            header.viewType = RootCategoryViewType.HEADER
            rootCategories?.add(0, header)
        }
        mlRootCategories = rootCategories
    }

    private fun listContainHeader(rootCategories: MutableList<RootCategory>?): Boolean {
        rootCategories?.apply {
            for (pl in this) {
                if (pl.viewType == RootCategoryViewType.HEADER) {
                    return true
                }
            }
        }
        return false
    }

    inner class DepartmentViewHolder(itemView: View) : DepartmentsBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val rootCategory: RootCategory? = mlRootCategories?.get(position)
            bindText(rootCategory!!, clickListener)
            loadImage(rootCategory)
        }

        private fun bindText(rootCategory: RootCategory, clickListener: (RootCategory) -> Unit) {
            itemView.tvDepartmentTitle.text = rootCategory.categoryName
            itemView.setOnClickListener { clickListener(rootCategory) }
        }

        private fun loadImage(rootCategory: RootCategory) {
            ImageManager.setPictureWithoutPlaceHolder(itemView.imProductCategory, rootCategory.imgUrl)
        }
    }

    inner class HeaderViewHolder(itemView: View) : DepartmentsBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            itemView.locationSelectedLayout.setOnClickListener { onEditDeliveryLocation() }
            if (Utils.getPreferredDeliveryLocation() == null || !SessionUtilities.getInstance().isUserAuthenticated) {
                itemView.tvDeliveringTo.text = itemView.context.resources.getString(R.string.delivery_or_collection)
                itemView.tvDeliveryLocation.visibility = View.GONE
                itemView.iconCaretRight.visibility = View.VISIBLE
                itemView.editLocation.visibility = View.INVISIBLE
            } else {
                itemView.iconCaretRight.visibility = View.GONE
                itemView.editLocation.visibility = View.VISIBLE
                KotlinUtils.setDeliveryAddressView(itemView.context as Activity,Utils.getPreferredDeliveryLocation(),itemView.tvDeliveringTo,itemView.tvDeliveryLocation,itemView.deliverLocationIcon)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return mlRootCategories?.get(position)?.viewType!!.value
    }

}