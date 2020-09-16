package za.co.woolworths.financial.services.android.ui.fragments.product.shop;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.awfs.coordination.R;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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
import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.IResponseListener;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.CartItemGroup;
import za.co.woolworths.financial.services.android.models.dto.CartResponse;
import za.co.woolworths.financial.services.android.models.dto.ChangeQuantity;
import za.co.woolworths.financial.services.android.models.dto.CommerceItem;
import za.co.woolworths.financial.services.android.models.dto.CommerceItemInfo;
import za.co.woolworths.financial.services.android.models.dto.Data;
import za.co.woolworths.financial.services.android.models.dto.GlobalMessages;
import za.co.woolworths.financial.services.android.models.dto.OrderSummary;
import za.co.woolworths.financial.services.android.models.dto.ProductDetails;
import za.co.woolworths.financial.services.android.models.dto.Province;
import za.co.woolworths.financial.services.android.models.dto.SetDeliveryLocationSuburbResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation;
import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.models.dto.SkuInventory;
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.models.service.event.CartState;
import za.co.woolworths.financial.services.android.models.service.event.ProductState;
import za.co.woolworths.financial.services.android.ui.activities.CartActivity;
import za.co.woolworths.financial.services.android.ui.activities.CartCheckoutActivity;
import za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.adapters.CartProductAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.KotlinUtils;
import za.co.woolworths.financial.services.android.util.MultiMap;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.ToastUtils;
import za.co.woolworths.financial.services.android.util.Utils;

import static android.app.Activity.RESULT_OK;
import static za.co.woolworths.financial.services.android.models.service.event.CartState.CHANGE_QUANTITY;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.CANCEL_DIALOG_TAPPED;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.CLOSE_PDP_FROM_ADD_TO_LIST;
import static za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow.CART_DEFAULT_ERROR_TAPPED;
import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_CART;
import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.PDP_REQUEST_CODE;
import static za.co.woolworths.financial.services.android.ui.views.actionsheet.ActionSheetDialogFragment.DIALOG_REQUEST_CODE;

public class CartFragment extends Fragment implements CartProductAdapter.OnItemClick, View.OnClickListener, NetworkChangeListener, ToastUtils.ToastInterface, WMaterialShowcaseView.IWalkthroughActionListener, RemoveProductsFromCartDialogFragment.IRemoveProductsFromCartDialog {

	private int mQuantity;
	private RelativeLayout rlLocationSelectedLayout;
	private boolean onRemoveItemFailed = false;
	private boolean mRemoveAllItemFailed = false;
	private static final int REQUEST_SUBURB_CHANGE = 143;
	private String mStoreId;
	private Map<String, String> mMapStoreId;
	private String TAG = this.getClass().getSimpleName();
	private ToastUtils mToastUtils;
	public static final int MOVE_TO_LIST_ON_TOAST_VIEW_CLICKED = 1020;
	private int mNumberOfListSelected;
	private List<ChangeQuantity> mChangeQuantityList;
	private boolean mRemoveAllItemFromCartTapped = false;
	private HashMap<String, List<SkuInventory>> mSkuInventories;
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
	private boolean isAllInventoryAPICallSucceed;
	private ImageView imgDeliveryLocation;
	private TextView upSellMessageTextView;
	private Map<String, Collection<CommerceItem>> mapStoreIdWithCommerceItems;
	private ImageView deliverLocationIcon;
	private ImageView deliverLocationRightArrow;
	private WTextView editLocation;

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
		}
		mMapStoreId = new HashMap<>();
		mChangeQuantityList = new ArrayList<>();
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
		imgDeliveryLocation = view.findViewById(R.id.truckIcon);
		upSellMessageTextView = view.findViewById(R.id.upSellMessageTextView);
		rlLocationSelectedLayout.setOnClickListener(this);
		mBtnRetry.setOnClickListener(this);
		btnCheckOut.setOnClickListener(this);
		tvDeliveryLocation = view.findViewById(R.id.tvDeliveryLocation);
		tvDeliveringToText = view.findViewById(R.id.tvDeliveringTo);
		deliverLocationIcon = view.findViewById(R.id.deliverLocationIcon);
		editLocation = view.findViewById(R.id.editLocation);
		deliverLocationRightArrow = view.findViewById(R.id.iconCaretRight);
		ShoppingDeliveryLocation lastDeliveryLocation = Utils.getPreferredDeliveryLocation();
		if (lastDeliveryLocation != null) {
			setDeliveryLocation(lastDeliveryLocation);
		}
		emptyCartUI(view);
		final Activity activity = getActivity();
		if (activity != null) {
			CartActivity cartActivity = (CartActivity) activity;
			cartActivity.hideEditCart();
		}

		loadShoppingCart(false);
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
									//setDeliveryLocation(cartState.getState());
								} else if (cartState.getIndexState() == CHANGE_QUANTITY) {
									mQuantity = cartState.getQuantity();
									postChangeQuantity();
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

	/****
	 * mChangeQuantityList save all ChangeQuantityRequest after quantity selection
	 * Top ChangeQuantity item in list is selected
	 * Extract commerceId of the selected ChangeQuantity object
	 * Perform changeQuantity call
	 * Remove top changeQuantity object from list
	 */

	private void postChangeQuantity() {
		mChangeQuantityList.add(mChangeQuantity);
		ChangeQuantity changeQuantity = mChangeQuantityList.get(0);
		changeQuantityAPI(new ChangeQuantity(mQuantity, changeQuantity.getCommerceId()));
		mChangeQuantityList.remove(0);
	}

	private void emptyCartUI(View view) {
		String firstName = SessionUtilities.getInstance().getJwt().name.get(0);
		ImageView imEmptyCart = view.findViewById(R.id.imgEmpyStateIcon);
		imEmptyCart.setImageResource(R.drawable.ic_empty_cart);
		WTextView txtEmptyStateTitle = view.findViewById(R.id.txtEmptyStateTitle);
		WTextView txtEmptyStateDesc = view.findViewById(R.id.txtEmptyStateDesc);
		WButton btnGoToProduct = view.findViewById(R.id.btnGoToProduct);
		txtEmptyStateTitle.setText("HI " + firstName + ",");
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
				if (NetworkManager.getInstance().isConnectedToNetwork(getActivity())) {
					errorMessageWasPopUp = false;
					rvCartList.setVisibility(View.VISIBLE);
					loadShoppingCart(false);
				}
				break;
			case R.id.btnCheckOut:
				Activity checkOutActivity = getActivity();
				if ((checkOutActivity != null) && btnCheckOut.isEnabled() && orderSummary != null) {
					if (KotlinUtils.Companion.isItemsQuantityForClickAndCollectExceed(orderSummary.totalItemsCount)) {
						showMaxItemView();
						return;
					}
					Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.CART_BEGIN_CHECKOUT);
					Intent openCheckOutActivity = new Intent(getContext(), CartCheckoutActivity.class);
					getActivity().startActivityForResult(openCheckOutActivity, CheckOutFragment.REQUEST_CART_REFRESH_ON_DESTROY);
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

	@Override
	public void onOpenProductDetail(CommerceItem commerceItem) {
		CartActivity cartActivity = (CartActivity) getActivity();
		ProductDetails productList = new ProductDetails();
		CommerceItemInfo commerceItemInfo = commerceItem.commerceItemInfo;
		productList.externalImageRef = commerceItemInfo.externalImageURL;
		productList.productName = commerceItemInfo.productDisplayName;
		productList.fromPrice = (float) commerceItem.priceInfo.getAmount();
		productList.productId = commerceItemInfo.productId;
		productList.sku = commerceItemInfo.catalogRefId;
		cartActivity.openProductDetailFragment("", productList);
	}

	public boolean toggleEditMode() {
		boolean isEditMode = cartProductAdapter.toggleEditMode();
		if (isAllInventoryAPICallSucceed)
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
			KotlinUtils.Companion.presentEditDeliveryLocationActivity(activity, REQUEST_SUBURB_CHANGE, null);
		}
	}

	public void bindCartData(CartResponse cartResponse) {
		parentLayout.setVisibility(View.VISIBLE);
		mSkuInventories = new HashMap<>();
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
            queryServiceInventoryCall(cartResponse.cartItems);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
			mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
			rvCartList.setLayoutManager(mLayoutManager);
			rvCartList.setAdapter(cartProductAdapter);
		} else {
			updateCartSummary(0);
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
			showFeatureWalkthrough();
		}
	}

	public void updateCart(CartResponse cartResponse, CommerceItem commerceItemToRemove) {
		this.orderSummary = cartResponse.orderSummary;
		if (cartResponse.cartItems.size() > 0 && cartProductAdapter != null && commerceItemToRemove != null) {
			ArrayList<CartItemGroup> emptyCartItemGroups = new ArrayList<>();
			for (CartItemGroup cartItemGroup : cartItems) {
				for (CommerceItem commerceItem : cartItemGroup.commerceItems) {
					if (commerceItem.commerceItemInfo.commerceId.equalsIgnoreCase(commerceItemToRemove.commerceItemInfo.commerceId)) {
						cartItemGroup.commerceItems.remove(commerceItem);
						break;
					}
				}
				/***
				 * Remove header when commerceItems is empty
				 */
				if (cartItemGroup.commerceItems.size() == 0) {
					emptyCartItemGroups.add(cartItemGroup);// Gather all the empty groups after deleting item.
				}
			}
			//remove all the empty groups
			for (CartItemGroup cartItemGroup : emptyCartItemGroups) {
				cartItems.remove(cartItemGroup);
			}

			cartProductAdapter.notifyAdapter(cartItems, orderSummary);
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
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (mDisposables != null
				&& !mDisposables.isDisposed()) {
			mDisposables.dispose();
		}
		if(CartActivity.walkThroughPromtView != null){
			CartActivity.walkThroughPromtView.removeFromWindow();
		}
	}

	public void changeQuantity(CartResponse cartResponse, ChangeQuantity changeQuantity) {
		if (cartResponse.cartItems.size() > 0 && cartProductAdapter != null) {
			CommerceItem updatedCommerceItem = getUpdatedCommerceItem(cartResponse.cartItems, changeQuantity.getCommerceId());
			//update list instead of using the new list to handle inventory data.
			if (updatedCommerceItem != null) {
				for (CartItemGroup cartItemGroup : cartItems) {
					for (CommerceItem commerceItem : cartItemGroup.commerceItems) {
						if (commerceItem.commerceItemInfo.commerceId.equalsIgnoreCase(updatedCommerceItem.commerceItemInfo.commerceId)) {
							commerceItem.commerceItemInfo = updatedCommerceItem.commerceItemInfo;
							commerceItem.priceInfo = updatedCommerceItem.priceInfo;
							commerceItem.setQuantityUploading(false);
						}
					}
				}
				orderSummary = cartResponse.orderSummary;
				cartProductAdapter.notifyAdapter(cartItems, orderSummary);
			}else {
				ArrayList<CartItemGroup> currentCartItemGroup = cartProductAdapter.getCartItems();
				for (CartItemGroup cartItemGroup : currentCartItemGroup) {
					for (CommerceItem currentItem : cartItemGroup.commerceItems) {
						if (currentItem.commerceItemInfo.commerceId.equalsIgnoreCase(changeQuantity.getCommerceId())) {
							cartItemGroup.commerceItems.remove(currentItem);
							if (cartItemGroup.commerceItems.size() == 0) {
								currentCartItemGroup.remove(cartItemGroup);
							}
							break;
						}
					}
				}

				boolean shouldEnableCheckOutAndEditButton = true;
				for (CartItemGroup items : currentCartItemGroup) {
					for (CommerceItem commerceItem : items.commerceItems) {
						if (commerceItem.getQuantityUploading()) {
							shouldEnableCheckOutAndEditButton = false;
							break;
						}
					}
				}

				if (shouldEnableCheckOutAndEditButton) {
					orderSummary = cartResponse.orderSummary;
					cartProductAdapter.notifyAdapter(currentCartItemGroup, orderSummary);
					fadeCheckoutButton(false);
				}
			}
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
		}
		onChangeQuantityComplete();
	}

	private CommerceItem getUpdatedCommerceItem(ArrayList<CartItemGroup> cartItems, String commerceId) {
		for (CartItemGroup cartItemGroup : cartItems) {
			for (CommerceItem commerceItem : cartItemGroup.commerceItems) {
				if (commerceItem.commerceItemInfo.commerceId.equalsIgnoreCase(commerceId))
					return commerceItem;
			}
		}
		return null;
	}

	private void updateCartSummary(int cartCount) {
		QueryBadgeCounter.getInstance().setCartCount(cartCount);
	}

	private void onChangeQuantityComplete() {
		boolean quantityUploaded = false;
		for (CartItemGroup cartItemGroup : cartItems) {
			for (CommerceItem commerceItem : cartItemGroup.commerceItems) {
				if (commerceItem.getQuantityUploading())
					quantityUploaded = true;
			}
		}
		if (isAllInventoryAPICallSucceed && !quantityUploaded) {
			mChangeQuantityList = new ArrayList<>();
			fadeCheckoutButton(false);
		}
		if (cartProductAdapter != null)
			cartProductAdapter.onChangeQuantityComplete();
	}

	private void onChangeQuantityLoad() {
		cartProductAdapter.onChangeQuantityLoad();
	}


	private Call<ShoppingCartResponse> loadShoppingCart(final boolean onItemRemove) {
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

		Call<ShoppingCartResponse> shoppingCartResponseCall = OneAppService.INSTANCE.getShoppingCart();
		shoppingCartResponseCall.enqueue(new CompletionHandler<>(new IResponseListener<ShoppingCartResponse>() {
			@Override
			public void onSuccess(ShoppingCartResponse shoppingCartResponse) {
				try {
					pBar.setVisibility(View.GONE);
					switch (shoppingCartResponse.httpCode) {
						case 200:
							onRemoveItemFailed = false;
							rlCheckOut.setVisibility(View.VISIBLE);
							rlCheckOut.setEnabled(true);
							CartResponse cartResponse = convertResponseToCartResponseObject(shoppingCartResponse);
							KotlinUtils.Companion.updateCheckOutLink(shoppingCartResponse.data[0].jSessionId);
							bindCartData(cartResponse);
							if (onItemRemove) {
								cartProductAdapter.setEditMode(true);
							}
							Utils.deliveryLocationEnabled(getActivity(), true, rlLocationSelectedLayout);
							break;
						case 440:
							//TODO:: improve error handling
							SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE);
							SessionExpiredUtilities.getInstance().showSessionExpireDialog((AppCompatActivity) getActivity(), CartFragment.this);
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

			@Override
			public void onFailure(Throwable error) {
				Activity activity = getActivity();
				if (activity != null && isAdded()) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (!onItemRemove) {
								Utils.deliveryLocationEnabled(getActivity(), true, rlLocationSelectedLayout);
								rvCartList.setVisibility(View.GONE);
								rlCheckOut.setVisibility(View.GONE);
								if (pBar != null)
									pBar.setVisibility(View.GONE);
								mErrorHandlerView.showErrorHandler();

							}
						}
					});
				}
			}
		},ShoppingCartResponse.class));

		return shoppingCartResponseCall;
	}

	private Call<ShoppingCartResponse> changeQuantityAPI(final ChangeQuantity changeQuantity) {
		cartProductAdapter.onChangeQuantityLoad();
		fadeCheckoutButton(true);
		Call<ShoppingCartResponse> shoppingCartResponseCall = OneAppService.INSTANCE.getChangeQuantity(changeQuantity);
		shoppingCartResponseCall.enqueue(new CompletionHandler<>(new IResponseListener<ShoppingCartResponse>() {
			@Override
			public void onSuccess(ShoppingCartResponse shoppingCartResponse) {
				try {
					if (shoppingCartResponse.httpCode == 200) {
						CartResponse cartResponse = convertResponseToCartResponseObject(shoppingCartResponse);
						changeQuantity(cartResponse, changeQuantity);
					} else {
						onChangeQuantityComplete();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			@Override
			public void onFailure(Throwable error) {
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
			}
		},ShoppingCartResponse.class));
		return shoppingCartResponseCall;
	}

	public void removeItem(CommerceItem commerceItem) {
		OneAppService.INSTANCE.removeCartItem(commerceItem.commerceItemInfo.commerceId).enqueue(new CompletionHandler<>(new IResponseListener<ShoppingCartResponse>() {
			@Override
			public void onSuccess(ShoppingCartResponse response) {
			}

			@Override
			public void onFailure(Throwable error) {

			}
		}, ShoppingCartResponse.class));
	}

	public Call<ShoppingCartResponse> removeCartItem(final CommerceItem commerceItem) {
		mCommerceItem = commerceItem;
		Call<ShoppingCartResponse> shoppingCartResponseCall = OneAppService.INSTANCE.removeCartItem(commerceItem.commerceItemInfo.getCommerceId());
		shoppingCartResponseCall.enqueue(new CompletionHandler<>(new IResponseListener<ShoppingCartResponse>() {
			@Override
			public void onSuccess(ShoppingCartResponse shoppingCartResponse) {
				try {
					if (shoppingCartResponse.httpCode == 200) {
						CartResponse cartResponse = convertResponseToCartResponseObject(shoppingCartResponse);
						updateCart(cartResponse, commerceItem);
						if (cartResponse.cartItems != null) {
							if (cartResponse.cartItems.size() == 0)
								mToggleItemRemoved.onRemoveSuccess();
						} else {
							mToggleItemRemoved.onRemoveSuccess();
						}
					} else {
						if (cartProductAdapter != null)
							resetItemDelete(true);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			@Override
			public void onFailure(Throwable error) {
				Activity activity = getActivity();
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
			}
		},ShoppingCartResponse.class));
		return shoppingCartResponseCall;
	}

	public Call<ShoppingCartResponse> removeAllCartItem(final CommerceItem commerceItem) {
		mRemoveAllItemFromCartTapped = true;
		mToggleItemRemoved.onRemoveItem(true);
		updateCartSummary(0);
		Call<ShoppingCartResponse> shoppingCartResponseCall = OneAppService.INSTANCE.removeAllCartItems();
		shoppingCartResponseCall.enqueue(new CompletionHandler<>(new IResponseListener<ShoppingCartResponse>() {
			@Override
			public void onSuccess(ShoppingCartResponse shoppingCartResponse) {
				try {
					if (shoppingCartResponse.httpCode == 200) {
						CartResponse cartResponse = convertResponseToCartResponseObject(shoppingCartResponse);
						mRemoveAllItemFromCartTapped = false;
						updateCart(cartResponse, commerceItem);
						mToggleItemRemoved.onRemoveSuccess();
					} else {
						mToggleItemRemoved.onRemoveItem(false);
					}
					Utils.deliveryLocationEnabled(getActivity(), true, rlLocationSelectedLayout);
				} catch (Exception ex) {
					if (ex.getMessage() != null)
						Log.e(TAG, ex.getMessage());
				}
			}

			@Override
			public void onFailure(Throwable error) {
				Activity activity = getActivity();
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
			}
		},ShoppingCartResponse.class));

		return shoppingCartResponseCall;
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

			displayUpSellMessage(response.data[0]);

			cartResponse = new CartResponse();
			cartResponse.httpCode = response.httpCode;
			Data data = response.data[0];
			cartResponse.orderSummary = data.orderSummary;
			// set delivery location
			if (!TextUtils.isEmpty(data.suburbName) && !TextUtils.isEmpty(data.provinceName)) {
				Province province = new Province();
				province.name = data.provinceName;
				if (cartResponse.orderSummary.suburb != null) {
					Utils.savePreferredDeliveryLocation(new ShoppingDeliveryLocation(province, cartResponse.orderSummary.suburb));
					setDeliveryLocation(Utils.getPreferredDeliveryLocation());
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
						CommerceItem commerceItem = new Gson().fromJson(String.valueOf(productsArray.getJSONObject(i)), CommerceItem.class);
						String fulfillmentStoreId = Utils.retrieveStoreId(commerceItem.fulfillmentType);
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
		Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.CART_LIST);
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
		if (resultCode == CART_DEFAULT_ERROR_TAPPED || resultCode == DIALOG_REQUEST_CODE) {
			Activity activity = getActivity();
			activity.setResult(CART_DEFAULT_ERROR_TAPPED);
			activity.finish();
			activity.overridePendingTransition(R.anim.slide_down_anim, R.anim.stay);
			return;
		}
		if (requestCode == SSOActivity.SSOActivityResult.LAUNCH.rawValue()) {
			if (SessionUtilities.getInstance().isUserAuthenticated()) {
				if (resultCode == Activity.RESULT_OK) {
					// Checkout completed successfully
                    final ShoppingDeliveryLocation lastDeliveryLocation = Utils.getPreferredDeliveryLocation();
                    if (lastDeliveryLocation != null) {

						// Show loading state
						rlCheckOut.setVisibility(View.GONE);
						pBar.setVisibility(View.VISIBLE);
						if (cartProductAdapter != null) {
							cartProductAdapter.clear();
						}
						Activity activity = getActivity();
						if (activity != null) {
							CartActivity cartActivity = (CartActivity) activity;
							cartActivity.hideEditCart();
						}
						Call<SetDeliveryLocationSuburbResponse> setDeliveryLocationSuburb = OneAppService.INSTANCE.setSuburb(lastDeliveryLocation.suburb.id);
						setDeliveryLocationSuburb.enqueue(new CompletionHandler<>(new IResponseListener<SetDeliveryLocationSuburbResponse>() {
							@Override
							public void onSuccess(SetDeliveryLocationSuburbResponse setDeliveryLocationSuburbResponse) {
								if(setDeliveryLocationSuburbResponse.httpCode == 200) {
									Utils.savePreferredDeliveryLocation(lastDeliveryLocation);
									setDeliveryLocation(lastDeliveryLocation);
									//Utils.sendBus(new CartState(lastDeliveryLocation.suburb.name + ", " + lastDeliveryLocation.province.name));
								}
								loadShoppingCartAndSetDeliveryLocation();
							}

							@Override
							public void onFailure(Throwable error) {
								Activity activity = getActivity();
								if (activity == null || error.getMessage() == null)return;

								activity.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										loadShoppingCartAndSetDeliveryLocation();
									}
								});

							}
						},SetDeliveryLocationSuburbResponse.class));
                    } else {
                    	// Fallback if there is no cached location
                        loadShoppingCartAndSetDeliveryLocation();
                    }
                } else {
					// Checkout was cancelled
                    loadShoppingCartAndSetDeliveryLocation();
                }
			} else {
				getActivity().onBackPressed();
			}
		}

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case PDP_REQUEST_CODE:
				case REQUEST_SUBURB_CHANGE:
                    loadShoppingCartAndSetDeliveryLocation();
					break;
				default:
					break;
			}
		}
	}

	private void loadShoppingCartAndSetDeliveryLocation() {
        loadShoppingCart(false);
        ShoppingDeliveryLocation lastDeliveryLocation = Utils.getPreferredDeliveryLocation();
        if (lastDeliveryLocation != null) {
            setDeliveryLocation(lastDeliveryLocation);
        }
    }

	@Override
	public void onConnectionChanged() {

		if (onRemoveItemFailed) {
			mErrorHandlerView.hideErrorHandler();
			loadShoppingCart(true);
			return;
		}

		if (mRemoveAllItemFailed) {
			removeAllCartItem(null);
			mRemoveAllItemFailed = false;
			return;
		}

		if (changeQuantityWasClicked) {
			if (cartProductAdapter != null) {
				cartProductAdapter.onChangeQuantityLoad(mCommerceItem);
			}
			postChangeQuantity();
			changeQuantityWasClicked = false;
		}
	}

	private void removeItemAPI(CommerceItem mCommerceItem) {
		removeCartItem(mCommerceItem);
	}

	private void queryServiceInventoryCall(ArrayList<CartItemGroup> items) {
		MultiMap<String, CommerceItem> multiMapCommerceItem = MultiMap.create();
		fadeCheckoutButton(true);
		for (CartItemGroup cartItemGroup : items) {
			for (CommerceItem commerceItem : cartItemGroup.getCommerceItems()) {
				multiMapCommerceItem.put(commerceItem.fulfillmentStoreId, commerceItem);
			}
		}

		mapStoreIdWithCommerceItems = multiMapCommerceItem.getEntries();

		for (Map.Entry<String, Collection<CommerceItem>> commerceItemCollectionMap : mapStoreIdWithCommerceItems.entrySet()) {
			Collection<CommerceItem> commerceItemCollectionValue = commerceItemCollectionMap.getValue();
			String fulfilmentStoreId = commerceItemCollectionMap.getKey().replaceAll("[^0-9]", "");
			List<String> skuIds = new ArrayList<>();
			for (CommerceItem commerceItem : commerceItemCollectionValue) {
				skuIds.add(commerceItem.commerceItemInfo.catalogRefId);
			}
			String groupBySkuIds = TextUtils.join("-", skuIds);

			/***
			 * Handles products with  no fulfilmentStoreId
			 * quantity = -2 is required to prevent change quantity api call
			 * triggered when commerceItemInfo.quantity > quantityInStock
			 */
			if (TextUtils.isEmpty(fulfilmentStoreId)) {
				ArrayList<CartItemGroup> cartItems = cartProductAdapter.getCartItems();
				for (CartItemGroup cartItemGroup : cartItems) {
					for (CommerceItem commerceItem : cartItemGroup.commerceItems) {
						if (commerceItem.fulfillmentStoreId.isEmpty()) {
							commerceItem.quantityInStock = 0;
							commerceItem.commerceItemInfo.quantity = -2;
							commerceItem.isStockChecked = true;
							removeItem(commerceItem);
						}
					}
				}
				this.cartItems = cartItems;
			} else {
				initInventoryRequest(fulfilmentStoreId, groupBySkuIds);
			}
		}
	}

	public Call<SkusInventoryForStoreResponse> initInventoryRequest(String storeId, String multiSku) {
		Call<SkusInventoryForStoreResponse> skusInventoryForStoreResponseCall = OneAppService.INSTANCE.getInventorySkuForStore(storeId, multiSku);
		skusInventoryForStoreResponseCall.enqueue(new CompletionHandler<>(new IResponseListener<SkusInventoryForStoreResponse>() {
			@Override
			public void onSuccess(SkusInventoryForStoreResponse skusInventoryForStoreResponse) {
				if (skusInventoryForStoreResponse.httpCode == 200) {
					mSkuInventories.put(skusInventoryForStoreResponse.storeId, skusInventoryForStoreResponse.skuInventory);
					if (mSkuInventories.size() == mapStoreIdWithCommerceItems.size()){
						updateCartListWithAvailableStock(mSkuInventories);
					}
				} else {
					isAllInventoryAPICallSucceed = false;
					if (!errorMessageWasPopUp) {
						Activity activity = getActivity();
						if (skusInventoryForStoreResponse.response == null || activity == null)
							return;
						if (TextUtils.isEmpty(skusInventoryForStoreResponse.response.desc))
							return;
						Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, skusInventoryForStoreResponse.response.desc);
						errorMessageWasPopUp = true;
					}
				}
			}
			@Override
			public void onFailure(Throwable error) {
				Activity activity = getActivity();
				disableQuantitySelector(error, activity);
			}
		},SkusInventoryForStoreResponse.class));
		return skusInventoryForStoreResponseCall;
	}

	private void disableQuantitySelector(Throwable error, Activity activity) {
		if (activity == null || !isAdded()) return;
		CartActivity cartActivity = (CartActivity) activity;
		activity.runOnUiThread(() -> {
			if (error instanceof SocketTimeoutException){
				if (cartProductAdapter != null && btnCheckOut != null) {
					ArrayList<CartItemGroup> cartItems = cartProductAdapter.getCartItems();
					for (CartItemGroup cartItemGroup : cartItems) {
						for (CommerceItem commerceItem : cartItemGroup.commerceItems) {
							if (!commerceItem.isStockChecked) {
								commerceItem.quantityInStock = -1;
								commerceItem.isStockChecked = true;
							}
						}
					}
					cartProductAdapter.updateStockAvailability(cartItems);
				}
			}else if (error instanceof ConnectException || error instanceof UnknownHostException){
				if (cartProductAdapter != null && btnCheckOut != null) {
					ArrayList<CartItemGroup> cartItems = cartProductAdapter.getCartItems();
					for (CartItemGroup cartItemGroup : cartItems) {
						for (CommerceItem commerceItem : cartItemGroup.commerceItems) {
							if (!commerceItem.isStockChecked) {
								commerceItem.quantityInStock = -1 ;
							}
						}
					}
					cartProductAdapter.updateStockAvailability(cartItems);
				}
			}
			cartActivity.enableEditCart();
			btnCheckOut.setEnabled(false);
			rlCheckOut.setEnabled(false);
		});
	}

	private void updateCartListWithAvailableStock(HashMap<String, List<SkuInventory>> mSkuInventories) {
		isAllInventoryAPICallSucceed = true;
		for (CartItemGroup cartItemGroup : this.cartItems) {
			for (CommerceItem commerceItem : cartItemGroup.commerceItems) {
				String fulfilmentStoreId = commerceItem.fulfillmentStoreId;
				String skuId = commerceItem.commerceItemInfo.getCatalogRefId();
				if (mSkuInventories.containsKey(fulfilmentStoreId)) {
					List<SkuInventory> skuInventories = mSkuInventories.get(fulfilmentStoreId);
					if (skuInventories != null) {
						for (SkuInventory skuInventory : skuInventories) {
							if (skuInventory.sku.equals(skuId)) {
								commerceItem.quantityInStock = skuInventory.quantity;
								commerceItem.isStockChecked = true;
							}
						}
					}
				}
			}
		}

		updateItemQuantityToMatchStock();

		if (cartProductAdapter != null)
			cartProductAdapter.updateStockAvailability(this.cartItems);
	}

	// If CommerceItem quantity in cart is more then inStock Update quantity to match stock
	private void updateItemQuantityToMatchStock() {
		boolean isAnyItemNeedsQuantityUpdate = false;
		ArrayList<CommerceItem> itemsTobeRemovedFromCart = new ArrayList<>();
		for (CartItemGroup cartItemGroup : cartItems) {
			for (CommerceItem commerceItem : cartItemGroup.commerceItems) {
				if (commerceItem.quantityInStock == 0) {
					itemsTobeRemovedFromCart.add(commerceItem);
				} else if (commerceItem.commerceItemInfo.getQuantity() > commerceItem.quantityInStock) {
					isAnyItemNeedsQuantityUpdate = true;
					mCommerceItem = commerceItem;
					mChangeQuantity.setCommerceId(commerceItem.commerceItemInfo.getCommerceId());
					mQuantity = commerceItem.quantityInStock;
					mCommerceItem.setQuantityUploading(true);
					postChangeQuantity();
				}
			}
		}
		if (!btnCheckOut.isEnabled() && isAllInventoryAPICallSucceed && !isAnyItemNeedsQuantityUpdate)
			fadeCheckoutButton(false);

		if (itemsTobeRemovedFromCart.size() > 0) {
			if (getActivity() != null && isAdded()) {
				RemoveProductsFromCartDialogFragment fromCartDialogFragment = RemoveProductsFromCartDialogFragment.Companion.newInstance(itemsTobeRemovedFromCart);
				fromCartDialogFragment.show(this.getChildFragmentManager(), this.getClass().getSimpleName());
			}
		}
	}

	/***
	 * @method fadeCheckoutButton() is called before inventory api get executed to
	 * disable the checkout button
	 * It is called again after the last inventory call if
	 * @params mShouldDisplayCheckout is true only to avoid blinking animation on
	 *                               checkout button
	 */
	private void fadeCheckoutButton(boolean value) {
		enableEditCart(value);
		Utils.fadeInFadeOutAnimation(btnCheckOut, value);
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
			if (shoppingListRequest != null) {
				for (ShoppingList shoppingList : shoppingListRequest) {
					if (shoppingList.shoppingListRowWasSelected) {
						intent.putExtra("listId", shoppingList.listId);
						intent.putExtra("listName", shoppingList.listName);
					}
				}
			}
		}
		activity.setResult(MOVE_TO_LIST_ON_TOAST_VIEW_CLICKED, intent);
		activity.finish();
		activity.overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
	}

	public void setDeliveryLocation(ShoppingDeliveryLocation shoppingDeliveryLocation) {
		if (getActivity() != null) {
			deliverLocationRightArrow.setVisibility(View.GONE);
			editLocation.setVisibility(View.VISIBLE);
			KotlinUtils.Companion.setDeliveryAddressView(getActivity(), shoppingDeliveryLocation, tvDeliveringToText, tvDeliveryLocation, deliverLocationIcon);
		}
	}

	private void enableEditCart(boolean enable) {
		Activity activity = getActivity();
		if (activity == null) return;
		CartActivity cartActivity = (CartActivity) activity;
		cartActivity.enableEditCart(enable);
	}

	public void showFeatureWalkthrough(){
		if (!AppInstanceObject.get().featureWalkThrough.showTutorials || AppInstanceObject.get().featureWalkThrough.deliveryLocation)
			return;
		Crashlytics.setString(getString(R.string.crashlytics_materialshowcase_key),this.getClass().getSimpleName());
		CartActivity.walkThroughPromtView = new WMaterialShowcaseView.Builder(getActivity(), WMaterialShowcaseView.Feature.DELIVERY_LOCATION)
				.setTarget(imgDeliveryLocation)
				.setTitle(R.string.your_delivery_location)
				.setDescription(R.string.walkthrough_delivery_location_desc)
				.setActionText(R.string.tips_edit_delivery_location)
				.setImage(R.drawable.tips_tricks_ic_stores)
				.setAction(this)
				.setShapePadding(24)
				.setArrowPosition(WMaterialShowcaseView.Arrow.TOP_LEFT)
				.setMaskColour(getResources().getColor(R.color.semi_transparent_black)).build();
		CartActivity.walkThroughPromtView.show(getActivity());
	}

	@Override
	public void onWalkthroughActionButtonClick(WMaterialShowcaseView.Feature feature) {
		if (feature == WMaterialShowcaseView.Feature.DELIVERY_LOCATION)
			this.onClick(rlLocationSelectedLayout);
	}

	@Override
	public void onPromptDismiss() {

	}

	public ArrayList<CartItemGroup> getCartItems() {
		return cartItems;
	}


	private void displayUpSellMessage(Data data) {
		if (data == null || data.globalMessages == null || mRemoveAllItemFromCartTapped) return;
		GlobalMessages globalMessages = data.globalMessages;

		if (globalMessages.getQualifierMessages() == null) return;

		String qualifierMessage = globalMessages.getQualifierMessages().get(0);

		upSellMessageTextView.setText(qualifierMessage);
		upSellMessageTextView.setVisibility(TextUtils.isEmpty(qualifierMessage) ? View.GONE : View.VISIBLE);
	}

	@Override
	public void onOutOfStockProductsRemoved() {
		loadShoppingCart(false);
	}

	private void showMaxItemView() {
		Utils.showGeneralErrorDialog(requireActivity().getSupportFragmentManager(), getString(R.string.click_and_collect_max_quantity_info, String.valueOf(WoolworthsApplication.getClickAndCollect().getMaxNumberOfItemsAllowed())));
	}

}
