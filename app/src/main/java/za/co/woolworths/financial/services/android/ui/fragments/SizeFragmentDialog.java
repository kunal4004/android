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

import za.co.woolworths.financial.services.android.models.dto.OtherSku;
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity;
import za.co.woolworths.financial.services.android.ui.adapters.StockFinderSizeColorAdapter;
import za.co.woolworths.financial.services.android.util.ColorInterface;

public class SizeFragmentDialog extends Fragment implements StockFinderSizeColorAdapter.RecyclerViewClickListener, ColorInterface {

	private WStockFinderActivity.RecyclerItemSelected mRecyclerItemSelected;
	private RecyclerView mSizeRecycleView;
	private SizeFragmentDialog mContext;
	private StockFinderSizeColorAdapter stockFinderSizeColorAdapter;
	private ArrayList<OtherSku> mOtherSKUList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.color_fragment, container, false);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mContext = this;
		try {
			mRecyclerItemSelected = (WStockFinderActivity.RecyclerItemSelected) this.getActivity();
		} catch (ClassCastException ignored) {
		}


		mSizeRecycleView = (RecyclerView) view.findViewById(R.id.recyclerColorList);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		mSizeRecycleView.setLayoutManager(mLayoutManager);
	}

	@Override
	public void recyclerViewListClicked(View v, int position) {
		mRecyclerItemSelected.onRecyclerItemClick(v, position, "size");
	}

	@Override
	public void onUpdate(final ArrayList<OtherSku> otherSkuList, final String viewType) {
		mOtherSKUList = otherSkuList;
		stockFinderSizeColorAdapter = new StockFinderSizeColorAdapter(mOtherSKUList, mContext, viewType);
		mSizeRecycleView.setAdapter(stockFinderSizeColorAdapter);
	}
}