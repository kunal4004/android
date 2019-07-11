package za.co.woolworths.financial.services.android.ui.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.ui.views.WTextView;

/**
 * Created by W7099877 on 2018/07/15.
 */

public class ProductColorPickerAdapter extends RecyclerView.Adapter<ProductColorPickerAdapter.SimpleViewHolder> {


	public interface OnItemSelection {
		void onColorSelected(String color);

		void onQuantitySelected(int selectedQuantity);
	}

	private OnItemSelection onItemSelection;
	private ArrayList<String> colorArray;
	private int maxQuantity;
	private boolean isQuantityType;

	public ProductColorPickerAdapter(ArrayList<String> colorArray, OnItemSelection onItemSelection) {
		this.onItemSelection = onItemSelection;
		this.colorArray = colorArray;
	}

	public ProductColorPickerAdapter(int maxQuantity, OnItemSelection onItemSelection) {
		this.onItemSelection = onItemSelection;
		this.maxQuantity = maxQuantity;
		this.isQuantityType = true;

	}

	static class SimpleViewHolder extends RecyclerView.ViewHolder {

		WTextView productName;

		SimpleViewHolder(View view) {
			super(view);
			productName = view.findViewById(R.id.name);
		}
	}

	@Override
	public void onBindViewHolder(final ProductColorPickerAdapter.SimpleViewHolder holder, final int position) {


		//skipping the filling of the view
		holder.productName.setText(isQuantityType ? String.valueOf(position+1) : colorArray.get(position));
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int position = holder.getAdapterPosition();

				if (isQuantityType)
					onItemSelection.onQuantitySelected(position+1);
				else
					onItemSelection.onColorSelected(colorArray.get(position));
			}
		});
	}

	@Override
	public ProductColorPickerAdapter.SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.color_item, parent, false);
		return new ProductColorPickerAdapter.SimpleViewHolder(v);
	}

	@Override
	public int getItemCount() {
		return isQuantityType ? maxQuantity : colorArray.size();
	}
}