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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest;
import za.co.woolworths.financial.services.android.models.dto.CartItemGroup;
import za.co.woolworths.financial.services.android.models.dto.CartResponse;
import za.co.woolworths.financial.services.android.models.dto.ChangeQuantity;
import za.co.woolworths.financial.services.android.models.dto.CommerceItem;
import za.co.woolworths.financial.services.android.models.dto.Data;
import za.co.woolworths.financial.services.android.models.dto.OrderSummary;
import za.co.woolworths.financial.services.android.models.dto.Province;
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation;
import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.models.dto.SkuInventory;
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse;
import za.co.woolworths.financial.services.android.models.dto.Suburb;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.rest.product.GetInventorySkusForStore;
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
import za.co.woolworths.financial.services.android.util.MultiMap;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.ToastUtils;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.models.service.event.BadgeState.CART_COUNT_TEMP;
import static za.co.woolworths.financial.services.android.models.service.event.CartState.CHANGE_QUANTITY;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.CANCEL_DIALOG_TAPPED;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.CLOSE_PDP_FROM_ADD_TO_LIST;
import static za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow.CART_DEFAULT_ERROR_TAPPED;

public class CartFragment extends Fragment implements CartProductAdapter.OnItemClick, View.OnClickListener, NetworkChangeListener, ToastUtils.ToastInterface {

	private int mQuantity;
	private String mSuburbName, mProvinceName;
	private RelativeLayout rlLocationSelectedLayout;
	private boolean onRemoveItemFailed = false;
	private boolean mRemoveAllItemFailed = false;
	private static final int REQUEST_SUBURB_CHANGE = 143;
	private boolean mShouldDisplayCheckout;
	private String mStoreId;
	private Map<String, String> mMapStoreId;
	private String TAG = this.getClass().getSimpleName();
	private ToastUtils mToastUtils;
	public static final int MOVE_TO_LIST_ON_TOAST_VIEW_CLICKED = 1020;
	private int mNumberOfListSelected;
	private Map<String, List<AddToListRequest>> mapOfItem;

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
	private WTextView tvDeliveringToText;
	private CompositeDisposable mDisposables = new CompositeDisposable();
	private RelativeLayout rlCheckOut;
	private ChangeQuantity mChangeQuantity;
	private BroadcastReceiver mConnectionBroadcast;
	private ErrorHandlerView mErrorHandlerView;
	private CommerceItem mCommerceItem;
	private boolean changeQuantityWasClicked = false;
	private boolean errorMessageWasPopUp = false;

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
		mMapStoreId = new HashMap<>();
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
		tvDeliveringToText = view.findViewById(R.id.tvDeliveringTo);
		ShoppingDeliveryLocation lastDeliveryLocation = Utils.getLastDeliveryLocation(getActivity());
		if (lastDeliveryLocation != null) {
			mSuburbName = lastDeliveryLocation.suburb.name;
			mProvinceName = lastDeliveryLocation.province.name;
			setDeliveryLocation(mSuburbName + ", " + mProvinceName);
		}
		emptyCartUI(view);
		final Activity activity = getActivity();
		if (activity != null) {
			CartActivity cartActivity = (CartActivity) activity;
			cartActivity.hideEditCart();
		}

		loadShoppingCart(false).execute();
		mToastUtils = new ToastUtils(this);
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
									setDeliveryLocation(mSuburbName + ", " + mProvinceName);
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
									case CLOSE_PDP_FROM_ADD_TO_LIST:
										mToastUtils.setActivity(activity);
										mToastUtils.setCurrentState(TAG);
										String shoppingList = getString(R.string.shopping_list);
										mNumberOfListSelected = productState.getCount();
										// shopping list vs shopping lists
										mToastUtils.setCartText((mNumberOfListSelected > 1) ? shoppingList + "s" : shoppingList);
										mToastUtils.setPixel(btnCheckOut.getHeight() * 2);
										mToastUtils.setView(btnCheckOut);
										mToastUtils.setMessage(R.string.added_to);
										mToastUtils.setViewState(true);
										mToastUtils.build();
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
		String firstName =  SessionUtilities.getInstance().getJwt().name.get(0);
		ImageView imEmptyCart = view.findViewById(R.id.imgEmpyStateIcon);
		imEmptyCart.setImageResource(R.drawable.ic_empty_cart);
		WTextView txtEmptyStateTitle = view.findViewById(R.id.txtEmptyStateTitle);
		WTextView txtEmptyStateDesc = view.findViewById(R.id.txtEmptyStateDesc);
		WButton btnGoToProduct = view.findViewById(R.id.btnGoToProduct);
		txtEmptyStateTitle.setText("HI "+firstName+",");
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
					errorMessageWasPopUp = false;
					rvCartList.setVisibility(View.VISIBLE);
					loadShoppingCart(false).execute();
				}
				break;
			case R.id.btnCheckOut:
				Activity checkOutActivity = getActivity();
				if ((checkOutActivity != null) && shouldDisplayCheckout()) {
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
			editQuantityIntent.putExtra("CART_QUANTITY_In_STOCK", commerceId.quantityInStock);
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
			startActivityForResult(openDeliveryLocationSelectionActivity, REQUEST_SUBURB_CHANGE);
			activity.overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay);
		}
	}

	public void bindCartData(CartResponse cartResponse) {
		parentLayout.setVisibility(View.VISIBLE);
		if (cartResponse.cartItems.size() > 0) {
			loadInventoryRequest(cartResponse.cartItems);
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
			Utils.deliveryLocationEnabled(getActivity(), true, rlLocationSelectedLayout);
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
			Utils.deliveryLocationEnabled(getActivity(), true, rlLocationSelectedLayout);
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
		setShouldDisplayCheckout(true);
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
				Utils.deliveryLocationEnabled(getActivity(), false, rlLocationSelectedLayout);
				rlCheckOut.setEnabled(onItemRemove ? false : true);
				rlCheckOut.setVisibility(onItemRemove ? View.VISIBLE : View.GONE);
				pBar.setVisibility(View.VISIBLE);
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
								Utils.deliveryLocationEnabled(getActivity(), true, rlLocationSelectedLayout);
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
							Utils.deliveryLocationEnabled(getActivity(), true, rlLocationSelectedLayout);
							break;
						case 440:
							//TODO:: improve error handling
							SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE);
							SessionExpiredUtilities.INSTANCE.showSessionExpireDialog(getActivity());
							onChangeQuantityComplete();
							break;
						default:
							Utils.deliveryLocationEnabled(getActivity(), true, rlLocationSelectedLayout);
							if (shoppingCartResponse.response != null)
								Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, shoppingCartResponse.response.desc, true);
							break;
					}
					Utils.deliveryLocationEnabled(getActivity(), true, rlLocationSelectedLayout);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
	}

	private HttpAsyncTask<String, String, ShoppingCartResponse> changeQuantityAPI(final ChangeQuantity changeQuantity) {
		mChangeQuantity = changeQuantity;
		return new HttpAsyncTask<String, String, ShoppingCartResponse>() {

			@Override
			protected void onPreExecute() {
				cartProductAdapter.onChangeQuantityLoad();
				setShouldDisplayCheckout(false);
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
					Utils.deliveryLocationEnabled(getActivity(), true, rlLocationSelectedLayout);
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
					suburb.fulfillmentStores = data.orderSummary.suburb.fulfillmentStores;
					Utils.saveRecentDeliveryLocation(new ShoppingDeliveryLocation(province, suburb), activity);
					setDeliveryLocation(mSuburbName + ", " + mProvinceName);
				}
			}
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
						CommerceItem commerceItem = new CommerceItem();
						commerceItem = new Gson().fromJson(String.valueOf(productsArray.getJSONObject(i)), CommerceItem.class);
						String fulfillmentStoreId = Utils.retrieveStoreId(commerceItem.fulfillmentType, getActivity());
						commerceItem.fulfillmentStoreId = fulfillmentStoreId.replaceAll("\"", "");
						productList.add(commerceItem);
					}
					cartItemGroup.setCommerceItems(productList);
				}
				cartItemGroups.add(cartItemGroup);
			}

			cartResponse.cartItems = cartItemGroups;


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
		if (requestCode == CheckOutFragment.REQUEST_CART_REFRESH_ON_DESTROY || requestCode == REQUEST_SUBURB_CHANGE) {
			loadShoppingCart(false).execute();
			ShoppingDeliveryLocation lastDeliveryLocation = Utils.getLastDeliveryLocation(getActivity());
			if (lastDeliveryLocation != null) {
				mSuburbName = lastDeliveryLocation.suburb.name;
				mProvinceName = lastDeliveryLocation.province.name;
			}
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

	public void loadInventoryRequest(ArrayList<CartItemGroup> items) {
		MultiMap<String, CommerceItem> multiListItems = MultiMap.create();
		fadeCheckoutButton();
		for (CartItemGroup cartItemGroup : items) {
			for (CommerceItem commerceItem : cartItemGroup.getCommerceItems()) {
				multiListItems.put(commerceItem.fulfillmentStoreId, commerceItem);
			}
		}
		Map<String, Collection<CommerceItem>> mapStoreIdWithCommerceItems = multiListItems.getEntries();

		for (Map.Entry<String, Collection<CommerceItem>> collectionEntry : mapStoreIdWithCommerceItems.entrySet()) {
			Collection<CommerceItem> collection = collectionEntry.getValue();
			String fullfilmentStoreId = collectionEntry.getKey();
			fullfilmentStoreId = fullfilmentStoreId.replaceAll("\"", "");
			List<String> skuIds = new ArrayList<>();
			for (CommerceItem commerceItem : collection) {
				skuIds.add(commerceItem.commerceItemInfo.catalogRefId);
			}
			String multiSKUS = TextUtils.join("-", skuIds);
			mMapStoreId.put("storeId", fullfilmentStoreId);
			initInventoryRequest(fullfilmentStoreId, multiSKUS).execute();
		}
	}

	public GetInventorySkusForStore initInventoryRequest(String storeId, String multiSku) {
		return new GetInventorySkusForStore(storeId, multiSku, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				SkusInventoryForStoreResponse skusInventoryForStoreResponse = (SkusInventoryForStoreResponse) object;
				switch (skusInventoryForStoreResponse.httpCode) {
					case 200:
						mStoreId = skusInventoryForStoreResponse.storeId;
						updateCartListWithAvailableStock(skusInventoryForStoreResponse.skuInventory, skusInventoryForStoreResponse.storeId);
						break;
					default:
						if (!errorMessageWasPopUp) {
							Activity activity = getActivity();
							if (activity == null) return;
							if (skusInventoryForStoreResponse == null) return;
							if (skusInventoryForStoreResponse.response == null) return;
							if (TextUtils.isEmpty(skusInventoryForStoreResponse.response.desc))
								return;
							Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, skusInventoryForStoreResponse.response.desc);
							errorMessageWasPopUp = true;
						}
						break;
				}
			}

			@Override
			public void onFailure(String e) {

			}
		});
	}

	public void updateCartListWithAvailableStock(List<SkuInventory> inventories, String storeID) {
		HashMap<String, Integer> inventoryMap = new HashMap<>();
		for (SkuInventory skuInventory : inventories) {
			inventoryMap.put(skuInventory.sku, skuInventory.quantity);
		}

		for (CartItemGroup cartItemGroup : cartItems) {
			for (CommerceItem commerceItem : cartItemGroup.commerceItems) {

				if (commerceItem.fulfillmentStoreId.equalsIgnoreCase(storeID)) {
					String sku = commerceItem.commerceItemInfo.getCatalogRefId();
					commerceItem.quantityInStock = inventoryMap.containsKey(sku) ? inventoryMap.get(sku) : 0;
					commerceItem.isStockChecked = true;
				}

				if (commerceItem.quantityInStock != 0) {
					setShouldDisplayCheckout(true);
				}
			}
		}
		/**
		 * @Method getLastValueInMap() return last stored store Id
		 * to trigger checkout button only once
		 */
		if (getLastValueInMap().equalsIgnoreCase(mStoreId)) {
			if (shouldDisplayCheckout())
				fadeCheckoutButton();
		}
		if (cartProductAdapter != null)
			cartProductAdapter.updateStockAvailability(cartItems);
	}

	/***
	 * @param displayCheckout true will enable checkout button with fade in animation
	 *                        false will disable checkout button with fade out animation
	 */
	private void setShouldDisplayCheckout(boolean displayCheckout) {
		mShouldDisplayCheckout = displayCheckout;
		fadeCheckoutButton();
	}

	/***
	 * @method fadeCheckoutButton() is called before inventory api get executed to
	 * disable the checkout button
	 * It is called again after the last inventory call if
	 * @params mShouldDisplayCheckout is true only to avoid blinking animation on
	 *                               checkout button
	 */
	private void fadeCheckoutButton() {
		Utils.fadeInFadeOutAnimation(btnCheckOut, !shouldDisplayCheckout());
	}

	private boolean shouldDisplayCheckout() {
		return mShouldDisplayCheckout;
	}

	private String getLastValueInMap() {
		for (Map.Entry<String, String> entry : mMapStoreId.entrySet()) {
			return entry.getValue();
		}
		return null;
	}

	public void deliveryLocationEnabled(boolean isEditMode) {
		Utils.deliveryLocationEnabled(getActivity(), isEditMode, rlLocationSelectedLayout);
	}

	@Override
	public void onToastButtonClicked(String currentState) {
		Activity activity = getActivity();
		if (activity == null) return;
		Intent intent = new Intent();
		intent.putExtra("count", mNumberOfListSelected);
		if (mNumberOfListSelected == 1) {
			WoolworthsApplication woolworthsApplication = WoolworthsApplication.getInstance();
			if (woolworthsApplication == null) return;
			WGlobalState globalState = woolworthsApplication.getWGlobalState();
			List<ShoppingList> shoppingListRequest = globalState.getShoppingListRequest();
			for (ShoppingList shoppingList : shoppingListRequest) {
				if (shoppingList.viewIsSelected) {
					intent.putExtra("listId", shoppingList.listId);
					intent.putExtra("listName", shoppingList.listName);
				}
			}
		}
		activity.setResult(MOVE_TO_LIST_ON_TOAST_VIEW_CLICKED, intent);
		activity.finish();
		activity.overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
	}

	public void setDeliveryLocation(String deliveryLocation) {
		tvDeliveringToText.setText(getContext().getString(R.string.delivering_to));
		tvDeliveryLocation.setVisibility(View.VISIBLE);
		tvDeliveryLocation.setText(deliveryLocation);
	}

}
