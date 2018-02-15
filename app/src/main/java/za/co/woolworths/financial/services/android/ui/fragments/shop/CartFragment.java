package za.co.woolworths.financial.services.android.ui.fragments.shop;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Random;

import retrofit.RetrofitError;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.CartItemGroup;
import za.co.woolworths.financial.services.android.models.dto.CartPriceValues;
import za.co.woolworths.financial.services.android.models.dto.CartProduct;
import za.co.woolworths.financial.services.android.models.dto.CartResponse;
import za.co.woolworths.financial.services.android.models.dto.OrderSummary;
import za.co.woolworths.financial.services.android.models.dto.PriceInfo;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.ui.activities.CartActivity;
import za.co.woolworths.financial.services.android.ui.activities.DeliveryLocationSelectionActivity;
import za.co.woolworths.financial.services.android.ui.adapters.CartProductAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.CancelableCallback;


public class CartFragment extends Fragment implements CartProductAdapter.OnItemClick, View.OnClickListener {

	private RecyclerView rvCartList;
	private WButton btnAddToCart;

	private CartProductAdapter cartProductAdapter;
	private WoolworthsApplication mWoolWorthsApplication;
	private RelativeLayout parentLayout;
	private ProgressBar pBar;
	private WTextView txtEmptyStateDesc;
	private ArrayList<CartItemGroup> cartItems;
	private OrderSummary orderSummary;

	public CartFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_cart, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		rvCartList = view.findViewById(R.id.cartList);
		btnAddToCart = view.findViewById(R.id.btnAddToCart);
		parentLayout = view.findViewById(R.id.parentLayout);
		pBar = view.findViewById(R.id.loadingBar);
		txtEmptyStateDesc = view.findViewById(R.id.txtEmptyStateDesc);
		mWoolWorthsApplication = ((WoolworthsApplication) getActivity().getApplication());
		view.findViewById(R.id.locationSelectedLayout).setOnClickListener(this);

		Activity activity = getActivity();
		if (activity != null) {
			CartActivity cartActivity = (CartActivity) activity;
			cartActivity.hideEditCart();
		}

		loadShoppingCart();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.locationSelectedLayout:
				locationSelectionClicked();
				break;
		}
	}

	@Override
	public void onItemClick(View view, int position) {
		Log.i("CartFragment", "Item #" + position + " clicked!");
	}

	@Override
	public void onItemDeleteClick(String productId) {
		// Log.i("CartFragment", "Item " + itemRow.productItem.productName + " delete button clicked!");

		// TODO: Make API call to remove item + show loading before removing from list
		removeCartItem(productId);
	}

	public boolean toggleEditMode() {
		boolean isEditMode = cartProductAdapter.toggleEditMode();
		btnAddToCart.setVisibility(isEditMode ? View.GONE : View.VISIBLE);
		return isEditMode;
	}

	private void locationSelectionClicked() {
		startActivity(new Intent(this.getContext(), DeliveryLocationSelectionActivity.class));
		this.getActivity().overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay);
	}

	public void loadShoppingCart() {
		pBar.setVisibility(View.VISIBLE);
		parentLayout.setVisibility(View.GONE);
		mWoolWorthsApplication.getAsyncApi().getShoppingCart(new CancelableCallback<String>() {
			@Override
			public void onSuccess(String s, retrofit.client.Response response) {
				pBar.setVisibility(View.GONE);
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
			public void onFailure(RetrofitError error) {
				Log.i("result ", "failed");
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
			btnAddToCart.setVisibility(View.VISIBLE);
			Activity activity = getActivity();
			if (activity != null) {
				CartActivity cartActivity = (CartActivity) activity;
				cartActivity.showEditCart();
			}
			cartItems=cartResponse.cartItems;
			orderSummary=cartResponse.orderSummary;

			cartProductAdapter = new CartProductAdapter(cartItems, this, orderSummary, getActivity());
			LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
			mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
			rvCartList.setLayoutManager(mLayoutManager);
			rvCartList.setAdapter(cartProductAdapter);
		} else {
			btnAddToCart.setVisibility(View.GONE);
			txtEmptyStateDesc.setVisibility(View.VISIBLE);
		}
	}

	public void removeCartItem(String productId)
	{
		mWoolWorthsApplication.getAsyncApi().removeCartItem(productId,new CancelableCallback<String>() {
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

	public void updateCart(CartResponse cartResponse)
	{
		if (cartResponse.cartItems.size() > 0 && cartProductAdapter!=null) {
			cartItems=cartResponse.cartItems;
			orderSummary=cartResponse.orderSummary;
			cartProductAdapter.removeItem(cartItems,orderSummary);

		}else {

		}
	}
}
