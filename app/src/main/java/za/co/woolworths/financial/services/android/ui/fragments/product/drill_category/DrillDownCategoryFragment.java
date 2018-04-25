package za.co.woolworths.financial.services.android.ui.fragments.product.drill_category;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.DrillDownCategoryLayoutBinding;
import com.google.gson.Gson;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.RootCategory;
import za.co.woolworths.financial.services.android.models.dto.SubCategory;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.adapters.sub_category.DrillDownCategoryAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.GridFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.sub_category.SubCategoryNavigator;
import za.co.woolworths.financial.services.android.ui.fragments.product.sub_category.SubCategoryViewModel;
import za.co.woolworths.financial.services.android.ui.views.expand.ExpandableRecyclerView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.Utils;


public class DrillDownCategoryFragment extends BaseFragment<DrillDownCategoryLayoutBinding, SubCategoryViewModel> implements View.OnClickListener, SubCategoryNavigator {

	private SubCategoryViewModel mDrillDownCategoryViewModel;
	private RootCategory mRootCategory;
	private List<SubCategory> mSubCategoryList;
	private int mCurrentGroupPosition = -1;
	private DrillDownCategoryAdapter drillDownCategoryAdapter;
	private ExpandableRecyclerView.SimpleGroupViewHolder mExpandableHeaderHolder;
	private ErrorHandlerView mErrorHandlerView;

	@Override
	public int getLayoutId() {
		return R.layout.drill_down_category_layout;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mDrillDownCategoryViewModel = ViewModelProviders.of(this).get(SubCategoryViewModel.class);
		mDrillDownCategoryViewModel.setNavigator(this);
		Bundle bundle = this.getArguments();
		if (bundle != null) {
			String rootCategory = bundle.getString("ROOT_CATEGORY");
			if (rootCategory != null)
				mRootCategory = new Gson().fromJson(rootCategory, RootCategory.class);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		slideDownOnToolbarNavigationOnClickListener();
		initView(mRootCategory);
		mErrorHandlerView = new ErrorHandlerView(getActivity(), getViewDataBinding().rlNoConnection);
		onRetryConnectionClicked(mRootCategory.categoryId, false);
		getViewDataBinding().btnRetry.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onRetryConnectionClicked(mRootCategory.categoryId, false);
			}
		});
	}

	private void initView(RootCategory mRootCategory) {
		setHeader(mRootCategory);
	}

	private void setCategoryAdapter(List<SubCategory> subCategoryList) {
		mSubCategoryList = subCategoryList;
		ExpandableRecyclerView expandableSubCategory = getViewDataBinding().rcvDrillCategory;
		assert expandableSubCategory != null;
		expandableSubCategory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
		drillDownCategoryAdapter = new DrillDownCategoryAdapter(subCategoryList, mDrillDownCategoryViewModel.getNavigator());
		expandableSubCategory.setAdapter(drillDownCategoryAdapter);
	}

	private void setHeader(RootCategory mRootCategory) {
		if (mRootCategory != null) {
			getViewDataBinding().imProductCategory.setImageURI(mRootCategory.imgUrl);
			getViewDataBinding().tvCategoryName.setText(mRootCategory.categoryName);
			getViewDataBinding().imClose.setOnClickListener(this);
		}
	}

	@Override
	public SubCategoryViewModel getViewModel() {
		return mDrillDownCategoryViewModel;
	}

	@Override
	public int getBindingVariable() {
		return BR.viewModel;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.imClose:
				popFragmentSlideDown();
				break;
			default:
				break;
		}
	}

	@Override
	public void bindSubCategoryResult(List<SubCategory> subCategoryList) {
		if (getViewModel().childItem()) { // child item
			SubCategory subCategory = mSubCategoryList.get(mCurrentGroupPosition);
			subCategory.singleViewLoading = false;
			subCategory.subCategoryList = subCategoryList;
			if (drillDownCategoryAdapter != null) {
				drillDownCategoryAdapter.updateList(mSubCategoryList, mExpandableHeaderHolder, mCurrentGroupPosition);
			}
			return;
		}

		setCategoryAdapter(subCategoryList); // header item
	}

	@Override
	public void unhandledResponseHandler(Response response) {
		if (!TextUtils.isEmpty(response.desc)) {
			Utils.displayValidationMessage(getActivity(),
					CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc);
		}
	}

	@Override
	public void onFailureResponse(String e) {
		mErrorHandlerView.networkFailureHandler(e);
		getViewDataBinding().pbSubCategory.setVisibility(View.GONE);
		getViewDataBinding().rcvDrillCategory.setVisibility(View.GONE);
	}

	@Override
	public void onLoad() {
		if (!getViewModel().childItem())
			showProgressBar(true);
	}

	@Override
	public void onLoadComplete() {
		showProgressBar(false);
	}

	@Override
	public void onItemClick(SubCategory subCategory) {
	}

	@Override
	public void retrieveChildItem(ExpandableRecyclerView.SimpleGroupViewHolder holder, SubCategory subCategory, int group) {
		if (subCategory.hasChildren) {
			this.mCurrentGroupPosition = group;
			this.mExpandableHeaderHolder = holder;
			onRetryConnectionClicked(subCategory.categoryId, true);
			return;
		}

		//Open GridFragment when hasChildren = false;
		onChildItemClicked(subCategory);
	}

	@Override
	public void onChildItemClicked(SubCategory subCategory) {
		//Navigate to product grid
		GridFragment gridFragment = new GridFragment();
		Bundle bundle = new Bundle();
		bundle.putString("sub_category_id", subCategory.categoryId);
		bundle.putString("sub_category_name", subCategory.categoryName);
		gridFragment.setArguments(bundle);
		pushFragment(gridFragment);
	}

	@Override
	public void noConnectionDetected() {
		if (mErrorHandlerView != null) {
			mErrorHandlerView.showToast();
			Utils.toggleStatusBarColor(getActivity(), R.color.red);
		}
	}


	private void onRetryConnectionClicked(String categoryId, boolean childItem) {
		if (isNetworkConnected()) {
			mErrorHandlerView.hideErrorHandler();
			//ChildItem params determine whether to perform header or child operation
			getViewModel().setChildItem(childItem);
			getViewModel().executeSubCategory(getActivity(), categoryId);
		} else {
			if (!getViewModel().childItem())
				mErrorHandlerView.networkFailureHandler("e");
		}
	}

	private void showProgressBar(boolean visible) {
		getViewDataBinding().pbSubCategory.setVisibility(visible ? View.VISIBLE : View.GONE);
		getViewDataBinding().rcvDrillCategory.setVisibility(visible ? View.GONE : View.VISIBLE);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			hideToolbar();
		}
	}
}
