package za.co.woolworths.financial.services.android.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Voucher;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.activities.WRewardDetailActivity;

public class VouchersListFragment extends Fragment {
    private List<Voucher> mVouchers = new ArrayList<>();
    private RewardsAdapter mRewardsAdapter;
    private VoucherResponse mVoucherResponse;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.vouchers_fragment, container, false);
        ListView list = (ListView) view.findViewById(R.id.w_rewards_list);
        list.setEmptyView(view.findViewById(R.id.voucher_fragment_empty_list));
        mRewardsAdapter = new RewardsAdapter();
        list.setAdapter(mRewardsAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), WRewardDetailActivity.class);
                intent.putExtra(WRewardDetailActivity.VOUCHER_ID, position);
                intent.putExtra(WRewardDetailActivity.DATA, new Gson().toJson(mVoucherResponse));
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        update(mVoucherResponse);
    }

    public void update(VoucherResponse voucherResponse) {
        if (voucherResponse == null){
            return;
        }
        mVoucherResponse = voucherResponse;
        mVouchers = mVoucherResponse.voucherCollection.vouchers;
        if (mRewardsAdapter != null) {
            mRewardsAdapter.notifyDataSetChanged();
        }
    }

    private class RewardsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            WoolworthsApplication.setNumVouchers(mVouchers == null ? 0 : mVouchers.size());
            return mVouchers == null? 0: mVouchers.size();
        }

        @Override
        public Voucher getItem(int position) {
            return mVouchers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.w_rewards_item, null);
            }
            ((TextView) convertView.findViewById(R.id.w_rewards_item_description)).setText(getItem(position).description.toUpperCase());
            return convertView;
        }
    }
}
