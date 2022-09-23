package za.co.woolworths.financial.services.android.ui.compose.voc

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.SeekBar
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.view_survey_question_rate_slider.view.*
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyAnswer
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyQuestion
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerActivity

class SurveyQuestionRateSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyle, defStyleRes) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_survey_question_rate_slider, this, true)
    }

    fun bind(question: SurveyQuestion, answer: SurveyAnswer?, callback: (Long, Int) -> Unit) {
        val minValue = (question.minValue ?: VoiceOfCustomerActivity.DEFAULT_VALUE_RATE_SLIDER_MIN) - 1
        val maxValue = (question.maxValue ?: VoiceOfCustomerActivity.DEFAULT_VALUE_RATE_SLIDER_MAX) - 1

        tvTitleRateSlider.text = question.title ?: ""
        tvDescRateSlider.text = resources.getString(R.string.voc_question_slider_desc, minValue, maxValue)

        sbRateSlider.max = maxValue - minValue // setMin() requires min API level 26. Doing this as a workaround.
        sbRateSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val selectedValue = progress + minValue
                tvTooltipRateSlider.text = selectedValue.toString()
                tvTooltipRateSlider.post {
                    tvTooltipRateSlider.x = sbRateSlider.thumb.bounds.left.toFloat() + (sbRateSlider.thumb.bounds.width() / 2 - tvTooltipRateSlider.width / 2)
                }
                ivTooltipRateSliderBg.x = sbRateSlider.thumb.bounds.left.toFloat() + (sbRateSlider.thumb.bounds.width() / 2 - ivTooltipRateSliderBg.width / 2)
                callback.invoke(question.id, selectedValue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { }

            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

        tvTooltipRateSlider.post {
            if (answer?.answerId != null) {
                sbRateSlider.progress = answer.answerId!! - minValue
            } else {
                sbRateSlider.progress = sbRateSlider.max
            }
        }
    }
}