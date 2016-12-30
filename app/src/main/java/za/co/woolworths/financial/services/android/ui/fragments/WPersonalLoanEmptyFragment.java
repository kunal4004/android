package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Defaults;
import za.co.woolworths.financial.services.android.ui.views.WButton;

/**
 * Created by W7099877 on 30/11/2016.
 */

public class WPersonalLoanEmptyFragment extends Fragment{

    public WButton applyNow;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.logged_out_state_personal_loan, container, false);
        applyNow=(WButton)view.findViewById(R.id.applynow);
        applyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WoolworthsApplication.getFaqLink())));
            }
        });


        return view;    }
}
