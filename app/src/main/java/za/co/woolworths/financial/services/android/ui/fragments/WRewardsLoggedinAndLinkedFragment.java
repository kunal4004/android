package za.co.woolworths.financial.services.android.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.activities.WOneAppBaseActivity;
import za.co.woolworths.financial.services.android.ui.adapters.ContactUsFragmentPagerAdapter;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

/**
 * Created by W7099877 on 05/01/2017.
 */

public class WRewardsLoggedinAndLinkedFragment extends Fragment {

    public TabLayout tabLayout;
    public ViewPager viewPager;
    ContactUsFragmentPagerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.wrewards_loggedin_and_linked_fragment, container, false);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        try
        {
            setupTabIcons();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
}
