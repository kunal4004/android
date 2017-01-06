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

import za.co.woolworths.financial.services.android.ui.adapters.WRewardsSavingsHorizontalScrollAdapter;
import za.co.woolworths.financial.services.android.util.RecycleViewClickListner;

/**
 * Created by W7099877 on 05/01/2017.
 */

public class WRewardsSavingsFragment extends Fragment {
    private RecyclerView.LayoutManager mLayoutManager;
    private WRewardsSavingsHorizontalScrollAdapter mAdapter;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wrewards_savings_fragment, container, false);
        recyclerView=(RecyclerView)view.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.HORIZONTAL,
                false
        );
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter=new WRewardsSavingsHorizontalScrollAdapter(getActivity());
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecycleViewClickListner(getActivity(),recyclerView, new RecycleViewClickListner.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //Zero position belongs to recycleview header view
                if(position>0) {
                    mAdapter.setSelectedPosition(position);
                    mAdapter.notifyDataSetChanged();

                    //Get data on Position-1 from Array List. And bind to UI

                }
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
        return view;
    }

}