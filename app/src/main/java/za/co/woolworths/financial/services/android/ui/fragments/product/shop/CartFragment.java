package za.co.woolworths.financial.services.android.ui.fragments.product.shop;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.CartItemGroup;
import za.co.woolworths.financial.services.android.models.dto.CartProduct;
import za.co.woolworths.financial.services.android.models.dto.CartResponse;
import za.co.woolworths.financial.services.android.models.dto.ChangeQuantity;
import za.co.woolworths.financial.services.android.models.dto.Data;
import za.co.woolworths.financial.services.android.models.dto.OrderSummary;
import za.co.woolworths.financial.services.android.models.dto.PriceInfo;
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.service.event.BadgeState;
import za.co.woolworths.financial.services.android.models.service.event.CartState;
import za.co.woolworths.financial.services.android.ui.activities.CartActivity;
import za.co.woolworths.financial.services.android.ui.activities.CartCheckoutActivity;
import za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.DeliveryLocationSelectionActivity;
import za.co.woolworths.financial.services.android.ui.adapters.CartProductAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.models.service.event.BadgeState.CART_COUNT;
import static za.co.woolworths.financial.services.android.models.service.event.CartState.CHANGE_QUANTITY;

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
	ChangeQuantity mChangeQuantity;

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
		mChangeQuantity = new ChangeQuantity();
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

		loadShoppingCart().execute();
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
									loadShoppingCart().execute();
									tvDeliveryLocation.setText(cartState.getState());
								} else if (cartState.getIndexState() == CHANGE_QUANTITY) {
									int quantity = cartState.getQuantity();
									executeChangeQuantity(new ChangeQuantity(quantity, mChangeQuantity.getCommerceId())).execute();
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
	}

	@Override
	public void onItemDeleteClick(String commerceId) {
		// Log.i("CartFragment", "Item " + itemRow.productItem.productName + " delete button clicked!");
		// TODO: Make API call to remove item + show loading before removing from list
		removeCartItem(commerceId).execute();
	}

	@Override
	public void onChangeQuantity(String commerceId) {
		mChangeQuantity.setCommerceId(commerceId);
		if (mWoolWorthsApplication != null) {
			WGlobalState wGlobalState = mWoolWorthsApplication.getWGlobalState();
			if (wGlobalState != null) {
				wGlobalState.navigateFromQuantity(1);
			}
		}
		Activity activity = getActivity();
		if (activity != null) {
			Intent editQuantityIntent = new Intent(activity, ConfirmColorSizeActivity.class);
			editQuantityIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, ConfirmColorSizeActivity.QUANTITY);
			activity.startActivity(editQuantityIntent);
			activity.overridePendingTransition(0, 0);
		}
	}

	public boolean toggleEditMode() {
		boolean isEditMode = cartProductAdapter.toggleEditMode();
		rlCheckOut.setVisibility(isEditMode ? View.GONE : View.VISIBLE);
		return isEditMode;
	}


	private void locationSelectionClicked() {
		startActivity(new Intent(this.getContext(), DeliveryLocationSelectionActivity.class));
		this.getActivity().overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay);
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
		updateCartSummary();
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


	public void changeQuantity(CartResponse cartResponse) {
		if (cartResponse.cartItems.size() > 0 && cartProductAdapter != null) {
			cartItems = cartResponse.cartItems;
			orderSummary = cartResponse.orderSummary;
			cartProductAdapter.changeQuantity(cartItems, orderSummary);
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
		updateCartSummary();
		onChangeQuantityComplete();
	}

	private void updateCartSummary() {
		Utils.sendBus(new BadgeState(CART_COUNT, CART_COUNT));
	}

	private void onChangeQuantityComplete() {
		cartProductAdapter.onChangeQuantityComplete();
	}

	private void onChangeQuantityLoad() {
		cartProductAdapter.onChangeQuantityLoad();
	}

	private HttpAsyncTask<String, String, ShoppingCartResponse> loadShoppingCart() {
		return new HttpAsyncTask<String, String, ShoppingCartResponse>() {

			@Override
			protected void onPreExecute() {
				pBar.setVisibility(View.VISIBLE);
				rlCheckOut.setVisibility(View.GONE);
				//parentLayout.setVisibility(View.GONE);
				Utils.showOneTimePopup(getActivity(), SessionDao.KEY.CART_FIRST_ORDER_FREE_DELIVERY, tvFreeDeliveryFirstOrder);

			}

			@Override
			protected Class<ShoppingCartResponse> httpDoInBackgroundReturnType() {
				return ShoppingCartResponse.class;
			}

			@Override
			protected ShoppingCartResponse httpDoInBackground(String... params) {
				return ((WoolworthsApplication) getActivity().getApplication()).getApi().getShoppingCart();
			}

			@Override
			protected ShoppingCartResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				return new ShoppingCartResponse();
			}

			@Override
			protected void onPostExecute(ShoppingCartResponse shoppingCartResponse) {
				try {
					pBar.setVisibility(View.GONE);
					rlCheckOut.setVisibility(View.VISIBLE);
					int httpCode = shoppingCartResponse.httpCode;
					switch (httpCode) {
						case 200:
							CartResponse cartResponse = convertResponseToCartResponseObject(shoppingCartResponse);
							bindCartData(cartResponse);
							break;
						case 440:
							final Activity activity = getActivity();
							if (activity != null) {
								activity.runOnUiThread(new Runnable() {
									@Override
									public void run() {

										//TODO:: improve error handling
										Utils.sessionDaoSave(activity, SessionDao.KEY.CART_FIRST_ORDER_FREE_DELIVERY, null);
										ScreenManager.presentSSOSignin(activity);
										activity.finish();
										activity.overridePendingTransition(0, 0);
									}
								});
							}
							break;
						default:
							if (shoppingCartResponse.response != null)
								Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, shoppingCartResponse.response.desc);
							break;
					}
					onChangeQuantityComplete();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
	}

	private HttpAsyncTask<String, String, ShoppingCartResponse> executeChangeQuantity(final ChangeQuantity changeQuantity) {
		return new HttpAsyncTask<String, String, ShoppingCartResponse>() {

			@Override
			protected void onPreExecute() {
				cartProductAdapter.onChangeQuantityLoad();
			}

			@Override
			protected Class<ShoppingCartResponse> httpDoInBackgroundReturnType() {
				return ShoppingCartResponse.class;
			}

			@Override
			protected ShoppingCartResponse httpDoInBackground(String... params) {
				return ((WoolworthsApplication) getActivity().getApplication()).getApi().getChangeQuantity(changeQuantity);
			}

			@Override
			protected ShoppingCartResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (cartProductAdapter != null)
								cartProductAdapter.onChangeQuantityComplete();
						}
					});
				}
				return new ShoppingCartResponse();
			}

			@Override
			protected void onPostExecute(ShoppingCartResponse shoppingCartResponse) {
				try {
					int httpCode = shoppingCartResponse.httpCode;
					switch (httpCode) {
						case 200:
							CartResponse cartResponse = convertResponseToCartResponseObject(shoppingCartResponse);
							changeQuantity(cartResponse);
							break;
						default:
							onChangeQuantityComplete();
							break;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
	}

	public HttpAsyncTask<String, String, ShoppingCartResponse> removeCartItem(final String commerceId) {
		return new HttpAsyncTask<String, String, ShoppingCartResponse>() {

			@Override
			protected void onPreExecute() {
				showProgress();
			}

			@Override
			protected Class<ShoppingCartResponse> httpDoInBackgroundReturnType() {
				return ShoppingCartResponse.class;
			}

			@Override
			protected ShoppingCartResponse httpDoInBackground(String... params) {
				if (commerceId == null)
					return ((WoolworthsApplication) getActivity().getApplication()).getApi().removeAllCartItems();
				else
					return ((WoolworthsApplication) getActivity().getApplication()).getApi().removeCartItem(commerceId);
			}

			@Override
			protected ShoppingCartResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				return new ShoppingCartResponse();
			}

			@Override
			protected void onPostExecute(ShoppingCartResponse shoppingCartResponse) {
				try {
					int httpCode = shoppingCartResponse.httpCode;
					switch (httpCode) {
						case 200:
							CartResponse cartResponse = convertResponseToCartResponseObject(shoppingCartResponse);
							updateCart(cartResponse);
							break;
						default:
							break;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
	}

	public CartResponse convertResponseToCartResponseObject(ShoppingCartResponse response) {
		CartResponse cartResponse = null;

		if (response == null)
			return null;

		try {
			cartResponse = new CartResponse();
			cartResponse.httpCode = response.httpCode;
			Data data = response.data[0];
			JSONObject itemsObject = new JSONObject(new Gson().toJson(data.items));
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

			cartResponse.orderSummary = data.orderSummary;

			// set delivery location
			if (data.suburbName != null && data.provinceName != null) {
				tvDeliveryLocation.setText(data.suburbName + ", " + data.provinceName);
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
}
