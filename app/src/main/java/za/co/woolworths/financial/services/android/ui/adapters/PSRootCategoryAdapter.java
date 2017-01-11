package za.co.woolworths.financial.services.android.ui.adapters;




import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.RootCategory;
import za.co.woolworths.financial.services.android.util.binder.EnumMapBindAdapter;
import za.co.woolworths.financial.services.android.util.binder.view.RootCategoryBinder;

public class PSRootCategoryAdapter extends EnumMapBindAdapter<PSRootCategoryAdapter.SampleViewType>  {

    private List<RootCategory> mRootCategories;

    enum SampleViewType {
        CBX_CONTENT
    }

    public PSRootCategoryAdapter(List<RootCategory> rootCategories) {
        this.mRootCategories = rootCategories;
        putBinder(SampleViewType.CBX_CONTENT, new RootCategoryBinder(this));
    }

    public void setCLIContent() {
        ((RootCategoryBinder) getDataBinder(SampleViewType.CBX_CONTENT)).addAll(mRootCategories);
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
