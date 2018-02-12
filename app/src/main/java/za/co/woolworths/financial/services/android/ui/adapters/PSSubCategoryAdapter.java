package za.co.woolworths.financial.services.android.ui.adapters;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.SubCategory;
import za.co.woolworths.financial.services.android.util.binder.EnumMapBindAdapter;
import za.co.woolworths.financial.services.android.util.binder.view.SubCategoryBinder;

public class PSSubCategoryAdapter extends EnumMapBindAdapter<PSSubCategoryAdapter.SampleViewType> {

    private List<SubCategory> mSubCategory;

    enum SampleViewType {
        CBX_CONTENT
    }

    public PSSubCategoryAdapter(List<SubCategory> subCategories, SubCategoryBinder.OnClickListener click) {
        this.mSubCategory = subCategories;
        putBinder(SampleViewType.CBX_CONTENT, new SubCategoryBinder(this, click));
    }

    public void setCLIContent() {
        ((SubCategoryBinder) getDataBinder(SampleViewType.CBX_CONTENT)).addAll(mSubCategory);
    }

    public void resetSelectedIndex() {
        ((SubCategoryBinder) getDataBinder(SampleViewType.CBX_CONTENT)).resetSelectedItemBg();
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
