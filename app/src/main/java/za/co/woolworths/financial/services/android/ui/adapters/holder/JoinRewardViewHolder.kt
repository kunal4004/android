package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.join_reward_walkthrough_row.view.*

class JoinRewardViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    constructor(parent: ViewGroup) : this(LayoutInflater.from(parent.context).inflate(R.layout.join_reward_walkthrough_row, parent, false))

    fun bind(imageDrawable: Int, title: Int, description: Int) {
        itemView.context?.apply {
            with(itemView) {
                tvReasonToJoinTitle?.text = getString(title)
                tvReasonToJoinDesc?.text = getString(description)
                imHeader?.setImageResource(imageDrawable)
            }
        }
    }
}