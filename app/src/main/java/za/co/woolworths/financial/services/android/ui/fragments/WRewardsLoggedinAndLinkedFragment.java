package za.co.woolworths.financial.services.android.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.activities.WRewardsErrorFragment;
import za.co.woolworths.financial.services.android.ui.adapters.WRewardsFragmentPagerAdapter;
import za.co.woolworths.financial.services.android.ui.views.ErrorHandlerView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WErrorDialog;

/**
 * Created by W7099877 on 05/01/2017.
 */

public class WRewardsLoggedinAndLinkedFragment extends Fragment {

    public TabLayout tabLayout;
    public ViewPager viewPager;
    WRewardsFragmentPagerAdapter adapter;
    ProgressBar progressBar;
    LinearLayout fragmentView;
    private ErrorHandlerView mErrorHandlerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wrewards_loggedin_and_linked_fragment, container, false);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        fragmentView = (LinearLayout) view.findViewById(R.id.fragmentView);
        RelativeLayout mRelErrorHandler = (RelativeLayout) view.findViewById(R.id.relErrorHandler);
        WTextView mTitleError = (WTextView) view.findViewById(R.id.errorTitle);

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        viewPager.setOffscreenPageLimit(3);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
        mErrorHandlerView = new ErrorHandlerView(getActivity(), mRelErrorHandler, mTitleError);

        getWRewards().execute();
        retryApiCall(view);

        return view;
    }

    private void setupViewPager(ViewPager viewPager, VoucherResponse voucherResponse) {
        Bundle bundle = new Bundle();
        bundle.putString("WREWARDS", Utils.objectToJson(voucherResponse));
        adapter = new WRewardsFragmentPagerAdapter(getChildFragmentManager(), bundle);
        adapter.addFrag(new WRewardsOverviewFragment(), getString(R.string.overview));
        adapter.addFrag(new WRewardsVouchersFragment(), getString(R.string.vouchers));
        adapter.addFrag(new WRewardsSavingsFragment(), getString(R.string.savings));
        viewPager.setAdapter(adapter);
        viewPager.invalidate();
        adapter.notifyDataSetChanged();
        tabLayout.setupWithViewPager(viewPager);
        try {
            setupTabIcons(voucherResponse.voucherCollection.vouchers.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTabIcons(int activeVoucherCount) {
        String[] tabTitle = {getActivity().getString(R.string.overview), getActivity().getString(R.string.vouchers), getActivity().getString(R.string.savings)};


        for (int i = 0; i < tabTitle.length; i++) {
            tabLayout.getTabAt(i).setCustomView(prepareTabView(i, tabTitle, activeVoucherCount));
        }
        tabLayout.getTabAt(0).getCustomView().setSelected(true);
    }


    private View prepareTabView(int pos, String[] tabTitle, int activeVoucherCount) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.wrewards_custom_tab, null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        TextView tv_count = (TextView) view.findViewById(R.id.tv_count);
        tv_title.setText(tabTitle[pos]);
        if (pos == 1 && activeVoucherCount > 0) {
            tv_count.setVisibility(View.VISIBLE);
            tv_count.setText("" + activeVoucherCount);
        } else
            tv_count.setVisibility(View.GONE);


        return view;
    }

    public HttpAsyncTask<String, String, VoucherResponse> getWRewards() {
        return new HttpAsyncTask<String, String, VoucherResponse>() {
            @Override
            protected void onPreExecute() {
                progressBar.setVisibility(View.VISIBLE);
                mErrorHandlerView.hideErrorHandlerLayout();
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
                networkFailureHandler(errorMessage);
                return new VoucherResponse();
            }

            @Override
            protected void onPostExecute(VoucherResponse voucherResponse) {
                super.onPostExecute(voucherResponse);
                progressBar.setVisibility(View.GONE);
                fragmentView.setVisibility(View.VISIBLE);
                handleVoucherResponse(voucherResponse);

            }
        };
    }

    public void handleVoucherResponse(VoucherResponse voucherResponse) {
        switch (voucherResponse.httpCode) {
            case 200:
                setupViewPager(viewPager, voucherResponse);
                break;
            case 440:
                AlertDialog mError = WErrorDialog.getSimplyErrorDialog(getActivity());
                mError.setTitle(getString(R.string.title_authentication_error));
                mError.setMessage(getString(R.string.session_out_message));
                mError.show();

                new android.os.AsyncTask<Void, Void, String>() {

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
                        Intent intent = new Intent();
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                        getFragmentManager().popBackStack();
                    }
                }.execute();

                break;
            default:
                setupErrorViewPager(viewPager);
                break;

        }
    }

    private void setupErrorViewPager(ViewPager viewPager) {
        Bundle bundle = new Bundle();
        bundle.putString("WREWARDS", "");
        adapter = new WRewardsFragmentPagerAdapter(getChildFragmentManager(), bundle);
        adapter.addFrag(new WRewardsErrorFragment(), getString(R.string.overview));
        adapter.addFrag(new WRewardsErrorFragment(), getString(R.string.vouchers));
        adapter.addFrag(new WRewardsErrorFragment(), getString(R.string.savings));
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        try {
            setupTabIcons(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void networkFailureHandler(final String errorMessage) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mErrorHandlerView.diplayErrorMessage(errorMessage);
            }
        });
    }

    private void retryApiCall(View view) {
        view.findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWRewards().execute();
            }
        });
    }
}
