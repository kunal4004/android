package za.co.woolworths.financial.services.android.util.expand;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.SubCategory;
import za.co.woolworths.financial.services.android.ui.fragments.product.sub_category.SubCategoryNavigator;
import za.co.woolworths.financial.services.android.util.NetworkManager;

public class SubCategoryAdapter extends ExpandableRecyclerAdapter<HeaderViewHolder, ParentSubCategoryViewHolder, SubCategoryViewHolder> {

	private Context mContext;
	private LayoutInflater mInflator;
	private SubCategoryNavigator mSubCategoryNavigator;
	private int ROW_NOTIFY_ITEM_CHANGED_DELAY = 50;

	public SubCategoryAdapter(Context context, SubCategoryNavigator subCategoryNavigator, List<? extends ParentListItem> parentItemList) {
		super(parentItemList);
		this.mInflator = LayoutInflater.from(context);
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
	public void onBindParentViewHolder(final ParentSubCategoryViewHolder parentSubCategoryViewHolder, final int parentPosition, final ParentListItem parentListItem) {
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

//				int sizeOfList = getParentItemList().size();
//				// retrieve one api call at a time
//				for (int index = 0; index < sizeOfList; index++) {
//					if (((SubCategoryModel) getParentItemList().get(index)).getSubCategory().singleProductItemIsLoading) {
//						return;
//					}
//				}
				if (mSubCategoryNavigator == null) return;
				if (!NetworkManager.getInstance().isConnectedToNetwork(mContext)) {
					mSubCategoryNavigator.noConnectionDetected();
					return;
				}
				if (subCategory.hasChildren) {
					subCategory.singleProductItemIsLoading = true;
					int headerIndexLoop = 0;
					int headerPosition = 0;
					for (ParentListItem pl : getParentItemList()) {
						if ((((SubCategoryModel) getParentItemList().get(headerIndexLoop)).getSubCategory().categoryId
								.equalsIgnoreCase(subCategory.categoryId))) {
							headerPosition = headerIndexLoop;
						}
						headerIndexLoop++;
					}
					mSubCategoryNavigator.retrieveChildItem(parentSubCategoryViewHolder, subCategoryModel.getSubCategory(), headerPosition);
					notifyItemChanged(headerPosition);
				} else

				{
					//Open ProductListingFragment when hasChildren = false;
					mSubCategoryNavigator.onChildItemClicked(subCategory);
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
		subCategoryViewHolder.bind(mSubCategoryNavigator, subCategoryChild);
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

	public void updateList(List<SubCategoryModel> mSubCategoryListModel, final ParentSubCategoryViewHolder mParentViewHolder, int selectedPosition) {
		/**
		 * The handler prevent notifyItemChanged and notifyDataInserted from expanded method
		 * to send update at the same time.
		 */
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				toggleExpandCollapseView(mParentViewHolder);
			}
		};
		Handler handler = new Handler();
		handler.postDelayed(runnable, ROW_NOTIFY_ITEM_CHANGED_DELAY);
		mSubCategoryListModel.get(selectedPosition).getSubCategory().setSingleProductItemIsLoading(false);
		setParentItemList(mSubCategoryListModel);
		notifyItemChanged(selectedPosition);
	}

	/***
	 * @Method hideChildItemProgressBar hide the progressbar and display arrow indicator on error response
	 */
	public void hideChildItemProgressBar() {
		int sizeOfList = getParentItemList().size();
		for (int index = 0; index < sizeOfList; index++) {
			((SubCategoryModel) getParentItemList().get(index)).getSubCategory().singleProductItemIsLoading = false;
		}
		notifyDataSetChanged();
	}

}
