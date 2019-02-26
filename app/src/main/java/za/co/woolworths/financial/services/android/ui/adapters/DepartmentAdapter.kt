package za.co.woolworths.financial.services.android.ui.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.department_row.view.*
import za.co.woolworths.financial.services.android.models.dto.RootCategory


internal class DepartmentAdapter(private var mlRootCategories: MutableList<RootCategory>?, private val clickListener: (RootCategory) -> Unit)
    : RecyclerView.Adapter<DepartmentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartmentAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.department_row, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: DepartmentAdapter.ViewHolder, position: Int) {
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
            Picasso.get().load(rootCategory.imgUrl).fit().into(itemView.imProductCategory)
        }
    }

    fun setRootCategories(rootCategories: MutableList<RootCategory>?) {
        mlRootCategories = rootCategories
    }
}