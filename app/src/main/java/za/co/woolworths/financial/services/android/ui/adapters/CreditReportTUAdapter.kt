package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.layout_credit_report_app_feature_list.view.*
import kotlinx.android.synthetic.main.layout_credit_report_app_feature_list_item.view.*
import kotlinx.android.synthetic.main.layout_credit_report_privacy_policy.view.*
import kotlinx.android.synthetic.main.layout_credit_report_privacy_policy_list_item.view.*

class CreditReportTUAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class CreditReportViewType(val value: Int) { HEADER_TITLE(0), APP_FEATURE_LIST(1), PRIVACY_POLICY(2) }

    val featureList = arrayOf(R.string.access_your_full_credit_report, R.string.view_your_credit_overview_and_history,
            R.string.compare_your_bebt_level_to_your_income, R.string.understand_your_credit_score_changes)
    val featureDrawableList = arrayOf(R.drawable.document_icon, R.drawable.profile_icon, R.drawable.auto_icon, R.drawable.question_icon)
    val privacyPolicy = arrayOf(R.string.transunion_personal_info_note, R.string.privacy_policy_note)

    companion object {
        const val ITEMS_COUNT = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            CreditReportViewType.HEADER_TITLE.value -> {
                HeaderTitleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_credit_report_header, parent, false))
            }
            CreditReportViewType.APP_FEATURE_LIST.value -> {
                AppFeatureViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_credit_report_app_feature_list, parent, false))
            }
            else -> PrivacyPolicyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_credit_report_privacy_policy, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AppFeatureViewHolder -> {
                holder.bind()
            }
            is PrivacyPolicyViewHolder -> {
                holder.bind()
            }
        }
    }

    override fun getItemCount(): Int = ITEMS_COUNT

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> CreditReportViewType.HEADER_TITLE.value
            1 -> CreditReportViewType.APP_FEATURE_LIST.value
            2 -> CreditReportViewType.PRIVACY_POLICY.value
            else -> -1
        }
    }

    inner class HeaderTitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class AppFeatureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind() {
            for ((i, feature) in featureList.withIndex()) {
                val listItem = LayoutInflater.from(context).inflate(R.layout.layout_credit_report_app_feature_list_item, null, false)
                listItem.app_feature_list_title_text.text = context.getString(feature)
                listItem.app_feature_list_title_text.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, featureDrawableList[i]), null, null, null)
                if (i == (featureList.size - 1))
                    listItem.layoutDivider.visibility = View.GONE
                itemView.app_feature_list_container.addView(listItem)
            }
        }
    }

    inner class PrivacyPolicyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            val layoutInflater = LayoutInflater.from(context)
            for (policy in privacyPolicy) {
                val listItem = layoutInflater.inflate(R.layout.layout_credit_report_privacy_policy_list_item, null, false)
                listItem.privacy_policy_list_title_text.text = context.getString(policy)
                itemView.credit_report_privacy_policy_list_container.addView(listItem)
            }
        }
    }
}
