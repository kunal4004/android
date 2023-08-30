package za.co.woolworths.financial.services.android.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FaqRowBinding
import za.co.woolworths.financial.services.android.contracts.ISelectQuestionListener
import za.co.woolworths.financial.services.android.models.dto.FAQDetail


class FAQAdapter(
    userDetail: List<FAQDetail>?,
    selectedQuestion: ISelectQuestionListener,
) : RecyclerView.Adapter<FAQAdapter.FaqViewHolder?>() {

    private val mSelectedQuestion: ISelectQuestionListener
    private var selectedIndex = -1
    private val mDataSet: List<FAQDetail>?

    init {
        mDataSet = userDetail
        mSelectedQuestion = selectedQuestion
    }

    class FaqViewHolder(val itemBinding: FaqRowBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: FAQDetail) {
            itemBinding.name.text = item.question
        }
    }

    override fun onBindViewHolder(
        holder: FaqViewHolder,
        @SuppressLint("RecyclerView") position: Int,
    ) {
        mDataSet?.getOrNull(position)?.let { holder.bind(it) }
        holder.itemView.setOnClickListener {
            selectedIndex = position
            if (selectedIndex < itemCount) mSelectedQuestion.onQuestionSelected(mDataSet?.getOrNull(
                selectedIndex))
            notifyDataSetChanged()
        }
        if (selectedIndex == position) {
            backgroundDrawable(holder, R.drawable.pressed_bg)
        } else {
            backgroundDrawable(holder, R.drawable.stores_details_background)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        return FaqViewHolder(
            FaqRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return mDataSet?.size ?: 0
    }

    fun resetIndex() {
        selectedIndex = -1
        notifyDataSetChanged()
    }

    private fun backgroundDrawable(holder: FaqViewHolder, id: Int) {
        holder.itemBinding.relFAQRow.background =
            ContextCompat.getDrawable(holder.itemBinding.relFAQRow.context, id)
    }
}