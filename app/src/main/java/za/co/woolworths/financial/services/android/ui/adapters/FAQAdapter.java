package za.co.woolworths.financial.services.android.ui.adapters;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.contracts.ISelectQuestionListener;
import za.co.woolworths.financial.services.android.models.dto.FAQDetail;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.SimpleViewHolder> {

	private ISelectQuestionListener mSelectedQuestion;
	private int selectedIndex = -1;
	private List<FAQDetail> mDataSet;

	public FAQAdapter(List<FAQDetail> userDetail,
					  ISelectQuestionListener selectedQuestion) {
		this.mDataSet = userDetail;
		this.mSelectedQuestion = selectedQuestion;
	}

	class SimpleViewHolder extends RecyclerView.ViewHolder {
		WTextView mFAQName;
		RelativeLayout mRelFAQRow;

		SimpleViewHolder(View view) {
			super(view);
			mFAQName = view.findViewById(R.id.name);
			mRelFAQRow = view.findViewById(R.id.relFAQRow);
		}
	}

	@Override
	public void onBindViewHolder(final SimpleViewHolder holder, final int position) {
		final FAQDetail faqDetail = mDataSet.get(position);
		holder.mFAQName.setText(faqDetail.question);
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectedIndex = position;
				if (selectedIndex < getItemCount())
					mSelectedQuestion.onQuestionSelected(mDataSet.get(selectedIndex));
				notifyDataSetChanged();
			}
		});

		if (selectedIndex == position) {
			backgroundDrawable(holder, R.drawable.pressed_bg);
		} else {
			backgroundDrawable(holder, R.drawable.stores_details_background);
		}
	}

	@Override
	public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.faq_row,
				parent, false);
		return new SimpleViewHolder(view);
	}

	@Override
	public int getItemCount() {
		return (mDataSet != null) ? mDataSet.size() : 0;
	}

	public void resetIndex() {
		selectedIndex = -1;
		notifyDataSetChanged();
	}

	private void backgroundDrawable(SimpleViewHolder holder, int id) {
		holder.mRelFAQRow.setBackground(ContextCompat.getDrawable(holder.mRelFAQRow.getContext(), id));
	}
}