package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.department_dash_banner.view.*
import kotlinx.android.synthetic.main.department_row.view.*
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidatePlace
import za.co.woolworths.financial.services.android.models.dto.Dash
import za.co.woolworths.financial.services.android.models.dto.RootCategory
import za.co.woolworths.financial.services.android.ui.adapters.holder.DepartmentsBaseViewHolder
import za.co.woolworths.financial.services.android.ui.adapters.holder.RootCategoryViewType
import za.co.woolworths.financial.services.android.util.*


class DepartmentAdapter(var mlRootCategories: MutableList<RootCategory>?,
                        private val clickListener: (RootCategory) -> Unit,
                        var validatePlace: ValidatePlace? = null)
    : RecyclerView.Adapter<DepartmentsBaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartmentsBaseViewHolder {
        return DepartmentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.department_row, parent, false))
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

    override fun getItemViewType(position: Int): Int {
        return mlRootCategories?.get(position)?.viewType!!.value
    }

}