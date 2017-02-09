package za.co.woolworths.financial.services.android.ui.adapters;


import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.util.SelectedProductView;
import za.co.woolworths.financial.services.android.util.binder.EnumMapBindAdapter;
import za.co.woolworths.financial.services.android.util.binder.view.ProductViewBinder;

public class ProductViewAdapter extends EnumMapBindAdapter<ProductViewAdapter.SampleViewType> {

    private final ProductViewBinder binder;
    private List<ProductList> mProductItem;

    enum SampleViewType {
        CBX_CONTENT
    }

    public ProductViewAdapter(List<ProductList> productItems, SelectedProductView selectedProductView) {
        this.mProductItem = productItems;
        putBinder(SampleViewType.CBX_CONTENT, new ProductViewBinder(this, selectedProductView));
        binder = getDataBinder(SampleViewType.CBX_CONTENT);
    }

    public void setCLIContent() {
        binder.addAll(mProductItem);
         notifyDataSetChanged();
    }

    public void refreshAdapter(List<ProductList>  moreProductList){
        binder.addAll(moreProductList);
        notifyDataSetChanged();
    }

    @Override
    public SampleViewType getEnumFromPosition(int position) {
        return SampleViewType.CBX_CONTENT;
    }

    @Override
    public SampleViewType getEnumFromOrdinal(int ordinal) {
        return SampleViewType.values()[ordinal];
    }
}
