package za.co.woolworths.financial.services.android.ui.adapters;


import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.FAQDetail;
import za.co.woolworths.financial.services.android.util.binder.EnumMapBindAdapter;
import za.co.woolworths.financial.services.android.util.binder.view.FAQTypeBinder;

public class FAQAdapter extends EnumMapBindAdapter<FAQAdapter.SampleViewType> {

    private List<FAQDetail> mFaqDetails;

    enum SampleViewType {
        CBX_CONTENT
    }

    public FAQAdapter(List<FAQDetail> faqDetails, FAQTypeBinder.SelectedQuestion selectedQuestion) {
        this.mFaqDetails = faqDetails;
        putBinder(SampleViewType.CBX_CONTENT, new FAQTypeBinder(this, selectedQuestion));
    }

    public void setCLIContent() {
        ((FAQTypeBinder) getDataBinder(SampleViewType.CBX_CONTENT)).addAll(mFaqDetails);
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
