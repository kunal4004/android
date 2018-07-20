package za.co.woolworths.financial.services.android.util.expand;

import android.view.View;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.fragments.product.sub_category.SubCategoryNavigator;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class SubCategoryViewHolder extends ChildViewHolder {

	private WTextView tvChildItemName;

	public SubCategoryViewHolder(View itemView) {
		super(itemView);
		tvChildItemName = itemView.findViewById(R.id.tvChildItemName);
	}

	public void bind(final SubCategoryNavigator subCategoryNavigator, SubCategoryChild subCategoryChild) {
		tvChildItemName.setText(subCategoryChild.getSubCategory().categoryName);
		listener(subCategoryNavigator, subCategoryChild);
	}

	public void listener(final SubCategoryNavigator subCategoryNavigator, final SubCategoryChild subCategoryChild) {
		tvChildItemName.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				subCategoryNavigator.onChildItemClicked(subCategoryChild.getSubCategory());
			}
		});
	}
}
