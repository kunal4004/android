package za.co.woolworths.financial.services.android.ui.fragments.shop;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

import za.co.woolworths.financial.services.android.models.dto.CartPriceValues;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.ui.adapters.CartProductAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;


public class CartFragment extends Fragment implements CartProductAdapter.OnItemClick {

    private RecyclerView rvCartList;
    private WButton btnAddToCart;

    private CartProductAdapter cartProductAdapter;

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

        CartPriceValues prices = new CartPriceValues(13,1185, 50, -36.56, -100, -18, -25, 1199);
        cartProductAdapter = new CartProductAdapter(getCartProductItems(), prices, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvCartList.setLayoutManager(mLayoutManager);
        rvCartList.setAdapter(cartProductAdapter);
    }

    private HashMap<String, ArrayList<ProductList>> getCartProductItems() {

        LinkedHashMap<String, ArrayList<ProductList>> productCategoryItems = new LinkedHashMap<>();

        for (int idxProductCategory = 0; idxProductCategory < 3; idxProductCategory++) {
            ArrayList<ProductList> productItems = new ArrayList<>();

            int count = new Random().nextInt(4) + 2;
            for (int idxProductItem = 0; idxProductItem < count; idxProductItem++) {
                ProductList dummyProductItem = new ProductList();
                dummyProductItem.productName = "Product Name";
                dummyProductItem.productId = idxProductItem + "";
                productItems.add(dummyProductItem);
            }

            switch (idxProductCategory) {
                case 0:
                    productCategoryItems.put("Food", productItems);
                    break;
                case 1:
                    productCategoryItems.put("Clothing", productItems);
                    break;
                case 2:
                    productCategoryItems.put("Home", productItems);
                    break;
            }
        }

        return productCategoryItems;
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.i("CartFragment", "Item #" + position + " clicked!");
    }

    @Override
    public void onItemDeleteClick(CartProductAdapter.CartProductItemRow itemRow) {
        Log.i("CartFragment", "Item " + itemRow.productItem.productName + " delete button clicked!");

        // TODO: Make API call to remove item + show loading before removing from list
        cartProductAdapter.removeItem(itemRow);
    }

    public boolean toggleEditMode() {
        boolean isEditMode = cartProductAdapter.toggleEditMode();
        btnAddToCart.setVisibility(isEditMode ? View.GONE : View.VISIBLE);
        return isEditMode;
    }
}
