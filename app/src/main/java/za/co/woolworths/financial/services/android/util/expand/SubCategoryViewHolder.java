package za.co.woolworths.financial.services.android.util.expand;

import android.view.View;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.expand.communicator.OnItemClickListener;

public class SubCategoryViewHolder extends ChildViewHolder {

	private WTextView mMoviesTextView;

	public SubCategoryViewHolder(View itemView) {
		super(itemView);
		mMoviesTextView = itemView.findViewById(R.id.tvChildItemName);
	}

	public void bind(SubCategoryChild subCategoryChild) {
		mMoviesTextView.setText(subCategoryChild.getSubCategory().categoryName);
	}

	public void listener(final OnItemClickListener onItemClickListener, final SubCategoryChild subCategoryChild) {
		itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onItemClickListener.onItemClick(subCategoryChild.getSubCategory().categoryId);
			}
		});
	}
}
