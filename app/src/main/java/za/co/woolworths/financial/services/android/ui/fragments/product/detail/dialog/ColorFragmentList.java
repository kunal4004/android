package za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity;
import za.co.woolworths.financial.services.android.ui.adapters.StockFinderSizeColorAdapter;
import za.co.woolworths.financial.services.android.util.ColorInterface;

public class ColorFragmentList extends Fragment implements StockFinderSizeColorAdapter.RecyclerViewClickListener, ColorInterface {

	private WStockFinderActivity.RecyclerItemSelected mRecyclerItemSelected;
	public RecyclerView mRecyclerColorList;
	private ColorFragmentList mContext;
	private WGlobalState wGlobalState;
	private ArrayList<OtherSkus> colorSKUList;

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
		mRecyclerColorList = view.findViewById(R.id.recyclerColorList);
		mRecyclerColorList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		StockFinderSizeColorAdapter stockFinderSizeColorAdapter = new StockFinderSizeColorAdapter(colorSKUList, mContext, "color");
		mRecyclerColorList.setAdapter(stockFinderSizeColorAdapter);
	}

	@Override
	public void recyclerViewListClicked(View v, int position) {
		mRecyclerItemSelected.onRecyclerItemClick(v, position, ColorFragmentList.this.getActivity().getResources().getString(R.string.color));
	}


	@Override
	public void onUpdate(ArrayList<OtherSkus> otherSkuList, String viewType, boolean shouldShowPrice) {

	}

	@Override
	public void onUpdate(List<Integer> quantityList) {

	}

	@Override
	public void onUpdate(ShoppingListItem shoppingListItem) {

	}
}