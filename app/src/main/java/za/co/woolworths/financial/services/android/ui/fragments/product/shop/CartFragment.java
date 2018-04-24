package za.co.woolworths.financial.services.android.ui.fragments.product.shop;

import android.app.Activity;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import za.co.woolworths.financial.services.android.models.dto.CommerceItem;
import za.co.woolworths.financial.services.android.models.dto.CartResponse;
import za.co.woolworths.financial.services.android.models.dto.ChangeQuantity;
import za.co.woolworths.financial.services.android.models.dto.Data;
import za.co.woolworths.financial.services.android.models.dto.DeliveryLocationHistory;
import za.co.woolworths.financial.services.android.models.dto.OrderSummary;
import za.co.woolworths.financial.services.android.models.dto.Province;
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse;
import za.co.woolworths.financial.services.android.models.dto.Suburb;
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
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import static za.co.woolworths.financial.services.android.models.service.event.BadgeState.CART_COUNT_TEMP;
import static za.co.woolworths.financial.services.android.models.service.event.CartState.CHANGE_QUANTITY;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.CANCEL_DIALOG_TAPPED;
import static za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow.CART_DEFAULT_ERROR_TAPPED;

public class CartFragment extends Fragment implements CartProductAdapter.OnItemClick, View.OnClickListener, NetworkChangeListener {

	private int mQuantity;
	private String mSuburbName, mProvinceName;
	private RelativeLayout rlLocationSelectedLayout;
	private boolean onRemoveItemFailed = false;
	private boolean mRemoveAllItemFailed = false;

	public interface ToggleRemoveItem {
		void onRemoveItem(boolean visibility);

		void onRemoveSuccess();
	}

	private ToggleRemoveItem mToggleItemRemoved;

	private RecyclerView rvCartList;
	private WButton btnCheckOut;
	private CartProductAdapter cartProductAdapter;
	private WoolworthsApplication mWoolWorthsApplication;
	private RelativeLayout parentLayout;
	private ProgressBar pBar;
	private RelativeLayout relEmptyStateHandler;
	private ArrayList<CartItemGroup> cartItems;
	private OrderSummary orderSummary;
	private WTextView tvDeliveryLocation;
	private CompositeDisposable mDisposables = new CompositeDisposable();
	private RelativeLayout rlCheckOut;
	private ChangeQuantity mChangeQuantity;
	private BroadcastReceiver mConnectionBroadcast;
	private ErrorHandlerView mErrorHandlerView;
	private CommerceItem mCommerceItem;
	private boolean changeQuantityWasClicked = false;

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
				mToggleItemRemoved = (ToggleRemoveItem) activity;
			}
		} catch (IllegalStateException ex) {
			Log.d("mToggleItemRemoved", ex.toString());
		}
		mChangeQuantity = new ChangeQuantity();
		rvCartList = view.findViewById(R.id.cartList);
		btnCheckOut = view.findViewById(R.id.btnCheckOut);
		rlCheckOut = view.findViewById(R.id.rlCheckOut);
		RelativeLayout rlNoConnectionLayout = view.findViewById(R.id.no_connection_layout);
		parentLayout = view.findViewById(R.id.parentLayout);
		pBar = view.findViewById(R.id.loadingBar);
		relEmptyStateHandler = view.findViewById(R.id.relEmptyStateHandler);
		WButton mBtnRetry = view.findViewById(R.id.btnRetry);
		mWoolWorthsApplication = ((WoolworthsApplication) getActivity().getApplication());
		mErrorHandlerView = new ErrorHandlerView(getActivity(), rlNoConnectionLayout);
		mErrorHandlerView.setMargin(rlNoConnectionLayout, 0, 0, 0, 0);
		mConnectionBroadcast = Utils.connectionBroadCast(getActivity(), this);
		rlLocationSelectedLayout = view.findViewById(R.id.locationSelectedLayout);
		rlLocationSelectedLayout.setOnClickListener(this);
		mBtnRetry.setOnClickListener(this);
		btnCheckOut.setOnClickListener(this);
		tvDeliveryLocation = view.findViewById(R.id.tvDeliveryLocation);
		emptyCartUI(view);
		final Activity activity = getActivity();
		if (activity != null) {
			CartActivity cartActivity = (CartActivity) activity;
			cartActivity.hideEditCart();
		}

		loadShoppingCart(false).execute();
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
									loadShoppingCart(false).execute();
									tvDeliveryLocation.setText(cartState.getState());
								} else if (cartState.getIndexState() == CHANGE_QUANTITY) {
									mQuantity = cartState.getQuantity();
									changeQuantityAPI(new ChangeQuantity(mQuantity, mChangeQuantity.getCommerceId())).execute();
								}
							} else if (object instanceof ProductState) {
								ProductState productState = (ProductState) object;
								switch (productState.getState()) {
									case CANCEL_DIALOG_TAPPED: // reset change quantity state value
										if (cartProductAdapter != null)
											cartProductAdapter.onPopUpCancel(CANCEL_DIALOG_TAPPED);
										break;
									default:
										break;
								}
							}
						}
					}
				}));
	}

	private void closeActivity(final Activity activity) {
		getView().postDelayed(new Runnable() {

			@Override
			public void run() {
				activity.finish();
				activity.overridePendingTransition(R.anim.slide_down_anim, R.anim.stay);
			}

		}, 10);
	}

	private void emptyCartUI(View view) {
		ImageView imEmptyCart = view.findViewById(R.id.imgEmpyStateIcon);
		imEmptyCart.setImageResource(R.drawable.cart_empty_vector);
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
					rvCartList.setVisibility(View.VISIBLE);
					loadShoppingCart(false).execute();
				}
				break;
			case R.id.btnCheckOut:
				Activity checkOutActivity = getActivity();
				if (checkOutActivity != null) {
					Intent openCheckOutActivity = new Intent(getContext(), CartCheckoutActivity.class);
					startActivityForResult(openCheckOutActivity, CheckOutFragment.REQUEST_CART_REFRESH_ON_DESTROY);
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
	public void onChangeQuantity(CommerceItem commerceId) {
		mCommerceItem = commerceId;
		mChangeQuantity.setCommerceId(commerceId.commerceItemInfo.getCommerceId());
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

	@Override
	public void totalItemInBasket(int total) {

	}

	public boolean toggleEditMode() {
		boolean isEditMode = cartProductAdapter.toggleEditMode();
		Utils.fadeInFadeOutAnimation(btnCheckOut, isEditMode);
		resetItemDelete(isEditMode);
		return isEditMode;
	}

	private void resetItemDelete(boolean isEditMode) {
		if (isEditMode) {
			for (CartItemGroup cartItemGroup : cartItems) {
				ArrayList<CommerceItem> commerceItemList = cartItemGroup.commerceItems;
				for (CommerceItem cm : commerceItemList) {
					cm.setDeleteIconWasPressed(false);
				}
			}
		}
		if (cartProductAdapter != null)
			cartProductAdapter.notifyDataSetChanged();
	}

	private void locationSelectionClicked() {
		Activity activity = getActivity();
		if (activity != null) {
			Intent openDeliveryLocationSelectionActivity = new Intent(this.getContext(), DeliveryLocationSelectionActivity.class);
			openDeliveryLocationSelectionActivity.putExtra("suburbName", mSuburbName);
			openDeliveryLocationSelectionActivity.putExtra("provinceName", mProvinceName);
			startActivityForResult(openDeliveryLocationSelectionActivity, CheckOutFragment.REQUEST_CART_REFRESH_ON_DESTROY);
			activity.overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay);
		}
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
			Utils.sendBus(new BadgeState(CART_COUNT_TEMP, 0));
			rvCartList.setVisibility(View.GONE);
			rlCheckOut.setVisibility(View.GONE);
			mToggleItemRemoved.onRemoveSuccess();
			relEmptyStateHandler.setVisibility(View.VISIBLE);
			deliveryLocationEnabled(true);
			Activity activity = getActivity();
			if (activity != null) {
				CartActivity cartActivity = (CartActivity) activity;
				cartActivity.resetToolBarIcons();
			}
		}
	}

	public void updateCart(CartResponse cartResponse, CommerceItem commerceItem) {
		this.cartItems = cartResponse.cartItems;
		this.orderSummary = cartResponse.orderSummary;
		if (cartResponse.cartItems.size() > 0 && cartProductAdapter != null) {
			cartProductAdapter.removeItem(cartResponse.cartItems, cartResponse.orderSummary, commerceItem);
		} else {
			cartProductAdapter.clear();
			Activity activity = getActivity();
			if (activity != null) {
				CartActivity cartActivity = (CartActivity) activity;
				cartActivity.resetToolBarIcons();
			}
			rlCheckOut.setVisibility(View.GONE);
			rvCartList.setVisibility(View.GONE);
			relEmptyStateHandler.setVisibility(View.VISIBLE);
			deliveryLocationEnabled(true);
		}
		updateCartSummary(cartResponse.orderSummary.totalItemsCount);
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
		updateCartSummary(cartResponse.orderSummary.totalItemsCount);
		onChangeQuantityComplete();
	}

	private void updateCartSummary(int cartCount) {
		Utils.sendBus(new BadgeState(CART_COUNT_TEMP, cartCount));
	}

	private void onChangeQuantityComplete() {
		if (cartProductAdapter != null)
			cartProductAdapter.onChangeQuantityComplete();
	}

	private void onChangeQuantityLoad() {
		cartProductAdapter.onChangeQuantityLoad();
	}

	private HttpAsyncTask<String, String, ShoppingCartResponse> loadShoppingCart(final boolean onItemRemove) {
		mErrorHandlerView.hideErrorHandler();
		return new HttpAsyncTask<String, String, ShoppingCartResponse>() {

			@Override
			protected void onPreExecute() {
				deliveryLocationEnabled(false);
				rlCheckOut.setEnabled(onItemRemove ? false : true);
				rlCheckOut.setVisibility(onItemRemove ? View.VISIBLE : View.GONE);
				pBar.setVisibility(View.VISIBLE);
				//parentLayout.setVisibility(View.GONE);
				//Utils.showOneTimePopup(getActivity(), SessionDao.KEY.CART_FIRST_ORDER_FREE_DELIVERY, tvFreeDeliveryFirstOrder);
				if (cartProductAdapter != null) {
					cartProductAdapter.clear();
				}
				Activity activity = getActivity();
				if (activity != null) {
					CartActivity cartActivity = (CartActivity) activity;
					cartActivity.hideEditCart();
				}
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
							if (!onItemRemove) {
								deliveryLocationEnabled(true);
								rvCartList.setVisibility(View.GONE);
								rlCheckOut.setVisibility(View.GONE);
								mErrorHandlerView.showErrorHandler();
							}
						}
					});
				}
				return new ShoppingCartResponse();
			}

			@Override
			protected void onPostExecute(ShoppingCartResponse shoppingCartResponse) {
				try {
					pBar.setVisibility(View.GONE);
					switch (shoppingCartResponse.httpCode) {
						case 200:
							onRemoveItemFailed = false;
							rlCheckOut.setVisibility(View.VISIBLE);
							rlCheckOut.setEnabled(true);
							CartResponse cartResponse = convertResponseToCartResponseObject(shoppingCartResponse);
							bindCartData(cartResponse);
							if (onItemRemove) {
								cartProductAdapter.setEditMode(true);
							}
							onChangeQuantityComplete();
							deliveryLocationEnabled(true);
							break;
						case 440:
							//TODO:: improve error handling
							SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE);
							SessionExpiredUtilities.INSTANCE.showSessionExpireDialog(getActivity());
							onChangeQuantityComplete();
							break;
						default:
							deliveryLocationEnabled(true);
							if (shoppingCartResponse.response != null)
								Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, shoppingCartResponse.response.desc, true);
							break;
					}
					deliveryLocationEnabled(true);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
	}

	public void deliveryLocationEnabled(boolean enabled) {
		Animation animFadeOut = AnimationUtils.loadAnimation(this.getContext(), R.anim.edit_mode_fade_out);
		animFadeOut.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				rlLocationSelectedLayout.setEnabled(false);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		Animation animFadeIn = AnimationUtils.loadAnimation(this.getContext(), R.anim.edit_mode_fade_in);
		animFadeIn.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				rlLocationSelectedLayout.setEnabled(true);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		if (enabled) {
			rlLocationSelectedLayout.startAnimation(animFadeIn);
		} else {
			rlLocationSelectedLayout.startAnimation(animFadeOut);
		}
	}

	private HttpAsyncTask<String, String, ShoppingCartResponse> changeQuantityAPI(final ChangeQuantity changeQuantity) {
		mChangeQuantity = changeQuantity;
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
							changeQuantityWasClicked = true;
							if (cartProductAdapter != null)
								cartProductAdapter.onChangeQuantityError();

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
			}

			@Override
			protected Class<ShoppingCartResponse> httpDoInBackgroundReturnType() {
				return ShoppingCartResponse.class;
			}

			@Override
			protected ShoppingCartResponse httpDoInBackground(String... params) {
				return ((WoolworthsApplication) getActivity().getApplication()).getApi().removeCartItem(commerceItem.commerceItemInfo.getCommerceId());
			}

			@Override
			protected ShoppingCartResponse httpError(final String errorMessage, HttpErrorCode httpErrorCode) {
				final Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (cartProductAdapter != null) {
								onRemoveItemLoadFail(commerceItem, true);
								onRemoveItemFailed = true;
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
							updateCart(cartResponse, commerceItem);
							if (cartResponse.cartItems != null) {
								if (cartResponse.cartItems.size() == 0)
									mToggleItemRemoved.onRemoveSuccess();
							} else {
								mToggleItemRemoved.onRemoveSuccess();
							}
							break;
						default:
							if (cartProductAdapter != null)
								resetItemDelete(true);
							break;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
	}

	public HttpAsyncTask<String, String, ShoppingCartResponse> removeAllCartItem(final CommerceItem commerceItem) {
		return new HttpAsyncTask<String, String, ShoppingCartResponse>() {

			@Override
			protected void onPreExecute() {
				//showProgress();
				mToggleItemRemoved.onRemoveItem(true);
			}

			@Override
			protected Class<ShoppingCartResponse> httpDoInBackgroundReturnType() {
				return ShoppingCartResponse.class;
			}

			@Override
			protected ShoppingCartResponse httpDoInBackground(String... params) {
				Utils.sendBus(new BadgeState(CART_COUNT_TEMP, 0));
				return ((WoolworthsApplication) getActivity().getApplication()).getApi().removeAllCartItems();
			}

			@Override
			protected ShoppingCartResponse httpError(final String errorMessage, HttpErrorCode httpErrorCode) {
				final Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mRemoveAllItemFailed = true;
							mToggleItemRemoved.onRemoveItem(false);
							mErrorHandlerView.hideErrorHandler();
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
							updateCart(cartResponse, commerceItem);
							mToggleItemRemoved.onRemoveSuccess();
							break;
						default:
							mToggleItemRemoved.onRemoveItem(false);
							break;
					}
					deliveryLocationEnabled(true);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
	}


	private void removeItemProgressBar(CommerceItem commerceItem, boolean visibility) {
		if (commerceItem == null) {
			mToggleItemRemoved.onRemoveItem(visibility);
		}
	}

	private void onRemoveItemLoadFail(CommerceItem commerceItem, boolean state) {
		mCommerceItem = commerceItem;
		resetItemDelete(true);
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
				else if (key.contains("homeCommerceItem"))
					cartItemGroup.setType("HOME");
				else if (key.contains("foodCommerceItem"))
					cartItemGroup.setType("FOOD");
				else if (key.contains("clothingCommerceItem"))
					cartItemGroup.setType("CLOTHING");
				else if (key.contains("premiumBrandCommerceItem"))
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
			if (!TextUtils.isEmpty(data.suburbName) && !TextUtils.isEmpty(data.provinceName)) {
				Activity activity = getActivity();
				mSuburbName = data.suburbName;
				mProvinceName = data.provinceName;
				if (activity != null) {
					String suburbId = String.valueOf(data.suburbId);
					Province province = new Province();
					province.name = data.provinceName;
					province.id = suburbId;
					Suburb suburb = new Suburb();
					suburb.name = data.suburbName;
					suburb.id = suburbId;
					Utils.saveRecentDeliveryLocation(new DeliveryLocationHistory(province, suburb), activity);
				}
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == CART_DEFAULT_ERROR_TAPPED) {
			Activity activity = getActivity();
			activity.setResult(CART_DEFAULT_ERROR_TAPPED);
			activity.finish();
			activity.overridePendingTransition(R.anim.slide_down_anim, R.anim.stay);
			return;
		}
		if (requestCode == CheckOutFragment.REQUEST_CART_REFRESH_ON_DESTROY) {
			loadShoppingCart(false).execute();
		}
	}

	@Override
	public void onConnectionChanged() {

		if (onRemoveItemFailed) {
			mErrorHandlerView.hideErrorHandler();
			loadShoppingCart(true).execute();
			return;
		}

		if (mRemoveAllItemFailed) {
			removeAllCartItem(null).execute();
			mRemoveAllItemFailed = false;
			return;
		}

		if (changeQuantityWasClicked) {
			if (cartProductAdapter != null) {
				cartProductAdapter.onChangeQuantityLoad(mCommerceItem);
			}
			changeQuantityAPI(new ChangeQuantity(mQuantity, mChangeQuantity.getCommerceId())).execute();
			changeQuantityWasClicked = false;
		}
	}

	private void removeItemAPI(CommerceItem mCommerceItem) {
		HttpAsyncTask<String, String, ShoppingCartResponse> removeCartItem = removeCartItem(mCommerceItem);
		removeCartItem.execute();
	}
}
