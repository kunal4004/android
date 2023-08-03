package za.co.woolworths.financial.services.android.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.util.List;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.activities.StoreDetailsActivity;
import za.co.woolworths.financial.services.android.ui.adapters.StockFinderListAdapter;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.RecycleViewClickListner;
import za.co.woolworths.financial.services.android.util.UpdateStoreFinderFragment;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.ui.fragments.StoreFinderMapFragment.CARD_CONTACT_INFO;

public class StoreFinderListFragment extends Fragment implements UpdateStoreFinderFragment {

	private String mCardContactInfo = null;

	public static StoreFinderListFragment newInstance(String cardContactInfo) {
		StoreFinderListFragment myFragment = new StoreFinderListFragment();
		Bundle args = new Bundle();
		args.putString(CARD_CONTACT_INFO, cardContactInfo);
		myFragment.setArguments(args);
		return myFragment;
	}

	private RecyclerView mFinderInStoreList;
	private List<StoreDetails> mStoreDetailList;
	private WGlobalState wGlobalState;
	private boolean listReceiveUpdate = false;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle argument = getArguments();
		if (argument != null)
			mCardContactInfo = getArguments().getString(CARD_CONTACT_INFO, "");

	}


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
		wGlobalState = ((WoolworthsApplication) getActivity().getApplication()).getWGlobalState();
		// handle store card
		TextView tvFlStockFinderMapHeader = view.findViewById(R.id.flStockFinderMapHeader);
		if (!TextUtils.isEmpty(mCardContactInfo)) {
			tvFlStockFinderMapHeader.setVisibility(View.VISIBLE);
			tvFlStockFinderMapHeader.setText(mCardContactInfo);
			Linkify.addLinks(tvFlStockFinderMapHeader, Linkify.ALL);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.STORES_SEARCH);
	}

	private void init(View view) {
		mFinderInStoreList = (RecyclerView) view.findViewById(R.id.storeList);
		mFinderInStoreList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

	}

	private void getData(List<StoreDetails> mStoreDetailList) {
		bindDataWithUI(mStoreDetailList);
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
				Intent cardDetailIntent = new Intent(getActivity(), StoreDetailsActivity.class);
				cardDetailIntent.putExtra("store", store);
				cardDetailIntent.putExtra("FromStockLocator", true);
				getActivity().startActivity(cardDetailIntent);
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
	public void onFragmentUpdate() {
		Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (!listReceiveUpdate) {
						mStoreDetailList = wGlobalState.getStoreDetailsArrayList();
						if (mStoreDetailList.size() > 0) {
							getData(mStoreDetailList);
						}
						listReceiveUpdate = true;
					}
				}
			});
		}
	}
}