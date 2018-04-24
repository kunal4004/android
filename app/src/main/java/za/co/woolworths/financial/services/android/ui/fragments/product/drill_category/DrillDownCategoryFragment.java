package za.co.woolworths.financial.services.android.ui.fragments.product.drill_category;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.DrillDownCategoryLayoutBinding;
import com.google.gson.Gson;

import za.co.woolworths.financial.services.android.models.dto.RootCategory;
import za.co.woolworths.financial.services.android.ui.adapters.sub_category.ExpandableTestAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.views.expand.ExpandableRecyclerView;


public class DrillDownCategoryFragment extends BaseFragment<DrillDownCategoryLayoutBinding, DrillDownCategoryViewModel> implements DrillDownInterface, View.OnClickListener {

	private DrillDownCategoryViewModel mDrillDownCategoryViewModel;
	private RootCategory mRootCategory;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mDrillDownCategoryViewModel = ViewModelProviders.of(this).get(DrillDownCategoryViewModel.class);
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
	}

	private void initView(RootCategory mRootCategory) {
		setHeader(mRootCategory);
		ExpandableRecyclerView expandableSubCategory = getViewDataBinding().rcvDrillCategory;
		assert expandableSubCategory != null;
		expandableSubCategory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
		final ExpandableTestAdapter testAdapter = new ExpandableTestAdapter();
		expandableSubCategory.setAdapter(testAdapter);
	}

	private void setHeader(RootCategory mRootCategory) {
		if (mRootCategory != null) {
			getViewDataBinding().imProductCategory.setImageURI(mRootCategory.imgUrl);
			getViewDataBinding().tvCategoryName.setText(mRootCategory.categoryName);
			getViewDataBinding().imClose.setOnClickListener(this);
		}
	}

	@Override
	public DrillDownCategoryViewModel getViewModel() {
		return mDrillDownCategoryViewModel;
	}

	@Override
	public int getBindingVariable() {
		return BR.viewModel;
	}

	@Override
	public int getLayoutId() {
		return R.layout.drill_down_category_layout;
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
}
