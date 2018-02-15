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
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity;
import za.co.woolworths.financial.services.android.ui.adapters.StockFinderSizeColorAdapter;
import za.co.woolworths.financial.services.android.util.ColorInterface;

public class SizeFragmentDialog extends Fragment implements StockFinderSizeColorAdapter.RecyclerViewClickListener, ColorInterface {

	private WStockFinderActivity.RecyclerItemSelected mRecyclerItemSelected;
	private RecyclerView mSizeRecycleView;
	private SizeFragmentDialog mContext;
	private StockFinderSizeColorAdapter stockFinderSizeColorAdapter;
	private ArrayList<OtherSkus> mOtherSKUList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.color_fragment, container, false);
		return v;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mContext = this;
		try {
			mRecyclerItemSelected = (WStockFinderActivity.RecyclerItemSelected) this.getActivity();
		} catch (ClassCastException ignored) {
		}

		mSizeRecycleView = view.findViewById(R.id.recyclerColorList);
	}

	@Override
	public void recyclerViewListClicked(View v, int position) {
		mRecyclerItemSelected.onRecyclerItemClick(v, position, "size");
	}

	@Override
	public void onUpdate(final ArrayList<OtherSkus> otherSkuList, final String viewType) {
		SizeFragmentDialog.this.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mOtherSKUList = otherSkuList;
				mSizeRecycleView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
				stockFinderSizeColorAdapter = new StockFinderSizeColorAdapter(mOtherSKUList, mContext, viewType);
				mSizeRecycleView.setAdapter(stockFinderSizeColorAdapter);
				mSizeRecycleView.scrollToPosition(0);
			}
		});
	}

	@Override
	public void onUpdate(List<Integer> quantityList) {

	}

}