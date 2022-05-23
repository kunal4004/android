package za.co.woolworths.financial.services.android.ui.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.department_dash_banner.view.*
import kotlinx.android.synthetic.main.department_header_delivery_location.view.*
import kotlinx.android.synthetic.main.department_row.view.*
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidatePlace
import za.co.woolworths.financial.services.android.models.dto.Dash
import za.co.woolworths.financial.services.android.models.dto.RootCategory
import za.co.woolworths.financial.services.android.ui.adapters.holder.DepartmentsBaseViewHolder
import za.co.woolworths.financial.services.android.ui.adapters.holder.RootCategoryViewType
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.wenum.Delivery


internal class DepartmentAdapter(private var mlRootCategories: MutableList<RootCategory>?, private val clickListener: (RootCategory) -> Unit, private val onEditDeliveryLocation: () -> Unit, private val onDashBannerClick: () -> Unit, var validatePlace: ValidatePlace? = null)
    : RecyclerView.Adapter<DepartmentsBaseViewHolder>() {
    var isValidateSuburbRequestInProgress = false
    private var mDashBanner: Dash? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartmentsBaseViewHolder {
        return when (viewType) {
            RootCategoryViewType.HEADER.value -> {
                HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.department_header_delivery_location, parent, false))
            }
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
        if (!listContainHeader(rootCategories)) {
            val header = RootCategory()
            header.viewType = RootCategoryViewType.HEADER
            rootCategories?.add(0, header)
        }
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
                KotlinUtils.getAnonymousUserLocationDetails()?.let {
                    itemView.iconCaretRight?.visibility = View.GONE
                    itemView.editLocation?.visibility = View.VISIBLE
                    KotlinUtils.setDeliveryAddressView(
                        itemView.context as Activity,
                        it,
                        itemView.tvDeliveringTo,
                        itemView.tvDeliveryLocation,
                        itemView.deliverLocationIcon
                    )
                    return
                }
                itemView.tvDeliveringTo?.text =
                    itemView.context.resources.getString(R.string.delivery_or_collection)
                itemView.tvDeliveryLocation?.visibility = View.GONE
                itemView.iconCaretRight?.visibility = View.VISIBLE
                itemView.editLocation?.visibility = View.INVISIBLE
            } else {
                itemView.iconCaretRight?.visibility = View.GONE
                itemView.editLocation?.visibility = View.VISIBLE
                KotlinUtils.setDeliveryAddressView(
                    itemView.context as Activity,
                    Utils.getPreferredDeliveryLocation(),
                    itemView.tvDeliveringTo,
                    itemView.tvDeliveryLocation,
                    itemView.deliverLocationIcon
                )
            }
            itemView.deliveryDatesProgressPlaceHolder.visibility = if (isValidateSuburbRequestInProgress) View.VISIBLE else View.GONE
            if (validatePlace == null) {
                itemView.deliveryDateLayout.visibility = View.GONE
            } else {
                validatePlace?.let { it ->
                    itemView.apply {
                        when (KotlinUtils.getPreferredDeliveryType()) {
                            Delivery.CNC -> {
                                earliestDateValue?.text = it.firstAvailableFoodDeliveryDate ?: ""
                                earliestDateValue?.visibility = View.VISIBLE
                                foodItemsDeliveryDateLayout?.visibility = View.GONE
                                otherItemsDeliveryDateLayout?.visibility = View.GONE
                            }
                            Delivery.STANDARD -> {
                                foodItemsDeliveryDate?.text = it.firstAvailableFoodDeliveryDate
                                        ?: ""
                                otherItemsDeliveryDate?.text = it.firstAvailableOtherDeliveryDate
                                        ?: ""
                                earliestDateValue?.visibility = View.GONE
                                foodItemsDeliveryDateLayout?.visibility = if (it.firstAvailableFoodDeliveryDate.isNullOrEmpty()) View.GONE else View.VISIBLE
                                otherItemsDeliveryDateLayout?.visibility = if (it.firstAvailableOtherDeliveryDate.isNullOrEmpty()) View.GONE else View.VISIBLE
                            }
                        }
                        earliestDateTitle?.text = bindString(if (KotlinUtils.getPreferredDeliveryType() == Delivery.STANDARD) R.string.earliest_delivery_date else R.string.earliest_collection_date)
                        deliveryDateLayout?.visibility = if (!it.firstAvailableFoodDeliveryDate.isNullOrEmpty() || !it.firstAvailableOtherDeliveryDate.isNullOrEmpty()) View.VISIBLE else View.GONE
                    }
                }
            }
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

    fun updateDeliveryDate(validatePlace: ValidatePlace) {
        this.validatePlace = validatePlace
        showDeliveryDatesProgress(false)
        notifyDataSetChanged()
    }

    fun hideDeliveryDates() {
        this.validatePlace = null
        showDeliveryDatesProgress(false)
        notifyDataSetChanged()
    }

    fun showDeliveryDatesProgress(isInProgress: Boolean) {
        isValidateSuburbRequestInProgress = isInProgress
        if (isInProgress)
            this.validatePlace = null
        notifyDataSetChanged()
    }

    public fun containsDashBanner(): Boolean {
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

    fun updateDashBanner(bannerText: String, isDashEnabled: Boolean) {
        if (!isDashEnabled || !containsDashBanner()) {
            return
        }
        mDashBanner?.apply {
            this.bannerText = bannerText
        }
        notifyItemChanged(1, mDashBanner)
    }
}