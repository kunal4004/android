package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.MoreBenefitChildItemBinding
import za.co.woolworths.financial.services.android.util.expand.ChildViewHolder

class MoreBenefitChildViewHolder(val binding: MoreBenefitChildItemBinding) : ChildViewHolder(binding.root) {
    fun bind(description: String?) {
        if (description?.contains("||") == true) {
            binding.bulletsDescriptionLinearLayout?.removeAllViews()
            val splitDescription = description.split("||")
            binding.root.context?.apply {
                splitDescription.forEach { items ->
                    val view = View.inflate(this, R.layout.account_sales_bullet_item, null)
                    val titleTextView: TextView? = view?.findViewById(R.id.bulletTitleTextView)
                    titleTextView?.text = items
                    binding.bulletsDescriptionLinearLayout?.addView(view)
                }
            }
            binding.moreBenefitDescriptionTextView?.visibility = GONE
            binding.bulletsDescriptionLinearLayout?.visibility = VISIBLE
        } else {
            binding.moreBenefitDescriptionTextView?.text = description
            binding.bulletsDescriptionLinearLayout?.visibility = GONE
            binding.moreBenefitDescriptionTextView?.visibility = VISIBLE
        }
    }
}