package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.SubCategory;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class SubCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	public static final int VIEW_TYPE_EMPTY = 0;
	public static final int VIEW_TYPE_NORMAL = 1;
	private SubCategoryClick mSubCategoryClick;
	public List<SubCategory> mSubCategoryList;

	public interface SubCategoryClick {
		void onItemClick(SubCategory subCategory);
	}

	public SubCategoryAdapter(List<SubCategory> subCategory, SubCategoryClick subCategoryClick) {
		this.mSubCategoryList = subCategory;
		this.mSubCategoryClick = subCategoryClick;
	}

	public void addItems(List<SubCategory> subCategoryList) {
		mSubCategoryList.addAll(subCategoryList);
		notifyDataSetChanged();
	}

	public void clearItems() {
		mSubCategoryList.clear();
	}

	@Override

	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case VIEW_TYPE_EMPTY:
				return new EmptyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sub_category_empty_view, parent, false));
			case VIEW_TYPE_NORMAL:
				return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ps_sub_categories_row, parent, false));
			default:
				return new EmptyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sub_category_empty_view, parent, false));
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		switch (holder.getItemViewType()) {
			case VIEW_TYPE_EMPTY:
				break;

			case VIEW_TYPE_NORMAL:
				ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
				SubCategory subCategory = mSubCategoryList.get(position);
				itemViewHolder.setCategoryName(subCategory);
				itemViewHolder.onItemClickListener(subCategory, mSubCategoryClick);
				break;

			default:

				break;
		}
	}


	@Override
	public int getItemCount() {
		if (mSubCategoryList != null && mSubCategoryList.size() > 0) {
			return mSubCategoryList.size();
		} else {
			return 1;
		}
	}

	@Override
	public int getItemViewType(int position) {
		if (mSubCategoryList != null && mSubCategoryList.size() > 0) {
			return VIEW_TYPE_NORMAL;
		} else {
			return VIEW_TYPE_EMPTY;
		}
	}

	public class ItemViewHolder extends RecyclerView.ViewHolder {

		private WTextView wSubCategoryName;

		public ItemViewHolder(View itemView) {
			super(itemView);
			wSubCategoryName = itemView.findViewById(R.id.subCategoryName);
		}

		public void setCategoryName(SubCategory subCategory) {
			wSubCategoryName.setText(subCategory.categoryName);
		}

		public void onItemClickListener(final SubCategory subCategory, final SubCategoryClick subCategoryClick) {
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					subCategoryClick.onItemClick(subCategory);
				}
			});
		}


	}

	public class EmptyViewHolder extends RecyclerView.ViewHolder {

		public EmptyViewHolder(View itemView) {
			super(itemView);
		}
	}

}
