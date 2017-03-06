package za.co.woolworths.financial.services.android.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.util.binder.ContactUsFragmentChange;

/**
 * Created by W7099877 on 18/02/2017.
 */

public class ContactUsGeneralEnquiriesFragment extends Fragment implements View.OnClickListener {
    public ContactUsFragmentChange contactUsFragmentChange;
    private static final int REQUEST_CALL = 1;
    Intent callIntent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.contact_us_general_enquiries, container, false);
        view.findViewById(R.id.localCaller).setOnClickListener(this);
        view.findViewById(R.id.internationalCaller).setOnClickListener(this);
        view.findViewById(R.id.productQuery).setOnClickListener(this);
        view.findViewById(R.id.storeQuery).setOnClickListener(this);
        view.findViewById(R.id.complaints).setOnClickListener(this);
        view.findViewById(R.id.technicalProblem).setOnClickListener(this);
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
        contactUsFragmentChange.onFragmentChanged(getActivity().getResources().getString(R.string.txt_general_enquiry));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.localCaller:
                makeCall(getActivity().getResources().getString(R.string.customer_service_local_caller_number));
                break;
            case R.id.internationalCaller:
                makeCall(getActivity().getResources().getString(R.string.customer_service_inter_national_caller_number));
                break;
            case R.id.productQuery:
                sendEmail(getActivity().getResources().getString(R.string.email_custserv));
                break;
            case R.id.storeQuery:
                sendEmail(getActivity().getResources().getString(R.string.email_custserv));
                break;
            case R.id.complaints:
                sendEmail(getActivity().getResources().getString(R.string.email_custserv));
                break;
            case R.id.technicalProblem:
                sendEmail(getActivity().getResources().getString(R.string.email_custserv));
                break;
        }
    }
    public void makeCall(String number){
        callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        //Check for permission before calling
        //The app will ask permission before calling only on first use after installation
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions( new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
        } else {
            startActivity(callIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case REQUEST_CALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startActivity(callIntent);
                } else {
                    ////
                }
        }
    }

    public void sendEmail(String emailId)
    {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"+emailId));
        startActivity(emailIntent);
    }
}
