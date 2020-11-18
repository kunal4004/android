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

class CreditReportTUAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val featureList = arrayOf(R.string.access_your_full_credit_report, R.string.view_your_credit_overview_and_history,
            R.string.compare_your_bebt_level_to_your_income, R.string.understand_your_credit_score_changes)
    val featureDrawableList = arrayOf(R.drawable.document_icon, R.drawable.profile_icon, R.drawable.auto_icon, R.drawable.question_icon)

    companion object {
        const val ITEMS_COUNT = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AppFeatureViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_credit_report_app_feature_list, parent, false))

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AppFeatureViewHolder -> {
                holder.bind()
            }
        }
    }

    override fun getItemCount(): Int = ITEMS_COUNT

    inner class AppFeatureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind() {
            for ((i, feature) in featureList.withIndex()) {
                val listItem = LayoutInflater.from(context).inflate(R.layout.layout_credit_report_app_feature_list_item, null, false)
                listItem.app_feature_list_title_text.text = context.getString(feature)
                listItem.app_feature_list_title_text.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, featureDrawableList[i]), null, null, null)
                itemView.app_feature_list_container.addView(listItem)
            }
        }
    }
}
