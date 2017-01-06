package za.co.woolworths.financial.services.android.ui.adapters;



import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.BankAccountType;
import za.co.woolworths.financial.services.android.util.binder.EnumMapBindAdapter;
import za.co.woolworths.financial.services.android.util.binder.view.CLIBankAccountTypeBinder;

public class CLIBankAccountTypeAdapter extends EnumMapBindAdapter<CLIBankAccountTypeAdapter.SampleViewType>  {

    private List<BankAccountType> mListBankAccountType;

    enum SampleViewType {
        CBX_CONTENT
    }

    public CLIBankAccountTypeAdapter(List<BankAccountType> bankAccountType,CLIBankAccountTypeBinder.OnCheckboxClickListener onCheckboxClickListener) {
        this.mListBankAccountType = bankAccountType;
        putBinder(SampleViewType.CBX_CONTENT, new CLIBankAccountTypeBinder(this,onCheckboxClickListener));
    }

    public void setCLIContent() {
        ((CLIBankAccountTypeBinder) getDataBinder(SampleViewType.CBX_CONTENT)).addAll(mListBankAccountType);
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
