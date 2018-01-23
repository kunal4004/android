package za.co.woolworths.financial.services.android.ui.fragments.product.category;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ProductSearchFragmentBinding;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.RootCategory;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.ObservableScrollViewCallbacks;
import za.co.woolworths.financial.services.android.util.ScrollState;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.zxing.QRActivity;

public class CategoryFragment extends BaseFragment<ProductSearchFragmentBinding, CategoryViewModel>
		implements CategoryNavigator, ObservableScrollViewCallbacks {

	private ErrorHandlerView mErrorHandlerView;

	private int mScrollY;
	private List<RootCategory> mRootCategories;
	private Toolbar mProductToolbar;

	@Override
	public int getLayoutId() {
		return R.layout.product_search_fragment;
	}

	@Override
	public CategoryViewModel getViewModel() {
		return ViewModelProviders.of(this).get(CategoryViewModel.class);
	}

	@Override
	public int getBindingVariable() {
		return BR.viewModel;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(false);// TO do:: set to true and manage toolbar view
		getViewModel().setNavigator(this);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getViewDataBinding().setHandlers(this);
		Utils.updateStatusBar(getActivity(), R.color.recent_search_bg);
		hideToolbar();
		mProductToolbar = getViewDataBinding().productToolbar;
		showBackNavigationIcon(false);
		renderUI();
		mErrorHandlerView = new ErrorHandlerView(getActivity(), getViewDataBinding().incNoConnectionHandler.noConnectionLayout);
		mErrorHandlerView.setMargin(getViewDataBinding().incNoConnectionHandler.noConnectionLayout, 0, 0, 0, 0);
		getViewDataBinding()
				.incNoConnectionHandler
				.btnRetry.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onRetryConnectionClicked();
			}
		});

		onRetryConnectionClicked();
	}

	@Override
	public void renderUI() {
		getViewDataBinding().mNestedScrollview.setScrollViewCallbacks(this);
	}

	@Override
	public void navigateToBarcode(View view) {
		checkCameraPermission();
	}

	@Override
	public void navigateToProductSearch(View view) {
		Intent openProductSearch = new Intent(getActivity(), ProductSearchActivity.class);
		startActivity(openProductSearch);
		getActivity().overridePendingTransition(0, 0);
	}

	@Override
	public void checkCameraPermission() {
		if (hasPermissions()) {
			Intent intent = new Intent(getActivity(), QRActivity.class);
			intent.putExtra("SCAN_MODE", "ONE_D_MODE");
			getActivity().startActivity(intent);
			getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
		} else {
			requestPerms();
		}
	}

	@Override
	public void toolbarState(boolean visibility) {
		if (visibility) {
			Utils.updateStatusBar(getActivity(), R.color.white);
		} else {
			Utils.updateStatusBar(getActivity(), R.color.recent_search_bg);
		}
	}

	@Override
	public void onRetryConnectionClicked() {
		if (isNetworkConnected()) {
			showBackNavigationIcon(false);
			mErrorHandlerView.hideErrorHandler();
			getViewModel()
					.categoryRequest(getViewDataBinding()
							.llCustomViews).execute();
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
	public void onCategoryItemClicked(RootCategory rootCategory) {
		if (getBottomNavigator() != null) {
			getBottomNavigator().pushFragment(getViewModel().enterNextFragment(rootCategory));
		}
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
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		boolean allowed = true;
		switch (requestCode) {
			case PERMS_REQUEST_CODE:
				for (int res : grantResults) {
					// if user granted all permissions.
					allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
				}
				break;
			default:
				// if user not granted permissions.
				allowed = false;
				break;
		}
		if (allowed) {
			checkCameraPermission();
		} else {
			// we will give warning to user that they haven't granted permissions.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
					Toast.makeText(getActivity(), "Camera Permissions denied.", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	@Override
	public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
		this.mScrollY = scrollY;
		if (scrollY < searchContainerHeight()) {
			hideProductToolbar();
		} else {
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
		getViewDataBinding().relSearchRowLayout.setAlpha(0);
		getViewDataBinding().productToolbar.setVisibility(View.VISIBLE);
		mProductToolbar.animate().translationY(0)
				.setDuration(300)
				.setInterpolator(new DecelerateInterpolator()).withStartAction(new Runnable() {

			@Override
			public void run() {
				toolbarState(true);
				mProductToolbar.setAlpha(1);
			}
		});
	}

	private void hideProductToolbar() {
		mProductToolbar.animate().translationY(-mProductToolbar.getBottom())
				.setDuration(300)
				.setInterpolator(new AccelerateInterpolator()).withStartAction(new Runnable() {

			@Override
			public void run() {
				toolbarState(false);
				getViewDataBinding().relSearchRowLayout.setAlpha(1);
				mProductToolbar.setAlpha(0);
			}
		});
	}

	@Override
	public void onDownMotionEvent() {

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.search_item, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		MenuItem menuItem = menu.findItem(R.id.action_search);
	}

	@Override
	public void bindViewWithUI(final List<RootCategory> rootCategories, LinearLayout llAddView) {
		mRootCategories = rootCategories;
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		int position = 0;
		while (position < mRootCategories.size()) {
			RootCategory rootCategory = rootCategories.get(position);
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

//	private void hideViews() {
//		mRelSearchRowLayout.setAlpha(0);
//		if (!actionBarIsHidden) {
//			actionBarIsHidden = true;
//			mProductToolbar.animate()
//					.translationY(-mProductToolbar.getBottom())
//					.setInterpolator(new AccelerateInterpolator())
//					.withEndAction(new Runnable() {
//						@Override
//						public void run() {
//							showBarcodeToolbar();
//							mProductToolbar
//									.animate()
//									.translationY(0)
//									.start();
//						}
//					}).start();
//		}
//	}
//
//	private void showViews() {
//		if (actionBarIsHidden) {
//			mProductToolbar.animate()
//					.translationY(-mProductToolbar.getBottom())
//					.setInterpolator(new DecelerateInterpolator())
//					.withEndAction(new Runnable() {
//						@Override
//						public void run() {
//							showAccountToolbar();
//							mRelSearchRowLayout.setAlpha(1);
//							mProductToolbar
//									.animate()
//									.translationY(0)
//									.setInterpolator(new DecelerateInterpolator())
//									.withEndAction(new Runnable() {
//										@Override
//										public void run() {
//											showAccountToolbar();
//										}
//									})
//									.start();
//							actionBarIsHidden = false;
//						}
//					}).start();
//		}
//	}

}
