package za.co.woolworths.financial.services.android.ui.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import za.co.wigroup.logger.lib.WiGroupLogger;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Contact;
import za.co.woolworths.financial.services.android.models.dto.ContactUsConfigResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.WErrorDialog;

public class ContactUsActivity extends BaseDrawerActivity {
    private static final String TAG = "ContactUsActivity";
    public static final String APPLY = "apply";
    private List<Contact> mContactList = new ArrayList<Contact>();
    private ContactAdaptor mAdapter;
    private ProgressDialog mProgressDialog;
    private AlertDialog mError;
    private boolean newAccount;
    private AlertDialog mLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWoolworthsTitle(R.string.drawer_contact);
        setContentView(R.layout.contact_activity);
        mAdapter = new ContactAdaptor();
        if (getIntent().hasExtra(APPLY)) {
            newAccount = getIntent().getExtras().getBoolean(APPLY, false);
        }
        mLogin = WErrorDialog.getLoginErrorDialog(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(FontHyperTextParser.getSpannable(getString(R.string.contact_loading), 1, this));
        mProgressDialog.setCancelable(false);
        mError = WErrorDialog.getSingleActionActivityErrorDialog(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        findViewById(R.id.contact_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + ((TextView) findViewById(R.id.contact_us_call_centre_number)).getText().toString()));
                    startActivity(callIntent);
                } catch (ActivityNotFoundException e) {
                    WiGroupLogger.e(ContactUsActivity.this, TAG, "failed to call support", e);
                }
            }
        });

        new HttpAsyncTask<String, String, ContactUsConfigResponse>() {

            @Override
            protected void onPreExecute() {
                mProgressDialog.show();
            }

            @Override
            protected Class<ContactUsConfigResponse> httpDoInBackgroundReturnType() {
                return ContactUsConfigResponse.class;
            }

            @Override
            protected ContactUsConfigResponse httpDoInBackground(String... params) {
                return ((WoolworthsApplication) getApplication()).getApi().getContactUsConfig();
            }

            @Override
            protected ContactUsConfigResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {

                WiGroupLogger.e(ContactUsActivity.this, TAG, errorMessage);
                ContactUsConfigResponse configResponse = new ContactUsConfigResponse();
                configResponse.httpCode = 408;
                configResponse.response = new Response();
                configResponse.response.desc = getString(R.string.err_002);
                return configResponse;
            }

            @Override
            protected void onPostExecute(ContactUsConfigResponse contactUsConfigResponse) {

                switch (contactUsConfigResponse.httpCode) {
                    case 200:
                        ArrayList<Contact> contacts = new ArrayList<Contact>();
                        List<Contact> contactList = contactUsConfigResponse.contactList;
                        for (int i = 0; i < contactList.size(); i++) {
                            Contact c = contactList.get(i);
                            if ("EMAIL".equals(c.contactType)) {
                                contacts.add(c);
                                if (newAccount && "SALES".equals(c.id)) {
                                    String to = c.contactValue;
                                    String subject = c.subject;
                                    StringBuilder builder = new StringBuilder("mailto:" + Uri.encode(to));
                                    builder.append("?subject=");
                                    builder.append(Uri.encode(Uri.encode(subject)));
                                    builder.append("&body=");
                                    builder.append(Uri.encode(Uri.encode(getString(R.string.new_account_body))));
                                    String uri = builder.toString();
                                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri));
                                    startActivity(intent);
                                }
                            } else {
                                //Fixed WFS-110
                                String callCentreNumberFormatted = String.format("%1$s", c.subject);
                                ((TextView) findViewById(R.id.contact_us_call_centre_number)).setText(callCentreNumberFormatted);
                            }
                        }
                        mContactList = contacts;
                        ListView clist =((ListView) findViewById(R.id.contactList));
                        clist.setAdapter(mAdapter);
                        clist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Contact selectedItem = mContactList.get(i);
                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{selectedItem.contactValue});
                                intent.putExtra(Intent.EXTRA_SUBJECT, selectedItem.subject);
                                //if (intent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(intent);
                               // }
                            }
                        });
                        break;
                    case 400:
                        if ("0619".equals(contactUsConfigResponse.response.code) || "0618".equals(contactUsConfigResponse.response.code)) {
                            mLogin.show();
                            break;
                        }
                    default:
                        mError.setMessage(FontHyperTextParser.getSpannable(contactUsConfigResponse.response.desc.toUpperCase(), 1, ContactUsActivity.this));
                        mError.show();
                }
                mProgressDialog.dismiss();
            }
        }.execute();
    }

    @Override
    protected void onPause() {
        mProgressDialog.dismiss();
        mError.dismiss();
        mLogin.dismiss();
        super.onPause();
    }

    @Override
    protected void setDrawerTitleClosed() {
        setWoolworthsTitle(R.string.drawer_contact);
    }

    private class ContactSpinnerAdaptor implements SpinnerAdapter {
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.subject_layout, null);
            }
            ((TextView) convertView.findViewById(R.id.subject_text_view)).setText(getItem(position).subject);
            return convertView;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public int getCount() {
            return mContactList.size();
        }

        @Override
        public Contact getItem(int position) {
            return mContactList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getDropDownView(position, convertView, parent);
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return getCount() == 0;
        }
    }

    private class ContactAdaptor extends BaseAdapter{

        @Override
        public int getCount() {
             return mContactList == null? 0: mContactList.size();
        }

        @Override
        public Contact getItem(int i) {
            return mContactList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.contact_us_item, null);
            }
            ((TextView) convertView.findViewById(R.id.contact_us_item_title)).setText(getItem(i).subject);
            return convertView;
        }
    }
}
