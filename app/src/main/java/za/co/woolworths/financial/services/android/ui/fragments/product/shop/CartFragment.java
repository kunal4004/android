package za.co.woolworths.financial.services.android.ui.fragments.product.shop;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.daimajia.swipe.util.Attributes;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.CartItemGroup;
import za.co.woolworths.financial.services.android.models.dto.CommerceItem;
import za.co.woolworths.financial.services.android.models.dto.CartResponse;
import za.co.woolworths.financial.services.android.models.dto.ChangeQuantity;
import za.co.woolworths.financial.services.android.models.dto.Data;
import za.co.woolworths.financial.services.android.models.dto.DeliveryLocationHistory;
import za.co.woolworths.financial.services.android.models.dto.OrderSummary;
import za.co.woolworths.financial.services.android.models.dto.Product;
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.service.event.BadgeState;
import za.co.woolworths.financial.services.android.models.service.event.CartState;
import za.co.woolworths.financial.services.android.models.service.event.ProductState;
import za.co.woolworths.financial.services.android.ui.activities.CartActivity;
import za.co.woolworths.financial.services.android.ui.activities.CartCheckoutActivity;
import za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.DeliveryLocationSelectionActivity;
import za.co.woolworths.financial.services.android.ui.adapters.CartProductAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.models.service.event.BadgeState.CART_COUNT;
import static za.co.woolworths.financial.services.android.models.service.event.CartState.CHANGE_QUANTITY;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.CANCEL_CALL;

public class CartFragment extends Fragment implements CartProductAdapter.OnItemClick, View.OnClickListener, NetworkChangeListener {

	public interface ToggleRemoveItem {
		void onRemoveItem(boolean visibility);

		void onRemoveSuccess();
	}

	private ToggleRemoveItem toggleRemoveItem;

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
	private ChangeQuantity mChangeQuantity;
	private BroadcastReceiver mConnectionBroadcast;
	private ErrorHandlerView mErrorHandlerView;
	private CommerceItem mCommerceItem;
	private boolean mRemoveItemFailed = false;
	private RelativeLayout rlNoConnectionLayout;
	private WButton mBtnRetry;

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

		try {
			Activity activity = getActivity();
			if (activity != null) {
				toggleRemoveItem = (ToggleRemoveItem) activity;
			}
		} catch (IllegalStateException ex) {
			Log.d("toggleRemoveItem", ex.toString());
		}

		mChangeQuantity = new ChangeQuantity();
		rvCartList = view.findViewById(R.id.cartList);
		btnCheckOut = view.findViewById(R.id.btnCheckOut);
		rlCheckOut = view.findViewById(R.id.rlCheckOut);
		rlNoConnectionLayout = view.findViewById(R.id.no_connection_layout);
		parentLayout = view.findViewById(R.id.parentLayout);
		pBar = view.findViewById(R.id.loadingBar);
		relEmptyStateHandler = view.findViewById(R.id.relEmptyStateHandler);
		mBtnRetry = view.findViewById(R.id.btnRetry);
		mWoolWorthsApplication = ((WoolworthsApplication) getActivity().getApplication());
		mErrorHandlerView = new ErrorHandlerView(getActivity(), rlNoConnectionLayout);
		mErrorHandlerView.setMargin(rlNoConnectionLayout, 0, 0, 0, 0);
		mConnectionBroadcast = Utils.connectionBroadCast(getActivity(), this);
		view.findViewById(R.id.locationSelectedLayout).setOnClickListener(this);
		mBtnRetry.setOnClickListener(this);

		btnCheckOut.setOnClickListener(this);
		tvFreeDeliveryFirstOrder = view.findViewById(R.id.tvFreeDeliveryFirstOrder);
		tvDeliveryLocation = view.findViewById(R.id.tvDeliveryLocation);
		progressDialog = new ProgressDialog(getActivity());
		emptyCartUI(view);
		Activity activity = getActivity();
		if (activity != null) {
			CartActivity cartActivity = (CartActivity) activity;
			cartActivity.hideEditCart();
			List<DeliveryLocationHistory> history = Utils.getDeliveryLocationHistory(activity);
			if (history != null && history.size() > 0) {
				tvDeliveryLocation.setText(history.get(0).suburb.name + ", " + history.get(0).province.name);
			} else {
				tvDeliveryLocation.setText(getString(R.string.set_your_delivery_location));
			}
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
							} else if (object instanceof ProductState) {
								ProductState productState = (ProductState) object;
								switch (productState.getState()) {
									case CANCEL_CALL: // reset change quantity state value
										if (cartProductAdapter != null)
											cartProductAdapter.onPopUpCancel(CANCEL_CALL);
										break;

									default:
										break;
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
			case R.id.btnRetry:
				if (new ConnectionDetector().isOnline(getActivity())) {
					loadShoppingCart().execute();
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
	public void onItemDeleteClick(CommerceItem commerceItem) {
		// TODO: Make API call to remove item + show loading before removing from list
		removeItemAPI(commerceItem);
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
		btnCheckOut.setEnabled(isEditMode ? false : true);
		rlCheckOut.setEnabled(isEditMode ? false : true);
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


			// Setting Mode to Single to reveal bottom View for one item in List
			// Setting Mode to Mutliple to reveal bottom Views for multile items in List
			((CartProductAdapter) cartProductAdapter).setMode(Attributes.Mode.Single);

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
		mErrorHandlerView.hideErrorHandler();
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
			protected ShoppingCartResponse httpError(final String errorMessage, HttpErrorCode httpErrorCode) {
				Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							rlCheckOut.setVisibility(View.GONE);
							mErrorHandlerView.showErrorHandler();
						}
					});
				}
				return new ShoppingCartResponse();
			}

			@Override
			protected void onPostExecute(ShoppingCartResponse shoppingCartResponse) {
				try {
					pBar.setVisibility(View.GONE);
					int httpCode = shoppingCartResponse.httpCode;
					switch (httpCode) {

						case 200:
							rlCheckOut.setVisibility(View.VISIBLE);
							CartResponse cartResponse = convertResponseToCartResponseObject(shoppingCartResponse);
							bindCartData(cartResponse);
							onChangeQuantityComplete();
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
							onChangeQuantityComplete();
							break;
						default:
							onChangeQuantityComplete();
							if (shoppingCartResponse.response != null)
								Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, shoppingCartResponse.response.desc);
							break;
					}
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
			protected ShoppingCartResponse httpError(final String errorMessage, HttpErrorCode httpErrorCode) {
				Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mErrorHandlerView.showToast();
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

	public HttpAsyncTask<String, String, ShoppingCartResponse> removeCartItem(final CommerceItem commerceItem) {
		mCommerceItem = commerceItem;
		return new HttpAsyncTask<String, String, ShoppingCartResponse>() {

			@Override
			protected void onPreExecute() {
				//showProgress();
				removeItemProgressBar(commerceItem, true);
			}

			@Override
			protected Class<ShoppingCartResponse> httpDoInBackgroundReturnType() {
				return ShoppingCartResponse.class;
			}

			@Override
			protected ShoppingCartResponse httpDoInBackground(String... params) {
				if (commerceItem == null)
					return ((WoolworthsApplication) getActivity().getApplication()).getApi().removeAllCartItems();
				else
					return ((WoolworthsApplication) getActivity().getApplication()).getApi().removeCartItem(commerceItem.getCommerceId());
			}

			@Override
			protected ShoppingCartResponse httpError(final String errorMessage, HttpErrorCode httpErrorCode) {
				final Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (commerceItem == null) {
								mRemoveItemFailed = true;
								toggleRemoveItem.onRemoveItem(false);
							} else {
								if (cartProductAdapter != null) {
									onRemoveItemLoadFail(commerceItem, true);
								}
							}
							mErrorHandlerView.showToast();
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
							updateCart(cartResponse);
							removeItemProgressBar(commerceItem, false);
							if (commerceItem == null) {
								toggleRemoveItem.onRemoveSuccess();
							}
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

	private void removeItemProgressBar(CommerceItem commerceItem, boolean visibility) {
		if (commerceItem == null) {
			toggleRemoveItem.onRemoveItem(visibility);
		}
	}

	private void onRemoveItemLoadFail(CommerceItem commerceItem, boolean state) {
		mRemoveItemFailed = state;
		mCommerceItem = commerceItem;
		// remove single item
		if (commerceItem.getCommerceId() == null) {
			cartProductAdapter.toggleDeleteSingleItem(commerceItem);
		}
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

				//GENERAL - "default",HOME - "homeCommerceItem",FOOD
				// - "foodCommerceItem",CLOTHING
				// - "clothingCommerceItem",PREMIUM BRANDS
				// - "premiumBrandCommerceItem",
				// Anything else: OTHER

				if (key.contains("default"))
					cartItemGroup.setType("GENERAL");
				else if (key.contains("home"))
					cartItemGroup.setType("HOME");
				else if (key.contains("food"))
					cartItemGroup.setType("FOOD");
				else if (key.contains("cloth"))
					cartItemGroup.setType("CLOTHING");
				else if (key.contains("premiumBrand"))
					cartItemGroup.setType("PREMIUM BRAND");
				else
					cartItemGroup.setType("OTHER");

				JSONArray productsArray = itemsObject.getJSONArray(key);
				if (productsArray.length() > 0) {
					ArrayList<CommerceItem> productList = new ArrayList<>();
					for (int i = 0; i < productsArray.length(); i++) {
						productList.add(new Gson().fromJson(String.valueOf(productsArray.getJSONObject(i)), CommerceItem.class));
					}
					cartItemGroup.setCommerceItems(productList);
				}
				cartItemGroups.add(cartItemGroup);
			}

			cartResponse.cartItems = cartItemGroups;

			cartResponse.orderSummary = data.orderSummary;

			// set delivery location
			if (data.suburbName != null && data.provinceName != null) {
				tvDeliveryLocation.setText(data.suburbName + ", " + data.provinceName);
			}

		} catch (JSONException e) {
			e.printStackTrace();
			cartResponse = null;
			return cartResponse;
		}

		return cartResponse;
	}

	@Override
	public void onResume() {
		super.onResume();
		Activity activity = getActivity();
		if (activity != null) {
			activity.registerReceiver(mConnectionBroadcast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		Activity activity = getActivity();
		if (activity != null) {
			activity.unregisterReceiver(mConnectionBroadcast);
		}
	}

	@Override
	public void onConnectionChanged() {
		if (mRemoveItemFailed) {
			if (mCommerceItem != null) {
				if (cartProductAdapter != null)
					cartProductAdapter.toggleDeleteSingleItem(mCommerceItem);
				removeItemAPI(mCommerceItem);
			} else {
				removeItemAPI(null);
			}
			mRemoveItemFailed = false;
		}
	}

	private void removeItemAPI(CommerceItem mCommerceItem) {
		HttpAsyncTask<String, String, ShoppingCartResponse> removeCartItem = removeCartItem(mCommerceItem);
		removeCartItem.execute();
	}
}
