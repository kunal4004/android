package za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.adapters.ProductSizePickerAdapter;
import za.co.woolworths.financial.services.android.util.CenterLayoutManager;

/**
 * Created by W7099877 on 2018/07/17.
 */

public class SizeSelectorFragment extends Fragment {

	private RecyclerView mSizeRecycleView;
	private ProductSizePickerAdapter sizePickerAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.color_fragment, container, false);
		return v;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mSizeRecycleView = view.findViewById(R.id.recyclerColorList);
		mSizeRecycleView.setLayoutManager(new CenterLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false, 1500));

	}
}
