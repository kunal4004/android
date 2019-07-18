package za.co.woolworths.financial.services.android.ui.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.WFormatter;

/**
 * Created by W7099877 on 2018/07/16.
 */

public class ProductSizePickerAdapter extends RecyclerView.Adapter<ProductSizePickerAdapter.SimpleViewHolder> {


	public interface OnSizeSelection {
		void onSizeSelected(OtherSkus selectedSizeSku);

		void onSizeSelectedForShoppingList(OtherSkus selectedSizeSku);

		void onSizeSelectedForFindInStore(OtherSkus selectedSizeSku);
	}

	private ProductSizePickerAdapter.OnSizeSelection onSizeSelection;
	private ArrayList<OtherSkus> otherSkuses;
	private boolean isForShoppingList;
	boolean isForFindInStore;

	/*public ProductSizePickerAdapter(ArrayList<OtherSkus> otherSkuses, ProductSizePickerAdapter.OnSizeSelection onSizeSelection) {
		this.onSizeSelection = onSizeSelection;
		this.otherSkuses = otherSkuses;
	}*/

	public ProductSizePickerAdapter(ArrayList<OtherSkus> otherSkuses, OnSizeSelection onSizeSelection, boolean isForShoppingList, boolean isForFindInStore) {
		this.onSizeSelection = onSizeSelection;
		this.otherSkuses = otherSkuses;
		this.isForShoppingList = isForShoppingList;
		this.isForFindInStore = isForFindInStore;
	}

	static class SimpleViewHolder extends RecyclerView.ViewHolder {

		WTextView productSize, productPrice;

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
		holder.productPrice.setText(WFormatter.formatAmount(otherSkuses.get(position).price));
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int position = holder.getAdapterPosition();
				if (isForShoppingList)
					onSizeSelection.onSizeSelectedForShoppingList(otherSkuses.get(position));
				else if (isForFindInStore)
					onSizeSelection.onSizeSelectedForFindInStore(otherSkuses.get(position));
				else
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