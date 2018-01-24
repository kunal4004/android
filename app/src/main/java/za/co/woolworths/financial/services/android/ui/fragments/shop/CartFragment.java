package za.co.woolworths.financial.services.android.ui.fragments.shop;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import za.co.woolworths.financial.services.android.models.dto.CartPriceValues;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.ui.adapters.CartProductAdapter;


public class CartFragment extends Fragment implements CartProductAdapter.OnItemClick {

    private RecyclerView rvCartList;

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
        rvCartList = (RecyclerView) view.findViewById(R.id.cartList);

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

            for (int idxProductItem = 0; idxProductItem < 5; idxProductItem++) {
                ProductList dummyProductItem = new ProductList();
                dummyProductItem.productName = "Item #" + (idxProductItem + 1) + " from category #" + (idxProductCategory + 1);
                dummyProductItem.productId = idxProductItem + "";
                productItems.add(dummyProductItem);
            }

            productCategoryItems.put("Category #" + (idxProductCategory + 1), productItems);
        }

        return productCategoryItems;
    }

    @Override
    public void onItemClick(View view, int position) {

    }
}
