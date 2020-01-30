package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.content.res.TypedArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_walkthrough_item.view.*

class AccountWalkThroughViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    constructor(parent: ViewGroup) : this(LayoutInflater.from(parent.context).inflate(R.layout.account_walkthrough_item, parent, false))

    fun bind(walkThroughItems: Triple<Array<String>?, TypedArray?, Array<String>?>?) {
        with(itemView) {
            accountOnboardingTitleTextView?.text = walkThroughItems?.first?.get(adapterPosition)
            accountOnboardingDescriptionTextView?.text = walkThroughItems?.third?.get(adapterPosition)
            walkThroughItems?.second?.getResourceId(adapterPosition, -1)?.let { imageId -> boardingImageView?.setImageResource(imageId) }
        }
    }
}