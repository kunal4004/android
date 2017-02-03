package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Intent;
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
import za.co.woolworths.financial.services.android.ui.activities.WRewardsVoucherDetailsActivity;
import za.co.woolworths.financial.services.android.ui.adapters.WRewardsSavingsHorizontalScrollAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.WRewardsVoucherListAdapter;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.RecycleViewClickListner;
import za.co.woolworths.financial.services.android.util.Utils;

/**
 * Created by W7099877 on 05/01/2017.
 */

public class WRewardsVouchersFragment extends Fragment {
    private RecyclerView.LayoutManager mLayoutManager;
    private WRewardsVoucherListAdapter mAdapter;
    private RecyclerView recyclerView;
    public VoucherResponse voucherResponse;
    public WTextView noVouchers;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wrewards_vouchers_fragment, container, false);
        Bundle bundle = getArguments();
        voucherResponse = new Gson().fromJson(bundle.getString("WREWARDS"), VoucherResponse.class);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        noVouchers = (WTextView) view.findViewById(R.id.noVouchers);
        mLayoutManager = new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false
        );
        recyclerView.setLayoutManager(mLayoutManager);
        if(voucherResponse.voucherCollection.vouchers==null || voucherResponse.voucherCollection.vouchers.size()==0)
        {
            displayNoVouchersView();
        }else {
            displayVouchers(voucherResponse);
        }

        return view;
    }


    public void displayNoVouchersView() {
        noVouchers.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    public void displayVouchers(final VoucherResponse vResponse) {
        noVouchers.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        mAdapter = new WRewardsVoucherListAdapter(getActivity(), vResponse.voucherCollection.vouchers);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecycleViewClickListner(getActivity(), recyclerView, new RecycleViewClickListner.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(getActivity(), WRewardsVoucherDetailsActivity.class);
                intent.putExtra("VOUCHERS", Utils.objectToJson(vResponse.voucherCollection));
                intent.putExtra("POSITION", position);
                startActivity(intent);

            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
    }
}