package za.co.woolworths.financial.services.android.ui.activities

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.*
import za.co.woolworths.financial.services.android.util.AppConstant

class DashDetailsAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val featureList = arrayOf(R.string.dash_details_what_you_ll_love, R.string.app_feature_get_delivered,
            R.string.app_feature_world_class_freshness, R.string.app_feature_track_order)
    val featureDrawableList = arrayOf(-1,R.drawable.ic_scooter_grey, R.drawable.ic_cart_grey, R.drawable.ic_location_grey)
    val termsAndConditions = arrayOf(R.string.terms_and_condition_not_linked, R.string.terms_and_condition_wreward_vouchers)

    companion object {
        const val ITEMS_COUNT = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            AppConstant.DashDetailsViewType.HEADER_TITLE.value -> {
                HeaderTitleViewHolder(
                    LayoutDashDetailsHeaderTitlesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }
            AppConstant.DashDetailsViewType.APP_FEATURE_LIST.value -> {
                AppFeatureViewHolder(
                    LayoutDashDetailsAppFeatureListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }
            else -> TermsAndConditionsViewHolder(
                LayoutDashDetailsTermsAndConditionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AppFeatureViewHolder -> {
                holder.bind()
            }
            is TermsAndConditionsViewHolder -> {
                holder.bind()
            }
        }
    }

    override fun getItemCount(): Int = ITEMS_COUNT

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> AppConstant.DashDetailsViewType.HEADER_TITLE.value
            1 -> AppConstant.DashDetailsViewType.APP_FEATURE_LIST.value
            2 -> AppConstant.DashDetailsViewType.TERMS_AND_CONDITION.value
            else -> -1
        }
    }

    inner class HeaderTitleViewHolder(val itemBinding: LayoutDashDetailsHeaderTitlesBinding) : RecyclerView.ViewHolder(itemBinding.root)
    inner class AppFeatureViewHolder(val itemBinding: LayoutDashDetailsAppFeatureListBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind() {
            for ((i, feature) in featureList.withIndex()) {
                val listItem = LayoutDashDetailsAppFeatureListItemBinding.inflate(LayoutInflater.from(context), null, false)
                listItem.appFeatureListTitleText.text = context.getString(feature)
                listItem.appFeatureListTitleText.setCompoundDrawablesWithIntrinsicBounds(if(featureDrawableList[i] != -1) ContextCompat.getDrawable(context, featureDrawableList[i]) else null, null, null, null)
                itemBinding.appFeatureListContainer.addView(listItem.root)
            }
        }
    }

    inner class TermsAndConditionsViewHolder(val itemBinding: LayoutDashDetailsTermsAndConditionBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind() {
            val layoutInflater = LayoutInflater.from(context)
            for (term in termsAndConditions) {
                val listItem = LayoutDashTermsAndConditionsListItemBinding.inflate(layoutInflater, null, false)
                listItem.termsListTitleText.text = context.getString(term)
                itemBinding.dashTermsAndConditionsListContainer.addView(listItem.root)
            }
        }
    }
}
