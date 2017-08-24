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

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.OtherSku;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity;
import za.co.woolworths.financial.services.android.ui.adapters.StockFinderSizeColorAdapter;
import za.co.woolworths.financial.services.android.util.ColorInterface;


public class ColorFragmentDialog extends Fragment implements StockFinderSizeColorAdapter.RecyclerViewClickListener, ColorInterface {

	private WStockFinderActivity.RecyclerItemSelected mRecyclerItemSelected;
	public RecyclerView mRecyclerColorList;
	private ColorFragmentDialog mContext;
	private WGlobalState wGlobalState;
	private ArrayList<OtherSku> colorSKUList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.color_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mContext = this;
		wGlobalState = ((WoolworthsApplication) getActivity().getApplication()).getWGlobalState();
		colorSKUList = wGlobalState.getColourSKUArrayList();
		try {
			mRecyclerItemSelected = (WStockFinderActivity.RecyclerItemSelected) this.getActivity();
		} catch (ClassCastException ignored) {
		}

		mRecyclerColorList = (RecyclerView) view.findViewById(R.id.recyclerColorList);
		mRecyclerColorList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		StockFinderSizeColorAdapter stockFinderSizeColorAdapter = new StockFinderSizeColorAdapter(colorSKUList, mContext, "color");
		mRecyclerColorList.setAdapter(stockFinderSizeColorAdapter);
	}

	@Override
	public void recyclerViewListClicked(View v, int position) {
		mRecyclerItemSelected.onRecyclerItemClick(v, position, ColorFragmentDialog.this.getActivity().getResources().getString(R.string.color));
	}

	@Override
	public void onUpdate(final ArrayList<OtherSku> otherSkuList, final String viewType) {

	}
}