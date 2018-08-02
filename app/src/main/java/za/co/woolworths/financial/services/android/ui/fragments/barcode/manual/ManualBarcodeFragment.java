package za.co.woolworths.financial.services.android.ui.fragments.barcode.manual;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ManualBarcodeLayoutBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductDetails;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.activities.product.ProductDetailsActivity;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.barcode.BarcodeNavigator;
import za.co.woolworths.financial.services.android.ui.fragments.barcode.BarcodeViewModel;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.ProductDetailFragment;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.KeyboardUtil;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.PDP_REQUEST_CODE;

public class ManualBarcodeFragment extends BaseFragment<ManualBarcodeLayoutBinding, BarcodeViewModel> implements BarcodeNavigator, View.OnClickListener, NetworkChangeListener {

	private BarcodeViewModel mBarcodeViewModel;
	private WLoanEditTextView mEditBarcodeNumber;
	private BroadcastReceiver mConnectionBroadcast;
	private String mEditBarcodeIdText;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBarcodeViewModel = ViewModelProviders.of(this).get(BarcodeViewModel.class);
		mBarcodeViewModel.setNavigator(this);
		setHasOptionsMenu(true);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mConnectionBroadcast = Utils.connectionBroadCast(getActivity(), this);
		setManualBarcodeToolbar(R.string.enter_barcode);
		configureView();
		showSoftKeyboard();
	}

	private void configureView() {
		mEditBarcodeNumber = getViewDataBinding().editBarcodeNumber;
		final WButton mBtnBarcodeConfirm = getViewDataBinding().btnBarcodeConfirm;
		mBtnBarcodeConfirm.setOnClickListener(this);
		mEditBarcodeNumber.setOnKeyPreImeListener(onKeyPreImeListener);
		mEditBarcodeNumber.setOnEditorActionListener(onEditorActionListener);
		getBottomNavigator().setHomeAsUpIndicator(R.drawable.back_white);
		mEditBarcodeNumber.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() > 0) {
					mBtnBarcodeConfirm.setVisibility(View.VISIBLE);
				} else {
					mBtnBarcodeConfirm.setVisibility(View.GONE);
				}
			}
		});
	}

	@Override
	public BarcodeViewModel getViewModel() {
		return mBarcodeViewModel;
	}

	@Override
	public int getBindingVariable() {
		return BR.viewModel;
	}

	@Override
	public int getLayoutId() {
		return R.layout.manual_barcode_layout;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			setManualBarcodeToolbar(R.string.enter_barcode);
		}
	}

	private void setManualBarcodeToolbar(int enter_barcode) {
		showToolbar(enter_barcode, R.color.black_50);
		setStatusBarColor(R.color.black, true);
	}

	/**
	 * Shows the soft keyboard
	 */
	public void showSoftKeyboard() {
		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		showSoftKeyboard(mEditBarcodeNumber);
	}


	@Override
	public void failureResponseHandler(String e) {
		showLoadingProgressBar(false);
	}

	@Override
	public void unhandledResponseCode(Response response) {
		showLoadingProgressBar(false);
		hideKeyboard();
		if (response.desc != null)
			Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc);
	}

	@Override
	public void onLoadProductSuccess(ProductDetailResponse productDetailResponse, String detailProduct) {
		hideKeyboard();
		Activity activity = getActivity();
		if (activity != null) {
			try {
				ArrayList<ProductDetails> mProductList;
				ProductDetails productList = productDetailResponse.product;
				mProductList = new ArrayList<>();
				if (productList != null) {
					mProductList.add(productList);
				}
				if (mProductList.size() > 0 && mProductList.get(0).productId != null) {
					GsonBuilder builder = new GsonBuilder();
					Gson gson = builder.create();
					String strProductList = gson.toJson(mProductList.get(0));
					Bundle bundle = new Bundle();
					bundle.putString("strProductList", strProductList);
					bundle.putString("strProductCategory", mProductList.get(0).productName);
					bundle.putString("productResponse", detailProduct);
					bundle.putBoolean("fetchFromJson", true);
					ScreenManager.presentProductDetails(getActivity(),bundle);

				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		showLoadingProgressBar(false);
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver();
		showKeyboard();
	}

	private void showKeyboard() {
		Activity activity = getActivity();
		if (activity == null) return;
		BottomNavigationActivity bottomNavigationActivity = (BottomNavigationActivity) activity;
		if (bottomNavigationActivity.getSlidingLayout().getPanelState() != SlidingUpPanelLayout.PanelState.EXPANDED) {
			if (mEditBarcodeNumber != null)
				showSoftKeyboard(mEditBarcodeNumber);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	private WLoanEditTextView.OnKeyPreImeListener onKeyPreImeListener = new WLoanEditTextView.OnKeyPreImeListener() {
		@Override
		public void onBackPressed() {
			ManualBarcodeFragment.this.onBackPressed();
		}
	};


	private void onBackPressed() {
		Activity activity = getActivity();
		if (activity != null) {
			// close product detail page and show keyboard
			BottomNavigationActivity bottomNavigationActivity = (BottomNavigationActivity) activity;
			SlidingUpPanelLayout slidingUpPanelLayout = bottomNavigationActivity.getSlidingLayout();
			if (slidingUpPanelLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
				bottomNavigationActivity.closeSlideUpPanel();
				return;
			}
			hideKeyboard();
			popFragmentSlideDown();
		}
	}

	private WLoanEditTextView.OnEditorActionListener onEditorActionListener = new WLoanEditTextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			// Identifier of the action. This will be either the identifier you supplied,
			// or EditorInfo.IME_NULL if being called due to the enter key being pressed.
			if (actionId == EditorInfo.IME_ACTION_SEARCH
					|| actionId == EditorInfo.IME_ACTION_DONE
					|| event.getAction() == KeyEvent.ACTION_DOWN
					&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
				onBackPressed();
				return true;
			}
			// Return true if you have consumed the action, else false.
			return false;
		}
	};

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btnBarcodeConfirm:
				mEditBarcodeIdText = mEditBarcodeNumber.getText().toString();
				executeBarcodeProduct(mEditBarcodeIdText);
				break;
			default:
				break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		showLoadingProgressBar(false);
		showKeyboard();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		// You can look up you menu item here and store it in a global variable by
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				break;
			default:
				break;
		}

		return true;
	}

	@Override
	public void onLoadStart() {
		showLoadingProgressBar(true);

	}

	@Override
	public void noItemFound() {
		showLoadingProgressBar(false);
	}

	private void executeBarcodeProduct(String barcodeId) {
		if (TextUtils.isEmpty(barcodeId)) return;
		getViewModel().setProductRequestBody(true, barcodeId);
		getViewModel().executeGetBarcodeProduct(getActivity());
	}

	private void showLoadingProgressBar(boolean visible) {
		ProgressBar pbDescriptionInfo = getViewDataBinding().mProgressBar;
		WTextView tvDescriptionInfo = getViewDataBinding().textInfo;
		pbDescriptionInfo.setVisibility(visible ? View.VISIBLE : View.GONE);
		tvDescriptionInfo.setVisibility(visible ? View.GONE : View.VISIBLE);
	}

	private void hideKeyboard() {
		KeyboardUtil.hideSoftKeyboard(getActivity());
	}

	private void unregisterReceiver() {
		Activity activity = getActivity();
		if (activity != null) {
			activity.unregisterReceiver(mConnectionBroadcast);
		}
	}

	private void registerReceiver() {
		Activity activity = getActivity();
		if (activity != null) {
			activity.registerReceiver(mConnectionBroadcast,
					new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		}
	}

	@Override
	public void onPause() {
		unregisterReceiver();
		super.onPause();
	}


	@Override
	public void onConnectionChanged() {
		if (getViewModel().connectionHasFailed()) {
			executeBarcodeProduct(mEditBarcodeIdText);
		}
	}


}
