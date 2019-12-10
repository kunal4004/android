package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.View
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.RotateAnimation
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder
import kotlinx.android.synthetic.main.more_benefit_parent_item.view.*
import za.co.woolworths.financial.services.android.models.dto.account.MoreBenefits

class MoreBenefitsParentViewHolder(itemView: View) : GroupViewHolder(itemView) {

    companion object {
        const val FROM_DEGREE = 360f
        const val TO_DEGREE = 180f
        const val PIVOT_X_VALUE = 0.5f
        const val DURATION: Long = 300
    }

    fun setBenefitParentItem(moreBenefit: MoreBenefits) {
        itemView.moreBenefitsTitleTextView?.text = moreBenefit.title
        itemView.moreBenefitsIconImageView?.setImageResource(moreBenefit.iconResId)
    }

    override fun expand() = animateExpand()

    override fun collapse() = animateCollapse()

    private fun animateExpand() {
        val rotate =
                RotateAnimation(FROM_DEGREE, TO_DEGREE, RELATIVE_TO_SELF, PIVOT_X_VALUE, RELATIVE_TO_SELF, PIVOT_X_VALUE)
        rotate.duration = DURATION
        rotate.fillAfter = true
        itemView.moreBenefitsArrowImageView?.animation = rotate
    }

    private fun animateCollapse() {
        val rotate =
                RotateAnimation(TO_DEGREE, FROM_DEGREE, RELATIVE_TO_SELF, PIVOT_X_VALUE, RELATIVE_TO_SELF, PIVOT_X_VALUE)
        rotate.duration = DURATION
        rotate.fillAfter = true
        itemView.moreBenefitsArrowImageView?.animation = rotate
    }
}