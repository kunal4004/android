package za.co.woolworths.financial.services.android.ui.fragments.wtoday;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DetailFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.GridFragment;
import za.co.woolworths.financial.services.android.ui.views.ProductProgressDialogFrag;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.MyRunnable;
import za.co.woolworths.financial.services.android.util.PauseHandlerFragment;
import za.co.woolworths.financial.services.android.util.Utils;

public class WebAppInterface {
	private ErrorHandlerView mErrorHandlerView;
	private Context mContext;
	private ProductProgressDialogFrag mProgressDialogFragment;
	private PauseHandlerFragment mPauseHandlerFragment;
	private BottomNavigator mBottomNavigator;

	/**
	 * Instantiate the interface and set the context
	 * must be added for API 17 or higher
	 */

	public WebAppInterface(Context c) {
		mContext = c;
		mErrorHandlerView = new ErrorHandlerView(c);
		mBottomNavigator = (BottomNavigator) mContext;
	}

	@JavascriptInterface
	public void showProducts(String categoryId, String categoryName) {
		GridFragment gridFragment = new GridFragment();
		Bundle bundle = new Bundle();
		bundle.putString("sub_category_id", categoryId);
		bundle.putString("sub_category_name", categoryName);
		bundle.putString("str_search_product", "");
		gridFragment.setArguments(bundle);
		mBottomNavigator.pushFragment(gridFragment);
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
							CustomPopUpWindow.MODAL_LAYOUT.SHOPPING_LIST_INFO,
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
					public void success(final String strProduct, retrofit.client.Response response) {
						final WProduct wProduct = Utils.stringToJson(mContext, strProduct);
						if (wProduct != null) {
							switch (wProduct.httpCode) {
								case 200:
									((AppCompatActivity) mContext).runOnUiThread(new Runnable() {
										@Override
										public void run() {
											try {
												ArrayList<WProductDetail> mProductList;
												WProductDetail productList = wProduct.product;
												mProductList = new ArrayList<>();
												if (productList != null) {
													mProductList.add(productList);
												}
												if (mProductList.size() > 0 && mProductList.get(0).productId != null) {
													GsonBuilder builder = new GsonBuilder();
													Gson gson = builder.create();
													DetailFragment detailFragment = new DetailFragment();
													String strProductList = gson.toJson(mProductList.get(0));
													Bundle bundle = new Bundle();
													bundle.putString("strProductList", strProductList);
													bundle.putString("strProductCategory", mProductList.get(0).productName);
													bundle.putString("productResponse", strProduct);
													bundle.putBoolean("fetchFromJson", true);
													detailFragment.setArguments(bundle);
													FragmentTransaction transaction = ((AppCompatActivity) mContext).getSupportFragmentManager().beginTransaction();
													transaction.replace(R.id.fragment_bottom_container, detailFragment).commit();
													mBottomNavigator.slideUpBottomView();
												}
											} catch (Exception ex) {
												ex.printStackTrace();
											}

											dismissFragmentDialog();
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