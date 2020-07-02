package za.co.woolworths.financial.services.android.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.department_row.view.*
import za.co.woolworths.financial.services.android.models.dto.RootCategory
import za.co.woolworths.financial.services.android.util.ImageManager


internal class DepartmentAdapter(private var mlRootCategories: MutableList<RootCategory>?, private val clickListener: (RootCategory) -> Unit)
    : RecyclerView.Adapter<DepartmentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.department_row, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rootCategory: RootCategory? = mlRootCategories?.get(position)
        holder.bindText(rootCategory!!, clickListener)
        holder.loadImage(rootCategory)
    }

    override fun getItemCount(): Int {
        return mlRootCategories?.size ?: 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindText(rootCategory: RootCategory, clickListener: (RootCategory) -> Unit) {
            itemView.tvDepartmentTitle.text = rootCategory.categoryName
            itemView.setOnClickListener { clickListener(rootCategory) }
        }

        fun loadImage(rootCategory: RootCategory) {
            ImageManager.setPictureWithoutPlaceHolder(itemView.imProductCategory,rootCategory.imgUrl)
        }
    }

    fun setRootCategories(rootCategories: MutableList<RootCategory>?) {
        mlRootCategories = rootCategories
    }
}