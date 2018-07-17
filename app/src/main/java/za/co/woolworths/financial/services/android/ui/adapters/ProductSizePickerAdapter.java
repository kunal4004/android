package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

/**
 * Created by W7099877 on 2018/07/16.
 */

public class ProductSizePickerAdapter extends RecyclerView.Adapter<ProductSizePickerAdapter.SimpleViewHolder> {


	public interface OnSizeSelection {
		void onSizeSelected(OtherSkus selectedSizeSku);
	}

	private ProductSizePickerAdapter.OnSizeSelection onSizeSelection;
	private ArrayList<OtherSkus> otherSkuses;

	public ProductSizePickerAdapter(ArrayList<OtherSkus> otherSkuses, ProductSizePickerAdapter.OnSizeSelection onSizeSelection) {
		this.onSizeSelection = onSizeSelection;
		this.otherSkuses = otherSkuses;

	}

	static class SimpleViewHolder extends RecyclerView.ViewHolder {

		WTextView productSize,productPrice;

		SimpleViewHolder(View view) {
			super(view);
			productSize = view.findViewById(R.id.name);
			productPrice = view.findViewById(R.id.tvPrice);
		}
	}

	@Override
	public void onBindViewHolder(final ProductSizePickerAdapter.SimpleViewHolder holder, final int position) {


		//skipping the filling of the view
		//holder.productName.setText(colorArray.get(position));
		holder.productSize.setText(otherSkuses.get(position).size);
		holder.productPrice.setText(otherSkuses.get(position).price);
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int position = holder.getAdapterPosition();
				onSizeSelection.onSizeSelected(otherSkuses.get(position));
			}
		});
	}

	@Override
	public ProductSizePickerAdapter.SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.size_item, parent, false);
		return new ProductSizePickerAdapter.SimpleViewHolder(v);
	}

	@Override
	public int getItemCount() {
		return otherSkuses.size();
	}
}