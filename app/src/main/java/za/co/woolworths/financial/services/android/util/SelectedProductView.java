package za.co.woolworths.financial.services.android.util;

import android.view.View;

import za.co.woolworths.financial.services.android.models.dto.ProductList;

public interface SelectedProductView {
    void onSelectedProduct(ProductList productList);
    void onSelectedProduct(View v, int position);
    void onLongPressState(View v, int position);
    void onSelectedColor(View v, int position);
}