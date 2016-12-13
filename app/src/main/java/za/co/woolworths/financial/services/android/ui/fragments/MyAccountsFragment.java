package za.co.woolworths.financial.services.android.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.activities.MessagesActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivityTest;
import za.co.woolworths.financial.services.android.ui.activities.WContactUsActivity;
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity;
import za.co.woolworths.financial.services.android.ui.adapters.MyAccountOverViewPagerAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.MyAccountsCardsAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.TestViewpagerAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.WCustomViewPager;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyAccountsFragment extends Fragment implements View.OnClickListener,ViewPager.OnPageChangeListener {

    ImageView openMessageActivity;
    ImageView openShoppingList;
    RelativeLayout contactUs;
    boolean isLoggedIn = true;
    boolean isCreditCard = true;
    boolean isStoreCard = true;
    boolean isPersonalCard = true;
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


        openMessageActivity.setOnClickListener(this);
        contactUs.setOnClickListener(this);
        applyPersonalCardView.setOnClickListener(this);
        applyStoreCardView.setOnClickListener(this);
        applyCreditCardView.setOnClickListener(this);
        openShoppingList.setOnClickListener(this);

        adapter=new MyAccountOverViewPagerAdapter(getActivity());
        viewPager.addOnPageChangeListener(this);
        setUiPageViewController();
        setUpView();
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
                startActivity(new Intent(getActivity(), MyAccountCardsActivityTest.class));
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.applyCrediCard:
                startActivity(new Intent(getActivity(), MyAccountCardsActivityTest.class));
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.applyPersonalLoan:
                startActivity(new Intent(getActivity(), MyAccountCardsActivityTest.class));
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.contactUs:
                startActivity(new Intent(getActivity(), WContactUsActivity.class));
                getActivity().overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
                break;
            case R.id.openShoppingList:
                startActivity(new Intent(getActivity(), WTransactionsActivity.class));
                break;


        }
    }

    public void setUpView() {
        if (isLoggedIn) {
            loggedInHeaderLayout.setVisibility(View.VISIBLE);
            loggedOutHeaderLayout.setVisibility(View.GONE);
            signOutBtn.setVisibility(View.VISIBLE);

            if (isCreditCard == false && isStoreCard == false && isPersonalCard == false) {
               linkedAccountsLayout.setVisibility(View.GONE);
                linkAccountsBtn.setVisibility(View.VISIBLE);
            } else if (isCreditCard == true && isStoreCard == true && isPersonalCard == true) {
                applyNowAccountsLayout.setVisibility(View.GONE);
            }

            if(isCreditCard)
                linkedCreditCardView.setVisibility(View.VISIBLE);
            else
                applyCreditCardView.setVisibility(View.VISIBLE);

            if(isStoreCard)
                linkedStoreCardView.setVisibility(View.VISIBLE);
            else
                applyStoreCardView.setVisibility(View.VISIBLE);

            if(isPersonalCard)
                linkedPersonalCardView.setVisibility(View.VISIBLE);
            else
                applyPersonalCardView.setVisibility(View.VISIBLE);

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
}
