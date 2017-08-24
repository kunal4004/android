package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;


import com.awfs.coordination.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Ingredient;
import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.ui.activities.ProductDetailActivity;
import za.co.woolworths.financial.services.android.ui.activities.ProductGridActivity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpDialogManager;
import za.co.woolworths.financial.services.android.ui.views.ProductProgressDialogFrag;

public class WebAppInterface {
	private ErrorHandlerView mErrorHandlerView;
	private Context mContext;
	private ProductProgressDialogFrag mProgressDialogFragment;
	private PauseHandlerFragment mPauseHandlerFragment;

	/**
	 * Instantiate the interface and set the context
	 * must be added for API 17 or higher
	 */

	public WebAppInterface(Context c) {
		mContext = c;
		mErrorHandlerView = new ErrorHandlerView(c);
	}

	@JavascriptInterface
	public void showProducts(String id, String productName) {
		Intent openProductName = new Intent(mContext, ProductGridActivity.class);
		openProductName.putExtra("searchProduct", "");
		openProductName.putExtra("title", id);
		openProductName.putExtra("titleNav", productName);
		mContext.startActivity(openProductName);
		((AppCompatActivity) mContext).overridePendingTransition(0, 0);
	}

	@JavascriptInterface
	public void addToShoppingList(String ingredients) {
		if (!TextUtils.isEmpty(ingredients)) {
			Gson gson = new Gson();
			Ingredient ingredient[] = gson.fromJson(ingredients, Ingredient[].class);
			if (ingredient.length > 0) {
				for (Ingredient i : ingredient) {
					Utils.addToShoppingCart(mContext, new ShoppingList(
							i.id, i.displayName, false));
				}
			}

			((AppCompatActivity) mContext).runOnUiThread(new Runnable() {
				public void run() {
					Utils.displayValidationMessage(mContext,
							CustomPopUpDialogManager.VALIDATION_MESSAGE_LIST.SHOPPING_LIST_INFO,
							"viewShoppingList");
				}
			});

		}

	}

	@JavascriptInterface
	public void showProduct(String productId, String skuId) {
		if (new ConnectionDetector().isOnline(mContext))
			onPauseHandler(productId, skuId);
		else
			mErrorHandlerView.showToast();
	}

	private void onCallback(final String productId, final String skuId) {
		mPauseHandlerFragment.runProtected(new MyRunnable() {
			@Override
			public void run(AppCompatActivity context) {
				//this block of code should be protected from IllegalStateException
				showProgressDialog();
				getProductDetail(productId, skuId);
			}
		});
	}

	private void onPauseHandler(final String productId, final String skuId) {
		//register pause handler
		FragmentManager fm = ((AppCompatActivity) mContext).getSupportFragmentManager();
		String PAUSE_HANDLER_FRAGMENT_TAG = "pause_handler";
		mPauseHandlerFragment = (PauseHandlerFragment) fm.
				findFragmentByTag(PAUSE_HANDLER_FRAGMENT_TAG);
		if (mPauseHandlerFragment == null) {
			mPauseHandlerFragment = new PauseHandlerFragment();
			fm.beginTransaction()
					.add(mPauseHandlerFragment, PAUSE_HANDLER_FRAGMENT_TAG)
					.commit();

		}
		onCallback(productId, skuId);
	}

	private void showProgressDialog() {
		FragmentManager fm = ((AppCompatActivity) mContext).getSupportFragmentManager();
		mProgressDialogFragment = ProductProgressDialogFrag.newInstance();
		if (!mProgressDialogFragment.isAdded()) {
			mProgressDialogFragment = ProductProgressDialogFrag.newInstance();
			mProgressDialogFragment.show(fm, "v");
		} else {
			mProgressDialogFragment.show(fm, "v");
		}
	}

	private void getProductDetail(final String productId, final String skuId) {
		((WoolworthsApplication) ((AppCompatActivity) mContext).getApplication()).getAsyncApi()
				.getProductDetail(productId, skuId, new Callback<String>() {
					@Override
					public void success(String strProduct, retrofit.client.Response response) {
						final WProduct wProduct = Utils.stringToJson(mContext, strProduct);
						if (wProduct != null) {
							switch (wProduct.httpCode) {
								case 200:
									((AppCompatActivity) mContext).runOnUiThread(new Runnable() {
										@Override
										public void run() {
											try {
												dismissFragmentDialog();
												ArrayList<WProductDetail> mProductList;
												WProductDetail productList = wProduct.product;
												mProductList = new ArrayList<>();
												if (productList != null) {
													mProductList.add(productList);
												}
												if (mProductList.size() > 0 && mProductList.get(0).productId != null) {
													GsonBuilder builder = new GsonBuilder();
													Gson gson = builder.create();
													Intent openDetailView = new Intent(mContext, ProductDetailActivity.class);
													openDetailView.putExtra("product_name", mProductList.get(0).productName);
													openDetailView.putExtra("product_detail", gson.toJson(mProductList));
													mContext.startActivity(openDetailView);
													((AppCompatActivity) mContext).overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
												} else {
													Log.e("ProductListSize", "empty");
												}
											} catch (Exception ex) {
												ex.printStackTrace();
											}
										}
									});
									break;

								default:
									dismissFragmentDialog();
									break;
							}
						}
					}

					@Override
					public void failure(RetrofitError error) {
						dismissFragmentDialog();
						if (error.toString().contains("Unable to resolve host"))
							mErrorHandlerView.showToast();
					}
				});
	}

	private void dismissFragmentDialog() {
		if (mProgressDialogFragment != null) {
			if (mProgressDialogFragment.isVisible()) {
				mProgressDialogFragment.dismiss();
			}
		}
	}
}