package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DetailNavigator;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.SelectedProductView;

public class ProductSizeAdapter extends RecyclerView.Adapter<ProductSizeAdapter.SimpleViewHolder> {
	private List<OtherSkus> mProductSizeList;
	private DetailNavigator mDetailNavigator;

	public ProductSizeAdapter(List<OtherSkus> mProductSizeList,
							  DetailNavigator detailNavigator) {
		this.mProductSizeList = mProductSizeList;
		this.mDetailNavigator = detailNavigator;
	}


	class SimpleViewHolder extends RecyclerView.ViewHolder {

		WTextView productName;

		SimpleViewHolder(View view) {
			super(view);
			productName = view.findViewById(R.id.name);
		}
	}

	@Override
	public void onBindViewHolder(final SimpleViewHolder holder, final int position) {
		OtherSkus productSize = mProductSizeList.get(position);
		holder.productName.setText(productSize.size);
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDetailNavigator.onSizeItemClicked(mProductSizeList.get(position));
			}
		});
	}

	@Override
	public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_size_list, parent, false);
		return new SimpleViewHolder(view);
	}

	@Override
	public int getItemCount() {
		return mProductSizeList.size();
	}


}