package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.LocationResponse;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.activities.StoreDetailsActivity;
import za.co.woolworths.financial.services.android.ui.adapters.StockFinderListAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.StoreSearchListAdapter;
import za.co.woolworths.financial.services.android.util.RecycleViewClickListner;

public class StoreFinderListFragment extends Fragment {

	private RecyclerView mFinderInStoreList;
	private List<StoreDetails> mStoreDetailList;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.store_finder_list_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		init(view);
		onItemSelected();
	}

	private void init(View view) {
		mFinderInStoreList = (RecyclerView) view.findViewById(R.id.storeList);
		mFinderInStoreList.setLayoutManager(new LinearLayoutManager(getActivity()));
	}

	private void getFinderStoreData(List<StoreDetails> mStoreDetailList) {
		this.mStoreDetailList = mStoreDetailList;
		try {
			if (mStoreDetailList != null && mStoreDetailList.size() != 0) {
				bindDataWithUI(mStoreDetailList);
			}
		} catch (Exception ex) {
		}
	}

	private void bindDataWithUI(List<StoreDetails> storeDetailsList) {
		StockFinderListAdapter mSearchAdapter = new StockFinderListAdapter(getActivity(), storeDetailsList);
		mFinderInStoreList.setAdapter(mSearchAdapter);
	}

	private void onItemSelected() {
		mFinderInStoreList.addOnItemTouchListener(new RecycleViewClickListner(getActivity(), mFinderInStoreList, new RecycleViewClickListner.ClickListener() {
			@Override
			public void onClick(View view, int position) {

				Gson gson = new Gson();
				String store = gson.toJson(mStoreDetailList.get(position));
				startActivity(new Intent(getActivity(), StoreDetailsActivity.class).putExtra("store", store));
				getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
			}

			@Override
			public void onLongClick(View view, int position) {

			}
		}));
	}

	public void update(List<StoreDetails> storeDetailsList) {
		getFinderStoreData(storeDetailsList);
	}

	public void listUpdated(){}
}