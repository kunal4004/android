package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.binder.ContactUsFragmentChange;

/**
 * Created by W7099877 on 01/12/2016.
 */

public class ContactUsMySchoolFragment extends Fragment implements View.OnClickListener {
    public ContactUsFragmentChange contactUsFragmentChange;
    private static final int REQUEST_CALL = 1;
    Intent callIntent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact_us_myschool, container, false);
        view.findViewById(R.id.localCaller).setOnClickListener(this);
        view.findViewById(R.id.mySchoolCard).setOnClickListener(this);
        view.findViewById(R.id.generalEnq).setOnClickListener(this);
        view.findViewById(R.id.complaints).setOnClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            contactUsFragmentChange = (ContactUsFragmentChange) getActivity();
        } catch (ClassCastException ex) {
            Log.e("Interface", ex.toString());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        contactUsFragmentChange.onFragmentChanged(getActivity().getResources().getString(R.string.contact_us_myschool));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.localCaller:
                Utils.makeCall(getActivity(),getActivity().getResources().getString(R.string.my_school_local_caller_number));
                break;
            case R.id.mySchoolCard:
                sendEmail(getActivity().getResources().getString(R.string.email_myschool),getActivity().getResources().getString(R.string.txt_myschool_card));
                break;
            case R.id.generalEnq:
                sendEmail(getActivity().getResources().getString(R.string.email_myschool),getActivity().getResources().getString(R.string.txt_general_enquiry));
                break;
            case R.id.complaints:
                sendEmail(getActivity().getResources().getString(R.string.email_myschool),getActivity().getResources().getString(R.string.txt_complaint));
                break;
        }
    }

    public void sendEmail(String emailId, String subject) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + emailId +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode("")));

        PackageManager pm = getActivity().getPackageManager();
        List<ResolveInfo> listOfEmail = pm.queryIntentActivities(emailIntent, 0);
        if (listOfEmail.size() > 0) {
            startActivity(emailIntent);
        } else {
            Utils.displayValidationMessage(getActivity(),
                    CustomPopUpWindow.MODAL_LAYOUT.INFO,
                    getActivity().getResources().getString(R.string.contact_us_no_email_error)
                            .replace("email_address", emailId).replace("subject_line",subject));
        }
    }
}

