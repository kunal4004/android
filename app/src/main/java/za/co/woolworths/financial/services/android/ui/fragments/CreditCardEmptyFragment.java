package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.activities.WebViewActivity;

public class CreditCardEmptyFragment extends BaseAccountFragment {
    @Override
    public int getTitle() {
        return R.string.credit_card;
    }

    @Override
    public int getTabColor() {
        return R.color.credit_card;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.credit_card_empty_fragment, null);
    }


}
