package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider.animation

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface IAnimation {
    fun fadeIn(view: View, hexStringColor: Pair<Int, Int>)
}

class AnimationImpl @Inject constructor(@ApplicationContext private val context: Context) :
    IAnimation {
    private val DURATION: Long = 1000
    override fun fadeIn(view: View, hexStringColor: Pair<Int, Int>) {
        val from = ContextCompat.getColor(context, hexStringColor.first)
        val to = ContextCompat.getColor(context, hexStringColor.second)

        val anim = ValueAnimator()
        anim.setIntValues(from, to)
        anim.setEvaluator(ArgbEvaluator())
        anim.addUpdateListener { valueAnimator -> view.setBackgroundColor(valueAnimator.animatedValue as Int) }

        anim.duration = DURATION
        anim.start()
    }

}