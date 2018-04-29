package za.co.woolworths.financial.services.android.util.expand;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.util.expand.communicator.DetachableResultReceiver;
import za.co.woolworths.financial.services.android.util.expand.communicator.OnItemClickListener;

public class DrillSubCategoryAdapter extends ExpandableRecyclerAdapter<HeaderViewHolder,ParentSubCategoryViewHolder, SubCategoryViewHolder> {

	private LayoutInflater mInflator;
	private OnItemClickListener onItemClickListener;

	public DrillSubCategoryAdapter(Context context, OnItemClickListener onItemClickListener, List<? extends ParentListItem> parentItemList) {
		super(parentItemList);
		this.mInflator = LayoutInflater.from(context);
		this.onItemClickListener = onItemClickListener;
	}

	@Override
	public ParentSubCategoryViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup, int viewType) {
		View parentCategoryView = mInflator.inflate(R.layout.sub_category_parent_view, parentViewGroup, false);
		return new ParentSubCategoryViewHolder(parentCategoryView);
	}

	@Override
	public SubCategoryViewHolder onCreateChildViewHolder(ViewGroup childViewGroup, int viewType) {
		View childView = mInflator.inflate(R.layout.sub_category_child_view, childViewGroup, false);
		return new SubCategoryViewHolder(childView);
	}

	@Override
	public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parentViewGroup) {
		return null;
	}

	@Override
	public void onBindParentViewHolder(ParentSubCategoryViewHolder parentSubCategoryViewHolder, int position, ParentListItem parentListItem) {
		SubCategoryModel subCategoryModel = (SubCategoryModel) parentListItem;
		parentSubCategoryViewHolder.bind(subCategoryModel);
	}

	@Override
	public void onBindChildViewHolder(SubCategoryViewHolder subCategoryViewHolder, int position, Object childListItem) {
		SubCategoryChild subCategoryChild = (SubCategoryChild) childListItem;
		subCategoryViewHolder.bind(subCategoryChild);
		subCategoryViewHolder.listener(onItemClickListener, subCategoryChild);
	}

	@Override
	public void onBindHeaderViewHolder(HeaderViewHolder headerViewHolder, int position, ParentListItem childListItem) {

	}

}
