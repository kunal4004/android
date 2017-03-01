package za.co.woolworths.financial.services.android.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.ui.activities.StoreDetailsActivity;
import za.co.woolworths.financial.services.android.ui.activities.TransientActivity;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.binder.ContactUsFragmentChange;

/**
 * Created by W7099877 on 01/12/2016.
 */

public class ContactUsFinancialServiceFragment extends Fragment implements View.OnClickListener {
    public ContactUsFragmentChange contactUsFragmentChange;
    private static final int REQUEST_CALL = 1;
    Intent callIntent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact_us_financial_services, container, false);
        view.findViewById(R.id.localCaller).setOnClickListener(this);
        view.findViewById(R.id.internationalCaller).setOnClickListener(this);
        view.findViewById(R.id.blackCrediCardQuery).setOnClickListener(this);
        view.findViewById(R.id.complains).setOnClickListener(this);
        view.findViewById(R.id.creditCardQuery).setOnClickListener(this);
        view.findViewById(R.id.wRewardsQuery).setOnClickListener(this);
        view.findViewById(R.id.insuranceClaim).setOnClickListener(this);
        view.findViewById(R.id.paymentQuery).setOnClickListener(this);
        view.findViewById(R.id.storeCardPesonalLoanQuery).setOnClickListener(this);
        view.findViewById(R.id.proofOfIncome).setOnClickListener(this);
        view.findViewById(R.id.technical).setOnClickListener(this);

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
        contactUsFragmentChange.onFragmentChanged(getActivity().getResources().getString(R.string.contact_us_financial_services));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.localCaller:
                makeCall(getActivity().getResources().getString(R.string.fs_local_caller_number));
                break;
            case R.id.internationalCaller:
                makeCall(getActivity().getResources().getString(R.string.fs_inter_national_caller_number));
                break;
            case R.id.blackCrediCardQuery:
                sendEmail(getActivity().getResources().getString(R.string.email_black_credit_card_query), getActivity().getResources().getString(R.string.txt_black_credit_card_query));
                break;
            case R.id.complains:
                sendEmail(getActivity().getResources().getString(R.string.email_complaints), getActivity().getResources().getString(R.string.txt_complaint));
                break;
            case R.id.creditCardQuery:
                sendEmail(getActivity().getResources().getString(R.string.email_credicard_query), getActivity().getResources().getString(R.string.txt_cc_query));
                break;
            case R.id.wRewardsQuery:
                sendEmail(getActivity().getResources().getString(R.string.email_wrewards_query), getActivity().getResources().getString(R.string.txt_wrewards_query));
                break;
            case R.id.insuranceClaim:
                sendEmail(getActivity().getResources().getString(R.string.email_insurance_claim), getActivity().getResources().getString(R.string.txt_insurance_claim));
                break;
            case R.id.paymentQuery:
                sendEmail(getActivity().getResources().getString(R.string.email_payment_query), getActivity().getResources().getString(R.string.txt_payment_query));
                break;
            case R.id.storeCardPesonalLoanQuery:
                sendEmail(getActivity().getResources().getString(R.string.email_sc_and_pl_query), getActivity().getResources().getString(R.string.txt_sc_and_pl_query));
                break;
            case R.id.proofOfIncome:
                sendEmail(getActivity().getResources().getString(R.string.email_proof_of_income), getActivity().getResources().getString(R.string.txt_proof_of_income));
                break;
            case R.id.technical:
                sendEmail(getActivity().getResources().getString(R.string.email_technical), getActivity().getResources().getString(R.string.txt_technical_problem));
                break;
        }
    }


    public void makeCall(String number) {
        callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        //Check for permission before calling
        //The app will ask permission before calling only on first use after installation
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
        } else {
            startActivity(callIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(callIntent);
                } else {
                    ////
                }
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
                    TransientActivity.VALIDATION_MESSAGE_LIST.INFO,
                    getActivity().getResources().getString(R.string.contact_us_no_email_error)
                            .replace("email_address", emailId).replace("subject_line",subject));
        }
    }
}
