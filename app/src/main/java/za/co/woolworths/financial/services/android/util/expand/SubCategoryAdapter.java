package za.co.woolworths.financial.services.android.util.expand;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.SubCategory;
import za.co.woolworths.financial.services.android.ui.fragments.product.sub_category.SubCategoryNavigator;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.expand.communicator.OnItemClickListener;

public class SubCategoryAdapter extends ExpandableRecyclerAdapter<HeaderViewHolder, ParentSubCategoryViewHolder, SubCategoryViewHolder> {

	private Context mContext;
	private LayoutInflater mInflator;
	private OnItemClickListener onItemClickListener;
	private SubCategoryNavigator mSubCategoryNavigator;

	public SubCategoryAdapter(Context context, SubCategoryNavigator subCategoryNavigator, OnItemClickListener onItemClickListener, List<? extends ParentListItem> parentItemList) {
		super(parentItemList);
		this.mInflator = LayoutInflater.from(context);
		this.onItemClickListener = onItemClickListener;
		this.mSubCategoryNavigator = subCategoryNavigator;
		this.mContext = context;
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
	public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup headerViewGroup) {
		View headerView = mInflator.inflate(R.layout.sub_category_header_view, headerViewGroup, false);
		return new HeaderViewHolder(headerView);
	}

	@Override
	public void onBindParentViewHolder(final ParentSubCategoryViewHolder parentSubCategoryViewHolder, final int position, ParentListItem parentListItem) {
		final SubCategoryModel subCategoryModel = (SubCategoryModel) parentListItem;
		parentSubCategoryViewHolder.bind(subCategoryModel);
		parentSubCategoryViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				SubCategory subCategory = subCategoryModel.getSubCategory();
				if (parentSubCategoryViewHolder.isExpanded()) {
					parentSubCategoryViewHolder.retrieveChildVisibility(false);
					parentSubCategoryViewHolder.collapseView();
					return;
				}

				if (subCategoryModel.getChildItemList() != null) {
					parentSubCategoryViewHolder.retrieveChildVisibility(false);
					parentSubCategoryViewHolder.expandView();
					return;
				}

				int sizeOfList = getParentItemList().size();
				// retrieve one api call at a time
				for (int index = 0; index < sizeOfList; index++) {
					if (((SubCategoryModel) getParentItemList().get(index)).getSubCategory().singleViewLoading) {
						return;
					}
				}
				if (mSubCategoryNavigator == null) return;
				if (!new ConnectionDetector().isOnline(mContext)) {
					mSubCategoryNavigator.noConnectionDetected();
					return;
				}
				if (subCategory.hasChildren) {
					subCategory.singleViewLoading = true;
					notifyItemChanged(parentSubCategoryViewHolder.getAdapterPosition());
					mSubCategoryNavigator.retrieveChildItem(parentSubCategoryViewHolder, subCategoryModel.getSubCategory(),
							position);
					return;
				}
			}
		});
	}

	private void toggleExpandCollapseView(ParentSubCategoryViewHolder parentSubCategoryViewHolder) {
		if (parentSubCategoryViewHolder.isExpanded()) {
			parentSubCategoryViewHolder.collapseView();
		} else {
			parentSubCategoryViewHolder.expandView();
		}
	}

	@Override
	public void onBindChildViewHolder(SubCategoryViewHolder subCategoryViewHolder, int position, Object childListItem) {
		SubCategoryChild subCategoryChild = (SubCategoryChild) childListItem;
		subCategoryViewHolder.bind(subCategoryChild);
		subCategoryViewHolder.listener(onItemClickListener, subCategoryChild);
	}

	@Override
	public void onBindHeaderViewHolder(HeaderViewHolder headerViewHolder, int position, ParentListItem headerListItem) {
		headerViewHolder.bind((SubCategoryModel) headerListItem);
		headerViewHolder.getImClose().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mSubCategoryNavigator.onCloseIconPressed();
			}
		});
	}

	public void updateList(List<SubCategoryModel> mSubCategoryListModel, ParentSubCategoryViewHolder mParentViewHolder, int position) {
		setParentItemList(mSubCategoryListModel);
		int sizeOfList = getParentItemList().size();
		// reset progressbar flag
		for (int index = 0; index < sizeOfList; index++) {
			((SubCategoryModel) getParentItemList().get(index)).getSubCategory().singleViewLoading = false;
		}
		notifyItemChanged(position);
		mParentViewHolder.retrieveChildVisibility(false);
		toggleExpandCollapseView(mParentViewHolder);
	}
}
