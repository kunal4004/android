package za.co.woolworths.financial.services.android.ui.fragments.product.category;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ProductSearchFragmentBinding;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.RootCategory;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.barcode.BarcodeFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.ObservableScrollViewCallbacks;
import za.co.woolworths.financial.services.android.util.ScrollState;
import za.co.woolworths.financial.services.android.util.Utils;

public class CategoryFragment extends BaseFragment<ProductSearchFragmentBinding, CategoryViewModel>
		implements CategoryNavigator, ObservableScrollViewCallbacks, View.OnClickListener {

	private static final float HIDE_ALPHA_VALUE = 0;
	private static final float SHOW_ALPHA_VALUE = 1;
	private final int ANIMATION_DURATION = 300;

	private ErrorHandlerView mErrorHandlerView;

	private int mScrollY;
	private List<RootCategory> mRootCategories;
	private Toolbar mProductToolbar;
	private CategoryViewModel mViewModel;

	public CategoryFragment() {
		setRetainInstance(true);
	}

	@Override
	public int getLayoutId() {
		return R.layout.product_search_fragment;
	}

	@Override
	public CategoryViewModel getViewModel() {
		return mViewModel;
	}

	@Override
	public int getBindingVariable() {
		return BR.viewModel;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mViewModel = ViewModelProviders.of(this).get(CategoryViewModel.class);
		setHasOptionsMenu(false);
		getViewModel().setNavigator(this);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (savedInstanceState == null) {
			getViewDataBinding().setHandlers(this);
			toolbarState(false);
			hideToolbar();
			mProductToolbar = getViewDataBinding().productToolbar;
			showBackNavigationIcon(false);
			renderUI();
			setUpConnectionError();
			getViewDataBinding()
					.incNoConnectionHandler
					.btnRetry.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					onRetryConnectionClicked();
				}
			});

			onRetryConnectionClicked();

			getViewDataBinding().textProductSearch.setOnClickListener(this);
			getViewDataBinding().llBarcodeScanner.setOnClickListener(this);
			getViewDataBinding().imBurgerButtonPressed.setOnClickListener(this);
			getViewDataBinding().textTBProductSearch.setOnClickListener(this);
			getViewDataBinding().imTBBarcodeScanner.setOnClickListener(this);
		}
	}

	private void setUpConnectionError() {
		assert getViewDataBinding().incNoConnectionHandler != null;
		mErrorHandlerView = new ErrorHandlerView(getActivity(), getViewDataBinding().incNoConnectionHandler.noConnectionLayout);
		mErrorHandlerView.setMargin(getViewDataBinding().incNoConnectionHandler.noConnectionLayout, 0, 0, 0, 0);
	}

	@Override
	public void renderUI() {
		getViewDataBinding().mNestedScrollview.setScrollViewCallbacks(this);
	}

	@Override
	public void navigateToBarcode() {
		BarcodeFragment barcodeFragment = new BarcodeFragment();
		Bundle bundle = new Bundle();
		bundle.putString("SCAN_MODE", "ONE_D_MODE");
		barcodeFragment.setArguments(bundle);
		getBottomNavigator().hideBottomNavigationMenu();
		pushFragmentSlideUp(barcodeFragment);
	}

	@Override
	public void navigateToProductSearch() {
		Intent openProductSearch = new Intent(getActivity(), ProductSearchActivity.class);
		startActivity(openProductSearch);
		getActivity().overridePendingTransition(0, 0);
	}

	@Override
	public void checkCameraPermission() {
		BarcodeFragment barcodeFragment = new BarcodeFragment();
		Bundle bundle = new Bundle();
		bundle.putString("SCAN_MODE", "ONE_D_MODE");
		barcodeFragment.setArguments(bundle);
		getBottomNavigator().hideBottomNavigationMenu();
		pushFragmentSlideUp(barcodeFragment);
	}

	@Override
	public void toolbarState(boolean visibility) {
		Utils.updateStatusBarBackground(getActivity(), R.color.white);
	}

	@Override
	public void onRetryConnectionClicked() {
		if (isNetworkConnected()) {
			showBackNavigationIcon(false);
			mErrorHandlerView.hideErrorHandler();
			getViewModel()
					.categoryRequest(getViewDataBinding()
							.llCustomViews).execute();
		} else {
			mErrorHandlerView.networkFailureHandler("e");
		}
	}

	@Override
	public void bindCategory(List<RootCategory> rootCategoryList) {
		if (rootCategoryList != null) {
			bindViewWithUI(rootCategoryList, getViewDataBinding().llCustomViews);
		}
	}

	@Override
	public void unhandledResponseCode(Response response) {
		if (!TextUtils.isEmpty(response.desc)) {
			Utils.displayValidationMessage(getActivity(),
					CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc);
		}
	}

	@Override
	public void onCategoryItemClicked(final RootCategory rootCategory) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (getBottomNavigator() != null) {
					getBottomNavigator().pushFragmentSlideUp(getViewModel().enterNextFragment(rootCategory), true);
				}
			}
		});
	}

	@Override
	public int searchContainerHeight() {
		return getViewDataBinding().relSearchRowLayout.getHeight() + (getToolBarHeight() / 2);
	}

	@Override
	public void failureResponseHandler(String e) {
		mErrorHandlerView.networkFailureHandler(e);
	}

	@Override
	public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
		this.mScrollY = scrollY;
		if (scrollY < searchContainerHeight()) {
			getGlobalState().setToolbarIsDisplayed(false);
			hideProductToolbar();
		} else {
			getGlobalState().setToolbarIsDisplayed(true);
			showProductToolbar();
		}
	}

	@Override
	public void onUpOrCancelMotionEvent(ScrollState scrollState) {
		if (mScrollY < searchContainerHeight()) {
			hideProductToolbar();
		}
	}

	private void showProductToolbar() {
		getViewDataBinding().relSearchRowLayout.setAlpha(HIDE_ALPHA_VALUE);
		getViewDataBinding().productToolbar.setVisibility(View.VISIBLE);
		mProductToolbar.animate().translationY(HIDE_ALPHA_VALUE)
				.setDuration(ANIMATION_DURATION)
				.setInterpolator(new DecelerateInterpolator()).withStartAction(new Runnable() {

			@Override
			public void run() {
				toolbarState(true);
				mProductToolbar.setAlpha(SHOW_ALPHA_VALUE);
			}
		});
	}

	private void hideProductToolbar() {
		mProductToolbar.animate().translationY(-mProductToolbar.getBottom())
				.setDuration(ANIMATION_DURATION)
				.setInterpolator(new AccelerateInterpolator()).withStartAction(new Runnable() {

			@Override
			public void run() {
				toolbarState(false);
				getViewDataBinding().relSearchRowLayout.setAlpha(SHOW_ALPHA_VALUE);
				mProductToolbar.setAlpha(HIDE_ALPHA_VALUE);
			}
		});
	}

	@Override
	public void onDownMotionEvent() {

	}

	@Override
	public void bindViewWithUI(final List<RootCategory> rootCategories, LinearLayout llAddView) {
		mRootCategories = rootCategories;
		Activity activity = getActivity();
		if (activity != null) {
			LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			int position = 0;
			while (position < mRootCategories.size()) {
				RootCategory rootCategory = rootCategories.get(position);
				assert inflater != null;
				@SuppressLint("InflateParams")
				View view = inflater.inflate(R.layout.product_search_root_category_row, null, false);
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				Resources r = getActivity().getResources();
				int sixteenDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());
				layoutParams.setMargins(sixteenDp, 0, sixteenDp, sixteenDp);
				view.setLayoutParams(layoutParams);
				view.setId(position);
				view.setTag(position);
				WTextView tv = view.findViewById(R.id.textProduct);
				tv.setText(rootCategory.categoryName);
				WrapContentDraweeView mImageProductCategory = view.findViewById(R.id.imProductCategory);
				mImageProductCategory.setId(position);
				mImageProductCategory.setTag(position);
				mImageProductCategory.setImageURI(rootCategory.imgUrl, getActivity());
				view.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						onCategoryItemClicked(mRootCategories.get(v.getId()));
					}
				});
				mImageProductCategory.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						onCategoryItemClicked(mRootCategories.get(v.getId()));
					}
				});
				position++;
				llAddView.addView(view);
			}
		}
	}

	@Override
	public void onHiddenChanged(final boolean hidden) {
		super.onHiddenChanged(hidden);
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!hidden) {
					//do when hidden
					fadeOutToolbar(R.color.recent_search_bg);
					showBackNavigationIcon(false);
				}
			}
		}, 10);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.imBurgerButtonPressed:
			case R.id.textTBProductSearch:
			case R.id.textProductSearch:
				navigateToProductSearch();
				break;
			case R.id.imTBBarcodeScanner:
			case R.id.llBarcodeScanner:
				checkLocationPermission(getBottomNavigator(), getBottomNavigator().getPermissionType(Manifest.permission.CAMERA), 2);
				break;

			default:
				break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.e("resultCode", String.valueOf(resultCode));
		// navigate to barcode view on camera runtime permission successfully granted
		if (requestCode == 2) {
			if (resultCode == 200) {
				navigateToBarcode();
			}
		}
	}
}
