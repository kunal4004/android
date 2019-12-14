package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.more_benefit_child_item.view.*
import za.co.woolworths.financial.services.android.util.expand.ChildViewHolder

class MoreBenefitChildViewHolder(itemView: View) : ChildViewHolder(itemView) {
    fun bind(description: String?) {
        if (description?.contains("||") == true) {
            itemView.bulletsDescriptionLinearLayout?.removeAllViews()
            val splitDescription = description.split("||")
            itemView.context?.apply {
                splitDescription.forEach { items ->
                    val view = View.inflate(this, R.layout.account_sales_bullet_item, null)
                    val titleTextView: TextView? = view?.findViewById(R.id.bulletTitleTextView)
                    titleTextView?.text = items
                    itemView.bulletsDescriptionLinearLayout?.addView(view)
                }
            }
            itemView.moreBenefitDescriptionTextView?.visibility = GONE
            itemView.bulletsDescriptionLinearLayout?.visibility = VISIBLE
        } else {
            itemView.moreBenefitDescriptionTextView?.text = description
            itemView.bulletsDescriptionLinearLayout?.visibility = GONE
            itemView.moreBenefitDescriptionTextView?.visibility = VISIBLE
        }
    }
}