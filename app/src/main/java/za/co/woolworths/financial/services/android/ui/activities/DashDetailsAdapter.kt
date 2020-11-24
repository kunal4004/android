package za.co.woolworths.financial.services.android.ui.activities

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.layout_dash_details_app_feature_list.view.*
import kotlinx.android.synthetic.main.layout_dash_details_app_feature_list_item.view.*
import kotlinx.android.synthetic.main.layout_dash_details_app_feature_list_item.view.app_feature_list_title_text
import kotlinx.android.synthetic.main.layout_dash_details_terms_and_condition.view.*
import kotlinx.android.synthetic.main.layout_dash_terms_and_conditions_list_item.view.*
import za.co.woolworths.financial.services.android.util.AppConstant


class DashDetailsAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val featureList = arrayOf(R.string.dash_details_what_you_ll_love, R.string.app_feature_get_delivered,
            R.string.app_feature_world_class_freshness, R.string.app_feature_track_order)
    val featureDrawableList = arrayOf(-1,R.drawable.ic_scooter, R.drawable.ic_cart, R.drawable.ic_location)
    val termsAndConditions = arrayOf(R.string.terms_and_condition_not_linked, R.string.terms_and_condition_wreward_vouchers)

    companion object {
        const val ITEMS_COUNT = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            AppConstant.DashDetailsViewType.HEADER_TITLE.value -> {
                HeaderTitleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_dash_details_header_titles, parent, false))
            }
            AppConstant.DashDetailsViewType.APP_FEATURE_LIST.value -> {
                AppFeatureViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_dash_details_app_feature_list, parent, false))
            }
            else -> TermsAndConditionsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_dash_details_terms_and_condition, parent, false))
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

    inner class HeaderTitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class AppFeatureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind() {
            for ((i, feature) in featureList.withIndex()) {
                val listItem = LayoutInflater.from(context).inflate(R.layout.layout_dash_details_app_feature_list_item, null, false)
                listItem.app_feature_list_title_text.text = context.getString(feature)
                listItem.app_feature_list_title_text.setCompoundDrawablesWithIntrinsicBounds(if(featureDrawableList[i] != -1) ContextCompat.getDrawable(context, featureDrawableList[i]) else null, null, null, null)
                itemView.app_feature_list_container.addView(listItem)
            }
        }
    }

    inner class TermsAndConditionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            val layoutInflater = LayoutInflater.from(context)
            for (term in termsAndConditions) {
                val listItem = layoutInflater.inflate(R.layout.layout_dash_terms_and_conditions_list_item, null, false)
                listItem.terms_list_title_text.text = context.getString(term)
                itemView.dash_terms_and_conditions_list_container.addView(listItem)
            }
        }
    }
}
