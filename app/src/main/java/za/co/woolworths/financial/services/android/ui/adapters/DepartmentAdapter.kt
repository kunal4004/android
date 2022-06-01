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


class DepartmentAdapter(var mlRootCategories: MutableList<RootCategory>?, private val clickListener: (RootCategory) -> Unit, private val onDashBannerClick: () -> Unit, var validatePlace: ValidatePlace? = null)
    : RecyclerView.Adapter<DepartmentsBaseViewHolder>() {
    private var mDashBanner: Dash? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartmentsBaseViewHolder {
        return when (viewType) {
            RootCategoryViewType.DASH_BANNER.value -> {
                DashBannerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.department_dash_banner, parent, false))
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
        mlRootCategories = rootCategories
    }

    fun setDashBanner(dash: Dash?, rootCategories: MutableList<RootCategory>?, bannerText: String) {

        synchronized(this) {
            if (dash == null) {
                removeDashBanner(rootCategories)
                return
            }
            val dashBanner = RootCategory()
            dashBanner.viewType = RootCategoryViewType.DASH_BANNER
            dash.bannerText = bannerText
            // Check if root category already added dash
            rootCategories?.apply {
                if (size >= 2) {
                    get(1)?.let {
                        if (it.viewType != RootCategoryViewType.DASH_BANNER) {
                            add(1, dashBanner)

                        }
                    }
                }
            }
            mlRootCategories = mutableListOf()
            mlRootCategories = rootCategories
            mDashBanner = dash
        }
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

    inner class DashBannerViewHolder(itemView: View) : DepartmentsBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            mDashBanner?.imgUrl?.let { ImageManager.setPictureWithoutPlaceHolder(itemView.image_dash_banner_bg, it) }
            itemView.list_item_dash_banner_title.text = mDashBanner?.categoryName
            itemView.list_item_dash_banner_subtitle.text = mDashBanner?.bannerText
            itemView.list_item_dash_banner_container.setOnClickListener { onDashBannerClick() }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return mlRootCategories?.get(position)?.viewType!!.value
    }

     fun containsDashBanner(): Boolean {
        return mDashBanner != null
    }

    fun removeDashBanner(rootCategories: MutableList<RootCategory>?) {
        if (!containsDashBanner()) {
            return
        }
        //1 is the position of Dash banner card
        mlRootCategories?.get(1)?.let {
            if (it.viewType == RootCategoryViewType.DASH_BANNER) {
                // Make sure the position is after header
                mlRootCategories?.remove(it)
                rootCategories?.remove(it)
                mDashBanner = null
                notifyItemRemoved(1)
            }
        }
    }
}