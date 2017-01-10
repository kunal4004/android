package za.co.woolworths.financial.services.android.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.awfs.coordination.R;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.activities.WOneAppBaseActivity;
import za.co.woolworths.financial.services.android.ui.adapters.ContactUsFragmentPagerAdapter;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.WErrorDialog;

import static com.google.android.gms.wearable.DataMap.TAG;

/**
 * Created by W7099877 on 05/01/2017.
 */

public class WRewardsLoggedinAndLinkedFragment extends Fragment {

    public TabLayout tabLayout;
    public ViewPager viewPager;
    ContactUsFragmentPagerAdapter adapter;
    ProgressBar progressBar;
    LinearLayout fragmentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.wrewards_loggedin_and_linked_fragment, container, false);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        progressBar=(ProgressBar)view.findViewById(R.id.progressBar);
        fragmentView=(LinearLayout) view.findViewById(R.id.fragmentView);

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);

        getWrewards();
        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter=new ContactUsFragmentPagerAdapter(getActivity().getSupportFragmentManager());
        adapter.addFrag(new WRewardsOverviewFragment(), getString(R.string.overview));
        adapter.addFrag(new WRewardsVouchersFragment(), getString(R.string.vouchers));
        adapter.addFrag(new WRewardsSavingsFragment(), getString(R.string.savings));
        viewPager.setAdapter(adapter);
    }

    private void setupTabIcons()
    {
        String[] tabTitle={getActivity().getString(R.string.overview),getActivity().getString(R.string.vouchers),getActivity().getString(R.string.savings)};


        for(int i=0;i<tabTitle.length;i++)
        {
            tabLayout.getTabAt(i).setCustomView(prepareTabView(i,tabTitle));
        }
    }


    private View prepareTabView(int pos,String[] tabTitle) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.wrewards_custom_tab,null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        TextView tv_count = (TextView) view.findViewById(R.id.tv_count);
        tv_title.setText(tabTitle[pos]);
        if(pos==1)
        {
            tv_count.setVisibility(View.VISIBLE);
            tv_count.setText("2");
        }
        else
            tv_count.setVisibility(View.GONE);


        return view;
    }

    public void getWrewards() {
        new HttpAsyncTask<String, String, VoucherResponse>() {
            @Override
            protected void onPreExecute() {
                progressBar.setVisibility(View.VISIBLE);
                super.onPreExecute();
            }

            @Override
            protected VoucherResponse httpDoInBackground(String... params) {

                return ((WoolworthsApplication) getActivity().getApplication()).getApi().getVouchers();
            }

            @Override
            protected Class<VoucherResponse> httpDoInBackgroundReturnType() {
                return VoucherResponse.class;
            }

            @Override
            protected VoucherResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                VoucherResponse voucherResponse = new VoucherResponse();
                voucherResponse.response = new Response();
                return voucherResponse;
            }

            @Override
            protected void onPostExecute(VoucherResponse voucherResponse) {
                super.onPostExecute(voucherResponse);
                progressBar.setVisibility(View.GONE);
                fragmentView.setVisibility(View.VISIBLE);
                handleVoucherResponse(voucherResponse);

            }
        }.execute();
    }

    public void handleVoucherResponse(VoucherResponse voucherResponse)
    {
        switch (voucherResponse.httpCode) {
            case 200:
                setupViewPager(viewPager);
                tabLayout.setupWithViewPager(viewPager);
                try
                {
                    setupTabIcons();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
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
                        //WRewardsLoggedinAndLinkedFragment.this.initialize();
                        Intent intent = new Intent();
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                        getFragmentManager().popBackStack();
                    }
                }.execute();

                break;
            default:break;

        }
    }



}
