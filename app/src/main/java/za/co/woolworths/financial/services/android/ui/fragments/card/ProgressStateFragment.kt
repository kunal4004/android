package za.co.woolworths.financial.services.android.ui.fragments.card

import android.app.Fragment
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.circle_progress_layout.*
import za.co.woolworths.financial.services.android.ui.views.tick_animation.Direction
import za.co.woolworths.financial.services.android.ui.views.tick_animation.UnitPosition

class ProgressStateFragment : Fragment() {

    companion object {
        fun newInstance() = ProgressStateFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.circle_progress_layout, container, false)
    }

    fun animationStart() {
        circularProgressIndicator?.apply {
            spinSpeed = 4.5f
            setValue(0f)
            setDirection(Direction.CW)
            setUnitPosition(UnitPosition.RIGHT_TOP)
        }
        circularProgressIndicator?.spin()
    }

    fun animateSuccessEnd() {
        circularProgressIndicator?.apply {
            stopSpinning()
            setValueAnimated(100f)
        }
        success_tick?.apply {
            visibility = View.VISIBLE
            startTickAnim()
        }
    }
}