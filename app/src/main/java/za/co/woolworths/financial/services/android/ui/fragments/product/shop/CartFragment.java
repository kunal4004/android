package za.co.woolworths.financial.services.android.ui.fragments.product.shop;

import static android.app.Activity.RESULT_OK;
import static za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.SAVED_ADDRESS_KEY;
import static za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressManagementBaseFragment.GEO_SLOT_SELECTION;
import static za.co.woolworths.financial.services.android.models.service.event.CartState.CHANGE_QUANTITY;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.CANCEL_DIALOG_TAPPED;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.CLOSE_PDP_FROM_ADD_TO_LIST;
import static za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow.CART_DEFAULT_ERROR_TAPPED;
import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_PRODUCT;
import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.PDP_REQUEST_CODE;
import static za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment.REQUEST_CHECKOUT_ON_DESTROY;
import static za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment.RESULT_RELOAD_CART;
import static za.co.woolworths.financial.services.android.ui.views.actionsheet.ActionSheetDialogFragment.DIALOG_REQUEST_CODE;
import static za.co.woolworths.financial.services.android.util.ScreenManager.CART_LAUNCH_VALUE;
import static za.co.woolworths.financial.services.android.util.ScreenManager.SHOPPING_LIST_DETAIL_ACTIVITY_REQUEST_CODE;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.awfs.coordination.R;
import com.google.firebase.analytics.FirebaseAnalytics;
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
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse;
import za.co.woolworths.financial.services.android.checkout.view.CheckoutActivity;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.IResponseListener;
import za.co.woolworths.financial.services.android.geolocation.GeoUtils;
import za.co.woolworths.financial.services.android.models.AppConfigSingleton;
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
import za.co.woolworths.financial.services.android.models.dto.LiquorCompliance;
import za.co.woolworths.financial.services.android.models.dto.OrderSummary;
import za.co.woolworths.financial.services.android.models.dto.ProductDetails;
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation;
import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.models.dto.SkuInventory;
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.dto.item_limits.ProductCountMap;
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.CouponClaimCode;
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.VoucherDetails;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.models.service.event.CartState;
import za.co.woolworths.financial.services.android.models.service.event.ProductState;
import za.co.woolworths.financial.services.android.ui.activities.CartCheckoutActivity;
import za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator;
import za.co.woolworths.financial.services.android.ui.activities.online_voucher_redemption.AvailableVouchersToRedeemInCart;
import za.co.woolworths.financial.services.android.ui.adapters.CartProductAdapter;
import za.co.woolworths.financial.services.android.ui.fragments.cart.GiftWithPurchaseDialogDetailFragment;
import za.co.woolworths.financial.services.android.ui.views.ToastFactory;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.AppConstant;
import za.co.woolworths.financial.services.android.util.CartUtils;
import za.co.woolworths.financial.services.android.util.Constant;
import za.co.woolworths.financial.services.android.util.CurrencyFormatter;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FirebaseManager;
import za.co.woolworths.financial.services.android.util.KotlinUtils;
import za.co.woolworths.financial.services.android.util.MultiMap;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.ProductType;
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.ToastUtils;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.wenum.Delivery;

public class CartFragment extends Fragment implements CartProductAdapter.OnItemClick, View.OnClickListener, NetworkChangeListener, ToastUtils.ToastInterface, WMaterialShowcaseView.IWalkthroughActionListener, RemoveProductsFromCartDialogFragment.IRemoveProductsFromCartDialog {

    private final String TAG = this.getClass().getSimpleName();
    private final String GIFT_ITEM = "GIFT";
    private final String GENERAL_ITEM = "GENERAL";
    private final String TAG_AVAILABLE_VOUCHERS_TOAST = "AVAILABLE_VOUCHERS";
    private final String TAG_ADDED_TO_LIST_TOAST = "ADDED_TO_LIST";

    private static final int CART_BACK_PRESSED_CODE = 9;
    private static final int PDP_LOCATION_CHANGED_BACK_PRESSED_CODE = 18;
    public static final int REQUEST_PAYMENT_STATUS = 4775;
    private static final int REQUEST_SUBURB_CHANGE = 143;
    public static final int REDEEM_VOUCHERS_REQUEST_CODE = 1979;
    public static final int APPLY_PROMO_CODE_REQUEST_CODE = 1989;
    public static final int MOVE_TO_LIST_ON_TOAST_VIEW_CLICKED = 1020;

    private int mNumberOfListSelected;
    private int localCartCount = 0;

    private static boolean isMaterialPopUpClosed = true;
    private boolean changeQuantityWasClicked = false;
    private boolean errorMessageWasPopUp = false;
    private boolean toastButtonWasClicked = false;
    private boolean onRemoveItemFailed = false;
    private boolean mRemoveAllItemFailed = false;
    private boolean mRemoveAllItemFromCartTapped = false;
    private boolean isAllInventoryAPICallSucceed;

    private List<ChangeQuantity> mChangeQuantityList;
    private HashMap<String, List<SkuInventory>> mSkuInventories;
    private Map<String, Collection<CommerceItem>> mapStoreIdWithCommerceItems;
    private ArrayList<CartItemGroup> cartItems;

    public ConstraintLayout itemLimitsBanner;
    private RelativeLayout orderTotalLayout, rlLocationSelectedLayout, parentLayout, relEmptyStateHandler;
    public TextView itemLimitsMessage, itemLimitsCounter, upSellMessageTextView, orderTotal;
    private ImageView deliverLocationIcon, deliverLocationRightArrow;
    private NestedScrollView nestedScrollView;
    private RecyclerView rvCartList;
    private WButton btnCheckOut;
    private ProgressBar pBar, pbRemoveAllItem;
    private WTextView tvDeliveryLocation, tvDeliveringToText, editLocation, btnEditCart, btnClearCart;
    private LinearLayout rlCheckOut;
    private ErrorHandlerView mErrorHandlerView;

    private CartProductAdapter cartProductAdapter;
    private OrderSummary orderSummary;
    private BroadcastReceiver mConnectionBroadcast;
    private ToastUtils mToastUtils;
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private ChangeQuantity mChangeQuantity;
    private CommerceItem mCommerceItem;
    private VoucherDetails voucherDetails;
    public ProductCountMap productCountMap;
    private LiquorCompliance liquorCompliance;

    public CartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();
        initViews();
        hideEditCart();
        localCartCount = QueryBadgeCounter.getInstance().getCartItemCount();

        mChangeQuantityList = new ArrayList<>(0);
        mChangeQuantity = new ChangeQuantity();
        mConnectionBroadcast = Utils.connectionBroadCast(getActivity(), this);
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
                                    mChangeQuantity.quantity = cartState.getQuantity();
                                    queryServiceChangeQuantity();
                                }
                            } else if (object instanceof ProductState) {
                                ProductState productState = (ProductState) object;
                                switch (productState.getState()) {
                                    case CANCEL_DIALOG_TAPPED: // reset change quantity state value
                                        if (cartProductAdapter != null)
                                            cartProductAdapter.onPopUpCancel(CANCEL_DIALOG_TAPPED);
                                        break;
                                    case CLOSE_PDP_FROM_ADD_TO_LIST:
                                        if (getActivity() != null) {
                                            mToastUtils.setActivity(getActivity());
                                            mToastUtils.setCurrentState(TAG_ADDED_TO_LIST_TOAST);
                                            String shoppingList = getString(R.string.shopping_list);
                                            mNumberOfListSelected = productState.getCount();
                                            // shopping list vs shopping lists
                                            mToastUtils.setCartText((mNumberOfListSelected > 1) ? shoppingList + "s" : shoppingList);
                                            mToastUtils.setPixel(btnCheckOut.getHeight() * 2);
                                            mToastUtils.setView(btnCheckOut);
                                            mToastUtils.setMessage(R.string.added_to);
                                            mToastUtils.setViewState(true);
                                            mToastUtils.build();
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    }
                }));

        initializeLoggedInUserCartUI();
    }

    private void initializeLoggedInUserCartUI() {
        if (!SessionUtilities.getInstance().isUserAuthenticated()) {
            return;
        }

        //Things to do after login is successful
        setEmptyCartUIUserName();
        //One time biometricsWalkthrough
        if (isVisible()) {
            ScreenManager.presentBiometricWalkthrough(getActivity());
        }
        loadShoppingCart(false);
    }

    private void initViews() {
        if (getView() == null) {
            return;
        }
        View view = getView();

        WButton mBtnRetry = view.findViewById(R.id.btnRetry);
        mBtnRetry.setOnClickListener(this);
        rvCartList = view.findViewById(R.id.cartList);
        btnCheckOut = view.findViewById(R.id.btnCheckOut);
        rlCheckOut = view.findViewById(R.id.rlCheckOut);
        parentLayout = view.findViewById(R.id.parentLayout);
        pBar = view.findViewById(R.id.loadingBar);
        relEmptyStateHandler = view.findViewById(R.id.relEmptyStateHandler);
        rlLocationSelectedLayout = view.findViewById(R.id.locationSelectedLayout);
        upSellMessageTextView = view.findViewById(R.id.upSellMessageTextView);
        tvDeliveryLocation = view.findViewById(R.id.tvDeliveryLocation);
        tvDeliveringToText = view.findViewById(R.id.tvDeliveringTo);
        deliverLocationIcon = view.findViewById(R.id.deliverLocationIcon);
        editLocation = view.findViewById(R.id.editLocation);
        deliverLocationRightArrow = view.findViewById(R.id.iconCaretRight);
        orderTotal = view.findViewById(R.id.orderTotal);
        orderTotalLayout = view.findViewById(R.id.orderTotalLayout);
        nestedScrollView = view.findViewById(R.id.nestedScrollView);
        itemLimitsBanner = view.findViewById(R.id.itemLimitsBanner);
        itemLimitsMessage = view.findViewById(R.id.itemLimitsMessage);
        itemLimitsCounter = view.findViewById(R.id.itemLimitsCounter);

        RelativeLayout rlNoConnectionLayout = view.findViewById(R.id.no_connection_layout);
        mErrorHandlerView = new ErrorHandlerView(getActivity(), rlNoConnectionLayout);
        mErrorHandlerView.setMargin(rlNoConnectionLayout, 0, 0, 0, 0);

        btnCheckOut.setOnClickListener(this);
        orderTotalLayout.setOnClickListener(this);
        rlLocationSelectedLayout.setOnClickListener(this);

        //Empty cart UI
        WTextView txtEmptyStateTitle = view.findViewById(R.id.txtEmptyStateTitle);
        WTextView txtEmptyStateDesc = view.findViewById(R.id.txtEmptyStateDesc);
        WButton btnGoToProduct = view.findViewById(R.id.btnGoToProduct);
        if (SessionUtilities.getInstance().isUserAuthenticated()) {
            String firstName = SessionUtilities.getInstance().getJwt().name.get(0);
            txtEmptyStateTitle.setText("HI " + firstName + ",");
        }
        txtEmptyStateDesc.setText(getString(R.string.empty_cart_desc));
        btnGoToProduct.setVisibility(View.VISIBLE);
        btnGoToProduct.setText(getString(R.string.start_shopping));
        btnGoToProduct.setOnClickListener(this);
    }

    private void initializeBottomTab() {
        if (getActivity() instanceof BottomNavigationActivity) {
            ((BottomNavigationActivity) getActivity()).showBottomNavigationMenu();
            ((BottomNavigationActivity) getActivity()).hideToolbar();
            ((BottomNavigationActivity) getActivity()).setToolbarTitle("");
        }
    }

    private void setupToolbar() {
        Utils.updateStatusBarBackground(getActivity());
        if (getView() == null) {
            return;
        }
        View view = getView();
        Toolbar toolbar = view.findViewById(R.id.cartToolbar);
        btnEditCart = toolbar.findViewById(R.id.btnEditCart);
        btnEditCart.setText(R.string.edit);
        pbRemoveAllItem = toolbar.findViewById(R.id.pbRemoveAllItem);
        btnClearCart = toolbar.findViewById(R.id.btnClearCart);

        btnClearCart.setVisibility(View.GONE);
        pbRemoveAllItem.setVisibility(View.GONE);
        btnEditCart.setOnClickListener(this);
        btnClearCart.setOnClickListener(this);

        if (getActivity() instanceof BottomNavigationActivity) {
            ((BottomNavigationActivity) getActivity()).hideToolbar();
        }
    }

    /****
     * mChangeQuantityList save all ChangeQuantityRequest after quantity selection
     * Top ChangeQuantity item in list is selected
     * Extract commerceId of the selected ChangeQuantity object
     * Perform changeQuantity call
     * Remove top changeQuantity object from list
     */

    private void queryServiceChangeQuantity() {
        mChangeQuantityList.add(mChangeQuantity);
        changeQuantityAPI(mChangeQuantityList.get(0));
        mChangeQuantityList.remove(0);
    }

    private void setEmptyCartUIUserName() {
        if (!SessionUtilities.getInstance().isUserAuthenticated() || getView() == null) {
            return;
        }
        View view = getView();
        String firstName = SessionUtilities.getInstance().getJwt().name.get(0);
        WTextView txtEmptyStateTitle = view.findViewById(R.id.txtEmptyStateTitle);
        txtEmptyStateTitle.setText("HI " + firstName + ",");
    }

    public void onRemoveItem(boolean visibility) {
        pbRemoveAllItem.setVisibility(visibility ? View.VISIBLE : View.GONE);
        btnClearCart.setVisibility(visibility ? View.GONE : View.VISIBLE);
        btnEditCart.setEnabled(visibility ? false : true);
    }

    public void onRemoveSuccess() {
        pbRemoveAllItem.setVisibility(View.GONE);
        btnClearCart.setVisibility(View.GONE);
    }

    public void resetToolBarIcons() {
        hideEditCart();
        btnClearCart.setVisibility(View.GONE);
    }

    public void showEditCart() {
        btnEditCart.setAlpha(1.0f);
        btnEditCart.setVisibility(View.VISIBLE);
        btnEditCart.setEnabled(true);
    }

    public void hideEditCart() {
        btnEditCart.setAlpha(0.0f);
        btnEditCart.setVisibility(View.GONE);
        btnEditCart.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnEditCart:
                toggleCartMode();
                // prevent remove all item progressbar visible
                dismissProgress();
                break;
            case R.id.btnClearCart:
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYCARTREMOVEALL, getActivity());
                removeAllCartItem(null);
                break;
            case R.id.locationSelectedLayout:
                locationSelectionClicked();
                break;
            case R.id.btnGoToProduct:
                Activity activity = getActivity();
                if (activity instanceof BottomNavigator) {
                    BottomNavigator navigator = (BottomNavigator) activity;
                    navigator.navigateToTabIndex(INDEX_PRODUCT, null);
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
                    Delivery deliveryType = Delivery.Companion.getType(Utils.getPreferredDeliveryLocation().fulfillmentDetails.getDeliveryType());
                    if (deliveryType == Delivery.CNC && productCountMap != null && productCountMap.getQuantityLimit() != null && !productCountMap.getQuantityLimit().getAllowsCheckout()) {
                        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.CART_CLCK_CLLCT_CNFRM_LMT, checkOutActivity);
                        showMaxItemView();
                        return;
                    }
                    // Go to Web checkout journey if...
                    if (AppConfigSingleton.INSTANCE.getNativeCheckout() != null
                            && !AppConfigSingleton.INSTANCE.getNativeCheckout().isNativeCheckoutEnabled()) {
                        Intent openCheckOutActivity = new Intent(getContext(), CartCheckoutActivity.class);
                        getActivity().startActivityForResult(openCheckOutActivity, CheckOutFragment.REQUEST_CART_REFRESH_ON_DESTROY);
                        checkOutActivity.overridePendingTransition(0, 0);
                    }
                    // else Go to native checkout journey
                    else {
                        if (pBar.getVisibility() == View.VISIBLE) {
                            return;
                        }
                        // Get list of saved address and navigate to proper Checkout page.
                        callSavedAddress();
                    }
                }
                break;
            case R.id.orderTotalLayout:
                nestedScrollView.post(() -> nestedScrollView.fullScroll(View.FOCUS_DOWN));
                break;
            default:
                break;
        }
    }

    public void toggleCartMode() {
        boolean isEditMode = toggleEditMode();
        btnEditCart.setText(isEditMode ? R.string.done : R.string.edit);
        btnClearCart.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        deliveryLocationEnabled(!isEditMode);
    }

    private void dismissProgress() {
        pbRemoveAllItem.setVisibility(View.GONE);
    }

    private void callSavedAddress() {

        pBar.setVisibility(View.VISIBLE);
        Call<SavedAddressResponse> savedAddressCall = OneAppService.INSTANCE.getSavedAddresses();
        savedAddressCall.enqueue(new CompletionHandler<>(new IResponseListener<SavedAddressResponse>() {
            @Override
            public void onSuccess(@org.jetbrains.annotations.Nullable SavedAddressResponse response) {
                switch (response.getHttpCode()) {
                    case AppConstant.HTTP_OK:
                        pBar.setVisibility(View.GONE);
                        navigateToCheckout(response);
                        break;
                    default:
                        pBar.setVisibility(View.GONE);
                        if (response.getResponse() != null) {
                            showErrorDialog(ErrorHandlerActivity.COMMON_WITH_BACK_BUTTON, response.getResponse().getMessage());
                        }
                        break;
                }
            }

            @Override
            public void onFailure(@org.jetbrains.annotations.Nullable Throwable error) {
                showErrorDialog(ErrorHandlerActivity.COMMON_WITH_BACK_BUTTON, error.getMessage());
            }
        }, SavedAddressResponse.class));
    }

    private void showErrorDialog(int errorType, String errorMessage) {
        if (getActivity() != null) {
            Activity activity = getActivity();
            Intent intent = new Intent(activity, ErrorHandlerActivity.class);
            intent.putExtra(ErrorHandlerActivity.ERROR_TYPE, errorType);
            intent.putExtra(ErrorHandlerActivity.ERROR_MESSAGE, errorMessage);
            activity.startActivityForResult(intent, ErrorHandlerActivity.RESULT_RETRY);
        }
    }

    private void navigateToCheckout(SavedAddressResponse response) {
        Activity activity = getActivity();
        if (KotlinUtils.Companion
                .getPreferredDeliveryType().equals(Delivery.STANDARD)
                &&  !TextUtils.isEmpty(response.getDefaultAddressNickname())
        )  {
           //   - CNAV : Checkout  activity

            //firebase event begin_checkout
            FirebaseAnalytics mFirebaseAnalytics = FirebaseManager.Companion.getInstance().getAnalytics();
            Bundle beginCheckoutParams = new Bundle();
            beginCheckoutParams.putString(FirebaseAnalytics.Param.CURRENCY, FirebaseManagerAnalyticsProperties.PropertyValues.CURRENCY_VALUE);
            beginCheckoutParams.putString(FirebaseAnalytics.Param.VALUE, String.valueOf(orderSummary.total));
            mFirebaseAnalytics.logEvent(FirebaseManagerAnalyticsProperties.CART_BEGIN_CHECKOUT, beginCheckoutParams);

            Intent checkoutActivityIntent = new Intent(getActivity(), CheckoutActivity.class);
            checkoutActivityIntent.putExtra(SAVED_ADDRESS_KEY, response);
            checkoutActivityIntent.putExtra(GEO_SLOT_SELECTION, true);
            if(liquorCompliance!=null&&liquorCompliance.isLiquorOrder()&&AppConfigSingleton.INSTANCE.getLiquor().getNoLiquorImgUrl()!=null&&!AppConfigSingleton.INSTANCE.getLiquor().getNoLiquorImgUrl().isEmpty()){
                checkoutActivityIntent.putExtra(Constant.LIQUOR_ORDER, liquorCompliance.isLiquorOrder());
                checkoutActivityIntent.putExtra(Constant.NO_LIQUOR_IMAGE_URL, AppConfigSingleton.INSTANCE.getLiquor().getNoLiquorImgUrl());
            }
            activity.startActivityForResult(checkoutActivityIntent,
                    REQUEST_PAYMENT_STATUS);
            activity.overridePendingTransition(R.anim.slide_from_right,
                    R.anim.slide_out_to_left);
        } else {
//            - GNAV
//            CNC or No Address or no default address*/
            String placeId = "";
            if (GeoUtils.Companion.getDelivertyType() == Delivery.CNC) {
                placeId = GeoUtils.Companion.getPlaceId();
            } else {
                placeId = GeoUtils.Companion.getSelectedPlaceId(response);
            }

            KotlinUtils.Companion.presentEditDeliveryGeoLocationActivity(
                    requireActivity(),
                    CartFragment.REQUEST_PAYMENT_STATUS,
                    GeoUtils.Companion.getDelivertyType(),
                    placeId,
                    true,
                    false,
                    response,
                    null,
                    "");
        }
    }

    @Override
    public void onItemDeleteClickInEditMode(CommerceItem commerceItem) {
        // TODO: Make API call to remove item + show loading before removing from list
        removeItemAPI(commerceItem);
    }

    @Override
    public void onItemDeleteClick(CommerceItem commerceId) {
        enableItemDelete(true);
        removeItemAPI(commerceId);
    }

    @Override
    public void onChangeQuantity(CommerceItem commerceId) {
        mCommerceItem = commerceId;
        mChangeQuantity.commerceId = commerceId.commerceItemInfo.getCommerceId();
        if (WoolworthsApplication.getInstance() != null) {
            WGlobalState wGlobalState = WoolworthsApplication.getInstance().getWGlobalState();
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
        if (!(getActivity() instanceof BottomNavigationActivity)) {
            return;
        }

        ProductDetails productDetails = new ProductDetails();
        CommerceItemInfo commerceItemInfo = commerceItem.commerceItemInfo;
        productDetails.externalImageRefV2 = commerceItemInfo.externalImageRefV2;
        productDetails.productName = commerceItemInfo.productDisplayName;
        productDetails.fromPrice = (float) commerceItem.priceInfo.getAmount();
        productDetails.productId = commerceItemInfo.productId;
        productDetails.sku = commerceItemInfo.catalogRefId;
        Gson gson = new Gson();
        String strProductList = gson.toJson(productDetails);
        if (getActivity() instanceof BottomNavigationActivity) {
            // Move to shop tab first.
            ((BottomNavigationActivity) getActivity()).getBottomNavigationById().setCurrentItem(INDEX_PRODUCT);
            ScreenManager.openProductDetailFragment(getActivity(), "", strProductList);
        }
    }

    @Override
    public void onGiftItemClicked(CommerceItem commerceItem) {
        Activity activity = getActivity();
        if (activity == null) return;
        GiftWithPurchaseDialogDetailFragment giftWithPurchaseDialogDetailFragment = new GiftWithPurchaseDialogDetailFragment();
        giftWithPurchaseDialogDetailFragment.show(((AppCompatActivity) activity).getSupportFragmentManager(), GiftWithPurchaseDialogDetailFragment.class.getSimpleName());
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
                    cm.setDeletePressed(false);
                }
            }
        }
        if (cartProductAdapter != null)
            cartProductAdapter.notifyDataSetChanged();
    }

    private void locationSelectionClicked() {
        Activity activity = getActivity();

        if (activity != null) {
            String placeId = "";
            if (Utils.getPreferredDeliveryLocation() != null) {
                if (Utils.getPreferredDeliveryLocation().fulfillmentDetails.getAddress() != null) {
                    placeId = Utils.getPreferredDeliveryLocation().fulfillmentDetails.getAddress().getPlaceId();
                }
            }
            KotlinUtils.Companion.presentEditDeliveryGeoLocationActivity(
                    activity, REQUEST_SUBURB_CHANGE,
                    KotlinUtils.Companion.getPreferredDeliveryType(),
                    placeId,
                    false,
                    false,
                    null,
                    null,
                    ""
            );
        }
    }

    public void bindCartData(CartResponse cartResponse) {
        parentLayout.setVisibility(View.VISIBLE);
        mSkuInventories = new HashMap<>();
        if (cartResponse.cartItems.size() > 0) {
            relEmptyStateHandler.setVisibility(View.GONE);
            rvCartList.setVisibility(View.VISIBLE);
            rlCheckOut.setVisibility(View.VISIBLE);
            showEditCart();

            cartItems = cartResponse.cartItems;

            orderSummary = cartResponse.orderSummary;
            voucherDetails = cartResponse.voucherDetails;
            productCountMap = cartResponse.productCountMap;
            liquorCompliance=new LiquorCompliance(cartResponse.liquorOrder,cartResponse.noLiquorImageUrl != null ? cartResponse.noLiquorImageUrl : "");
            cartProductAdapter = new CartProductAdapter(cartItems, this, orderSummary, getActivity(), voucherDetails,liquorCompliance);
            queryServiceInventoryCall(cartResponse.cartItems);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            rvCartList.setLayoutManager(mLayoutManager);
            rvCartList.setAdapter(cartProductAdapter);
            updateOrderTotal();
            isMaterialPopUpClosed = false;
            showRedeemVoucherFeatureWalkthrough();
        } else {
            updateCartSummary(0);
            rvCartList.setVisibility(View.GONE);
            rlCheckOut.setVisibility(View.GONE);
            onRemoveSuccess();
            relEmptyStateHandler.setVisibility(View.VISIBLE);
            Utils.deliveryLocationEnabled(getActivity(), true, rlLocationSelectedLayout);
            resetToolBarIcons();
            isMaterialPopUpClosed = true;
            showEditDeliveryLocationFeatureWalkthrough();
        }
        setItemLimitsBanner();
    }

    public void updateCart(CartResponse cartResponse, CommerceItem commerceItemToRemove) {
        this.orderSummary = cartResponse.orderSummary;
        this.voucherDetails = cartResponse.voucherDetails;
        this.productCountMap = cartResponse.productCountMap;
        this.liquorCompliance = new LiquorCompliance(cartResponse.liquorOrder, cartResponse.noLiquorImageUrl != null ? cartResponse.noLiquorImageUrl : "");
        setItemLimitsBanner();
        if (cartResponse.cartItems.size() > 0 && cartProductAdapter != null) {
            ArrayList<CartItemGroup> emptyCartItemGroups = new ArrayList<>(0);
            for (CartItemGroup cartItemGroup : cartItems) {

                if (commerceItemToRemove != null) {
                    for (CommerceItem commerceItem : cartItemGroup.commerceItems) {
                        if (commerceItem.commerceItemInfo.commerceId.equalsIgnoreCase(commerceItemToRemove.commerceItemInfo.commerceId)) {
                            cartItemGroup.commerceItems.remove(commerceItem);
                            break;
                        }
                    }
                }

                for (CommerceItem commerceItem : cartItemGroup.commerceItems) {
                    CommerceItem updatedCommerceItem = CartUtils.Companion.filterCommerceItemFromCartResponse(cartResponse, commerceItem.commerceItemInfo.commerceId);
                    if (updatedCommerceItem != null) {
                        commerceItem.priceInfo = updatedCommerceItem.priceInfo;
                    }
                }

                if (cartItemGroup.type.equalsIgnoreCase("GIFT")) {
                    boolean isGiftsThere = false;
                    for (CartItemGroup UpdatedCartItemGroup : cartResponse.cartItems) {
                        if (UpdatedCartItemGroup.type.equalsIgnoreCase("GIFT")) {
                            cartItemGroup.commerceItems = UpdatedCartItemGroup.commerceItems;
                            isGiftsThere = true;
                        }
                    }
                    if (!isGiftsThere)
                        cartItemGroup.commerceItems.clear();
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

            cartProductAdapter.notifyAdapter(cartItems, orderSummary, voucherDetails, liquorCompliance);
        } else {
            cartProductAdapter.clear();
            resetToolBarIcons();
            rlCheckOut.setVisibility(View.GONE);
            rvCartList.setVisibility(View.GONE);
            relEmptyStateHandler.setVisibility(View.VISIBLE);
            Utils.deliveryLocationEnabled(getActivity(), true, rlLocationSelectedLayout);
        }

        if (productCountMap != null) {
            updateCartSummary(productCountMap.getTotalProductCount() > 0 ? productCountMap.getTotalProductCount() : 0);
        }
    }

    private BottomNavigationActivity getBottomNavigationActivity() {
        Activity activity = getActivity();
        if (!(activity instanceof BottomNavigationActivity)) return null;
        return (BottomNavigationActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mDisposables != null
                && !mDisposables.isDisposed()) {
            mDisposables.dispose();
        }

        if (getBottomNavigationActivity() != null && getBottomNavigationActivity().walkThroughPromtView != null) {
            getBottomNavigationActivity().walkThroughPromtView.removeFromWindow();
        }
    }

    public void changeQuantity(CartResponse cartResponse, ChangeQuantity changeQuantity) {
        if (cartResponse.cartItems.size() > 0 && cartProductAdapter != null) {
            CommerceItem updatedCommerceItem = getUpdatedCommerceItem(cartResponse.cartItems, changeQuantity.commerceId);
            //update list instead of using the new list to handle inventory data
            for (CartItemGroup cartItemGroupUpdated : cartResponse.cartItems) {
                boolean isGroup = false;
                for (CartItemGroup cartItemGroup : cartItems) {
                    if (cartItemGroupUpdated.type.equalsIgnoreCase(cartItemGroup.type)) {
                        isGroup = true;
                        break;
                    }
                }
                if (!isGroup)
                    cartItems.add(cartItemGroupUpdated);
            }

            if (updatedCommerceItem != null) {
                ArrayList<CartItemGroup> emptyCartItemGroups = new ArrayList<>();
                for (CartItemGroup cartItemGroup : cartItems) {
                    for (CommerceItem commerceItem : cartItemGroup.commerceItems) {
                        if (commerceItem.commerceItemInfo.commerceId.equalsIgnoreCase(updatedCommerceItem.commerceItemInfo.commerceId)) {
                            commerceItem.commerceItemInfo = updatedCommerceItem.commerceItemInfo;
                            commerceItem.priceInfo = updatedCommerceItem.priceInfo;
                            commerceItem.setQuantityUploading(false);
                        }
                    }

                    if (cartItemGroup.type.equalsIgnoreCase("GIFT")) {
                        boolean isGiftsThere = false;
                        for (CartItemGroup UpdatedCartItemGroup : cartResponse.cartItems) {
                            if (UpdatedCartItemGroup.type.equalsIgnoreCase("GIFT")) {
                                cartItemGroup.commerceItems = UpdatedCartItemGroup.commerceItems;
                                isGiftsThere = true;
                            }
                        }
                        if (!isGiftsThere)
                            cartItemGroup.commerceItems.clear();
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

                orderSummary = cartResponse.orderSummary;
                voucherDetails = cartResponse.voucherDetails;
                productCountMap = cartResponse.productCountMap;
                liquorCompliance = new LiquorCompliance(cartResponse.liquorOrder, cartResponse.noLiquorImageUrl != null ? cartResponse.noLiquorImageUrl : "");

                cartProductAdapter.notifyAdapter(cartItems, orderSummary, voucherDetails, liquorCompliance);
            } else {
                ArrayList<CartItemGroup> currentCartItemGroup = cartProductAdapter.getCartItems();
                for (CartItemGroup cartItemGroup : currentCartItemGroup) {
                    for (CommerceItem currentItem : cartItemGroup.commerceItems) {
                        if (currentItem.commerceItemInfo.commerceId.equalsIgnoreCase(changeQuantity.commerceId)) {
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
                    voucherDetails = cartResponse.voucherDetails;
                    productCountMap = cartResponse.productCountMap;
                    liquorCompliance = new LiquorCompliance(cartResponse.liquorOrder, cartResponse.noLiquorImageUrl != null ? cartResponse.noLiquorImageUrl : "");
                    cartProductAdapter.notifyAdapter(currentCartItemGroup, orderSummary, voucherDetails, liquorCompliance);
                    fadeCheckoutButton(false);
                }
            }
            if (productCountMap != null && productCountMap.getTotalProductCount() > 0) {
                QueryBadgeCounter.getInstance().setCartCount(productCountMap.getTotalProductCount());
            }
        } else {
            cartProductAdapter.clear();
            resetToolBarIcons();
            rlCheckOut.setVisibility(View.GONE);
            rvCartList.setVisibility(View.GONE);
            relEmptyStateHandler.setVisibility(View.VISIBLE);
        }
        onChangeQuantityComplete();
        setItemLimitsBanner();
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

        hideEditCart();
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
                            if (shoppingCartResponse.data[0].orderSummary.fulfillmentDetails != null) {
                                Utils.savePreferredDeliveryLocation(new ShoppingDeliveryLocation(shoppingCartResponse.data[0].orderSummary.fulfillmentDetails));
                            }
                            setItemLimitsBanner();
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
                    FirebaseManager.Companion.logException(ex);
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
        }, ShoppingCartResponse.class));

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
                    FirebaseManager.Companion.logException(ex);
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
        }, ShoppingCartResponse.class));
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
                            if (cartResponse.cartItems.isEmpty())
                                onRemoveSuccess();
                        } else {
                            onRemoveSuccess();
                        }
                    } else {
                        if (cartProductAdapter != null)
                            resetItemDelete(true);
                    }
                    enableItemDelete(false);
                } catch (Exception ex) {
                    FirebaseManager.Companion.logException(ex);
                }
                removeItemFromCart();
            }

            void removeItemFromCart(){
                FirebaseAnalytics mFirebaseAnalytics = FirebaseManager.Companion.getInstance().getAnalytics();

                Bundle removeFromCartParams = new Bundle();
                removeFromCartParams.putString(FirebaseAnalytics.Param.CURRENCY, FirebaseManagerAnalyticsProperties.PropertyValues.CURRENCY_VALUE);
                removeFromCartParams.putString(FirebaseAnalytics.Param.VALUE,  String.valueOf(commerceItem.getPriceInfo().amount));
                Bundle removeFromCartItem = new Bundle();
                removeFromCartItem.putString(FirebaseAnalytics.Param.ITEM_ID,mCommerceItem.commerceItemInfo.productId);
                removeFromCartItem.putString(FirebaseAnalytics.Param.ITEM_NAME, mCommerceItem.commerceItemInfo.productDisplayName);
                removeFromCartItem.putString(FirebaseAnalytics.Param.PRICE, String.valueOf(commerceItem.getPriceInfo().amount));
                removeFromCartItem.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, mCommerceItem.commerceItemInfo.productDisplayName);
                removeFromCartItem.putString(FirebaseAnalytics.Param.QUANTITY, String.valueOf(mCommerceItem.commerceItemInfo.quantity));
                removeFromCartParams.putParcelableArray(FirebaseAnalytics.Param.ITEMS,  new Parcelable[]{ removeFromCartItem });

                mFirebaseAnalytics.logEvent(FirebaseManagerAnalyticsProperties.REMOVE_FROM_CART, removeFromCartParams);
            }

            @Override
            public void onFailure(Throwable error) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        if (cartProductAdapter != null) {
                            onRemoveItemLoadFail(commerceItem, true);
                            onRemoveItemFailed = true;
                            enableItemDelete(false);
                        }
                        mErrorHandlerView.showToast();
                    });
                }
            }
        }, ShoppingCartResponse.class));
        return shoppingCartResponseCall;
    }

    public Call<ShoppingCartResponse> removeAllCartItem(final CommerceItem commerceItem) {
        mRemoveAllItemFromCartTapped = true;
        onRemoveItem(true);
        Call<ShoppingCartResponse> shoppingCartResponseCall = OneAppService.INSTANCE.removeAllCartItems();
        shoppingCartResponseCall.enqueue(new CompletionHandler<>(new IResponseListener<ShoppingCartResponse>() {
            @Override
            public void onSuccess(ShoppingCartResponse shoppingCartResponse) {
                try {
                    if (shoppingCartResponse.httpCode == 200) {
                        CartResponse cartResponse = convertResponseToCartResponseObject(shoppingCartResponse);
                        mRemoveAllItemFromCartTapped = false;
                        updateCart(cartResponse, commerceItem);
                        updateCartSummary(0);
                        onRemoveSuccess();
                    } else {
                        onRemoveItem(false);
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
                    activity.runOnUiThread(() -> {
                        mRemoveAllItemFailed = true;
                        onRemoveItem(false);
                        mErrorHandlerView.hideErrorHandler();
                        mErrorHandlerView.showToast();
                    });
                }
            }
        }, ShoppingCartResponse.class));

        return shoppingCartResponseCall;
    }

    private void removeItemProgressBar(CommerceItem commerceItem, boolean visibility) {
        if (commerceItem == null) {
            onRemoveItem(visibility);
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
            cartResponse.voucherDetails = data.voucherDetails;
            cartResponse.productCountMap = data.productCountMap;// set delivery location
            cartResponse.liquorOrder=data.liquorOrder;
            cartResponse.noLiquorImageUrl=data.noLiquorImageUrl;
            if (cartResponse.orderSummary.fulfillmentDetails != null) {
                Utils.savePreferredDeliveryLocation(new ShoppingDeliveryLocation(cartResponse.orderSummary.fulfillmentDetails));
            }
            setDeliveryLocation(Utils.getPreferredDeliveryLocation());
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

                Log.e("giftWithPurchase", key);

                if (key.contains(ProductType.DEFAULT.getValue()))
                    cartItemGroup.setType(ProductType.DEFAULT.getShortHeader());
                else if (key.contains(ProductType.GIFT_COMMERCE_ITEM.getValue()))
                    cartItemGroup.setType(ProductType.GIFT_COMMERCE_ITEM.getShortHeader());
                else if (key.contains(ProductType.HOME_COMMERCE_ITEM.getValue()))
                    cartItemGroup.setType(ProductType.HOME_COMMERCE_ITEM.getShortHeader());
                else if (key.contains(ProductType.FOOD_COMMERCE_ITEM.getValue()))
                    cartItemGroup.setType(ProductType.FOOD_COMMERCE_ITEM.getShortHeader());
                else if (key.contains(ProductType.CLOTHING_COMMERCE_ITEM.getValue()))
                    cartItemGroup.setType(ProductType.CLOTHING_COMMERCE_ITEM.getShortHeader());
                else if (key.contains(ProductType.PREMIUM_BRAND_COMMERCE_ITEM.getValue()))
                    cartItemGroup.setType(ProductType.PREMIUM_BRAND_COMMERCE_ITEM.getShortHeader());
                else
                    cartItemGroup.setType(ProductType.OTHER_ITEMS.getShortHeader());

                JSONArray productsArray = itemsObject.getJSONArray(key);
                if (productsArray.length() > 0) {
                    ArrayList<CommerceItem> productList = new ArrayList<>();
                    for (int i = 0; i < productsArray.length(); i++) {
                        JSONObject commerceItemObject = productsArray.getJSONObject(i);
                        CommerceItem commerceItem = new Gson().fromJson(String.valueOf(commerceItemObject), CommerceItem.class);
                        String fulfillmentStoreId = Utils.retrieveStoreId(commerceItem.fulfillmentType);
                        commerceItem.fulfillmentStoreId = fulfillmentStoreId.replaceAll("\"", "");
                        productList.add(commerceItem);
                    }
                    cartItemGroup.setCommerceItems(productList);
                }
                cartItemGroups.add(cartItemGroup);
            }

            CartItemGroup giftCartItemGroup = new CartItemGroup();
            giftCartItemGroup.type = GIFT_ITEM;

            CartItemGroup generalCartItemGroup = new CartItemGroup();
            generalCartItemGroup.type = GENERAL_ITEM;
            int generalIndex = -1;
            if (cartItemGroups.contains(giftCartItemGroup) && cartItemGroups.contains(generalCartItemGroup)) {
                for (int cartGroupIndex = 0; cartGroupIndex < cartItemGroups.size(); cartGroupIndex++) {
                    CartItemGroup cartItemGroup = cartItemGroups.get(cartGroupIndex);
                    if (cartItemGroup.type.equalsIgnoreCase(GENERAL_ITEM)) {
                        generalIndex = cartGroupIndex;
                    }
                    if (cartItemGroup.type.equalsIgnoreCase(GIFT_ITEM)) {
                        giftCartItemGroup = cartItemGroup;
                        cartItemGroups.remove(cartGroupIndex);
                    }
                }
                cartItemGroups.add(generalIndex + 1, giftCartItemGroup);
            }

            cartResponse.cartItems = cartItemGroups;

        } catch (JSONException e) {
            FirebaseManager.Companion.logException(e);
            return null;
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
        ShoppingDeliveryLocation lastDeliveryLocation = Utils.getPreferredDeliveryLocation();
        if (lastDeliveryLocation != null) {
            setDeliveryLocation(lastDeliveryLocation);
        }

        FirebaseAnalytics mFirebaseAnalytics = FirebaseManager.Companion.getInstance().getAnalytics();
        Bundle removeFromCartParams = new Bundle();
        removeFromCartParams.putString(FirebaseAnalytics.Param.CURRENCY,FirebaseManagerAnalyticsProperties.PropertyValues.CURRENCY_VALUE);
        removeFromCartParams.putString(FirebaseAnalytics.Param.VALUE, " ");
        mFirebaseAnalytics.logEvent(FirebaseManagerAnalyticsProperties.VIEW_CART, removeFromCartParams);
    }

    @Override
    public void onPause() {
        super.onPause();
        Activity activity = getActivity();
        if (activity != null && mConnectionBroadcast != null) {
            activity.unregisterReceiver(mConnectionBroadcast);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == CART_DEFAULT_ERROR_TAPPED || resultCode == DIALOG_REQUEST_CODE) {
            Activity activity = getActivity();
            if (activity != null) {
                activity.setResult(CART_DEFAULT_ERROR_TAPPED);
                activity.finish();
                activity.overridePendingTransition(R.anim.slide_down_anim, R.anim.stay);
                return;
            }
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
                        hideEditCart();
                        //TODO: need to refactor
                       /* Call<SetDeliveryLocationSuburbResponse> setDeliveryLocationSuburb = OneAppService.INSTANCE.setSuburb(lastDeliveryLocation.storePickup ? lastDeliveryLocation.store.getId() : lastDeliveryLocation.suburb.id);
                        setDeliveryLocationSuburb.enqueue(new CompletionHandler<>(new IResponseListener<SetDeliveryLocationSuburbResponse>() {
                            @Override
                            public void onSuccess(SetDeliveryLocationSuburbResponse setDeliveryLocationSuburbResponse) {
                                if (setDeliveryLocationSuburbResponse.httpCode == 200) {
                                    Utils.savePreferredDeliveryLocation(lastDeliveryLocation);
                                    setDeliveryLocation(lastDeliveryLocation);
                                    //Utils.sendBus(new CartState(lastDeliveryLocation.suburb.name + ", " + lastDeliveryLocation.province.name));
                                }
                                loadShoppingCart(false);
                                loadShoppingCartAndSetDeliveryLocation();
                            }

                            @Override
                            public void onFailure(Throwable error) {
                                Activity activity = getActivity();
                                if (activity == null || error.getMessage() == null) return;

                                activity.runOnUiThread(() -> {
                                            loadShoppingCart(false);
                                            loadShoppingCartAndSetDeliveryLocation();
                                        }
                                );

                            }
                        }, SetDeliveryLocationSuburbResponse.class));*/
                    } else {
                        // Fallback if there is no cached location
                        loadShoppingCart(false);
                        loadShoppingCartAndSetDeliveryLocation();
                    }
                } else {
                    // Checkout was cancelled
                    loadShoppingCart(false);
                    loadShoppingCartAndSetDeliveryLocation();
                }
            } else {
                getActivity().onBackPressed();
            }
        } else if (requestCode == CART_BACK_PRESSED_CODE) {
            reloadFragment();
            return;
        } else if (requestCode == PDP_LOCATION_CHANGED_BACK_PRESSED_CODE || requestCode == SHOPPING_LIST_DETAIL_ACTIVITY_REQUEST_CODE) {
            checkLocationChangeAndReload();
        }

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PDP_REQUEST_CODE:
                    Activity activity = getActivity();
                    if (activity == null) return;
                    loadShoppingCart(false);
                    loadShoppingCartAndSetDeliveryLocation();
                    ProductCountMap productCountMap = (ProductCountMap) Utils.jsonStringToObject(data.getStringExtra("ProductCountMap"), ProductCountMap.class);
                    int itemsCount = data.getIntExtra("ItemsCount", 0);

                    if (KotlinUtils.Companion.isDeliveryOptionClickAndCollect() && productCountMap.getQuantityLimit().getFoodLayoutColour() != null) {
                        ToastFactory.Companion.showItemsLimitToastOnAddToCart(rlCheckOut, productCountMap, activity, itemsCount, false);
                    } else {
                        ToastFactory.Companion.buildAddToCartSuccessToast(rlCheckOut, false, activity, null);
                    }
                    break;
                case REQUEST_SUBURB_CHANGE:
                    loadShoppingCartAndSetDeliveryLocation();
                    break;
                case REDEEM_VOUCHERS_REQUEST_CODE:
                case APPLY_PROMO_CODE_REQUEST_CODE:
                    ShoppingCartResponse shoppingCartResponse = (ShoppingCartResponse) Utils.strToJson(data.getStringExtra("ShoppingCartResponse"), ShoppingCartResponse.class);
                    updateCart(convertResponseToCartResponseObject(shoppingCartResponse), null);
                    if (requestCode == REDEEM_VOUCHERS_REQUEST_CODE)
                        showVouchersOrPromoCodeAppliedToast(getString(CartUtils.Companion.getAppliedVouchersCount(voucherDetails.getVouchers()) > 0 ? R.string.vouchers_applied_toast_message : R.string.vouchers_removed_toast_message));
                    if (requestCode == APPLY_PROMO_CODE_REQUEST_CODE)
                        showVouchersOrPromoCodeAppliedToast(getString(R.string.promo_code_applied_toast_message));
                    break;
                default:
                    break;
            }
        }
        if (requestCode == REQUEST_PAYMENT_STATUS) {
            switch (resultCode) {
                case REQUEST_CHECKOUT_ON_DESTROY:
                    reloadFragment();
                    break;
                case RESULT_RELOAD_CART:
                    checkLocationChangeAndReload();
                    break;
                case RESULT_OK:
                    if (getActivity() != null) getActivity().onBackPressed();
                    break;
            }
        }

        if (requestCode == REQUEST_SUBURB_CHANGE) {
            loadShoppingCartAndSetDeliveryLocation();
        }

        if (requestCode == CART_LAUNCH_VALUE && resultCode == SSOActivity.SSOActivityResult.STATE_MISMATCH.rawValue()) {
            // login screen opens on cart and user closes it without login then move tab to last opened tab.
            Activity activity = getActivity();
            if (activity != null && activity instanceof BottomNavigationActivity) {
                int previousTabIndex = ((BottomNavigationActivity) activity).getPreviousTabIndex();
                ((BottomNavigationActivity) activity).getBottomNavigationById().setCurrentItem(previousTabIndex);
            }
        }

        // Retry callback when saved address api fails
        if (resultCode == ErrorHandlerActivity.RESULT_RETRY) {
            callSavedAddress();
        }
    }

    private void checkLocationChangeAndReload() {
        //TODO: need to refactor
       /* ShoppingDeliveryLocation deliveryLocation = Utils.getPreferredDeliveryLocation();
        String currentSuburbId = null;
        String currentStoreId = null;
        int currentCartCount = QueryBadgeCounter.getInstance().getCartCount();
        if (deliveryLocation != null) {
            if (deliveryLocation.suburb != null)
                currentSuburbId = deliveryLocation.suburb.id;
            if (deliveryLocation.store != null)
                currentStoreId = deliveryLocation.store.getId();
        }
        if (currentStoreId == null && currentSuburbId == null) {
            //Fresh install with no location selection.
        } else if (currentSuburbId == null && !(currentStoreId.equals(localStoreId))) {
            localStoreId = currentStoreId;
            localSuburbId = null;
            reloadFragment();
            return;

        } else if (currentStoreId == null && !(localSuburbId.equals(currentSuburbId))) {
            localSuburbId = currentSuburbId;
            localStoreId = null;
            reloadFragment();
            return;
        } else if (productCountMap != null && productCountMap.getTotalProductCount() != currentCartCount) {
            reloadFragment();
        }*/
    }

    private void loadShoppingCartAndSetDeliveryLocation() {
        ShoppingDeliveryLocation lastDeliveryLocation = Utils.getPreferredDeliveryLocation();
        if (lastDeliveryLocation != null) {
            setDeliveryLocation(lastDeliveryLocation);
            loadShoppingCart(false);
        }
    }

    public void reloadFragment() {
        //Reload screen
        setupToolbar();
        initializeBottomTab();
        initializeLoggedInUserCartUI();
        loadShoppingCart(false);
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
            queryServiceChangeQuantity();
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
                CommerceItemInfo commerceItemInfo = commerceItem.commerceItemInfo;
                if (!commerceItemInfo.isGWP)
                    skuIds.add(commerceItemInfo.catalogRefId);
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
        Call<SkusInventoryForStoreResponse> skuInventoryForStoreResponseCall = OneAppService.INSTANCE.getInventorySkuForStore(storeId, multiSku);
        skuInventoryForStoreResponseCall.enqueue(new CompletionHandler<>(new IResponseListener<SkusInventoryForStoreResponse>() {
            @Override
            public void onSuccess(SkusInventoryForStoreResponse skusInventoryForStoreResponse) {
                if (skusInventoryForStoreResponse.httpCode == 200) {
                    mSkuInventories.put(skusInventoryForStoreResponse.storeId, skusInventoryForStoreResponse.skuInventory);
                    if (mSkuInventories.size() == mapStoreIdWithCommerceItems.size()) {
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
        }, SkusInventoryForStoreResponse.class));
        return skuInventoryForStoreResponseCall;
    }

    private void disableQuantitySelector(Throwable error, Activity activity) {
        if (activity == null || !isAdded()) return;
        activity.runOnUiThread(() -> {
            if (error instanceof SocketTimeoutException) {
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
            } else if (error instanceof ConnectException || error instanceof UnknownHostException) {
                if (cartProductAdapter != null && btnCheckOut != null) {
                    ArrayList<CartItemGroup> cartItems = cartProductAdapter.getCartItems();
                    for (CartItemGroup cartItemGroup : cartItems) {
                        for (CommerceItem commerceItem : cartItemGroup.commerceItems) {
                            if (!commerceItem.isStockChecked) {
                                commerceItem.quantityInStock = -1;
                            }
                        }
                    }
                    cartProductAdapter.updateStockAvailability(cartItems);
                }
            }
            enableEditCart();
            btnCheckOut.setEnabled(false);
            rlCheckOut.setEnabled(false);
        });
    }

    private void updateCartListWithAvailableStock(HashMap<String, List<SkuInventory>> mSkuInventories) {
        isAllInventoryAPICallSucceed = true;
        for (CartItemGroup cartItemGroup : this.cartItems) {

            if (cartItemGroup.type.equalsIgnoreCase(GIFT_ITEM)) {
                for (CommerceItem commerceItem : cartItemGroup.commerceItems) {
                    commerceItem.commerceItemInfo.quantity = 1;
                    commerceItem.quantityInStock = 2;
                    commerceItem.isStockChecked = true;
                }
            }

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
                    mChangeQuantity.commerceId = commerceItem.commerceItemInfo.getCommerceId();
                    mChangeQuantity.quantity = commerceItem.quantityInStock;
                    mCommerceItem.setQuantityUploading(true);
                    queryServiceChangeQuantity();
                }
            }
        }
        if (!btnCheckOut.isEnabled() && isAllInventoryAPICallSucceed && !isAnyItemNeedsQuantityUpdate) {
            fadeCheckoutButton(false);
            if (voucherDetails != null && isAdded())
                showAvailableVouchersToast(voucherDetails.getActiveVouchersCount());
        }

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

    public void deliveryLocationEnabled(boolean isEditMode) {
        Utils.deliveryLocationEnabled(getActivity(), isEditMode, rlLocationSelectedLayout);
    }

    @Override
    public void onToastButtonClicked(String currentState) {
        switch (currentState) {
            case TAG_ADDED_TO_LIST_TOAST: {
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
            break;
            case TAG_AVAILABLE_VOUCHERS_TOAST: {
                Activity activity = getActivity();
                if (activity == null) return;
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.Cart_ovr_popup_view, activity);
                navigateToAvailableVouchersPage();
            }
            break;
            default:
                break;
        }

    }

    public void setDeliveryLocation(ShoppingDeliveryLocation shoppingDeliveryLocation) {
        if (getActivity() != null) {
            deliverLocationRightArrow.setVisibility(View.GONE);
            editLocation.setVisibility(View.VISIBLE);
            KotlinUtils.Companion.setDeliveryAddressView(getActivity(), shoppingDeliveryLocation, tvDeliveringToText, tvDeliveryLocation, deliverLocationIcon);
        }
    }

    private void enableEditCart(boolean enable) {
        Utils.fadeInFadeOutAnimation(btnEditCart, enable);
        btnEditCart.setEnabled(!enable);
    }

    public void enableEditCart() {
        Utils.fadeInFadeOutAnimation(btnEditCart, false);
        btnEditCart.setEnabled(true);
    }

    private void showEditDeliveryLocationFeatureWalkthrough() {
        if (!AppInstanceObject.get().featureWalkThrough.showTutorials || AppInstanceObject.get().featureWalkThrough.deliveryLocation
                || getBottomNavigationActivity() == null || deliverLocationIcon == null || !isAdded() || !isVisible())
            return;
        BottomNavigationActivity activity = getBottomNavigationActivity();
        FirebaseManager.Companion.setCrashlyticsString(getString(R.string.crashlytics_materialshowcase_key), this.getClass().getSimpleName());
        activity.walkThroughPromtView = new WMaterialShowcaseView.Builder(activity, WMaterialShowcaseView.Feature.DELIVERY_LOCATION)
                .setTarget(deliverLocationIcon)
                .setTitle(R.string.your_delivery_location)
                .setDescription(R.string.walkthrough_delivery_location_desc)
                .setActionText(R.string.tips_edit_delivery_location)
                .setImage(R.drawable.tips_tricks_ic_stores)
                .setAction(this)
                .setShapePadding(24)
                .setArrowPosition(WMaterialShowcaseView.Arrow.TOP_LEFT)
                .setMaskColour(getResources().getColor(R.color.semi_transparent_black)).build();
        activity.walkThroughPromtView.show(activity);
    }

    @Override
    public void onWalkthroughActionButtonClick(WMaterialShowcaseView.Feature feature) {
        if (feature == WMaterialShowcaseView.Feature.DELIVERY_LOCATION)
            this.onClick(rlLocationSelectedLayout);
    }

    @Override
    public void onPromptDismiss() {
        isMaterialPopUpClosed = true;
        if (voucherDetails != null && isAdded())
            showAvailableVouchersToast(voucherDetails.getActiveVouchersCount());
    }

    public ArrayList<CartItemGroup> getCartItems() {
        return cartItems;
    }

    private void displayUpSellMessage(Data data) {
        if (data == null || data.globalMessages == null || mRemoveAllItemFromCartTapped) return;
        GlobalMessages globalMessages = data.globalMessages;

        if (globalMessages.getQualifierMessages() == null || globalMessages.getQualifierMessages().isEmpty())
            return;

        String qualifierMessage = globalMessages.getQualifierMessages().get(0);

        upSellMessageTextView.setText(qualifierMessage);
        upSellMessageTextView.setVisibility(TextUtils.isEmpty(qualifierMessage) ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onOutOfStockProductsRemoved() {
        loadShoppingCart(false);
    }

    private void showMaxItemView() {
        KotlinUtils.Companion.showGeneralInfoDialog(requireActivity().getSupportFragmentManager(), getString(R.string.unable_process_checkout_desc), getString(R.string.unable_process_checkout_title), getString(R.string.got_it), R.drawable.payment_overdue_icon);
    }

    public void showRedeemVoucherFeatureWalkthrough() {
        BottomNavigationActivity activity = getBottomNavigationActivity();
        if (activity == null || !isAdded() || !isVisible()) {
            return;
        }
        if (!AppInstanceObject.get().featureWalkThrough.showTutorials || AppInstanceObject.get().featureWalkThrough.cartRedeemVoucher) {
            isMaterialPopUpClosed = true;
            return;
        }
        activity.walkThroughPromtView = new WMaterialShowcaseView.Builder(activity, WMaterialShowcaseView.Feature.CART_REDEEM_VOUCHERS)
                .setTarget(new View(activity))
                .setTitle(R.string.redeem_voucher_walkthrough_title)
                .setDescription(R.string.redeem_voucher_walkthrough_desc)
                .setActionText(R.string.got_it)
                .setImage(R.drawable.tips_tricks_ic_redeem_voucher)
                .setAction(this)
                .setShouldRender(false)
                .setArrowPosition(WMaterialShowcaseView.Arrow.NONE)
                .setMaskColour(getResources().getColor(R.color.semi_transparent_black)).build();
        activity.walkThroughPromtView.show(activity);
    }

    public void showAvailableVouchersToast(int availableVouchersCount) {
        if (availableVouchersCount < 1 || !isMaterialPopUpClosed)
            return;
        mToastUtils.setActivity(getActivity());
        mToastUtils.setCurrentState(TAG_AVAILABLE_VOUCHERS_TOAST);
        mToastUtils.setCartText(getString(R.string.available));
        mToastUtils.setPixel((int) (btnCheckOut.getHeight() * 2.5));
        mToastUtils.setView(btnCheckOut);
        mToastUtils.setMessage(availableVouchersCount + getString(availableVouchersCount > 1 ? R.string.available_vouchers_toast_message : R.string.available_voucher_toast_message));
        mToastUtils.setAllCapsUpperCase(true);
        mToastUtils.setViewState(true);
        mToastUtils.build();
    }

    public void showVouchersOrPromoCodeAppliedToast(String message) {
        if (isAdded()) {
            mToastUtils.setActivity(getActivity());
            mToastUtils.setCurrentState(TAG);
            mToastUtils.setPixel((int) (btnCheckOut.getHeight() * 2.5));
            mToastUtils.setView(btnCheckOut);
            mToastUtils.setMessage(message);
            mToastUtils.setViewState(false);
            mToastUtils.buildCustomToast();
        }
    }

    @Override
    public void onViewVouchers() {
        navigateToAvailableVouchersPage();
    }

    void navigateToAvailableVouchersPage() {
        Intent intent = new Intent(getContext(), AvailableVouchersToRedeemInCart.class);
        intent.putExtra("VoucherDetails", Utils
                .toJson(voucherDetails));
        startActivityForResult(intent
                , REDEEM_VOUCHERS_REQUEST_CODE);
    }

    @Override
    public void updateOrderTotal() {
        if (orderSummary != null) {
            orderTotal.setText(CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace(orderSummary.total));
        }
    }

    @Override
    public void onEnterPromoCode() {
        Activity activity = getActivity();
        if (activity == null) return;
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.Cart_promo_enter, activity);
        navigateToApplyPromoCodePage();
    }

    @Override
    public void onRemovePromoCode(String promoCode) {
        Activity activity = getActivity();
        if (activity == null) return;
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.Cart_promo_remove, activity);
        showProgressBar();
        OneAppService.INSTANCE.removePromoCode(new CouponClaimCode(promoCode)).enqueue(new CompletionHandler<>(new IResponseListener<ShoppingCartResponse>() {
            @Override
            public void onSuccess(ShoppingCartResponse response) {
                hideProgressBar();
                switch (response.httpCode) {
                    case 200:
                        updateCart(convertResponseToCartResponseObject(response), null);
                        if (voucherDetails.getPromoCodes() == null || voucherDetails.getPromoCodes().size() == 0)
                            showVouchersOrPromoCodeAppliedToast(getString(R.string.promo_code_removed_toast_message));
                        break;
                    case 502:
                        if (response.response != null)
                            Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.response.desc, true);
                        break;
                    case 440:
                        SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE);
                        SessionExpiredUtilities.getInstance().showSessionExpireDialog((AppCompatActivity) getActivity(), CartFragment.this);
                        break;
                    default:
                        if (response.response != null)
                            Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, getString(R.string.general_error_desc), true);
                        break;

                }
            }

            @Override
            public void onFailure(Throwable error) {
                hideProgressBar();
                Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, getString(R.string.general_error_desc), true);
            }

        }, ShoppingCartResponse.class));
    }

    void navigateToApplyPromoCodePage() {
        Intent intent = new Intent(getContext(), AvailableVouchersToRedeemInCart.class);
        startActivityForResult(intent
                , APPLY_PROMO_CODE_REQUEST_CODE);
    }

    private void hideProgressBar() {
        if (getActivity() != null) {
            pBar.setVisibility(View.GONE);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private void showProgressBar() {
        if (getActivity() != null) {
            pBar.setVisibility(View.VISIBLE);
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    @Override
    public void onPromoDiscountInfo() {
        KotlinUtils.Companion.showGeneralInfoDialog(requireActivity().getSupportFragmentManager(), getString(R.string.promo_discount_dialog_desc), getString(R.string.promo_discount_dialog_title), getString(R.string.got_it), 0);
    }

    private void setItemLimitsBanner() {
        Activity activity = getActivity();
        if (activity != null && isAdded()) {
            CartUtils.Companion.updateItemLimitsBanner(productCountMap, itemLimitsBanner, itemLimitsMessage, itemLimitsCounter, KotlinUtils.Companion.getPreferredDeliveryType() == Delivery.CNC);
        }
    }

    public void enableItemDelete(boolean enable) {
        enableEditCart(enable);
        fadeCheckoutButton(enable);
        deliveryLocationEnabled(!enable);
    }
}
