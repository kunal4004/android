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

import za.co.woolworths.financial.services.android.models.dto.OtherSku;
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity;
import za.co.woolworths.financial.services.android.ui.adapters.StockFinderSizeColorAdapter;

public class ColorFragmentDialog extends Fragment implements StockFinderSizeColorAdapter.RecyclerViewClickListener {

	private WStockFinderActivity.RecyclerItemSelected mRecyclerItemSelected;
	private StockFinderSizeColorAdapter mColorSizeAdapter;
	public static ColorFragmentDialog newInstance(String text, String filter_type) {
		ColorFragmentDialog f = new ColorFragmentDialog();
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

		RecyclerView mRecyclerColorList = (RecyclerView) view.findViewById(R.id.recyclerColorList);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		mRecyclerColorList.setLayoutManager(mLayoutManager);

		String mFilterType = getArguments().getString("filter_type");
		String mColorList = getArguments().getString("msg");
		mColorSizeAdapter = new StockFinderSizeColorAdapter(getOtherSKUList(mColorList), this, mFilterType);
		mRecyclerColorList.setAdapter(mColorSizeAdapter);
	}

	private ArrayList<OtherSku> getOtherSKUList(String item) {
		return new Gson().fromJson(item, new TypeToken<ArrayList<OtherSku>>() {
		}.getType());
	}

	@Override
	public void recyclerViewListClicked(View v, int position) {
		mRecyclerItemSelected.onRecyclerItemClick(v, position, "color");
	}

	public void resetIndex() {
		if (mColorSizeAdapter != null)
			mColorSizeAdapter.setIndex(-1);
	}
}