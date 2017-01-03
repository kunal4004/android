package za.co.woolworths.financial.services.android.ui.adapters;



import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.IncomeProof;
import za.co.woolworths.financial.services.android.util.binder.EnumMapBindAdapter;
import za.co.woolworths.financial.services.android.util.binder.view.CLIIncomeProofBinder;

public class CLIIncomeProofAdapter extends EnumMapBindAdapter<CLIIncomeProofAdapter.SampleViewType>  {

    private List<IncomeProof> mIncomeProof;

    enum SampleViewType {
        CBX_CONTENT
    }

    public CLIIncomeProofAdapter(List<IncomeProof> incomeProofs) {
        this.mIncomeProof = incomeProofs;
        putBinder(SampleViewType.CBX_CONTENT, new CLIIncomeProofBinder(this));
    }

    public void setCLIContent() {
        ((CLIIncomeProofBinder) getDataBinder(SampleViewType.CBX_CONTENT)).addAll(mIncomeProof);
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
