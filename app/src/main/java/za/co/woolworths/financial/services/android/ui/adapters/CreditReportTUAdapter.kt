package za.co.woolworths.financial.services.android.ui.adapters

import android.app.Activity
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.*
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.util.KotlinUtils

class CreditReportTUAdapter(val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class CreditReportViewType(val value: Int) { HEADER_TITLE(0), APP_FEATURE_LIST(1), PRIVACY_POLICY(2) }

    val featureList = arrayOf(R.string.access_your_full_credit_report, R.string.view_your_credit_overview_and_history,
            R.string.compare_your_bebt_level_to_your_income, R.string.understand_your_credit_score_changes)
    val featureDrawableList = arrayOf(R.drawable.document_icon, R.drawable.profile_icon, R.drawable.auto_icon, R.drawable.question_icon)
    private val policyNote = SpannableStringBuilder()
            .append(context.getString(R.string.privacy_policy_note_1))
            .append(getClickablePolicyNote())
            .append(context.getString(R.string.privacy_policy_note_3))
    val privacyPolicy = arrayOf(context.getString(R.string.transunion_personal_info_note), policyNote)

    companion object {
        const val ITEMS_COUNT = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            CreditReportViewType.HEADER_TITLE.value -> {
                HeaderTitleViewHolder(
                    LayoutCreditReportHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }
            CreditReportViewType.APP_FEATURE_LIST.value -> {
                AppFeatureViewHolder(
                    LayoutCreditReportAppFeatureListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }
            else -> PrivacyPolicyViewHolder(
                LayoutCreditReportPrivacyPolicyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
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

    private fun getClickablePolicyNote(): SpannableString {
        val spanableNote = SpannableString(context.getString(R.string.privacy_policy_note_2))
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                KotlinUtils.openUrlInPhoneBrowser(AppConfigSingleton.creditView?.transUnionPrivacyPolicyUrl, context)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = getColor(context, (R.color.permanent_card_block_desc_color))
            }
        }
        spanableNote.setSpan(clickableSpan, 0, spanableNote.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spanableNote
    }


    inner class HeaderTitleViewHolder(val itemBinding: LayoutCreditReportHeaderBinding) : RecyclerView.ViewHolder(itemBinding.root)
    inner class AppFeatureViewHolder(val itemBinding: LayoutCreditReportAppFeatureListBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind() {
            for ((i, feature) in featureList.withIndex()) {
                val listItem = LayoutCreditReportAppFeatureListItemBinding.inflate(LayoutInflater.from(context), null, false)
                listItem.appFeatureListTitleText.text = context.getString(feature)
                listItem.appFeatureListTitleText.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, featureDrawableList[i]), null, null, null)
                if (i == (featureList.size - 1))
                    listItem.layoutDivider.visibility = View.GONE
                itemBinding.appFeatureListContainer.addView(listItem.root)
            }
        }
    }

    inner class PrivacyPolicyViewHolder(val itemBinding: LayoutCreditReportPrivacyPolicyBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind() {
            val layoutInflater = LayoutInflater.from(context)
            for (policy in privacyPolicy) {
                val listItem = LayoutCreditReportPrivacyPolicyListItemBinding.inflate(layoutInflater, null, false)
                listItem.privacyPolicyListTitleText.text = policy
                listItem.privacyPolicyListTitleText.movementMethod = LinkMovementMethod.getInstance()
                itemBinding.creditReportPrivacyPolicyListContainer.addView(listItem.root)
            }
        }
    }
}
