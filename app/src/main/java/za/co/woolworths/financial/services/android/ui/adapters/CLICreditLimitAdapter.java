package za.co.woolworths.financial.services.android.ui.adapters;



import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.CreditLimit;
import za.co.woolworths.financial.services.android.util.binder.EnumMapBindAdapter;
import za.co.woolworths.financial.services.android.util.binder.view.CLICreditLimitContentBinder;

public class CLICreditLimitAdapter extends EnumMapBindAdapter<CLICreditLimitAdapter.SampleViewType>  {

    private List<CreditLimit> mCreditLimit;

    enum SampleViewType {
        CBX_CONTENT
    }

    public CLICreditLimitAdapter(List<CreditLimit> creditLimit, CLICreditLimitContentBinder.OnClickListener onClickListener) {
        this.mCreditLimit = creditLimit;
        putBinder(SampleViewType.CBX_CONTENT, new CLICreditLimitContentBinder(this,onClickListener));
    }

    public void setCLIContent() {
        ((CLICreditLimitContentBinder) getDataBinder(SampleViewType.CBX_CONTENT)).addAll(mCreditLimit);
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
