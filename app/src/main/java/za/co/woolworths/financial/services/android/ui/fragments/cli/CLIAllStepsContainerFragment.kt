package za.co.woolworths.financial.services.android.ui.fragments.cli

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.cli_step_indicator_layout.*
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity
import za.co.woolworths.financial.services.android.ui.extension.bindColor
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.getFuturaMediumFont
import za.co.woolworths.financial.services.android.ui.extension.getFuturaSemiBoldFont
import za.co.woolworths.financial.services.android.util.controller.CLIStepIndicatorListener

class CLIAllStepsContainerFragment : Fragment(), CLIStepIndicatorListener {

    private var mCurrentStepTypeface: Typeface? = getFuturaSemiBoldFont()
    private var mDefaultStepTypeFace: Typeface? = getFuturaMediumFont()

    private val listOfIndicators: MutableList<Triple<FrameLayout?, TextView?, TextView?>>? =
            mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.cli_all_steps_container_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listOfIndicators?.apply {
            add(Triple(incomeIndicatorRelativeLayout, incomeIndicatorTextView, incomeTitleTextView))
            add(Triple(expenseIndicatorRelativeLayout, expenseIndicatorTextView, expenseTitleTextView))
            add(Triple(offerIndicatorRelativeLayout, otherIndicatorTextView, offerTitleTextView))
            add(Triple(completeIndicatorRelativeLayout, completeIndicatorTextView, completeTitleTextView))
        }

        (activity as? CLIPhase2Activity)?.initFragment(this)
    }

    private fun updateStepIndicator(position: Int) {
        val stepNumber = position - 1
        listOfIndicators?.forEachIndexed { index, element ->
            with(element) {
                when {
                    index < stepNumber -> {
                        first?.background = (bindDrawable(R.drawable.cli_step_indicator_active))
                        second?.visibility = View.INVISIBLE
                        third?.apply {
                            typeface = mDefaultStepTypeFace
                            setTextColor(bindColor(R.color.cli_step_indicator_done_text_color))
                        }
                    }
                    index == stepNumber -> {
                        first?.background =
                                (bindDrawable(R.drawable.cli_step_indicator_background_current_screen))
                        second?.apply {
                            visibility = View.VISIBLE
                            setTextColor(bindColor(R.color.white))
                        }
                        third?.apply {
                            typeface = mCurrentStepTypeface
                            setTextColor(bindColor(R.color.black))
                        }
                    }
                    else -> {
                        first?.background =
                                (bindDrawable(R.drawable.cli_step_indicator_background_next_screen))
                        second?.apply {
                            visibility = View.VISIBLE
                            setTextColor(bindColor(R.color.mask_opacity))
                        }
                        third?.apply {
                            typeface = mDefaultStepTypeFace
                            setTextColor(bindColor(R.color.mask_opacity))
                        }
                    }
                }
            }
        }
    }

    override fun onStepSelected(step: Int) {
        updateStepIndicator(step)
    }
}