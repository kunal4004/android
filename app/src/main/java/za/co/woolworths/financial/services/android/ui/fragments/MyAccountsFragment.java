package za.co.woolworths.financial.services.android.ui.fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.wigroup.logger.lib.WiGroupLogger;
import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;

import za.co.woolworths.financial.services.android.ui.activities.CLIActivity;
import za.co.woolworths.financial.services.android.ui.activities.MessagesActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivityTest;
import za.co.woolworths.financial.services.android.ui.activities.WContactUsActivity;
import za.co.woolworths.financial.services.android.ui.activities.MessagesActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivity;
import za.co.woolworths.financial.services.android.ui.activities.PersonalLoanWithdrawalActivity;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.WContactUsActivity;
import za.co.woolworths.financial.services.android.ui.activities.WOnboardingActivity;
import za.co.woolworths.financial.services.android.ui.activities.WOneAppBaseActivity;
import za.co.woolworths.financial.services.android.ui.adapters.MyAccountOverViewPagerAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WErrorDialog;
import za.co.woolworths.financial.services.android.util.WFormatter;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyAccountsFragment extends Fragment implements View.OnClickListener,ViewPager.OnPageChangeListener {

    ImageView openMessageActivity;
    ImageView openShoppingList;
    RelativeLayout contactUs;

    boolean isLoggedIn = true;
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
    WTextView userName;
    WTextView userInitials;

    private ProgressDialog mGetAccountsProgressDialog;
    private ProgressBar scProgressBar;
    private ProgressBar ccProgressBar;
    private ProgressBar plProgressBar;

    Map<String, Account> accounts;
    List<String> unavailableAccounts;
    private AccountsResponse accountsResponse; //purely referenced to be passed forward as Intent Extra


    private int dotsCount;
    private ImageView[] dots;
    public MyAccountsFragment() {
        // Required empty public constructor
        this.accounts = new HashMap<>();
        this.unavailableAccounts = new ArrayList<>();
        this.accountsResponse = null;
    }

    WoolworthsApplication woolworthsApplication;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_accounts_fragment, container, false);
        // ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("ACCOUNTS");
        woolworthsApplication = (WoolworthsApplication)getActivity().getApplication();
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
        userName = (WTextView) view.findViewById(R.id.user_name);
        userInitials = (WTextView) view.findViewById(R.id.initials);

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
       // setUpView(isLoggedIn,isStoreCard,isCreditCard,isPersonalCard);
        //loadAccounts();

        view.findViewById(R.id.loginAccount).setOnClickListener(this.btnSignin_onClick);
        view.findViewById(R.id.registerAccount).setOnClickListener(this.btnRegister_onClick);
        view.findViewById(R.id.linkAccountsBtn).setOnClickListener(this.btnLinkAccounts_onClick);



        //hide all views, load accounts may occur
        this.initialize();
        return view;
    }

    private void initialize() {
        this.hideAllLayers();
        this.accounts.clear();
        this.unavailableAccounts.clear();
        this.unavailableAccounts.addAll(Arrays.asList("SC", "CC", "PL"));

        JWTDecodedModel jwtDecodedModel = ((WOneAppBaseActivity)getActivity()).getJWTDecoded();
        if(jwtDecodedModel != null && jwtDecodedModel.C2Id != null && !jwtDecodedModel.C2Id.equals("")){
            this.loadAccounts();
        } else{
            this.configureView();
        }
    }

    private void configureView() {
        this.configureAndLayoutTopLayerView();

        //show content for all available products
        for(Map.Entry<String, Account> item : accounts.entrySet()){
            Account account = item.getValue();
            if(account.productGroupCode.equals("SC")){
                linkedStoreCardView.setVisibility(View.VISIBLE);
                applyStoreCardView.setVisibility(View.GONE);

                sc_available_funds.setText(FontHyperTextParser.getSpannable(WFormatter.formatAmount(account.availableFunds), 1, getActivity()));
                scProgressBar.setProgress(Math.round(100 - ((float) account.availableFunds / (float) account.creditLimit * 100f)));

            } else if(account.productGroupCode.equals("CC")){
                linkedCreditCardView.setVisibility(View.VISIBLE);
                applyCreditCardView.setVisibility(View.GONE);

                cc_available_funds.setText(FontHyperTextParser.getSpannable(WFormatter.formatAmount(account.availableFunds), 1, getActivity()));
                ccProgressBar.setProgress(Math.round(100 - ((float) account.availableFunds / (float) account.creditLimit * 100f)));

            } else if(account.productGroupCode.equals("PL")){
                linkedPersonalCardView.setVisibility(View.VISIBLE);
                applyPersonalCardView.setVisibility(View.GONE);

                pl_available_funds.setText(FontHyperTextParser.getSpannable(WFormatter.formatAmount(account.availableFunds), 1, getActivity()));
                plProgressBar.setProgress(Math.round(100 - ((float) account.availableFunds / (float) account.creditLimit * 100f)));
            }
        }

        //hide content for unavailable products
        for(String s : unavailableAccounts){
            if(s.equals("SC")){
                applyStoreCardView.setVisibility(View.VISIBLE);
                linkedStoreCardView.setVisibility(View.GONE);
            } else if(s.equals("CC")){
                applyCreditCardView.setVisibility(View.VISIBLE);
                linkedCreditCardView.setVisibility(View.GONE);
            } else if(s.equals("PL")){
                applyPersonalCardView.setVisibility(View.VISIBLE);
                linkedPersonalCardView.setVisibility(View.GONE);
            }
        }

        if(unavailableAccounts.size() == 0){
            //all accounts are shown/linked
            applyNowAccountsLayout.setVisibility(View.GONE);
        } else{
            applyNowAccountsLayout.setVisibility(View.VISIBLE);
        }

        contactUs.setVisibility(View.VISIBLE);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
    }

    private void configureAndLayoutTopLayerView() {

        JWTDecodedModel jwtDecodedModel = ((WOneAppBaseActivity)getActivity()).getJWTDecoded();
        if(jwtDecodedModel.AtgSession != null){
            loggedInHeaderLayout.setVisibility(View.VISIBLE);
            //logged in user's name and family name will be displayed on the page
            userName.setText(jwtDecodedModel.name + " " + jwtDecodedModel.family_name);
            //initials of the logged in user will be displayed on the page
            String initials = jwtDecodedModel.name.substring(0, 1).concat(" ").concat(jwtDecodedModel.family_name.substring(0, 1));
            userInitials.setText(initials);
            if(jwtDecodedModel.C2Id != null && !jwtDecodedModel.C2Id.equals("")){
                //user is linked and signed in
                linkedAccountsLayout.setVisibility(View.VISIBLE);
                signOutBtn.setVisibility(View.VISIBLE);
            } else{
                //user is not linked
                //but signed in
                linkAccountsBtn.setVisibility(View.VISIBLE);
                setUiPageViewController();
            }
        }else{
            //user is signed out
            loggedOutHeaderLayout.setVisibility(View.VISIBLE);
            setUiPageViewController();
        }
    }

    private void hideAllLayers(){

        loggedInHeaderLayout.setVisibility(View.GONE);
        loggedOutHeaderLayout.setVisibility(View.GONE);
        signOutBtn.setVisibility(View.GONE);
        linkedAccountsLayout.setVisibility(View.GONE);
        applyNowAccountsLayout.setVisibility(View.GONE);
        contactUs.setVisibility(View.GONE);
        linkAccountsBtn.setVisibility(View.GONE);
    }

    private void setUiPageViewController() {

        pager_indicator.removeAllViews();
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

    private View.OnClickListener btnSignin_onClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ScreenManager.presentSSOSignin(getActivity());
        }
    };

    private View.OnClickListener btnRegister_onClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ScreenManager.presentSSORegister(getActivity());
        }
    };

    private View.OnClickListener btnLinkAccounts_onClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ScreenManager.presentSSOLinkAccounts(getActivity());
        }
    };

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
                accountResponse.response.desc = errorMessage;
                return accountResponse;
            }

            @Override
            protected void onPostExecute(AccountsResponse accountsResponse) {
                mGetAccountsProgressDialog.dismiss();

                switch (accountsResponse.httpCode) {
                    case 200:
                        MyAccountsFragment.this.accountsResponse = accountsResponse;
                        ArrayList<BaseAccountFragment> baseAccountFragments = new ArrayList<BaseAccountFragment>();
                        List<Account> accountList = accountsResponse.accountList;
                        for (Account p : accountList) {
                            accounts.put(p.productGroupCode.toUpperCase(), p);

                            int indexOfUnavailableAccount = unavailableAccounts.indexOf(p.productGroupCode.toUpperCase());
                            if(indexOfUnavailableAccount > -1){
                                try{
                                    unavailableAccounts.remove(indexOfUnavailableAccount);
                                }
                                catch(Exception e){
                                    Log.e("", e.getMessage());
                                }
                            }
                        }

                        configureView();

                        break;
                    case 440:
                        AlertDialog mError = WErrorDialog.getSimplyErrorDialog(getActivity());
                        mError.setTitle("Authentication Error");
                        mError.setMessage("Your session expired. You've been signed out.");
                        mError.show();

                        new android.os.AsyncTask<Void, Void, String>(){

                            @Override
                            protected String doInBackground(Void... params) {
                                try {
                                    new SessionDao(getActivity(), SessionDao.KEY.USER_TOKEN).delete();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return "";
                            }

                            @Override
                            protected void onPostExecute(String s) {
                                MyAccountsFragment.this.initialize();
                            }
                        }.execute();

                        break;
                    default:break;
                }
            }
        }.execute();
    }
    public void redirectToMyAccountsCardsActivity(int position)
    {
        woolworthsApplication.setCliCardPosition(position);
        Intent intent = new Intent(getActivity(), MyAccountCardsActivity.class);

        intent.putExtra("position",position);
        if(accountsResponse != null) {
            intent.putExtra("accounts", Utils.objectToJson(accountsResponse));
        }
        startActivityForResult(intent, 0);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()){
            //Save JWT
            SessionDao sessionDao = new SessionDao(getActivity(), SessionDao.KEY.USER_TOKEN);
            sessionDao.value = data.getStringExtra(SSOActivity.TAG_JWT);
            try {
                sessionDao.save();
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }

            initialize();
        } else if (resultCode == SSOActivity.SSOActivityResult.EXPIRED.rawValue()){
            initialize();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMessages();
    }
}
