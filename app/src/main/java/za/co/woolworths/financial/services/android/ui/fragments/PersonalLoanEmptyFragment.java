package za.co.woolworths.financial.services.android.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

public class PersonalLoanEmptyFragment extends BaseAccountFragment {
    @Override
    public int getTitle() {
        return R.string.personal_loan;
    }

    @Override
    public int getTabColor() {
        return R.color.personal_loan;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.personal_loan_empty_fragment, null);
    }
}
