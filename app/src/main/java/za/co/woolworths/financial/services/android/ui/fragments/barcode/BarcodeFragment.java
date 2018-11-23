package za.co.woolworths.financial.services.android.ui.fragments.barcode;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.BarcodeMainLayoutBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.Result;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductDetails;
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.barcode.manual.ManualBarcodeFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.KeyboardUtil;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.barcode.AutoFocusMode;
import za.co.woolworths.financial.services.android.util.barcode.CodeScanner;
import za.co.woolworths.financial.services.android.util.barcode.CodeScannerView;
import za.co.woolworths.financial.services.android.util.barcode.DecodeCallback;
import za.co.woolworths.financial.services.android.util.barcode.ErrorCallback;

public class BarcodeFragment extends BaseFragment<BarcodeMainLayoutBinding, BarcodeViewModel> implements BarcodeNavigator, View.OnClickListener, NetworkChangeListener {

	private CodeScanner mCodeScanner;
	private BarcodeViewModel mBarcodeViewModel;
	private BroadcastReceiver mConnectionBroadcast;
	private String barcodeId;

	@Override
	public int getLayoutId() {
		return R.layout.barcode_main_layout;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mBarcodeViewModel = ViewModelProviders.of(this).get(BarcodeViewModel.class);
		mBarcodeViewModel.setNavigator(this);
	}

	@Override
	public void onViewCreated(final View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final Activity activity = getActivity();
		getViewDataBinding().btnManual.setOnClickListener(this);
		mConnectionBroadcast = Utils.connectionBroadCast(activity, this);
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				CodeScannerView scannerView = view.findViewById(R.id.scanner_view);
				// Use builder
				mCodeScanner = CodeScanner.builder()
						.formats(CodeScanner.ONE_DIMENSIONAL_FORMATS)
						.autoFocusMode(AutoFocusMode.SAFE)
						.autoFocusInterval(2000L)
						.flash(false)
						.onDecoded(new DecodeCallback() {
							@Override
							public void onDecoded(@NonNull final Result result) {
								activity.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										setBarcodeId(result.getText());
										executeBarcodeProduct(getBarcodeId());
									}
								});
							}
						})
						.onError(new ErrorCallback() {
							@Override
							public void onError(@NonNull final Exception error) {
							}
						}).build(activity, scannerView);

				startPreview();

				scannerView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						mCodeScanner.startPreview();
					}
				});
				setBarcodeScannerToolbar();
				slideDownOnToolbarNavigationOnClickListener();
			}
		}, getDelayMillis());

	}

	private int getDelayMillis() {
		return 400;
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver();
		startPreview();
	}

	private void startPreview() {
		if (mCodeScanner != null)
			mCodeScanner.startPreview();
	}

	@Override
	public void onPause() {
		unregisterReceiver();
		if (mCodeScanner != null)
			mCodeScanner.releaseResources();
		super.onPause();
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
	public void onDetach() {
		super.onDetach();
		if (getBottomNavigator() != null)
			getBottomNavigator().showBottomNavigationMenu();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		startPreview();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btnManual:
				stopPreview();
				ManualBarcodeFragment manualBarcodeFragment = new ManualBarcodeFragment();
				pushFragmentSlideUp(manualBarcodeFragment);
				break;

			default:
				break;
		}
	}

	private void stopPreview() {
		if (mCodeScanner != null)
			mCodeScanner.stopPreview();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			startPreview();
			setBarcodeScannerToolbar();
		}
	}

	private void setBarcodeScannerToolbar() {
		showToolbar(R.string.scan_product, R.color.black_50);
		setStatusBarColor(R.color.black, true);
		getBottomNavigator().setHomeAsUpIndicator(R.drawable.close_white);
	}

	@Override
	public void failureResponseHandler(String e) {
		showLoadingProgressBar(false);
	}

	@Override
	public void unhandledResponseCode(Response response) {
		showLoadingProgressBar(false);
		hideKeyboard();
		Activity activity = getActivity();
		if (response.desc != null && activity != null)
			Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc);
	}

	private void hideKeyboard() {
		Activity activity = getActivity();
		if (activity == null) return;
		KeyboardUtil.hideSoftKeyboard(activity);
	}

	@Override
	public void onLoadProductSuccess(ProductDetailResponse productDetailResponse, String detailProduct) {
		Activity activity = getActivity();
		showLoadingProgressBar(false);
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
					ScreenManager.presentProductDetails(activity, bundle);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
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
		getViewModel().setProductRequestBody(ProductsRequestParams.SearchType.BARCODE, barcodeId);
		getViewModel().executeGetBarcodeProduct(getActivity());
	}

	private void showLoadingProgressBar(boolean visible) {
		ProgressBar pbDescriptionInfo = getViewDataBinding().ppBar;
		WTextView tvDescriptionInfo = getViewDataBinding().textInfo;
		pbDescriptionInfo.setVisibility(visible ? View.VISIBLE : View.GONE);
		tvDescriptionInfo.setVisibility(visible ? View.GONE : View.VISIBLE);
	}

	@Override
	public void onConnectionChanged() {
		if (getViewModel().connectionHasFailed()) {
			executeBarcodeProduct(getBarcodeId());
		}
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

	public void setBarcodeId(String barcodeId) {
		this.barcodeId = barcodeId;
	}

	public String getBarcodeId() {
		return barcodeId;
	}

}
