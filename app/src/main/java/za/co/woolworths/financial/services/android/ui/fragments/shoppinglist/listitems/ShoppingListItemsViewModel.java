package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems;

import android.app.Activity;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.AddItemToCart;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse;
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse;
import za.co.woolworths.financial.services.android.models.rest.product.GetCartSummary;
import za.co.woolworths.financial.services.android.models.rest.product.GetInventorySkusForStore;
import za.co.woolworths.financial.services.android.models.rest.product.PostAddItemToCart;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.DeleteShoppingListItem;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.GetShoppingListItems;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

/**
 * Created by W7099877 on 2018/03/08.
 */


public class ShoppingListItemsViewModel extends BaseViewModel<ShoppingListItemsNavigator> {
	private boolean addedToCart;
	private boolean internetConnectionWasLost = false;


	public ShoppingListItemsViewModel() {
		super();
	}

	public ShoppingListItemsViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}

	public GetShoppingListItems getShoppingListItems(String listId) {
		return new GetShoppingListItems(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				ShoppingListItemsResponse shoppingListItemsResponse = (ShoppingListItemsResponse) object;
				getNavigator().onShoppingListItemsResponse(shoppingListItemsResponse);
			}

			@Override
			public void onFailure(String e) {
				getNavigator().onGetListFailure(e);
			}
		}, listId);
	}

	public DeleteShoppingListItem deleteShoppingListItem(String listId, String id, String productId, String catalogRefId) {
		return new DeleteShoppingListItem(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				ShoppingListItemsResponse shoppingListItemsResponse = (ShoppingListItemsResponse) object;
				getNavigator().onShoppingListItemDelete(shoppingListItemsResponse);
			}

			@Override
			public void onFailure(String e) {
				getNavigator().onDeleteItemFailed();
			}
		}, listId, id, productId, catalogRefId);
	}

	protected PostAddItemToCart postAddItemToCart(List<AddItemToCart> addItemToCart) {
		getNavigator().onAddToCartPreExecute();
		addedToCartFail(false);
		return new PostAddItemToCart(addItemToCart, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				if (object != null) {
					AddItemToCartResponse addItemToCartResponse = (AddItemToCartResponse) object;
					if (addItemToCartResponse != null) {
						switch (addItemToCartResponse.httpCode) {
							case 200:
								getNavigator().onAddToCartSuccess(addItemToCartResponse);
								break;

							case 440:
								if (addItemToCartResponse.response != null)
									getNavigator().onSessionTokenExpired(addItemToCartResponse.response);
								break;

							default:
								if (addItemToCartResponse.response != null)
									getNavigator().otherHttpCode(addItemToCartResponse.response);
								break;
						}
						addedToCartFail(false);
					}
				}
			}

			@Override
			public void onFailure(String e) {
				addedToCartFail(true);
				getNavigator().onAddItemToCartFailure(e);
			}
		});
	}


	protected GetCartSummary getCartSummary(Activity activity) {
		addedToCartFail(false);
		getNavigator().onAddToCartLoad();
		return new GetCartSummary(activity, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				if (object != null) {
					CartSummaryResponse cartSummaryResponse = (CartSummaryResponse) object;
					if (cartSummaryResponse != null) {
						switch (cartSummaryResponse.httpCode) {
							case 200:
								getNavigator().onCartSummarySuccess(cartSummaryResponse);
								break;

							case 440:
								if (cartSummaryResponse.response != null)
									getNavigator().onCartSummaryExpiredSession(cartSummaryResponse.response);
								break;

							default:
								getNavigator().onCartSummaryOtherHttpCode(cartSummaryResponse.response);
								break;
						}
					}
				}
				addedToCartFail(false);
			}

			@Override
			public void onFailure(String e) {
				addedToCartFail(true);
				getNavigator().onTokenFailure(e);
			}
		});
	}

	public void addedToCartFail(boolean addedToCart) {
		this.addedToCart = addedToCart;
	}

	public boolean addedToCart() {
		return addedToCart;
	}


	public GetInventorySkusForStore getInventoryStockForStore(String storeId, String multiSku) {
		setInternetConnectionWasLost(false);
		return new GetInventorySkusForStore(storeId, multiSku, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				SkusInventoryForStoreResponse skusInventoryForStoreResponse = (SkusInventoryForStoreResponse) object;
				getNavigator().getInventoryForStoreSuccess(skusInventoryForStoreResponse);
			}

			@Override
			public void onFailure(String e) {
				setInternetConnectionWasLost(true);
				getNavigator().geInventoryForStoreFailure(e);
			}
		});
	}

	public void setInternetConnectionWasLost(boolean internetConnectionWasLost) {
		this.internetConnectionWasLost = internetConnectionWasLost;
	}

	public boolean internetConnectionWasLost() {
		return internetConnectionWasLost;
	}
}
