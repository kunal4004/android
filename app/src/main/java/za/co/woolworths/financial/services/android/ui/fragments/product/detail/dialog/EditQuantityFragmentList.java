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

import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity;
import za.co.woolworths.financial.services.android.ui.adapters.EditQuantityAdapter;
import za.co.woolworths.financial.services.android.util.ColorInterface;

public class EditQuantityFragmentList extends Fragment implements ColorInterface, EditQuantityAdapter.RecyclerViewClickListener {

	private RecyclerView rclEditQuantity;
	private EditQuantityAdapter mEditQuantityAdapter;
	private EditQuantityFragmentList mContext;
	private WStockFinderActivity.RecyclerItemSelected mRecyclerItemSelected;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.color_fragment, container, false);
		return v;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mContext = this;
		rclEditQuantity = view.findViewById(R.id.recyclerColorList);
		try {
			mRecyclerItemSelected = (WStockFinderActivity.RecyclerItemSelected) this.getActivity();
		} catch (ClassCastException ignored) {
		}
	}


	@Override
	public void onUpdate(ArrayList<OtherSkus> otherSkuList, String viewType, boolean shouldShowPrice) {
	}

	@Override
	public void onUpdate(final List<Integer> quantityList) {
		EditQuantityFragmentList.this.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				rclEditQuantity.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
				mEditQuantityAdapter = new EditQuantityAdapter(quantityList, mContext);
				rclEditQuantity.setAdapter(mEditQuantityAdapter);
				rclEditQuantity.scrollToPosition(0);
			}
		});
	}

	@Override
	public void onUpdate(final ShoppingListItem shoppingListItem) {
		final ArrayList list = new ArrayList();
		for (int index = 0; index < shoppingListItem.quantityInStock; index++) {
			list.add(index + 1);
		}
		EditQuantityFragmentList.this.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				rclEditQuantity.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
				mEditQuantityAdapter = new EditQuantityAdapter(list, mContext);
				rclEditQuantity.setAdapter(mEditQuantityAdapter);
			}
		});
	}

	@Override
	public void recyclerViewListClicked(int quantity) {
		if (mRecyclerItemSelected != null) {
			mRecyclerItemSelected.onQuantitySelected(quantity);
		}
	}
}