package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.row_survey_footer.view.*
import kotlinx.android.synthetic.main.row_survey_question_free_text.view.*
import kotlinx.android.synthetic.main.row_survey_question_rate_slider.view.*
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyAnswer
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyQuestion
import za.co.woolworths.financial.services.android.ui.fragments.voc.SurveyAnswerDelegate


class SurveyQuestionAdapter(
        val context: Context,
        private val questions: List<SurveyQuestion>,
        val delegate: SurveyAnswerDelegate
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_FOOTER = -1
    }

    override fun getItemCount(): Int = questions.size + 1

    override fun getItemViewType(position: Int): Int {
        if (position < questions.size) {
            return SurveyQuestion.QuestionType.ofType(questions[position].type)?.viewType
                    ?: SurveyQuestion.QuestionType.FREE_TEXT.viewType
        }
        return VIEW_TYPE_FOOTER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_FOOTER -> FooterViewHolder(LayoutInflater.from(context).inflate(R.layout.row_survey_footer, parent, false))
            SurveyQuestion.QuestionType.RATE_SLIDER.viewType -> RateSliderViewHolder(LayoutInflater.from(context).inflate(R.layout.row_survey_question_rate_slider, parent, false))
            else -> FreeTextViewHolder(LayoutInflater.from(context).inflate(R.layout.row_survey_question_free_text, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FooterViewHolder -> {
                holder.bind(
                        submitCallback = {
                            delegate.onSubmit()
                        },
                        optOutCallback = {
                            delegate.onOptOut()
                        }
                )
            }
            is RateSliderViewHolder -> {
                holder.bind(questions[position], delegate.getAnswer(questions[position].id)) { questionId, value ->
                    delegate.onInputRateSlider(questionId, value)
                }
            }
            is FreeTextViewHolder -> {
                holder.bind(questions[position], delegate.getAnswer(questions[position].id)) { questionId, value ->
                    delegate.onInputFreeText(questionId, value)
                }
            }
        }
    }

    inner class RateSliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(question: SurveyQuestion, answer: SurveyAnswer?, callback: (Long, Int) -> Unit) {
            itemView.apply {
                val minValue = question.minValue ?: 0
                val maxValue = question.maxValue ?: 0

                tvTitleRateSlider.text = question.title ?: ""

                sbRateSlider.max = maxValue - minValue // setMin() requires min API level 26. Doing this as a workaround.
                sbRateSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        val selectedValue = progress + minValue
                        tvTooltipRateSlider.text = selectedValue.toString()
                        tvTooltipRateSlider.x = sbRateSlider.thumb.bounds.left.toFloat() + (sbRateSlider.thumb.bounds.width() / 2 - tvTooltipRateSlider.width / 2)
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
    }

    inner class FreeTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(question: SurveyQuestion, answer: SurveyAnswer?, callback: (Long, String) -> Unit) {
            itemView.apply {
                tvTitleFreeText.text = question.title

                (etValueFreeText.tag as? TextWatcher)?.let {
                    etValueFreeText.removeTextChangedListener(it)
                }

                if (answer?.textAnswer != null) {
                    etValueFreeText.setText(answer?.textAnswer)
                } else {
                    etValueFreeText.text = null
                }

                val textWatcher = object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        callback.invoke(question.id, etValueFreeText.text.toString())
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
                }
                etValueFreeText.addTextChangedListener(textWatcher)
                etValueFreeText.tag = textWatcher

                // Hide keyboard as soon as EditText is out of focus during scroll
                etValueFreeText.setOnFocusChangeListener { v, hasFocus ->
                    if (!hasFocus && v != null) {
                        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
                            hideSoftInputFromWindow(v.windowToken, 0)
                        }
                    }
                }
            }
        }
    }

    inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(submitCallback: () -> Unit, optOutCallback: () -> Unit) {
            itemView.apply {
                btnSurveySubmit.setOnClickListener {
                    submitCallback.invoke()
                }
                btnSurveyOptOut.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                btnSurveyOptOut.setOnClickListener {
                    optOutCallback.invoke()
                }
            }
        }
    }
}