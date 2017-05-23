package za.co.woolworths.financial.services.android.util;

import android.view.View;

public interface SelectedProductView {
    void onSelectedProduct(View v, int position);
    void onLongPressState(View v, int position);
    void onSelectedColor(View v, int position);
}