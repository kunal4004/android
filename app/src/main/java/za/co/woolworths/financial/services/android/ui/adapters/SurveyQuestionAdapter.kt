package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.row_survey_question_rate_slider.view.*

class SurveyQuestionAdapter(val context: Context, val questionList: ArrayList<Int>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class QuestionType(val value: Int) { RATE_SLIDER(0), FREE_TEXT(1) }

    override fun getItemCount(): Int = questionList.size

    override fun getItemViewType(position: Int): Int =
            if (position == 0) QuestionType.RATE_SLIDER.value else QuestionType.FREE_TEXT.value // TODO: update logic

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            QuestionType.RATE_SLIDER.value -> {
                RateSliderViewHolder(LayoutInflater.from(context).inflate(R.layout.row_survey_question_rate_slider, parent, false))
            }
            else -> {
                FreeTextViewHolder(LayoutInflater.from(context).inflate(R.layout.row_survey_question_free_text, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RateSliderViewHolder -> {
                holder.bind()
            }
            is FreeTextViewHolder -> {
                holder.bind()
            }
        }
    }

    inner class RateSliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            itemView.apply {
                // TODO
                sbRateSlider.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        tvTooltipRateSlider.text = progress.toString()
                        tvTooltipRateSlider.x = sbRateSlider.thumb.bounds.left.toFloat() + (sbRateSlider.thumb.bounds.width() / 2 - tvTooltipRateSlider.width / 2)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {

                    }
                })
            }
        }
    }

    inner class FreeTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            itemView.apply {
                // TODO
            }
        }
    }
}