package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.FAQDetail;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.MyViewHolder> {

	private int row_index = -1;
	public interface SelectedQuestion {
		void onQuestionSelected(View v, int position);
	}

	private SelectedQuestion mSelectedQuestion;
	private List<FAQDetail> faqDetails;

	public FAQAdapter(List<FAQDetail> faq, SelectedQuestion onClickListener) {
		this.faqDetails = faq;
		this.mSelectedQuestion = onClickListener;
	}

	public class MyViewHolder extends RecyclerView.ViewHolder {
		private WTextView mFaqName;
		private RelativeLayout mRelFAQRow;

		public MyViewHolder(View view) {
			super(view);
			mFaqName = (WTextView) view.findViewById(R.id.name);
			mRelFAQRow = (RelativeLayout) view.findViewById(R.id.relFAQRow);

		}
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new MyViewHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.faq_row, parent, false));
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, final int position) {
		FAQDetail faqDetail = faqDetails.get(position);
		if (faqDetail != null) {
			holder.mFaqName.setText(faqDetail.question);
		}

		if (row_index == position) {
			holder.mRelFAQRow.setBackground(ContextCompat.getDrawable(holder.mFaqName.getContext(), R.drawable.pressed_bg));
		} else {
			holder.mRelFAQRow.setBackground(ContextCompat.getDrawable(holder.mFaqName.getContext(), R.drawable.top_border));
		}

		holder.mFaqName.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				row_index = holder.getAdapterPosition();
				mSelectedQuestion.onQuestionSelected(view, holder.getAdapterPosition());
				notifyDataSetChanged();
			}
		});
	}

	@Override
	public int getItemCount() {
		return faqDetails.size();
	}

	public void resetAdapter() {
		row_index = -1;
		notifyDataSetChanged();
	}
}
