package za.co.woolworths.financial.services.android.ui.adapters;



import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Bank;
import za.co.woolworths.financial.services.android.util.binder.EnumMapBindAdapter;
import za.co.woolworths.financial.services.android.util.binder.view.CLICbxContentBinder;

public class CLIDeaBankMapAdapter extends EnumMapBindAdapter<CLIDeaBankMapAdapter.SampleViewType>  {

    private List<Bank> mCLIBank;

    enum SampleViewType {
        CBX_CONTENT
    }

    public CLIDeaBankMapAdapter(List<Bank> bank, CLICbxContentBinder.OnCheckboxClickListener onCheckboxClickListener) {
        this.mCLIBank = bank;
        putBinder(SampleViewType.CBX_CONTENT, new CLICbxContentBinder(this,onCheckboxClickListener));
    }

    public void setCLIContent() {
        ((CLICbxContentBinder) getDataBinder(SampleViewType.CBX_CONTENT)).addAll(mCLIBank);
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
