package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.refinements_on_promotion_layout.view.*
import kotlinx.android.synthetic.main.refinements_options_layout.view.*
import kotlinx.android.synthetic.main.refinements_section_header_layout.view.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.BreadCrumb
import za.co.woolworths.financial.services.android.models.dto.RefinementHistory
import za.co.woolworths.financial.services.android.models.dto.RefinementNavigation
import za.co.woolworths.financial.services.android.models.dto.RefinementSelectableItem
import za.co.woolworths.financial.services.android.ui.adapters.holder.RefinementBaseViewHolder
import za.co.woolworths.financial.services.android.ui.fragments.product.refine.RefinementNavigationFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.BaseFragmentListner
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefinementOptionSelected
import za.co.woolworths.financial.services.android.util.Utils

class RefinementNavigationAdapter(val context: Context, val listner: OnRefinementOptionSelected, var dataList: ArrayList<RefinementSelectableItem>, var history: RefinementHistory) : RecyclerView.Adapter<RefinementBaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RefinementBaseViewHolder {
        return when (viewType) {
            RefinementSelectableItem.ViewType.SECTION_HEADER.value -> {
                SectionHeaderHolder(LayoutInflater.from(context).inflate(R.layout.refinements_section_header_layout, parent, false))
            }
            RefinementSelectableItem.ViewType.PROMOTION.value -> {
                PromotionHolder(LayoutInflater.from(context).inflate(R.layout.refinements_on_promotion_layout, parent, false))
            }
            RefinementSelectableItem.ViewType.OPTIONS.value -> {
                OptionsHolder(LayoutInflater.from(context).inflate(R.layout.refinements_options_layout, parent, false))
            }
            else -> SectionHeaderHolder(LayoutInflater.from(context).inflate(R.layout.refinements_section_header_layout, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: RefinementBaseViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class SectionHeaderHolder(itemView: View) : RefinementBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            var item = dataList[position].item as RefinementNavigation
            itemView.refinementSectionHeader.text = if (item.displayName.contentEquals(RefinementNavigationFragment.ON_PROMOTION)) context.resources.getString(R.string.refinement_show_me) else context.resources.getString(R.string.refinement_filter_by)
        }
    }

    inner class PromotionHolder(itemView: View) : RefinementBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val refinementSelectableItem = dataList[position]
            itemView.promotionSwitch.isChecked = refinementSelectableItem.isSelected
            itemView.promotionSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                refinementSelectableItem.isSelected = isChecked
                notifyDataSetChanged()
                val navigationItem = refinementSelectableItem.item as RefinementNavigation
                val navigationState = if (navigationItem.multiSelect) navigationItem.refinements[0].navigationState else navigationItem.refinementCrumbs[0].navigationState
                Utils.triggerFireBaseEvents(if (navigationItem.multiSelect) FirebaseManagerAnalyticsProperties.REFINE_EVENT_PROMO_ON else FirebaseManagerAnalyticsProperties.REFINE_EVENT_PROMO_OFF)
                listner.onBackPressedWithRefinement(navigationState)
            }
        }
    }

    inner class OptionsHolder(itemView: View) : RefinementBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            var item = dataList[position].item as RefinementNavigation
            var isBreadCrumbsExist = history.categoryDimensions.size > 0 && history.categoryDimensions[0].breadCrumbs.size > 0
            if ((item.displayName.contentEquals(RefinementNavigationFragment.CATEGORY) && isBreadCrumbsExist) || (TextUtils.isEmpty(item.displayName) && isBreadCrumbsExist)) {
                val breadCrumbs: ArrayList<BreadCrumb>? = history.categoryDimensions[0].breadCrumbs
                itemView.label.text = breadCrumbs!![breadCrumbs.size - 1].label
                if (item.refinements.size > 0) {
                    itemView.displayName.text = item.displayName
                    itemView.rightArrow.visibility = View.VISIBLE
                    itemView.isClickable = true
                } else {
                    itemView.displayName.text = RefinementNavigationFragment.CATEGORY
                    itemView.rightArrow.visibility = View.GONE
                    itemView.isClickable = false
                }

            } else {
                itemView.displayName.text = item.displayName
                itemView.rightArrow.visibility = View.VISIBLE
                if (item.refinementCrumbs.size > 0) {
                    itemView.label.text = item.refinementCrumbs.joinToString(",")
                }
            }
            itemView.refinementOptions.setOnClickListener {
                listner.onRefinementOptionSelected(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return dataList[position].type.value
    }
}