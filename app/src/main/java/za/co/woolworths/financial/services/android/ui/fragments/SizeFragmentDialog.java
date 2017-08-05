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
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import za.co.woolworths.financial.services.android.models.dto.OtherSku;
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity;
import za.co.woolworths.financial.services.android.ui.adapters.StockFinderSizeColorAdapter;

public class SizeFragmentDialog extends Fragment implements StockFinderSizeColorAdapter.RecyclerViewClickListener {

	private WStockFinderActivity.RecyclerItemSelected mRecyclerItemSelected;
	private StockFinderSizeColorAdapter mColorSizeAdapter;
	private ArrayList<OtherSku> mOtherSKUList;
	private String mFilterType;
	private RecyclerView mRecyclerColorList;

	public static SizeFragmentDialog newInstance(String text, String filter_type) {
		SizeFragmentDialog f = new SizeFragmentDialog();
		Bundle b = new Bundle();
		b.putString("msg", text);
		b.putString("filter_type", filter_type);
		f.setArguments(b);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.color_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		try {
			mRecyclerItemSelected = (WStockFinderActivity.RecyclerItemSelected) this.getActivity();
		} catch (ClassCastException ignored) {
		}

		mRecyclerColorList = (RecyclerView) view.findViewById(R.id.recyclerColorList);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		mRecyclerColorList.setLayoutManager(mLayoutManager);

		mFilterType = getArguments().getString("filter_type");
		String mColorList = getArguments().getString("msg");
		mOtherSKUList = getOtherSKUList(mColorList);
		updateSizeAdapter(mOtherSKUList, "size");
	}

	private ArrayList<OtherSku> getOtherSKUList(String item) {
		return new Gson().fromJson(item, new TypeToken<ArrayList<OtherSku>>() {
		}.getType());
	}

	@Override
	public void recyclerViewListClicked(View v, int position) {
		mRecyclerItemSelected.onRecyclerItemClick(v, position, "size");
	}

	public void updateSizeAdapter(ArrayList<OtherSku> otherSkus, String mFilterType) {
		mOtherSKUList = otherSkus;
		ArrayList<OtherSku> commonSizeList = new ArrayList<>();
		//remove duplicates
		for (OtherSku os : mOtherSKUList) {
			if (!sizeValueExist(commonSizeList, os.size)) {
				commonSizeList.add(os);
			}
		}
		//sort ascending
		Collections.sort(commonSizeList, new Comparator<OtherSku>() {
			@Override
			public int compare(OtherSku lhs, OtherSku rhs) {
				return lhs.size.compareToIgnoreCase(rhs.size);
			}
		});

		mColorSizeAdapter = new StockFinderSizeColorAdapter(commonSizeList, this, mFilterType);
		mRecyclerColorList.setAdapter(mColorSizeAdapter);
	}

	public void resetIndex() {
		if (mColorSizeAdapter != null)
			mColorSizeAdapter.setIndex(-1);
	}

	private boolean sizeValueExist(ArrayList<OtherSku> list, String name) {
		for (OtherSku item : list) {
			if (item.size.equals(name)) {
				return true;
			}
		}
		return false;
	}

}