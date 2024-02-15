package za.co.woolworths.financial.services.android.ui.adapters

import android.app.Activity
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.RefinementsOnPromotionLayoutBinding
import com.awfs.coordination.databinding.RefinementsOptionsLayoutBinding
import com.awfs.coordination.databinding.RefinementsSectionHeaderLayoutBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.BreadCrumb
import za.co.woolworths.financial.services.android.models.dto.RefinementHistory
import za.co.woolworths.financial.services.android.models.dto.RefinementNavigation
import za.co.woolworths.financial.services.android.models.dto.RefinementSelectableItem
import za.co.woolworths.financial.services.android.ui.adapters.holder.RefinementBaseViewHolder
import za.co.woolworths.financial.services.android.ui.fragments.product.refine.RefinementNavigationFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefinementOptionSelected
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.security.InvalidKeyException

class RefinementNavigationAdapter(val context: Activity, val listner: OnRefinementOptionSelected, var dataList: ArrayList<RefinementSelectableItem>, var history: RefinementHistory) : RecyclerView.Adapter<RefinementBaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RefinementBaseViewHolder {
        return when (viewType) {
            RefinementSelectableItem.ViewType.SECTION_HEADER.value -> {
                SectionHeaderHolder(
                    RefinementsSectionHeaderLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
            RefinementSelectableItem.ViewType.PROMOTION.value -> {
                PromotionHolder(
                    RefinementsOnPromotionLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
            RefinementSelectableItem.ViewType.OPTIONS.value -> {
                OptionsHolder(
                    RefinementsOptionsLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
            else -> SectionHeaderHolder(
                RefinementsSectionHeaderLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: RefinementBaseViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class SectionHeaderHolder(val itemBinding: RefinementsSectionHeaderLayoutBinding) : RefinementBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            var item = dataList[position].item as RefinementNavigation
            itemBinding.refinementSectionHeader.text = if (item.displayName.contentEquals(RefinementNavigationFragment.ON_PROMOTION)) context.resources.getString(R.string.refinement_show_me) else context.resources.getString(R.string.refinement_filter_by)
        }
    }

    inner class PromotionHolder(val itemBinding: RefinementsOnPromotionLayoutBinding) : RefinementBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            val refinementSelectableItem = dataList.getOrNull(position)
            refinementSelectableItem?.let {
                itemBinding.promotionSwitch.isChecked = refinementSelectableItem.isSelected
                itemBinding.promotionSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                    refinementSelectableItem.isSelected = isChecked
                    notifyDataSetChanged()
                    val navigationItem = refinementSelectableItem.item as RefinementNavigation
                    val navigationState = if (navigationItem.multiSelect)
                        navigationItem.refinements.getOrNull(0)?.navigationState
                    else
                        navigationItem.refinementCrumbs.getOrNull(0)?.navigationState
                    Utils.triggerFireBaseEvents(
                        if (navigationItem.multiSelect) FirebaseManagerAnalyticsProperties.REFINE_EVENT_PROMO_ON
                        else FirebaseManagerAnalyticsProperties.REFINE_EVENT_PROMO_OFF,
                        context
                    )
                    if (navigationState != null) {
                        listner.onBackPressedWithRefinement(navigationState, false)
                    } else {
                        FirebaseManager.logException(InvalidKeyException("navigation state is null."))
                    }
                }
            }
        }
    }

    inner class OptionsHolder(val itemBinding: RefinementsOptionsLayoutBinding) : RefinementBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            var item = dataList[position].item as RefinementNavigation
            var isBreadCrumbsExist = history.categoryDimensions.size > 0 && history.categoryDimensions[0].breadCrumbs.size > 0
            if ((item.displayName.contentEquals(RefinementNavigationFragment.CATEGORY) && isBreadCrumbsExist) || (TextUtils.isEmpty(item.displayName) && isBreadCrumbsExist)) {
                val breadCrumbs: ArrayList<BreadCrumb>? = history.categoryDimensions[0].breadCrumbs
                itemBinding.label.text = breadCrumbs!![breadCrumbs.size - 1].label
                if (item.refinements.size > 0) {
                    itemBinding.displayName.text = item.displayName
                    itemBinding.rightArrow.visibility = View.VISIBLE
                    itemBinding.root.isClickable = true
                } else {
                    itemBinding.displayName.text = RefinementNavigationFragment.CATEGORY
                    itemBinding.rightArrow.visibility = View.GONE
                    itemBinding.root.isClickable = false
                }

            } else {
                itemBinding.displayName.text = item.displayName
                itemBinding.rightArrow.visibility = View.VISIBLE
                if (item.refinementCrumbs.size > 0) {
                    itemBinding.label.text = item.refinementCrumbs.joinToString(",")
                }
            }
            itemBinding.refinementOptions.setOnClickListener {
                listner.onRefinementOptionSelected(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return dataList[position].type.value
    }
}