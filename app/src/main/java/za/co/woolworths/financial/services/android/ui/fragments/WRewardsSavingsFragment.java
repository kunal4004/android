package za.co.woolworths.financial.services.android.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.adapters.WRewardsSavingsHorizontalScrollAdapter;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.RecycleViewClickListner;
import za.co.woolworths.financial.services.android.util.WFormatter;

/**
 * Created by W7099877 on 05/01/2017.
 */

public class WRewardsSavingsFragment extends Fragment {
    private RecyclerView.LayoutManager mLayoutManager;
    private WRewardsSavingsHorizontalScrollAdapter mAdapter;
    private RecyclerView recyclerView;
    public VoucherResponse voucherResponse;
    public WTextView tireStatus;
    public WTextView wRewardsInstantSaving;
    public WTextView wRewardsGreenEarned;
    public WTextView quarterlyVoucherEarned;
    public WTextView yearToDateSpend;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wrewards_savings_fragment, container, false);
        Bundle bundle=getArguments();
        voucherResponse=new Gson().fromJson(bundle.getString("WREWARDS"),VoucherResponse.class);

        recyclerView=(RecyclerView)view.findViewById(R.id.recycler_view);
        tireStatus=(WTextView)view.findViewById(R.id.tireStatus);
        wRewardsInstantSaving=(WTextView)view.findViewById(R.id.wrewardsInstantSavings);
        wRewardsGreenEarned=(WTextView)view.findViewById(R.id.wrewardsGreenEarned);
        quarterlyVoucherEarned=(WTextView)view.findViewById(R.id.quarterlyVouchersEarned);
        yearToDateSpend=(WTextView)view.findViewById(R.id.yearToDateSpend);
        mLayoutManager = new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.HORIZONTAL,
                false
        );
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter=new WRewardsSavingsHorizontalScrollAdapter(getActivity(),voucherResponse.tierHistoryList);
        recyclerView.setAdapter(mAdapter);
        if(voucherResponse.tierHistoryList.size()>0)
        {
            tireStatus.setText(voucherResponse.tierHistoryList.get(0).tier);
            wRewardsInstantSaving.setText(WFormatter.formatAmount(voucherResponse.tierHistoryList.get(0).monthlySavings));
            wRewardsGreenEarned.setText(WFormatter.formatAmount(voucherResponse.tierHistoryList.get(0).monthlyGreenValueEarned));
            quarterlyVoucherEarned.setText(WFormatter.formatAmount(voucherResponse.tierHistoryList.get(0).wVouchers));
            yearToDateSpend.setText(WFormatter.formatAmount(voucherResponse.tierHistoryList.get(0).monthlySpend));

        }
        recyclerView.addOnItemTouchListener(new RecycleViewClickListner(getActivity(),recyclerView, new RecycleViewClickListner.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //Zero position belongs to recycleview header view
                if(position>0) {
                    mAdapter.setSelectedPosition(position);
                    mAdapter.notifyDataSetChanged();

                    //Get data on Position-1 from Array List. And bind to UI
                    tireStatus.setText(voucherResponse.tierHistoryList.get(position-1).tier);
                    wRewardsInstantSaving.setText(WFormatter.formatAmount(voucherResponse.tierHistoryList.get(position-1).monthlySavings));
                    wRewardsGreenEarned.setText(WFormatter.formatAmount(voucherResponse.tierHistoryList.get(position-1).monthlyGreenValueEarned));
                    quarterlyVoucherEarned.setText(WFormatter.formatAmount(voucherResponse.tierHistoryList.get(position-1).wVouchers));
                    yearToDateSpend.setText(WFormatter.formatAmount(voucherResponse.tierHistoryList.get(position-1).monthlySpend));
                }
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
        return view;
    }

}