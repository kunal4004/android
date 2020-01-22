package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.join_reward_walkthrough_row.view.*
import android.text.SpannableStringBuilder
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan

class JoinRewardViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    constructor(parent: ViewGroup) : this(LayoutInflater.from(parent.context).inflate(R.layout.join_reward_walkthrough_row, parent, false))

    fun bind(imageDrawable: Int, title: Int, description: Int) {
        itemView.context?.apply {
            with(itemView) {
                tvReasonToJoinTitle?.text = getString(title)
                val spanBuilder = updateWRewardCharacter(description)
                tvReasonToJoinDesc?.text = spanBuilder
                imHeader?.setImageResource(imageDrawable)
            }
        }
        uniqueIdsForRewards()
    }

    private fun uniqueIdsForRewards() {
        itemView.context?.resources?.apply {
            itemView.infoLinearLayoutCompat?.contentDescription = getString(R.string.infoLayout)
        }
    }

    private fun Context.updateWRewardCharacter(description: Int): SpannableStringBuilder {
        val spanBuilder = SpannableStringBuilder(getString(description))
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