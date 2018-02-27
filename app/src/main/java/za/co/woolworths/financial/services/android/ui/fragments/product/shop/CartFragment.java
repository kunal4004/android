package za.co.woolworths.financial.services.android.ui.fragments.product.shop;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit.Callback;
import retrofit.RetrofitError;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.CartItemGroup;
import za.co.woolworths.financial.services.android.models.dto.CartProduct;
import za.co.woolworths.financial.services.android.models.dto.CartResponse;
import za.co.woolworths.financial.services.android.models.dto.OrderSummary;
import za.co.woolworths.financial.services.android.models.dto.PriceInfo;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.models.service.event.CartState;
import za.co.woolworths.financial.services.android.ui.activities.CartActivity;
import za.co.woolworths.financial.services.android.ui.activities.CartCheckoutActivity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.DeliveryLocationSelectionActivity;
import za.co.woolworths.financial.services.android.ui.adapters.CartProductAdapter;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DetailFragment;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.CancelableCallback;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.Utils;


public class CartFragment extends Fragment implements CartProductAdapter.OnItemClick, View.OnClickListener {

	private RecyclerView rvCartList;
	private WButton btnCheckOut;

	private CartProductAdapter cartProductAdapter;
	private WoolworthsApplication mWoolWorthsApplication;
	private RelativeLayout parentLayout;
	private ProgressBar pBar;
	private RelativeLayout relEmptyStateHandler;
	private ArrayList<CartItemGroup> cartItems;
	private OrderSummary orderSummary;
	private ProgressDialog progressDialog;
	private WTextView tvDeliveryLocation;
	private WTextView tvFreeDeliveryFirstOrder;
	private CompositeDisposable mDisposables = new CompositeDisposable();
	private RelativeLayout rlCheckOut;

	public CartFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_cart, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		rvCartList = view.findViewById(R.id.cartList);
		btnCheckOut = view.findViewById(R.id.btnCheckOut);
		rlCheckOut = view.findViewById(R.id.rlCheckOut);
		parentLayout = view.findViewById(R.id.parentLayout);
		pBar = view.findViewById(R.id.loadingBar);
		relEmptyStateHandler = view.findViewById(R.id.relEmptyStateHandler);
		mWoolWorthsApplication = ((WoolworthsApplication) getActivity().getApplication());
		view.findViewById(R.id.locationSelectedLayout).setOnClickListener(this);
		btnCheckOut.setOnClickListener(this);
		tvFreeDeliveryFirstOrder = view.findViewById(R.id.tvFreeDeliveryFirstOrder);
		tvDeliveryLocation = view.findViewById(R.id.tvDeliveryLocation);
		progressDialog = new ProgressDialog(getActivity());
		emptyCartUI(view);
		Activity activity = getActivity();
		if (activity != null) {
			CartActivity cartActivity = (CartActivity) activity;
			cartActivity.hideEditCart();
		}

		loadShoppingCart();

		mDisposables.add(WoolworthsApplication.getInstance()
				.bus()
				.toObservable()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Consumer<Object>() {
					@Override
					public void accept(Object object) throws Exception {
						if (object != null) {
							if (object instanceof CartState) {
								CartState cartState = (CartState) object;
								if (!TextUtils.isEmpty(cartState.getState())) {
									loadShoppingCart();
									tvDeliveryLocation.setText(cartState.getState());
								}
							}
						}
					}
				}));

	}

	private void emptyCartUI(View view) {
		ImageView imgEmpyStateIcon = view.findViewById(R.id.imgEmpyStateIcon);
		WTextView txtEmptyStateTitle = view.findViewById(R.id.txtEmptyStateTitle);
		WTextView txtEmptyStateDesc = view.findViewById(R.id.txtEmptyStateDesc);
		WButton btnGoToProduct = view.findViewById(R.id.btnGoToProduct);

		txtEmptyStateTitle.setText(getString(R.string.empty_cart));
		txtEmptyStateDesc.setText(getString(R.string.empty_cart_desc));
		btnGoToProduct.setVisibility(View.VISIBLE);
		btnGoToProduct.setText(getString(R.string.start_shopping));
		btnGoToProduct.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.locationSelectedLayout:
				locationSelectionClicked();
				break;
			case R.id.btnGoToProduct:
				Activity activity = getActivity();
				if (activity != null) {
					activity.setResult(Activity.RESULT_OK);
					activity.finish();
					activity.overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
				}
				break;

			case R.id.btnCheckOut:
				Activity checkOutActivity = getActivity();
				if (checkOutActivity != null) {
					Intent openCheckOutActivity = new Intent(checkOutActivity, CartCheckoutActivity.class);
					checkOutActivity.startActivity(openCheckOutActivity);
					checkOutActivity.overridePendingTransition(0, 0);
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void onItemClick(CartProduct cartProduct) {
		getProductDetail(cartProduct.productId, cartProduct.catalogRefId);
	}

	@Override
	public void onItemDeleteClick(String commerceId) {
		// Log.i("CartFragment", "Item " + itemRow.productItem.productName + " delete button clicked!");

		// TODO: Make API call to remove item + show loading before removing from list
		removeCartItem(commerceId);
	}

	public boolean toggleEditMode() {
		boolean isEditMode = cartProductAdapter.toggleEditMode();
		rlCheckOut.setVisibility(isEditMode ? View.GONE : View.VISIBLE);
		return isEditMode;
	}

	public void clearAllCartItems() {
		showProgress();
		mWoolWorthsApplication.getAsyncApi().removeAllCartItems(new CancelableCallback<String>() {
			@Override
			public void onSuccess(String s, retrofit.client.Response response) {
				Log.i("result ", s);
				CartResponse cartResponse = convertResponseToCartResponseObject(s);
				if (cartResponse != null) {
					switch (cartResponse.httpCode) {
						case 200:
							updateCart(cartResponse);
							break;
						default:
							break;
					}
				}
			}

			@Override
			public void onFailure(RetrofitError error) {
				Log.i("result ", "failed");
			}
		});
	}

	private void locationSelectionClicked() {
		startActivity(new Intent(this.getContext(), DeliveryLocationSelectionActivity.class));
		this.getActivity().overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay);
	}

	public void loadShoppingCart() {
		pBar.setVisibility(View.VISIBLE);
		rlCheckOut.setVisibility(View.GONE);
		//parentLayout.setVisibility(View.GONE);
		Utils.showOneTimePopup(getActivity(), SessionDao.KEY.CART_FIRST_ORDER_FREE_DELIVERY, tvFreeDeliveryFirstOrder);
		mWoolWorthsApplication.getAsyncApi().getShoppingCart(new CancelableCallback<String>() {
			@Override
			public void onSuccess(String s, retrofit.client.Response response) {
				pBar.setVisibility(View.GONE);
				rlCheckOut.setVisibility(View.VISIBLE);
				Log.i("result ", s);
				CartResponse cartResponse = convertResponseToCartResponseObject(s);
				if (cartResponse != null) {
					switch (cartResponse.httpCode) {
						case 200:
							bindCartData(cartResponse);
							break;
						default:
							break;
					}
				}
			}

			@Override
			public void onFailure(final RetrofitError error) {
				final Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {

							//TODO:: improve error handling
							if (error.getBody().toString().contains("440")) {
								Utils.sessionDaoSave(activity, SessionDao.KEY.CART_FIRST_ORDER_FREE_DELIVERY, null);
								ScreenManager.presentSSOSignin(activity);
								activity.finish();
								activity.overridePendingTransition(0, 0);
							} else {
								Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, error.getBody().toString());
							}
						}
					});
				}
			}
		});
	}

	public CartResponse convertResponseToCartResponseObject(String response) {
		CartResponse cartResponse = null;

		if (TextUtils.isEmpty(response))
			return null;

		try {
			JSONObject jsonObject = new JSONObject(response);
			cartResponse = new CartResponse();
			cartResponse.httpCode = jsonObject.getInt("httpCode");

			JSONObject dataObject = jsonObject.getJSONArray("data").getJSONObject(0);
			JSONObject itemsObject = dataObject.getJSONObject("items");
			Iterator<String> keys = itemsObject.keys();
			ArrayList<CartItemGroup> cartItemGroups = new ArrayList<>();
			while ((keys.hasNext())) {
				CartItemGroup cartItemGroup = new CartItemGroup();
				String key = keys.next();

				if (key.contains("food"))
					cartItemGroup.setType("FOOD");
				else if (key.contains("cloth"))
					cartItemGroup.setType("CLOTHING");
				else if (key.contains("home"))
					cartItemGroup.setType("HOME");
				else
					cartItemGroup.setType("OTHERS");

				JSONArray productsArray = itemsObject.getJSONArray(key);
				if (productsArray.length() > 0) {
					ArrayList<CartProduct> productList = new ArrayList<>();
					for (int i = 0; i < productsArray.length(); i++) {
						JSONObject proObject = productsArray.getJSONObject(i);
						CartProduct cartProduct = new CartProduct();
						cartProduct.setQuantity(proObject.getInt("quantity"));
						cartProduct.setProductId(proObject.getString("productId"));
						cartProduct.setInternalImageURL(proObject.getString("internalImageURL"));
						cartProduct.setExternalImageURL(proObject.getString("externalImageURL"));
						cartProduct.setCatalogRefId(proObject.getString("catalogRefId"));
						cartProduct.setProductDisplayName(proObject.getString("productDisplayName"));
						cartProduct.setCommerceId(proObject.getString("id"));

						PriceInfo pInfo = new PriceInfo();
						pInfo.setAmount(proObject.getJSONObject("priceInfo").getDouble("amount"));
						pInfo.setListPrice(proObject.getJSONObject("priceInfo").getDouble("listPrice"));

						cartProduct.setPriceInfo(pInfo);

						productList.add(cartProduct);
					}
					cartItemGroup.setCartProducts(productList);
				}
				cartItemGroups.add(cartItemGroup);
			}

			cartResponse.cartItems = cartItemGroups;

			OrderSummary orderSummary = new OrderSummary();
			orderSummary.setBasketTotal(dataObject.getJSONObject("orderSummary").getDouble("basketTotal"));
			orderSummary.setTotal(dataObject.getJSONObject("orderSummary").getDouble("total"));
			orderSummary.setEstimatedDelivery(dataObject.getJSONObject("orderSummary").getDouble("estimatedDelivery"));
			orderSummary.setTotalItemsCount(dataObject.getJSONObject("orderSummary").getInt("totalItemsCount"));

			cartResponse.orderSummary = orderSummary;

			// set delivery location
			if (dataObject.has("suburbName") && dataObject.has("provinceName")) {
				tvDeliveryLocation.setText(dataObject.getString("suburbName") + ", " + dataObject.getString("provinceName"));
			} else {
				tvDeliveryLocation.setText(getString(R.string.set_your_delivery_location));
			}

		} catch (JSONException e) {
			e.printStackTrace();
			cartResponse = null;
			return cartResponse;
		}

		return cartResponse;
	}

	public void bindCartData(CartResponse cartResponse) {
		parentLayout.setVisibility(View.VISIBLE);
		if (cartResponse.cartItems.size() > 0) {
			rlCheckOut.setVisibility(View.VISIBLE);
			Activity activity = getActivity();
			if (activity != null) {
				CartActivity cartActivity = (CartActivity) activity;
				cartActivity.showEditCart();
			}
			cartItems = cartResponse.cartItems;
			orderSummary = cartResponse.orderSummary;

			cartProductAdapter = new CartProductAdapter(cartItems, this, orderSummary, getActivity());
			LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
			mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
			rvCartList.setLayoutManager(mLayoutManager);
			rvCartList.setAdapter(cartProductAdapter);
		} else {
			rlCheckOut.setVisibility(View.GONE);
			relEmptyStateHandler.setVisibility(View.VISIBLE);
		}
	}

	public void removeCartItem(String commerceId) {
		showProgress();
		mWoolWorthsApplication.getAsyncApi().removeCartItem(commerceId, new CancelableCallback<String>() {
			@Override
			public void onSuccess(String s, retrofit.client.Response response) {
				Log.i("result ", s);
				CartResponse cartResponse = convertResponseToCartResponseObject(s);
				if (cartResponse != null) {
					switch (cartResponse.httpCode) {
						case 200:
							updateCart(cartResponse);
							break;
						default:
							break;

					}
				}
			}

			@Override
			public void onFailure(RetrofitError error) {
				Log.i("result ", "failed");
			}
		});
	}

	public void updateCart(CartResponse cartResponse) {
		if (cartResponse.cartItems.size() > 0 && cartProductAdapter != null) {
			cartItems = cartResponse.cartItems;
			orderSummary = cartResponse.orderSummary;
			cartProductAdapter.removeItem(cartItems, orderSummary);

		} else {
			cartProductAdapter.clear();
			Activity activity = getActivity();
			if (activity != null) {
				CartActivity cartActivity = (CartActivity) activity;
				cartActivity.resetToolBarIcons();
			}
			rlCheckOut.setVisibility(View.GONE);
			relEmptyStateHandler.setVisibility(View.VISIBLE);
		}
		progressDialog.dismiss();
	}

	public void showProgress() {
		progressDialog.setMessage("Removing...");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(false);
		progressDialog.show();

	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (mDisposables != null
				&& !mDisposables.isDisposed()) {
			mDisposables.dispose();
		}
	}

	private void getProductDetail(final String productId, final String skuId) {
		final Activity activity = getActivity();
		if (activity != null) {
			pBar.setVisibility(View.VISIBLE);
			WoolworthsApplication.getInstance().getAsyncApi()
					.getProductDetail(productId, skuId, new Callback<String>() {
						@Override
						public void success(final String strProduct, retrofit.client.Response response) {
							final WProduct wProduct = Utils.stringToJson(activity, strProduct);
							if (wProduct != null) {
								switch (wProduct.httpCode) {
									case 200:
										activity.runOnUiThread(new Runnable() {
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
														FragmentTransaction transaction = ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction();
														transaction.replace(R.id.bottom_Fragment, detailFragment).commit();
													}
													CartActivity cartActivity = (CartActivity) activity;
													cartActivity.slideUpBottomView();

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
						}
					});
		}
	}

	private void dismissFragmentDialog() {
		pBar.setVisibility(View.GONE);
	}
}
