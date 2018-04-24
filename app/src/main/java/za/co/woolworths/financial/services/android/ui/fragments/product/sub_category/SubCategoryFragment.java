package za.co.woolworths.financial.services.android.ui.fragments.product.sub_category;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.FragmentSubCategoryBinding;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.SubCategory;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity;
import za.co.woolworths.financial.services.android.ui.adapters.SubCategoryAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.SimpleDividerItemDecoration;
import za.co.woolworths.financial.services.android.util.Utils;

public class SubCategoryFragment extends BaseFragment<FragmentSubCategoryBinding, SubCategoryViewModel> implements SubCategoryNavigator {

	private ErrorHandlerView mErrorHandlerView;

	private String mRootCategoryName;
	private String mRootCategoryId;
	private SubCategoryViewModel mSubCategoryViewModel;
	private SubCategoryAdapter mSubCategoryAdapter;

	@Override
	public SubCategoryViewModel getViewModel() {
		return mSubCategoryViewModel;
	}

	@Override
	public int getBindingVariable() {
		return BR.viewModel;
	}

	@Override
	public int getLayoutId() {
		return R.layout.fragment_sub_category;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mSubCategoryViewModel = ViewModelProviders.of(this).get(SubCategoryViewModel.class);
		mSubCategoryViewModel.setNavigator(this);
		Bundle bundle = this.getArguments();
		if (bundle != null) {
			mRootCategoryId = bundle.getString("root_category_id");
			mRootCategoryName = bundle.getString("root_category_name");
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		showToolbar(mRootCategoryName);
		setStatusBarColor(R.color.white);
		RelativeLayout relNoConnectionLayout = getViewDataBinding().incNoConnectionHandler.noConnectionLayout;
		mErrorHandlerView = new ErrorHandlerView(getActivity()
				, relNoConnectionLayout);
		mErrorHandlerView.setMargin(relNoConnectionLayout, 0, 0, 0, 0);
		getViewDataBinding()
				.incNoConnectionHandler
				.btnRetry.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onRetryConnectionClicked();
			}
		});

		onRetryConnectionClicked();
		slideStartOnToolbarNavigationClickListener();
	}

	private void onRetryConnectionClicked() {
		if (isNetworkConnected()) {
			mErrorHandlerView.hideErrorHandler();
			getViewModel().executeSubCategory(getActivity(), mRootCategoryId);
		} else {
			mErrorHandlerView.networkFailureHandler("e");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (getViewModel() != null)
			getViewModel().cancelRequest();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.drill_down_category_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_drill_search:
				Intent openSearchActivity = new Intent(getBaseActivity(), ProductSearchActivity.class);
				startActivity(openSearchActivity);
				getBaseActivity().overridePendingTransition(0, 0);
				return true;

			default:
				break;
		}

		return false;
	}

	@Override
	public void bindSubCategoryResult(List<SubCategory> subCat) {
		setUpList(subCat);
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
	}

	@Override
	public void onLoad() {
		showView(getViewDataBinding().mProgressBar);
	}

	@Override
	public void onLoadComplete() {
		hideView(getViewDataBinding().mProgressBar);
	}

	private void setUpList(List<SubCategory> subCategoryList) {
		mSubCategoryAdapter = new SubCategoryAdapter(subCategoryList, this);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		getViewDataBinding().productSearchList.setLayoutManager(mLayoutManager);
		Activity activity = getActivity();
		if (activity != null) {
			getViewDataBinding().productSearchList.addItemDecoration(new SimpleDividerItemDecoration(activity));
			getViewDataBinding().productSearchList.setItemAnimator(new DefaultItemAnimator());
			getViewDataBinding().productSearchList.setAdapter(mSubCategoryAdapter);
		}
	}


	@Override
	public void onItemClick(SubCategory subCategory) {
		if (getBottomNavigator() != null) {
			getBottomNavigator().pushFragment(getViewModel().enterNextFragment(subCategory));
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			if (mSubCategoryAdapter != null) {
				showToolbar(mRootCategoryName);
				mSubCategoryAdapter.resetAdapter();
			}
		}
	}
}
