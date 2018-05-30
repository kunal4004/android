package za.co.woolworths.financial.services.android.ui.adapters;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.SubCategory;
import za.co.woolworths.financial.services.android.ui.fragments.product.sub_category.SubCategoryFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.sub_category.SubCategoryNavigator;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class SubCategoryAdapter extends RecyclerView.Adapter<SubCategoryAdapter.MyViewHolder> {

	private int row_index = -1;
	private SubCategoryNavigator mSubCategoryNavigator;
	private List<SubCategory> mSubCategories;

	public SubCategoryAdapter(List<SubCategory> subCategoryList, SubCategoryFragment mSubCategoryNavigator) {
		this.mSubCategories = subCategoryList;
		this.mSubCategoryNavigator = mSubCategoryNavigator;
	}

	public class MyViewHolder extends RecyclerView.ViewHolder {
		private WTextView mSubCategoryName;

		public MyViewHolder(View view) {
			super(view);
			mSubCategoryName = view.findViewById(R.id.subCategoryName);
		}
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new MyViewHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.ps_sub_categories_row, parent, false));
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, final int position) {
		SubCategory subCategory = mSubCategories.get(position);
		holder.mSubCategoryName.setText(subCategory.categoryName);

		holder.mSubCategoryName.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				row_index = position;
				mSubCategoryNavigator.onItemClick(mSubCategories.get(position));
				notifyDataSetChanged();
			}
		});

		if (row_index == position) {
			holder.itemView.setBackground(ContextCompat.getDrawable(holder.mSubCategoryName.getContext(),
					R.drawable.pressed_bg));
		} else {
			holder.itemView.setBackgroundColor(Color.WHITE);
		}
	}

	@Override
	public int getItemCount() {
		return mSubCategories.size();
	}

	public void resetAdapter() {
		row_index = -1;
		notifyDataSetChanged();
	}
}
