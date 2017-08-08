package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.ui.activities.StoreDetailsActivity;
import za.co.woolworths.financial.services.android.ui.adapters.StockFinderListAdapter;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.RecycleViewClickListner;
import za.co.woolworths.financial.services.android.util.UpdateStoreFinderFragment;

public class StoreFinderListFragment extends Fragment implements UpdateStoreFinderFragment {

	private RecyclerView mFinderInStoreList;
	private List<StoreDetails> mStoreDetailList;
	private WTextView tvNoResult;
	private ProgressBar pStoreProgressBar;
	private RelativeLayout rlProgressBar;

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
		showProgressBar();
	}

	private void init(View view) {
		mFinderInStoreList = (RecyclerView) view.findViewById(R.id.storeList);
		mFinderInStoreList.setLayoutManager(new LinearLayoutManager(getActivity()));
		tvNoResult = (WTextView) view.findViewById(R.id.tvNoResult);
		pStoreProgressBar = (ProgressBar) view.findViewById(R.id.storesProgressBar);
		rlProgressBar = (RelativeLayout) view.findViewById(R.id.rlProgressBar);
		pStoreProgressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
	}

	private void getData(List<StoreDetails> mStoreDetailList) {
		this.mStoreDetailList = mStoreDetailList;
		if (mStoreDetailList != null && mStoreDetailList.size() != 0) {
			bindDataWithUI(mStoreDetailList);
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
		getData(storeDetailsList);
	}

	@Override
	public void onFragmentUpdate(Location location, final List<StoreDetails> storeDetails) {
		try {
			StoreFinderListFragment.this.getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					hideProgressBar();
					if (storeDetails.size() > 0) {
						noResultFound(View.GONE);
						getData(storeDetails);
					} else {
						noResultFound(View.VISIBLE);
					}
				}
			});
		} catch (NullPointerException ex) {
			Log.e("StoreFinderList", ex.toString());
		}
	}

	private void noResultFound(int state) {
		tvNoResult.setVisibility(state);
	}

	private void showProgressBar() {
		rlProgressBar.setVisibility(View.VISIBLE);
		mFinderInStoreList.setVisibility(View.GONE);
		pStoreProgressBar.setVisibility(View.VISIBLE);
	}

	private void hideProgressBar() {
		rlProgressBar.setVisibility(View.GONE);
		pStoreProgressBar.setVisibility(View.GONE);
		mFinderInStoreList.setVisibility(View.VISIBLE);
	}
}