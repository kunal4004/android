package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.MoreBenefitChildItemBinding
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.utils.UniqueIdentifiers
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.utils.setContentDescription
import za.co.woolworths.financial.services.android.util.expand.ChildViewHolder

class MoreBenefitChildViewHolder(val binding: MoreBenefitChildItemBinding) : ChildViewHolder(binding.root) {
    fun bind(description: String?, sectionTitle: String, position: Int) {
        with(binding){
            if (description?.contains("||") == true) {
                bulletsDescriptionLinearLayout.removeAllViews()
                val splitDescription = description.split("||")
                root.context?.apply {
                    splitDescription.forEach { items ->
                        val view = View.inflate(this, R.layout.account_sales_bullet_item, null)
                        val titleTextView: TextView? = view?.findViewById(R.id.bulletTitleTextView)
                        titleTextView?.text = items
                        bulletsDescriptionLinearLayout.addView(view)
                    }
                }
                moreBenefitDescriptionTextView.visibility = GONE
                bulletsDescriptionLinearLayout.visibility = VISIBLE
            } else {
                moreBenefitDescriptionTextView.text = description
                bulletsDescriptionLinearLayout.visibility = GONE
                moreBenefitDescriptionTextView.visibility = VISIBLE
                moreBenefitDescriptionTextView.setContentDescription(sectionTitle, position = position,viewName = UniqueIdentifiers.Description)
            }
        }
    }
}