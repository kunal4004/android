package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R

class SurveyQuestionAdapter(val context: Context, val questionList: ArrayList<Int>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class QuestionType(val value: Int) { RATE_SLIDER(0), FREE_TEXT(1) }

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

    override fun getItemCount(): Int = questionList.size

    override fun getItemViewType(position: Int): Int =
            if (position == 0) QuestionType.RATE_SLIDER.value else QuestionType.FREE_TEXT.value // TODO: update logic

    inner class RateSliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            itemView.apply {
                // TODO
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