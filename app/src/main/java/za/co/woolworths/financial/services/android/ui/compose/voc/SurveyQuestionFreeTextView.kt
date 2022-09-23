package za.co.woolworths.financial.services.android.ui.compose.voc

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.SeekBar
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.view_survey_question_free_text.view.*
import kotlinx.android.synthetic.main.view_survey_question_rate_slider.view.*
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyAnswer
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyQuestion
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerActivity

class SurveyQuestionFreeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyle, defStyleRes) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_survey_question_free_text, this, true)
    }

    fun bind(question: SurveyQuestion, answer: SurveyAnswer?, callback: (Long, String) -> Unit) {
        tvTitleFreeText.text = question.title
        etValueFreeText.hint = resources.getString(if (question.required == true) R.string.voc_question_freetext_hint_required else R.string.voc_question_freetext_hint_optional)

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