package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.JoinRewardWalkthroughRowBinding
import za.co.woolworths.financial.services.android.ui.fragments.wreward.unique_locators.WRewardUniqueLocatorsHelper

class JoinRewardViewHolder(val itemBinding: JoinRewardWalkthroughRowBinding) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(imageDrawable: Int, title: Int, description: Int) {
        itemBinding.apply {
            with(itemView) {
                tvReasonToJoinTitle?.text = root.context.getString(title)
                val spanBuilder = updateWRewardCharacter(root.context, description)
                tvReasonToJoinDesc?.text = spanBuilder
                imHeader?.setImageResource(imageDrawable)
                WRewardUniqueLocatorsHelper.setLogOutFragLocators(absoluteAdapterPosition,imHeader,tvReasonToJoinTitle,tvReasonToJoinDesc)
            }
            uniqueIdsForRewards()
        }
    }

    private fun JoinRewardWalkthroughRowBinding.uniqueIdsForRewards() {
        root.context?.resources?.apply {
            infoLinearLayoutCompat?.contentDescription = getString(R.string.infoLayout)
        }
    }

    private fun updateWRewardCharacter(context: Context, description: Int): SpannableStringBuilder {
        val spanBuilder = SpannableStringBuilder(context.getString(description))
        with(spanBuilder) {
            if (contains("WRe")) {
                val rewardTextPosition = indexOf("WRewards")
                val updateWCharacterPosition = rewardTextPosition + 1
                setSpan(StyleSpan(Typeface.BOLD), rewardTextPosition, updateWCharacterPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(ForegroundColorSpan(Color.GRAY), rewardTextPosition, updateWCharacterPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        return spanBuilder
    }
}