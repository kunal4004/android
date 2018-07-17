package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v7.widget.RecyclerView;
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


	public interface OnColorSelection {
		void onColorSelected(String color);
	}

	private OnColorSelection onColorSelection;
	private ArrayList<String> colorArray;

	public ProductColorPickerAdapter(ArrayList<String> colorArray, OnColorSelection onColorSelection) {
		this.onColorSelection = onColorSelection;
		this.colorArray = colorArray;

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
		holder.productName.setText(colorArray.get(position));
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int position = holder.getAdapterPosition();
				onColorSelection.onColorSelected(colorArray.get(position));
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
		return colorArray.size();
	}
}