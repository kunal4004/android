package za.co.woolworths.financial.services.android.ui.fragments.product.sub_category;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.FragmentSubCategoryBinding;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.SubCategory;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.adapters.SubCategoryAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.SimpleDividerItemDecoration;
import za.co.woolworths.financial.services.android.util.Utils;

public class SubCategoryFragment extends BaseFragment<FragmentSubCategoryBinding, SubCategoryViewModel> implements SubCategoryNavigator, SubCategoryAdapter.SubCategoryClick {

	private ErrorHandlerView mErrorHandlerView;

	private String mRootCategoryName;
	private String mRootCategoryId;
	private String mSubCategoryName;
	private int mCatStep;
	private SubCategoryViewModel mSubCategoryViewModel;

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
			mCatStep = bundle.getInt("catStep");
			mRootCategoryId = bundle.getString("root_category_id");
			mRootCategoryName = bundle.getString("root_category_name");
			mSubCategoryName = bundle.getString("sub_category_name");
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		showToolbar();
		setTitle(mRootCategoryName);
		showBackNavigationIcon(true);
		mErrorHandlerView = new ErrorHandlerView(getActivity()
				, getViewDataBinding().incNoConnectionHandler.noConnectionLayout);
		getViewModel().executeSubCategory(getActivity(), mRootCategoryId);
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
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		//MenuItem menuItem = menu.findItem(R.id.action_drill_search);
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

	private void setUpList(List<SubCategory> subCategoryList) {
		SubCategoryAdapter subCategoryAdapter = new SubCategoryAdapter(subCategoryList, this);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		getViewDataBinding().productSearchList.setLayoutManager(mLayoutManager);
		getViewDataBinding().productSearchList.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
		getViewDataBinding().productSearchList.setItemAnimator(new DefaultItemAnimator());
		getViewDataBinding().productSearchList.setAdapter(subCategoryAdapter);
	}

	@Override
	public void onItemClick(SubCategory subCategory) {
		if (getBottomNavigator() != null) {
			getBottomNavigator().pushFragment(getViewModel().enterNextFragment(subCategory));
		}
	}
}
