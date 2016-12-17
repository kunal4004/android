package za.co.woolworths.financial.services.android.ui.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import za.co.wigroup.logger.lib.WiGroupLogger;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.ui.activities.AccountsActivity;
import za.co.woolworths.financial.services.android.ui.activities.MessagesActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivityTest;
import za.co.woolworths.financial.services.android.ui.activities.WContactUsActivity;
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity;
import za.co.woolworths.financial.services.android.ui.adapters.MyAccountOverViewPagerAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.MyAccountsCardsAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.TestViewpagerAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WCustomViewPager;
import za.co.woolworths.financial.services.android.util.WFormatter;

import static android.R.attr.id;
import static com.google.android.gms.plus.PlusOneDummyView.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyAccountsFragment extends Fragment implements View.OnClickListener,ViewPager.OnPageChangeListener {

    ImageView openMessageActivity;
    ImageView openShoppingList;
    RelativeLayout contactUs;
    boolean isLoggedIn = false;
    boolean isCreditCard = false;
    boolean isStoreCard = false;
    boolean isPersonalCard = false;
    CardView applyCreditCardView;
    CardView applyStoreCardView;
    CardView applyPersonalCardView;
    CardView linkedCreditCardView;
    CardView linkedStoreCardView;
    CardView linkedPersonalCardView;
    LinearLayout linkedAccountsLayout;
    LinearLayout applyNowAccountsLayout;
    LinearLayout loggedOutHeaderLayout;
    LinearLayout loggedInHeaderLayout;
    WButton linkAccountsBtn;
    RelativeLayout signOutBtn;
    ViewPager viewPager;
    MyAccountOverViewPagerAdapter adapter;
    LinearLayout pager_indicator;

    WTextView sc_available_funds;
    WTextView cc_available_funds;
    WTextView pl_available_funds;
    WTextView messageCounter;

    private ProgressDialog mGetAccountsProgressDialog;
    private ProgressBar scProgressBar;
    private ProgressBar ccProgressBar;
    private ProgressBar plProgressBar;

    public AccountsResponse accounts=null;


    private int dotsCount;
    private ImageView[] dots;
    public MyAccountsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_accounts_fragment, container, false);
        // ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("ACCOUNTS");
        openMessageActivity = (ImageView) view.findViewById(R.id.openMessageActivity);
        openShoppingList=(ImageView) view.findViewById(R.id.openShoppingList);
        contactUs = (RelativeLayout) view.findViewById(R.id.contactUs);
        applyStoreCardView = (CardView) view.findViewById(R.id.applyStoreCard);
        applyCreditCardView = (CardView) view.findViewById(R.id.applyCrediCard);
        applyPersonalCardView = (CardView) view.findViewById(R.id.applyPersonalLoan);
        linkedCreditCardView = (CardView) view.findViewById(R.id.linkedCrediCard);
        linkedStoreCardView = (CardView) view.findViewById(R.id.linkedStoreCard);
        linkedPersonalCardView = (CardView) view.findViewById(R.id.linkedPersonalLoan);
        linkedAccountsLayout = (LinearLayout) view.findViewById(R.id.linkedLayout);
        applyNowAccountsLayout = (LinearLayout) view.findViewById(R.id.applyNowLayout);
        loggedOutHeaderLayout = (LinearLayout) view.findViewById(R.id.loggedOutHeaderLayout);
        loggedInHeaderLayout = (LinearLayout) view.findViewById(R.id.loggedInHeaderLayout);
        linkAccountsBtn=(WButton) view.findViewById(R.id.linkAccountsBtn);
        signOutBtn=(RelativeLayout)view.findViewById(R.id.signOutBtn);
        viewPager=(ViewPager)view.findViewById(R.id.pager) ;
        pager_indicator = (LinearLayout) view.findViewById(R.id.viewPagerCountDots);
        sc_available_funds=(WTextView)view.findViewById(R.id.sc_available_funds);
        cc_available_funds=(WTextView)view.findViewById(R.id.cc_available_funds);
        pl_available_funds=(WTextView)view.findViewById(R.id.pl_available_funds);
        scProgressBar=(ProgressBar)view.findViewById(R.id.scProgressBar);
        ccProgressBar=(ProgressBar)view.findViewById(R.id.ccProgressBar);
        plProgressBar=(ProgressBar)view.findViewById(R.id.plProgressBar);
        messageCounter=(WTextView)view.findViewById(R.id.messageCounter);

        openMessageActivity.setOnClickListener(this);
        contactUs.setOnClickListener(this);
        applyPersonalCardView.setOnClickListener(this);
        applyStoreCardView.setOnClickListener(this);
        applyCreditCardView.setOnClickListener(this);
        linkedStoreCardView.setOnClickListener(this);
        linkedCreditCardView.setOnClickListener(this);
        linkedPersonalCardView.setOnClickListener(this);
        openShoppingList.setOnClickListener(this);

        adapter=new MyAccountOverViewPagerAdapter(getActivity());
        viewPager.addOnPageChangeListener(this);
        setUiPageViewController();
        setUpView(isLoggedIn,isStoreCard,isCreditCard,isPersonalCard);
        loadAccounts();
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openMessageActivity:
                startActivity(new Intent(getActivity(), MessagesActivity.class).putExtra("fromNotification", false));
                getActivity().overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
                break;
            case R.id.applyStoreCard:
                redirectToMyAccountsCardsActivity(0);
                break;
            case R.id.applyCrediCard:
                redirectToMyAccountsCardsActivity(1);
                break;
            case R.id.applyPersonalLoan:
                redirectToMyAccountsCardsActivity(2);
                break;
            case R.id.linkedStoreCard:
               redirectToMyAccountsCardsActivity(0);
                break;
            case R.id.linkedCrediCard:
                redirectToMyAccountsCardsActivity(1);
                break;
            case R.id.linkedPersonalLoan:
                redirectToMyAccountsCardsActivity(2);
                break;
            case R.id.contactUs:
                startActivity(new Intent(getActivity(), WContactUsActivity.class));
                getActivity().overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
                break;
            case R.id.openShoppingList:
                break;


        }
    }



    private void setUiPageViewController() {

        dotsCount = adapter.getCount();
        dots = new ImageView[dotsCount];

        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new ImageView(getActivity());
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.page_control_inactive));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(4, 0, 4, 0);

            pager_indicator.addView(dots[i], params);
        }

        dots[0].setImageDrawable(getResources().getDrawable(R.drawable.page_control_active));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < dotsCount; i++) {
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.page_control_inactive));
        }

        dots[position].setImageDrawable(getResources().getDrawable(R.drawable.page_control_active));

    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
    private void loadAccounts() {
        mGetAccountsProgressDialog = new ProgressDialog(getActivity());
        mGetAccountsProgressDialog.setMessage(FontHyperTextParser.getSpannable(getString(R.string.getting_accounts), 1, getActivity()));
        mGetAccountsProgressDialog.setCancelable(false);

        new HttpAsyncTask<String, String, AccountsResponse>() {

            @Override
            protected void onPreExecute() {
                mGetAccountsProgressDialog.show();
            }

            @Override
            protected Class<AccountsResponse> httpDoInBackgroundReturnType() {
                return AccountsResponse.class;
            }

            @Override
            protected AccountsResponse httpDoInBackground(String... params) {
                return ((WoolworthsApplication)getActivity().getApplication()).getApi().getAccounts();
            }

            @Override
            protected AccountsResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {

                WiGroupLogger.e(getActivity(), TAG, errorMessage);
                AccountsResponse accountResponse = new AccountsResponse();
                accountResponse.httpCode = 408;
                accountResponse.response = new Response();
                accountResponse.response.desc = getString(R.string.err_002);
                return accountResponse;
            }

            @Override
            protected void onPostExecute(AccountsResponse accountsResponse) {
                accounts=accountsResponse;
                handleAccountsResponse(accountsResponse);
                mGetAccountsProgressDialog.dismiss();
            }
        }.execute();
    }
    private void handleAccountsResponse(AccountsResponse accountsResponse) {
        switch (accountsResponse.httpCode) {
            case 200:
                 isLoggedIn=true;
                ((WoolworthsApplication) getActivity().getApplication()).getUserManager().setAccounts(accountsResponse);
                ArrayList<BaseAccountFragment> baseAccountFragments = new ArrayList<BaseAccountFragment>();
                List<Account> accountList = accountsResponse.accountList;
                boolean containsStoreCard = false, containsCreditCard = false, containsPersonalLoan = false;
                if (accountList != null) {
                    for (Account p : accountList) {
                        if ("SC".equals(p.productGroupCode)) {
                            containsStoreCard = true;
                            sc_available_funds.setText(FontHyperTextParser.getSpannable(WFormatter.formatAmount(p.availableFunds), 1, getActivity()));
                            scProgressBar.setProgress(Math.round(100 - ((float) p.availableFunds / (float) p.creditLimit * 100f)));
                        } else if ("CC".equals(p.productGroupCode)) {
                            containsCreditCard = true;
                            cc_available_funds.setText(FontHyperTextParser.getSpannable(WFormatter.formatAmount(p.availableFunds), 1, getActivity()));
                            ccProgressBar.setProgress(Math.round(100 - ((float) p.availableFunds / (float) p.creditLimit * 100f)));

                        } else if ("PL".equals(p.productGroupCode)) {
                            containsPersonalLoan = true;
                            pl_available_funds.setText(FontHyperTextParser.getSpannable(WFormatter.formatAmount(p.availableFunds), 1, getActivity()));
                            plProgressBar.setProgress(Math.round(100 - ((float) p.availableFunds / (float) p.creditLimit * 100f)));

                        }
                    }
                }
                setUpView(isLoggedIn,containsStoreCard,containsCreditCard,containsPersonalLoan);

                break;
            case 400:
                if ("0619".equals(accountsResponse.response.code) || "0618".equals(accountsResponse.response.code)) {

                    break;

                }
            case 502:
                Log.i("Handling 502","Handled a 502 error from the server");
                break;
            default:

        }
    }

    public void setUpView(boolean isLoggedIn,boolean isStoreCard,boolean isCreditCard,boolean isPersonalCard) {
        if (isLoggedIn) {
            loggedInHeaderLayout.setVisibility(View.VISIBLE);
            loggedOutHeaderLayout.setVisibility(View.GONE);
            signOutBtn.setVisibility(View.VISIBLE);

            if (isCreditCard == false && isStoreCard == false && isPersonalCard == false) {
                linkedAccountsLayout.setVisibility(View.GONE);
                linkAccountsBtn.setVisibility(View.VISIBLE);
            } else if (isCreditCard == true && isStoreCard == true && isPersonalCard == true) {
                applyNowAccountsLayout.setVisibility(View.GONE);
            }else if(isCreditCard == true || isStoreCard == true || isPersonalCard == true)
            {
                linkedAccountsLayout.setVisibility(View.VISIBLE);
            }



            if(isCreditCard) {
                linkedCreditCardView.setVisibility(View.VISIBLE);
                applyCreditCardView.setVisibility(View.GONE);
            }
            else {
                applyCreditCardView.setVisibility(View.VISIBLE);
                linkedCreditCardView.setVisibility(View.GONE);
            }

            if(isStoreCard) {
                linkedStoreCardView.setVisibility(View.VISIBLE);
                applyStoreCardView.setVisibility(View.GONE);
            }
            else {
                applyStoreCardView.setVisibility(View.VISIBLE);
                linkedStoreCardView.setVisibility(View.GONE);
            }

            if(isPersonalCard) {
                linkedPersonalCardView.setVisibility(View.VISIBLE);
                applyPersonalCardView.setVisibility(View.GONE);
            }
            else {
                applyPersonalCardView.setVisibility(View.VISIBLE);
                linkedPersonalCardView.setVisibility(View.GONE);
            }

        }
        else {
            loggedInHeaderLayout.setVisibility(View.GONE);
            linkedAccountsLayout.setVisibility(View.GONE);
            applyCreditCardView.setVisibility(View.VISIBLE);
            applyStoreCardView.setVisibility(View.VISIBLE);
            applyPersonalCardView.setVisibility(View.VISIBLE);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(0);
        }
    }

    public void redirectToMyAccountsCardsActivity(int position)
    {
        Intent intent=new Intent(getActivity(),MyAccountCardsActivityTest.class);
        intent.putExtra("position",position);
        if(accounts!=null)
        {
            intent.putExtra("accounts",Utils.objectToJson(accounts));
        }
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

    }
    public void loadMessages() {
        new HttpAsyncTask<String, String, MessageResponse>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected MessageResponse httpDoInBackground(String... params) {

                return ((WoolworthsApplication) getActivity().getApplication()).getApi().getMessagesResponse(5, 1);
            }

            @Override
            protected Class<MessageResponse> httpDoInBackgroundReturnType() {
                return MessageResponse.class;
            }

            @Override
            protected MessageResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                MessageResponse messageResponse = new MessageResponse();
                messageResponse.response = new Response();
                return messageResponse;
            }

            @Override
            protected void onPostExecute(MessageResponse messageResponse) {

                super.onPostExecute(messageResponse);
               if( messageResponse.unreadCount>0)
               {
                   messageCounter.setVisibility(View.VISIBLE);
                  messageCounter.setText(String.valueOf(messageResponse.unreadCount));
               }else {
                   messageCounter.setVisibility(View.GONE);
               }


            }
        }.execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMessages();
    }
}
