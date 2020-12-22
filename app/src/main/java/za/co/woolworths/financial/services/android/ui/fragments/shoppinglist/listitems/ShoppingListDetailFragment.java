package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.awfs.coordination.R;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.IResponseListener;
import za.co.woolworths.financial.services.android.contracts.IToastInterface;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse;
import za.co.woolworths.financial.services.android.models.dto.FulfillmentStoreMap;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse;
import za.co.woolworths.financial.services.android.models.dto.SkuInventory;
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.ui.activities.CartActivity;
import za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity;
import za.co.woolworths.financial.services.android.ui.activities.product.shop.ShoppingListDetailActivity;
import za.co.woolworths.financial.services.android.ui.adapters.ShoppingListItemsAdapter;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.IOnConfirmDeliveryLocationActionListener;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog.ConfirmDeliveryLocationFragment;
import za.co.woolworths.financial.services.android.ui.views.ToastFactory;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.EmptyCartView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.KotlinUtils;
import za.co.woolworths.financial.services.android.util.MultiMap;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.PostItemToCart;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.ToastUtils;
import za.co.woolworths.financial.services.android.util.Utils;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.OPEN_CART_REQUEST;
import static za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity.PRODUCT_SEARCH_ACTIVITY_REQUEST_CODE;
import static za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment.ADDED_TO_SHOPPING_LIST_RESULT_CODE;
import static za.co.woolworths.financial.services.android.util.AppConstant.HTTP_EXPECTATION_FAILED_417;
import static za.co.woolworths.financial.services.android.util.AppConstant.HTTP_SESSION_TIMEOUT_440;
import static za.co.woolworths.financial.services.android.util.AppConstant.HTTP_OK;

public class ShoppingListDetailFragment extends Fragment implements View.OnClickListener, EmptyCartView.EmptyCartInterface, NetworkChangeListener, ToastUtils.ToastInterface, ShoppingListItemsNavigator, IToastInterface, IOnConfirmDeliveryLocationActionListener {

    private final int DELIVERY_LOCATION_REQUEST = 2;
    public static final int ADD_TO_CART_SUCCESS_RESULT = 2000;
    private final int SET_DELIVERY_LOCATION_REQUEST_CODE = 2011;
    private int DELIVERY_LOCATION_REQUEST_CODE_FROM_SELECT_ALL = 1222;
    public final static int QUANTITY_CHANGED_FROM_LIST = 2010;
    private int REQUEST_SUBURB_CHANGE = 12345;

    private String listName;
    private String listId;
    private List<ShoppingListItem> mShoppingListItems;
    private ShoppingListItemsAdapter shoppingListItemsAdapter;
    private boolean isMenuItemReadyToShow = false;
    private ErrorHandlerView mErrorHandlerView;
    private BroadcastReceiver mConnectionBroadcast;
    private Call<AddItemToCartResponse> mPostAddToCart;
    private Call<SkusInventoryForStoreResponse> mGetInventorySkusForStore;
    private ArrayList<FulfillmentStoreMap> fulfillmentStoreMapArrayList;
    private boolean errorMessageWasPopUp;
    private ShoppingListItem mOpenShoppingListItem;
    private Integer mDeliveryResultCode;
    private boolean addedToCart;
    private boolean internetConnectionWasLost = false;
    private RecyclerView rcvShoppingListItems;
    private ProgressBar loadingBar;
    private RelativeLayout rlCheckOut;
    private ProgressBar pbLoadingIndicator;
    private WButton btnCheckOut;
    private LinearLayout rlEmptyView;
    private boolean openFromMyList;
    private List<String> matchesFullFillmentTypeKey;
    private TextView selectDeselectAllTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle argument = getArguments();
        if (argument != null) {
            listId = argument.getString("listId");
            listName = argument.getString("listName");
            openFromMyList = argument.getBoolean("openFromMyList", false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopping_list_detail_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewAndEvent(view);
        initGetShoppingListItems();
        selectDeselectAllTextView.setOnClickListener(this);
    }

    private void initViewAndEvent(View view) {
        fulfillmentStoreMapArrayList = new ArrayList<>();
        RelativeLayout rlNoConnectionLayout = view.findViewById(R.id.no_connection_layout);
        rcvShoppingListItems = view.findViewById(R.id.rcvShoppingListItems);
        WTextView textProductSearch = view.findViewById(R.id.textProductSearch);
        loadingBar = view.findViewById(R.id.loadingBar);
        rlCheckOut = view.findViewById(R.id.rlCheckOut);
        pbLoadingIndicator = view.findViewById(R.id.pbLoadingIndicator);
        btnCheckOut = view.findViewById(R.id.btnCheckOut);
        rlEmptyView = view.findViewById(R.id.rlEmptyListView);
        Activity activity = getActivity();
        if (activity != null)
            selectDeselectAllTextView = activity.findViewById(R.id.selectDeselectAllTextView);
        initList(rcvShoppingListItems);
        setScrollListener(rcvShoppingListItems);

        textProductSearch.setOnClickListener(this);
        setUpAddToCartButton();

        mErrorHandlerView = new ErrorHandlerView(getActivity(), rlNoConnectionLayout);
        mErrorHandlerView.setMargin(rlNoConnectionLayout, 0, 0, 0, 0);
        mConnectionBroadcast = Utils.connectionBroadCast(getActivity(), this);
        view.findViewById(R.id.btnRetry).setOnClickListener(this);
        EmptyCartView emptyCartView = new EmptyCartView(view, this);
        emptyCartView.setView(getString(R.string.title_empty_shopping_list), getString(R.string.description_empty_shopping_list), getString(R.string.button_empty_shopping_list), R.drawable.emptyshoppinglist);
    }

    private void updateList(List<ShoppingListItem> listItems) {
        if (shoppingListItemsAdapter != null) {
            shoppingListItemsAdapter.updateList(listItems);
            setUpView();
        }
    }

    public void loadShoppingListItems(ShoppingListItemsResponse shoppingListItemsResponse) {
        loadingBar.setVisibility(GONE);
        mShoppingListItems = shoppingListItemsResponse.listItems;
        makeInventoryCall();
    }

    private void makeInventoryCall() {
        Activity activity = getActivity();
        if (activity == null) return;

        ShoppingDeliveryLocation shoppingDeliveryLocation = Utils.getPreferredDeliveryLocation();
        if (shoppingDeliveryLocation == null) {
            if (mShoppingListItems == null)
                mShoppingListItems = new ArrayList<>();
            updateList(mShoppingListItems);
            enableAdapterClickEvent(true);
            return;
        }

        if (shoppingListInventory()) return;

        if (mShoppingListItems == null)
            mShoppingListItems = new ArrayList<>();
        updateList(mShoppingListItems);


        //Activated when a user click on quantity selector but no suburb was set
        // OpenQuantitySelector automatically
        if (mOpenShoppingListItem != null) {
            for (ShoppingListItem shoppingListItem : mShoppingListItems) {
                if (shoppingListItem.catalogRefId == null) continue;
                if (shoppingListItem.catalogRefId.equalsIgnoreCase(mOpenShoppingListItem.catalogRefId)) {
                    mOpenShoppingListItem.quantityInStock = shoppingListItem.quantityInStock;
                }
            }

            if (mOpenShoppingListItem.quantityInStock == 0) {
                ToastFactory.Companion.showToast(getActivity(), btnCheckOut, activity.getResources().getString(R.string.product_unavailable_desc));
                return;
            }
            if (mOpenShoppingListItem.quantityInStock == -1) return;
            Intent editQuantityIntent = new Intent(activity, ConfirmColorSizeActivity.class);
            editQuantityIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, ConfirmColorSizeActivity.QUANTITY);
            editQuantityIntent.putExtra("CART_QUANTITY_In_STOCK", mOpenShoppingListItem.quantityInStock);
            activity.startActivityForResult(editQuantityIntent, QUANTITY_CHANGED_FROM_LIST);
            activity.overridePendingTransition(0, 0);

        }
    }

    private boolean shoppingListInventory() {
        if (mShoppingListItems == null) {
            setUpView();
            enableAdapterClickEvent(true);
            return true;
        }
        MultiMap<String, ShoppingListItem> multiListItem = MultiMap.create();
        for (ShoppingListItem shoppingListItem : mShoppingListItems) {
            if (!shoppingListItem.inventoryCallCompleted
                    && !TextUtils.isEmpty(shoppingListItem.catalogRefId))
                multiListItem.put(shoppingListItem.fulfillmentType, shoppingListItem);
        }

        Map<String, String> collectOtherSkuId = new HashMap<>();
        Map<String, Collection<ShoppingListItem>> collections = multiListItem.getEntries();
        for (Map.Entry<String, Collection<ShoppingListItem>> collectionEntry : collections.entrySet()) {
            Collection<ShoppingListItem> collectionEntryValue = collectionEntry.getValue();
            String fulFillmentTypeIdCollection = collectionEntry.getKey();
            List<String> skuIds = new ArrayList<>();
            for (ShoppingListItem shoppingListItem : collectionEntryValue) {
                skuIds.add(shoppingListItem.catalogRefId);
            }
            String multiSKUS = TextUtils.join("-", skuIds);
            collectOtherSkuId.put(fulFillmentTypeIdCollection, multiSKUS);
            String fulFillmentStoreId = Utils.retrieveStoreId(fulFillmentTypeIdCollection);
            if (!TextUtils.isEmpty(fulFillmentStoreId)) {
                fulFillmentStoreId = fulFillmentStoreId.replace("\"", "");
                fulfillmentStoreMapArrayList.add(new FulfillmentStoreMap(fulFillmentTypeIdCollection, fulFillmentStoreId, false));
                executeGetInventoryForStore(fulFillmentStoreId, multiSKUS);
            } else {
                for (String sku : skuIds) {
                    for (ShoppingListItem inventoryItems : mShoppingListItems) {
                        if (inventoryItems.catalogRefId.equalsIgnoreCase(sku))
                            inventoryItems.inventoryCallCompleted = true;
                    }
                }
                enableAdapterClickEvent(true);
            }
        }
        return false;
    }

    private void setUpView() {
        rlEmptyView.setVisibility(mShoppingListItems == null || mShoppingListItems.size() == 0 ? VISIBLE : GONE);
        // 1 to exclude header
        rcvShoppingListItems.setVisibility(mShoppingListItems == null || mShoppingListItems.size() == 0 ? GONE : VISIBLE);
        manageSelectAllMenuVisibility();
    }

    private void initList(RecyclerView rcvShoppingListItems) {
        mShoppingListItems = new ArrayList<>();
        matchesFullFillmentTypeKey = new ArrayList<>();
        shoppingListItemsAdapter = new ShoppingListItemsAdapter(mShoppingListItems, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rcvShoppingListItems.setLayoutManager(mLayoutManager);
        rcvShoppingListItems.setAdapter(shoppingListItemsAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.selectDeselectAllTextView:
                onOptionsItemSelected();
                break;
            case R.id.textProductSearch:
                openProductSearchActivity();
                break;
            case R.id.btnRetry:
                if (NetworkManager.getInstance().isConnectedToNetwork(getActivity())) {
                    errorMessageWasPopUp = false;
                    initGetShoppingListItems();
                }
                break;
            case R.id.btnCheckOut:
                addItemsToCart();
                break;

            default:
                break;
        }
    }

    private void openProductSearchActivity() {
        Activity activity = getActivity();
        if (activity != null) {
            Intent openProductSearchActivity = new Intent(activity, ProductSearchActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("listName", listName);
            openProductSearchActivity.putExtra("SEARCH_TEXT_HINT", getString(R.string.shopping_search_hint));
            openProductSearchActivity.putExtra("listID", listId);
            getActivity().startActivityForResult(openProductSearchActivity, PRODUCT_SEARCH_ACTIVITY_REQUEST_CODE);
            getActivity().overridePendingTransition(0, 0);
        }
    }

    public void onShoppingListItemsResponse(ShoppingListItemsResponse shoppingListItemsResponse) {
        switch (shoppingListItemsResponse.httpCode) {
            case HTTP_OK:
                loadShoppingListItems(shoppingListItemsResponse);
                break;
            case HTTP_SESSION_TIMEOUT_440:
                SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, shoppingListItemsResponse.response.stsParams, getActivity());
                break;
            default:
                enableAdapterClickEvent(true);
                loadingBar.setVisibility(GONE);
                Activity activity = getActivity();
                if (activity == null) return;
                if (shoppingListItemsResponse.response == null) return;
                if (TextUtils.isEmpty(shoppingListItemsResponse.response.desc)) return;
                Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, shoppingListItemsResponse.response.desc);
                break;
        }

    }

    private void enableAdapterClickEvent(boolean clickable) {
        if (shoppingListItemsAdapter != null)
            shoppingListItemsAdapter.adapterClickable(clickable);
    }

    @Override
    public void onItemSelectionChange(List<ShoppingListItem> items) {
        boolean itemWasSelected = getButtonStatus(items);
        rlCheckOut.setVisibility(itemWasSelected ? VISIBLE : GONE);
        Utils.setRecyclerViewMargin(rcvShoppingListItems, itemWasSelected ? Utils.dp2px(60) : 0);
        if (isAdded()) {
            if (items.size() > 0)
                setSelectAllButtonText(items);
            else
                selectDeselectAllTextView.setVisibility(GONE);
        } else {
            setSelectAllButtonText(items);
        }
    }

    public void setSelectAllButtonText(List<ShoppingListItem> items) {
        if (getActivity() != null && selectDeselectAllTextView != null)
            selectDeselectAllTextView.setText(getString(getSelectAllMenuVisibility(items) ? R.string.deselect_all : R.string.select_all));
    }

    public void onShoppingListItemDelete(ShoppingListItemsResponse shoppingListItemsResponse) {
        mShoppingListItems = shoppingListItemsResponse.listItems;
        manageSelectAllMenuVisibility();
        updateList(mShoppingListItems);
    }

    @Override
    public void onItemDeleteClick(String id, String productId, String catalogRefId, final boolean shouldUpdateShoppingList) {
        int listSize = shoppingListItemsAdapter.getShoppingListItems().size();
        if (listSize == 1) {
            if (!shouldUpdateShoppingList) {
                rlEmptyView.setVisibility(VISIBLE);
                rcvShoppingListItems.setVisibility(GONE);
                hideEditShoppingListMode();
                isMenuItemReadyToShow = false;
                Activity activity = getActivity();
                if (activity != null) {
                    activity.invalidateOptionsMenu();
                    ((ShoppingListDetailActivity) activity).setToolbarText(getString(R.string.edit));
                }
                rlCheckOut.setVisibility(GONE);
            }
        }
        editButtonVisibility();
        Call<ShoppingListItemsResponse> shoppingListItemsResponseCall = OneAppService.INSTANCE.deleteShoppingListItem(listId, id, productId, catalogRefId);
        shoppingListItemsResponseCall.enqueue(new CompletionHandler<>(new IResponseListener<ShoppingListItemsResponse>() {
            @Override
            public void onSuccess(ShoppingListItemsResponse shoppingListItemsResponse) {
                if (shouldUpdateShoppingList)
                    onShoppingListItemDelete(shoppingListItemsResponse);
            }

            @Override
            public void onFailure(Throwable error) {
                if (shouldUpdateShoppingList)
                    onDeleteItemFailed();
            }
        }, ShoppingListItemsResponse.class));

    }

    @Override
    public void onShoppingSearchClick() {
        openProductSearchActivity();
    }

    public void onAddToCartPreExecute() {
        pbLoadingIndicator.setVisibility(VISIBLE);
        enableAddToCartButton(GONE);
    }

    public void onAddToCartSuccess(AddItemToCartResponse addItemToCartResponse, int size) {
        Activity activity = getActivity();
        if (activity == null) return;
        Intent resultIntent = new Intent();
        if (addItemToCartResponse.data.size() > 0) {
            String successMessage = addItemToCartResponse.data.get(0).message;
            resultIntent.putExtra("addedToCartMessage", successMessage);
            resultIntent.putExtra("ProductCountMap", Utils.toJson(addItemToCartResponse.data.get(0).productCountMap));
            resultIntent.putExtra("ItemsCount", size);
        }

        // reset selection after items added to cart
        if (shoppingListItemsAdapter != null) {
            shoppingListItemsAdapter.resetSelection();
        }

        pbLoadingIndicator.setVisibility(GONE);
        btnCheckOut.setVisibility(VISIBLE);

        // Present toast on BottomNavigationMenu if shopping list detail was opened from my list
        if (openFromMyList) {
            activity.setResult(RESULT_OK, resultIntent);
            activity.finish();
            activity.overridePendingTransition(0, 0);
        } else {
            // else display shopping list toast
            if (KotlinUtils.Companion.isDeliveryOptionClickAndCollect() && addItemToCartResponse.data.get(0).productCountMap.getQuantityLimit().getFoodLayoutColour() != null) {
                ToastFactory.Companion.showItemsLimitToastOnAddToCart(rlCheckOut, addItemToCartResponse.data.get(0).productCountMap, activity, size);
            } else {
                ToastFactory.Companion.buildAddToCartSuccessToast(rlCheckOut, true, activity, this);
            }
        }
    }

    @Override
    public void requestDeliveryLocation(String requestMessage) {
        if (isAdded()) {
            pbLoadingIndicator.setVisibility(GONE);
            enableAddToCartButton(VISIBLE);
            shoppingListItemsAdapter.resetSelection();
            Utils.displayValidationMessageForResult(this,
                    getActivity(),
                    CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                    null,
                    requestMessage,
                    getResources().getString(R.string.set_delivery_location_button),
                    SET_DELIVERY_LOCATION_REQUEST_CODE);
        }
    }

    private final void confirmDeliveryLocation() {
        FragmentManager fragmentManager = this.getChildFragmentManager();
        ShoppingListDetailFragment.this.getActivity().runOnUiThread(() -> {
            ConfirmDeliveryLocationFragment confirmDeliveryLocationFragment = ConfirmDeliveryLocationFragment.Companion.newInstance();
            if (confirmDeliveryLocationFragment != null) {
                confirmDeliveryLocationFragment.setCancelable(false);
                confirmDeliveryLocationFragment.show(fragmentManager, ConfirmDeliveryLocationFragment.class.getSimpleName());
            }
        });
    }

    private void enableAddToCartButton(int visible) {
        btnCheckOut.setVisibility(visible);
    }

    public void onSessionTokenExpired(final Response response) {
        SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, response.stsParams, getActivity());
    }

    public void otherHttpCode(Response response) {
        if (isAdded()) {
            pbLoadingIndicator.setVisibility(GONE);
            enableAddToCartButton(VISIBLE);
            Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc);
        }
    }

    @Override
    public void onQuantityChangeClick(int position, ShoppingListItem shoppingListItem) {
        this.mOpenShoppingListItem = shoppingListItem;
        navigateFromQuantity();
        Activity activity = getActivity();
        if (activity != null) {
            Intent editQuantityIntent = new Intent(activity, ConfirmColorSizeActivity.class);
            editQuantityIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, ConfirmColorSizeActivity.QUANTITY);
            editQuantityIntent.putExtra("QUANTITY_IN_STOCK", Utils.toJson(shoppingListItem));
            activity.startActivityForResult(editQuantityIntent, QUANTITY_CHANGED_FROM_LIST);
            activity.overridePendingTransition(0, 0);
        }
    }

    @Override
    public void onDeleteItemFailed() {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mErrorHandlerView.showToast();
                    shoppingListItemsAdapter.updateList(mShoppingListItems);
                }
            });
        }
    }

    @Override
    public void openProductDetailFragment(String productName, ProductList productList) {
        Gson gson = new Gson();
        String strProductList = gson.toJson(productList);
        Bundle bundle = new Bundle();
        bundle.putString("strProductList", strProductList);
        bundle.putString("strProductCategory", productName);
        ScreenManager.presentProductDetails(getActivity(), bundle);
    }


    private void initGetShoppingListItems() {
        Activity activity = getActivity();
        if (activity == null) return;
        mErrorHandlerView.hideErrorHandler();
        mShoppingListItems = new ArrayList<>();
        rlEmptyView.setVisibility(GONE);
        rcvShoppingListItems.setVisibility(GONE);
        loadingBar.setVisibility(VISIBLE);
        ((ShoppingListDetailActivity) activity).setToolbarText(getString(R.string.edit));


        Call<ShoppingListItemsResponse> shoppingListItemsResponseCall = OneAppService.INSTANCE.getShoppingListItems(listId);
        shoppingListItemsResponseCall.enqueue(new CompletionHandler<>(new IResponseListener<ShoppingListItemsResponse>() {
            @Override
            public void onSuccess(ShoppingListItemsResponse shoppingListItemsResponse) {
                onShoppingListItemsResponse(shoppingListItemsResponse);
            }

            @Override
            public void onFailure(Throwable error) {

            }
        }, ShoppingListItemsResponse.class));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        cancelRequest(mPostAddToCart);
        cancelRequest(mGetInventorySkusForStore);
    }

    private void cancelRequest(Call call) {
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
    }

    public boolean getButtonStatus(List<ShoppingListItem> items) {
        for (ShoppingListItem shoppingListItem : items) {
            if (shoppingListItem.isSelected)
                return true;
        }
        return false;
    }

    public boolean getSelectAllMenuVisibility(List<ShoppingListItem> items) {
        for (ShoppingListItem shoppingListItem : items) {
            if (!shoppingListItem.isSelected && shoppingListItem.quantityInStock > 0)
                return false;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        Activity activity = getActivity();
        if (activity == null) return;
        ShoppingListDetailActivity shoppingListActivity = ((ShoppingListDetailActivity) activity);
        if (isMenuItemReadyToShow) {
            selectDeselectAllTextView.setVisibility(VISIBLE);
            shoppingListActivity.editButtonVisibility(true);
        } else {
            selectDeselectAllTextView.setVisibility(GONE);
            editButtonVisibility();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onOptionsItemSelected() {
        if (shouldUserSetSuburb()) {
            deliverySelectionIntent(DELIVERY_LOCATION_REQUEST_CODE_FROM_SELECT_ALL);
            return;
        }

        if (selectDeselectAllTextView.getText().toString().equalsIgnoreCase("SELECT ALL")) {
            selectAllListItems(true);
            selectDeselectAllTextView.setText(getString(R.string.deselect_all));
        } else {
            selectAllListItems(false);
            selectDeselectAllTextView.setText(getString(R.string.select_all));
        }
    }


    @Override
    public void onEmptyCartRetry() {
        openProductSearchActivity();
    }

    public void setScrollListener(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                // actionSearchVisibility(dy > rcvShoppingListItems.getHeight());
            }
        });
    }

    private void setUpAddToCartButton() {
        btnCheckOut.setOnClickListener(this);
        btnCheckOut.setText(getString(R.string.add_to_cart));
    }

    private void executeAddToCart(List<ShoppingListItem> items) {
        onAddToCartPreExecute();
        List<AddItemToCart> selectedItems = new ArrayList<>();
        for (ShoppingListItem item : items) {
            if (item.isSelected && item.quantityInStock > 0)
                selectedItems.add(new AddItemToCart(item.productId, item.catalogRefId, item.userQuantity));
        }

        mPostAddToCart = postAddItemToCart(selectedItems);
    }

    public void manageSelectAllMenuVisibility() {
        isMenuItemReadyToShow = false;
        for (ShoppingListItem shoppingListItem : mShoppingListItems) {
            if (shoppingListItem.quantityInStock > 0) {
                isMenuItemReadyToShow = true;
                break;
            }
        }
        Activity activity = getActivity();
        if (activity != null)
            activity.invalidateOptionsMenu();
    }

    public void selectAllListItems(boolean setSelection) {
        if (shoppingListItemsAdapter != null && mShoppingListItems != null && mShoppingListItems.size() > 0) {
            for (ShoppingListItem item : mShoppingListItems) {
                if (item.quantityInStock > 0) {
                    item.isSelected = setSelection;
                    int quantity = item.userQuantity > 1 ? item.userQuantity : 1; // Click -> Select all - when one item quantity is > 1
                    item.userQuantity = setSelection ? quantity : 0;
                }
            }
            shoppingListItemsAdapter.updateList(mShoppingListItems);
        }
    }

    @Override
    public void onConnectionChanged() {
        if (internetConnectionWasLost()) {
            shoppingListInventory();
            setInternetConnectionWasLost(false);
        }

        if (addedToCart()) {
            addItemsToCart();
            addedToCartFail(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.SHOPPING_LIST_ITEMS);
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
    public void onToastButtonClicked(String currentState) {

    }

    public void addItemsToCart() {
        executeAddToCart(mShoppingListItems);
    }

    public void executeGetInventoryForStore(String storeId, String multiSku) {
        selectAllTextVisibility(false);
        mGetInventorySkusForStore = getInventoryStockForStore(storeId, multiSku);
    }

    private void selectAllTextVisibility(boolean visible) {
        if (selectDeselectAllTextView != null)
            selectDeselectAllTextView.setVisibility(visible ? VISIBLE : GONE);
    }

    public void getInventoryForStoreSuccess(SkusInventoryForStoreResponse skusInventoryForStoreResponse) {
        if (skusInventoryForStoreResponse.httpCode == HTTP_OK) {
            String fulFillmentType = null;
            for (FulfillmentStoreMap mapId : fulfillmentStoreMapArrayList) {
                if (mapId.getStoreID().equalsIgnoreCase(skusInventoryForStoreResponse.storeId) && !mapId.getInventoryCompletedForStore()) {
                    fulFillmentType = mapId.getTypeID();
                    mapId.setInventoryCompletedForStore(true);
                    break;
                }
            }

            matchesFullFillmentTypeKey.add(fulFillmentType);
            List<SkuInventory> skuInventory = skusInventoryForStoreResponse.skuInventory;
            // skuInventory is empty or null
            if (skuInventory.isEmpty()) {
                for (ShoppingListItem inventoryItems : mShoppingListItems) {
                    if (TextUtils.isEmpty(inventoryItems.fulfillmentType)) continue;
                    if (inventoryItems.fulfillmentType.equalsIgnoreCase(fulFillmentType)) {
                        inventoryItems.inventoryCallCompleted = true;
                        inventoryItems.quantityInStock = -1;
                    }
                }
            }

            if (skuInventory.size() > 0) {
                for (ShoppingListItem shoppingListItem : mShoppingListItems) {
                    if (shoppingListItem.fulfillmentType.equalsIgnoreCase(fulFillmentType)) {
                        String otherSkuId = shoppingListItem.catalogRefId;
                        shoppingListItem.inventoryCallCompleted = true;
                        shoppingListItem.quantityInStock = -1;
                        for (SkuInventory inventorySku : skusInventoryForStoreResponse.skuInventory) {
                            if (otherSkuId.equalsIgnoreCase(inventorySku.sku)) {
                                shoppingListItem.quantityInStock = inventorySku.quantity;
                                break;
                            }
                        }
                    }
                }
            }

            updateShoppingList();

            if (getLastValueInMap().equalsIgnoreCase(skusInventoryForStoreResponse.storeId)) {

                /***
                 * Triggered when "SELECT ALL" is selected from toolbar
                 * and no deliverable location found
                 * @params: allItemsAreOutOfStock returns true if one or more item
                 * is available
                 * tvMenuSelectAll.performClick() checked all available items
                 */
                if (getDeliveryResultCode() != null) {
                    setResultCode(null);
                    boolean allItemsAreOutOfStock = true;
                    for (ShoppingListItem shoppingListItem : shoppingListItemsAdapter.getShoppingListItems()) {
                        if (shoppingListItem.quantityInStock > 0) {
                            allItemsAreOutOfStock = false;
                            break;
                        }
                    }
                    if (!allItemsAreOutOfStock)
                        selectDeselectAllTextView.performClick();
                }

            }
        } else {
            // Hide quantity progress bar indicator
            if (mShoppingListItems.size() > 0) {
                for (ShoppingListItem shoppingListItem : mShoppingListItems) {
                    shoppingListItem.inventoryCallCompleted = true;
                    shoppingListItem.quantityInStock = -1;
                }
            }
            updateList();
            if (!errorMessageWasPopUp) {
                Activity activity = getActivity();
                if (activity == null) return;
                if (skusInventoryForStoreResponse.response == null) return;
                if (TextUtils.isEmpty(skusInventoryForStoreResponse.response.desc)) return;
                errorMessageWasPopUp = true;
                Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, skusInventoryForStoreResponse.response.desc);
            }
        }
    }

    private void updateShoppingList() {
        manageSelectAllMenuVisibility();
        updateList();
    }

    private void updateList() {
        enableAdapterClickEvent(true);
        if (shoppingListItemsAdapter != null)
            shoppingListItemsAdapter.updateList(mShoppingListItems);
    }

    public void geInventoryForStoreFailure(final String errorMessage) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mErrorHandlerView.showToast();
                    updateList();
                }
            });
        }
    }

    @Override
    public void openSetSuburbProcess(ShoppingListItem shoppingListItem) {
        this.mOpenShoppingListItem = shoppingListItem;
        navigateFromQuantity();
        deliverySelectionIntent(DELIVERY_LOCATION_REQUEST);
    }

    private void navigateFromQuantity() {
        WoolworthsApplication woolworthsApplication = WoolworthsApplication.getInstance();
        if (woolworthsApplication != null) {
            WGlobalState wGlobalState = woolworthsApplication.getWGlobalState();
            if (wGlobalState != null) {
                wGlobalState.navigateFromQuantity(QUANTITY_CHANGED_FROM_LIST);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // add to list from search result
        if (requestCode == PRODUCT_SEARCH_ACTIVITY_REQUEST_CODE && resultCode == ADDED_TO_SHOPPING_LIST_RESULT_CODE) {
            int count = data.getIntExtra("listItems", 0);
            ToastFactory.Companion.buildShoppingListFromSearchResultToast(getActivity(), rlCheckOut, listName, count);
            initGetShoppingListItems();
            return;
        }

        if (requestCode == DELIVERY_LOCATION_REQUEST && resultCode == RESULT_OK) { // on suburb selection successful
            makeInventoryCall();
        }

        if (requestCode == REQUEST_SUBURB_CHANGE) {
                initGetShoppingListItems();
        }

        if (requestCode == QUANTITY_CHANGED_FROM_LIST) {
            if (resultCode == QUANTITY_CHANGED_FROM_LIST) {
                Bundle bundleUpdatedQuantity = data.getExtras();
                int updatedQuantity = 0;
                if (bundleUpdatedQuantity != null) {
                    updatedQuantity = bundleUpdatedQuantity.getInt("QUANTITY_CHANGED_FROM_LIST");
                }
                if (updatedQuantity > 0) {
                    if (shoppingListItemsAdapter == null) return;
                    List<ShoppingListItem> shoppingListItems = shoppingListItemsAdapter.getShoppingListItems();
                    if (shoppingListItems == null) return;
                    for (ShoppingListItem shoppingListItem : shoppingListItems) {
                        if (shoppingListItem.catalogRefId == null) continue;
                        if (shoppingListItem.catalogRefId.equalsIgnoreCase(mOpenShoppingListItem.catalogRefId)) {
                            shoppingListItem.userQuantity = updatedQuantity;
                            shoppingListItem.isSelected = true;
                            shoppingListItemsAdapter.updateList(mShoppingListItems);
                        }
                    }
                }
            }
        }

        if (requestCode == DELIVERY_LOCATION_REQUEST_CODE_FROM_SELECT_ALL) {
            if (resultCode == RESULT_OK) { // on suburb selection successful
                setResultCode(DELIVERY_LOCATION_REQUEST_CODE_FROM_SELECT_ALL);
                makeInventoryCall();
            }
        }

        if (resultCode == RESULT_OK && requestCode == SET_DELIVERY_LOCATION_REQUEST_CODE) {
            startActivityToSelectDeliveryLocation(false);
        }
    }

    private void deliverySelectionIntent(int resultCode) {
        Activity activity = getActivity();
        if (activity == null) return;
        KotlinUtils.Companion.presentEditDeliveryLocationActivity(activity, resultCode, null);
    }

    private void startActivityToSelectDeliveryLocation(boolean addItemToCartOnFinished) {
        if (getActivity() != null) {
            if (addItemToCartOnFinished) {
                KotlinUtils.Companion.presentEditDeliveryLocationActivity(getActivity(), REQUEST_SUBURB_CHANGE, null);
            } else {
                KotlinUtils.Companion.presentEditDeliveryLocationActivity(getActivity(), 0, null);
            }
            getActivity().overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay);
        }
    }

    private String getLastValueInMap() {
        return (fulfillmentStoreMapArrayList == null) ? null : fulfillmentStoreMapArrayList.get(fulfillmentStoreMapArrayList.size() - 1).getStoreID();
    }

    private void setResultCode(Integer resultCode) {
        this.mDeliveryResultCode = resultCode;
    }

    public Integer getDeliveryResultCode() {
        return mDeliveryResultCode;
    }

    protected Call<AddItemToCartResponse> postAddItemToCart(List<AddItemToCart> addItemToCart) {
        onAddToCartPreExecute();
        addedToCartFail(false);

        PostItemToCart postItemToCart = new PostItemToCart();
        return postItemToCart.make(addItemToCart, new IResponseListener<AddItemToCartResponse>() {
            @Override
            public void onSuccess(AddItemToCartResponse addItemToCartResponse) {
                switch (addItemToCartResponse.httpCode) {
                    case HTTP_OK:
                        onAddToCartSuccess(addItemToCartResponse, addItemToCart.size());
                        break;

                    case HTTP_EXPECTATION_FAILED_417:
                        // Preferred Delivery Location has been reset on server
                        // As such, we give the user the ability to set their location again
                        if (addItemToCartResponse.response != null)
                            confirmDeliveryLocation();
                        break;

                    case HTTP_SESSION_TIMEOUT_440:
                        if (addItemToCartResponse.response != null)
                            onSessionTokenExpired(addItemToCartResponse.response);
                        break;

                    default:
                        if (addItemToCartResponse.response != null)
                            otherHttpCode(addItemToCartResponse.response);
                        break;
                }
                addedToCartFail(false);
            }

            @Override
            public void onFailure(Throwable error) {
                addedToCartFail(true);
                Activity activity = getActivity();
                if (activity == null || !isAdded()) return;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pbLoadingIndicator.setVisibility(GONE);
                        btnCheckOut.setVisibility(VISIBLE);
                    }
                });
            }
        });
    }

    public void addedToCartFail(boolean addedToCart) {
        this.addedToCart = addedToCart;
    }

    public boolean addedToCart() {
        return addedToCart;
    }


    public Call<SkusInventoryForStoreResponse> getInventoryStockForStore(String storeId, String multiSku) {
        setInternetConnectionWasLost(false);

        Call<SkusInventoryForStoreResponse> skusInventoryForStoreResponseCall = OneAppService.INSTANCE.getInventorySkuForStore(storeId, multiSku);
        skusInventoryForStoreResponseCall.enqueue(new CompletionHandler<>(new IResponseListener<SkusInventoryForStoreResponse>() {
            @Override
            public void onSuccess(SkusInventoryForStoreResponse skusInventoryForStoreResponse) {
                getInventoryForStoreSuccess(skusInventoryForStoreResponse);
            }

            @Override
            public void onFailure(Throwable error) {
                if (error == null) return;
                setInternetConnectionWasLost(true);
                geInventoryForStoreFailure(error.getMessage());
            }
        }, SkusInventoryForStoreResponse.class));

        return skusInventoryForStoreResponseCall;
    }

    public void setInternetConnectionWasLost(boolean internetConnectionWasLost) {
        this.internetConnectionWasLost = internetConnectionWasLost;
    }

    public boolean internetConnectionWasLost() {
        return internetConnectionWasLost;
    }

    @Override
    public void onToastButtonClicked(JsonElement jsonElement) {
        Activity activity = getActivity();
        if (activity != null) {
            Intent openCartActivity = new Intent(activity, CartActivity.class);
            startActivityForResult(openCartActivity, OPEN_CART_REQUEST);
            activity.overridePendingTransition(R.anim.anim_accelerate_in, R.anim.stay);
            activity.finish();
            activity.overridePendingTransition(0, 0);
        }
    }

    public boolean shouldUserSetSuburb() {
        ShoppingDeliveryLocation shoppingDeliveryLocation = Utils.getPreferredDeliveryLocation();
        return shoppingDeliveryLocation.suburb == null;
    }

    public void toggleEditButton(String name) {
        Activity activity = getActivity();
        if (activity == null || activity.getResources() == null) return;
        Resources resources = activity.getResources();
        mShoppingListItems = shoppingListItemsAdapter.getShoppingListItems();
        if (name.equalsIgnoreCase(resources.getString(R.string.edit))) {
            hideEditShoppingListMode();
            shoppingListItemsAdapter.editButtonEnabled(false);
            selectDeselectAllTextView.setEnabled(true);
            selectDeselectAllTextView.setVisibility(VISIBLE);
        } else {
            showEditShoppingListSetting();
            shoppingListItemsAdapter.editButtonEnabled(true);
            selectDeselectAllTextView.setEnabled(false);
            selectDeselectAllTextView.setVisibility(GONE);
        }

        resetSelectAll();
        selectDeselectAllTextView.setText(getString(getSelectAllMenuVisibility(mShoppingListItems) ? R.string.deselect_all : R.string.select_all));
        rlCheckOut.setVisibility(getButtonStatus(mShoppingListItems) ? VISIBLE : GONE);
    }

    private void resetSelectAll() {
        isMenuItemReadyToShow = false;
        for (ShoppingListItem shoppingListItem : mShoppingListItems) {
            if (shoppingListItem.quantityInStock > 0) {
                isMenuItemReadyToShow = true;
                break;
            }
        }
        if (!isMenuItemReadyToShow) {
            selectDeselectAllTextView.setVisibility(GONE);
        }
    }

    private void hideEditShoppingListMode() {
        selectDeselectAllTextView.setVisibility(VISIBLE);
        rlCheckOut.setEnabled(true);
        btnCheckOut.setEnabled(true);
        rlCheckOut.setAlpha(1.0f);
    }

    private void showEditShoppingListSetting() {
        selectDeselectAllTextView.setVisibility(GONE);
        rlCheckOut.setEnabled(false);
        btnCheckOut.setEnabled(false);
        rlCheckOut.setAlpha(0.5f);

    }

    private void editButtonVisibility() {
        Activity activity = getActivity();
        if (activity == null) return;
        ShoppingListDetailActivity shoppingListDetailActivity = (ShoppingListDetailActivity) activity;
        if (shoppingListItemsAdapter.getItemCount() >= 2) {
            shoppingListDetailActivity.editButtonVisibility(true);
        } else {
            shoppingListDetailActivity.editButtonVisibility(false);
        }
    }


    @Override
    public void onConfirmLocation() {
        addItemsToCart();
    }

    @Override
    public void onSetNewLocation() {
        KotlinUtils.Companion.presentEditDeliveryLocationActivity(this.getActivity(), REQUEST_SUBURB_CHANGE, null);
    }
}
