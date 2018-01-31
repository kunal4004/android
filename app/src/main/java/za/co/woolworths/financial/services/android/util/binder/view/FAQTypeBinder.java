package za.co.woolworths.financial.services.android.util.binder.view;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.FAQDetail;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.binder.DataBindAdapter;
import za.co.woolworths.financial.services.android.util.binder.DataBinder;

public class FAQTypeBinder extends DataBinder<FAQTypeBinder.ViewHolder> {

	public interface SelectedQuestion {
		void onQuestionSelected(FAQDetail faqDetail);
	}

	private SelectedQuestion mSelectQuestion;
	private List<FAQDetail> mDataSet = new ArrayList<>();
	private int selectedPosition = -1;

	public FAQTypeBinder(DataBindAdapter dataBindAdapter, SelectedQuestion selectedQuestion) {
		super(dataBindAdapter);
		this.mSelectQuestion = selectedQuestion;
	}

	@Override
	public ViewHolder newViewHolder(ViewGroup parent) {
		View view = LayoutInflater.from(parent.getContext()).inflate(
				R.layout.faq_row, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void bindViewHolder(final ViewHolder holder, int position) {
		final FAQDetail faqDetail = mDataSet.get(position);
		if (faqDetail != null) {
			holder.mFAQName.setText(faqDetail.question);
		}

		if (selectedPosition == position) {
			holder.mRelFAQRow.setBackground(ContextCompat.getDrawable(holder.mFAQName.getContext(), R.drawable.pressed_bg));
		} else {
			holder.mRelFAQRow.setBackground(ContextCompat.getDrawable(holder.mFAQName.getContext(), R.drawable.top_border));
		}

		holder.mRelFAQRow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				selectedPosition = holder.getAdapterPosition();
				notifyBinderDataSetChanged();
				mSelectQuestion.onQuestionSelected(mDataSet.get(selectedPosition));
			}
		});
	}

	@Override
	public int getItemCount() {
		return mDataSet.size();
	}

	public void addAll(List<FAQDetail> dataSet) {
		mDataSet.addAll(dataSet);
		notifyBinderDataSetChanged();
	}

	public void clear() {
		mDataSet.clear();
		notifyBinderDataSetChanged();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {

		WTextView mFAQName;
		private RelativeLayout mRelFAQRow;

		public ViewHolder(View view) {
			super(view);
			mFAQName = (WTextView) view.findViewById(R.id.name);
			mRelFAQRow = (RelativeLayout) view.findViewById(R.id.relFAQRow);
		}
	}
}
