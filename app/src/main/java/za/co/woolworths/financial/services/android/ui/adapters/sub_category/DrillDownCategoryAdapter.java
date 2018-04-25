package za.co.woolworths.financial.services.android.ui.adapters.sub_category;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.SubCategory;
import za.co.woolworths.financial.services.android.ui.fragments.product.sub_category.SubCategoryNavigator;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.expand.ExpandableRecyclerView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;

public class DrillDownCategoryAdapter extends ExpandableRecyclerView.Adapter<DrillDownCategoryAdapter.ChildViewHolder, ExpandableRecyclerView.SimpleGroupViewHolder, String, String> {

	private SubCategoryNavigator mSubCategoryNavigator;
	private List<SubCategory> mSubCategoryList;

	public DrillDownCategoryAdapter(List<SubCategory> subCategoryList, SubCategoryNavigator navigator) {
		this.mSubCategoryList = subCategoryList;
		this.mSubCategoryNavigator = navigator;
	}

	@Override
	public int getGroupItemCount() {
		return (mSubCategoryList != null) ? mSubCategoryList.size() - 1 : 0;
	}

	@Override
	public int getChildItemCount(int group) {
		if (mSubCategoryList == null) return 0;
		SubCategory subCategory = mSubCategoryList.get(group);
		return subCategory.hasChildren ? (subCategory.subCategoryList == null ? subCategory.subCategoryList.size() : subCategory.subCategoryList.size()) : 0;
	}

	@Override
	public String getGroupItem(int position) {
		return (mSubCategoryList != null) ? mSubCategoryList.get(position).categoryName : "";
	}

	@Override
	public String getChildItem(int group, int position) {
		if (mSubCategoryList == null) return "";
		SubCategory subCategoryGroup = mSubCategoryList.get(group);
		List<SubCategory> childItemList = subCategoryGroup.subCategoryList;
		return (childItemList == null) ? "" : childItemList.get(position).categoryName;
	}

	@Override
	protected ExpandableRecyclerView.SimpleGroupViewHolder onCreateGroupViewHolder(ViewGroup parent) {
		return new ExpandableRecyclerView.SimpleGroupViewHolder(parent.getContext());
	}

	@Override
	protected ChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(R.layout.list_item_sub_category, parent, false);
		return new ChildViewHolder(view);
	}

	@Override
	public int getChildItemViewType(int group, int position) {
		return 1;
	}

	@Override
	public void onBindGroupViewHolder(final ExpandableRecyclerView.SimpleGroupViewHolder holder, final int group) {
		super.onBindGroupViewHolder(holder, group);
		if (mSubCategoryList != null) {
			SubCategory subCategory = mSubCategoryList.get(group);
			holder.setText(getGroupItem(group));
			holder.setArrowVisibility(subCategory.hasChildren);
			if (subCategory.hasChildren)
				holder.setViewIsLoading(subCategory.singleViewLoading);
			holder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					SubCategory selectedSubCategoryList = mSubCategoryList.get(group);
					if (holder.itemView.getContext() != null) {
						Context contextCompat = holder.itemView.getContext();
						if (!new ConnectionDetector().isOnline(contextCompat)) {
							mSubCategoryNavigator.noConnectionDetected();
							return;
						}
					}

					// close expanded row
					if (isExpanded(group)) {
						collapse(group);
						if (holder instanceof ExpandableRecyclerView.GroupViewHolder)
							holder.collapse();
						return;
					}
					if (selectedSubCategoryList.hasChildren) {
						// load one view at a time, dismiss current click if a view is loading
						for (SubCategory subCat : mSubCategoryList) {
							if (subCat.singleViewLoading) {
								return;
							}
						}

						// loading not detected, retrieve child item
						mSubCategoryNavigator.retrieveChildItem(holder, selectedSubCategoryList, group);
					}


					if (!selectedSubCategoryList.hasChildren) {
						mSubCategoryNavigator.retrieveChildItem(holder, selectedSubCategoryList, group);
						return;
					}

					selectedSubCategoryList.singleViewLoading = true;
					notifyItemChanged(group);
				}
			});
		}
	}

	private void expandCollapseGroup(int group, ExpandableRecyclerView.SimpleGroupViewHolder holder) {
		if (isExpanded(group)) {
			collapse(group);
			if (holder instanceof ExpandableRecyclerView.GroupViewHolder)
				holder.collapse();
		} else {
			expand(group);
			if (holder instanceof ExpandableRecyclerView.GroupViewHolder)
				holder.expand();
		}
	}

	@Override
	public void onBindChildViewHolder(ChildViewHolder holder, final int group, final int position) {
		super.onBindChildViewHolder(holder, group, position);
		holder.tvChildItemName.setText(getChildItem(group, position));
		holder.tvChildItemName.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mSubCategoryNavigator.onChildItemClicked(mSubCategoryList.get(group).subCategoryList.get(position));
			}
		});
		//set shadow to first child row
		setDrawable(holder, (position == 0) ? R.drawable.border_shadow : R.drawable.sub_category_child_bg);
	}

	private void setDrawable(ChildViewHolder holder, int border_shadow) {
		if (holder.llRootChildContainer.getContext() != null)
			holder.llRootChildContainer.setBackground(ContextCompat.getDrawable(holder.llRootChildContainer.getContext(), border_shadow));
	}

	public void updateList(List<SubCategory> mSubCategoryList,
						   ExpandableRecyclerView.SimpleGroupViewHolder mExpandableHeaderHolder,
						   int mCurrentGroupPosition) {
		this.mSubCategoryList = mSubCategoryList;
		expandCollapseGroup(mCurrentGroupPosition, mExpandableHeaderHolder);
		notifyDataSetChanged();
	}

	protected class ChildViewHolder extends RecyclerView.ViewHolder {
		private final WTextView tvChildItemName;
		private final LinearLayout llRootChildContainer;

		ChildViewHolder(View itemView) {
			super(itemView);
			tvChildItemName = itemView.findViewById(R.id.tvChildItemName);
			llRootChildContainer = itemView.findViewById(R.id.llRootChildContainer);
		}
	}
}
