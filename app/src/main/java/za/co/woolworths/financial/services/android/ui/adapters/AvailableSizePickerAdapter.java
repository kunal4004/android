package za.co.woolworths.financial.services.android.ui.adapters;

import android.graphics.Paint;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.CurrencyFormatter;
import za.co.woolworths.financial.services.android.util.WFormatter;

/**
 * Created by W7099877 on 2018/07/17.
 */

public class AvailableSizePickerAdapter extends RecyclerView.Adapter<AvailableSizePickerAdapter.SimpleViewHolder> {


	public interface OnAvailableSizeSelection {
		void onAvailableSizeSelected(OtherSkus selectedAvailableSizeSku);

		void onFindInStoreForNotAvailableProducts(OtherSkus notAvailableSKU);
	}

	private OnAvailableSizeSelection onAvailableSizeSelection;
	private ArrayList<OtherSkus> otherSkuses;

	public AvailableSizePickerAdapter(ArrayList<OtherSkus> otherSkuses, OnAvailableSizeSelection onAvailableSizeSelection) {
		this.onAvailableSizeSelection = onAvailableSizeSelection;
		this.otherSkuses = otherSkuses;
	}

	static class SimpleViewHolder extends RecyclerView.ViewHolder {

		WTextView productSize, productPrice, outOfStockText;

		SimpleViewHolder(View view) {
			super(view);
			productSize = view.findViewById(R.id.name);
			productPrice = view.findViewById(R.id.tvPrice);
			outOfStockText = view.findViewById(R.id.outOfStock);
		}
	}

	@Override
	public void onBindViewHolder(final AvailableSizePickerAdapter.SimpleViewHolder holder, final int position) {


		boolean isQuantityAvailable = otherSkuses.get(position).quantity > 0;
		holder.productSize.setText(otherSkuses.get(position).size);
		holder.productPrice.setText(isQuantityAvailable ? CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace(otherSkuses.get(position).price) : holder.productPrice.getContext().getString(R.string.stock_finder_btn_label));
		holder.productSize.setAlpha(isQuantityAvailable ? 1f : 0.5f);
		holder.productPrice.setAlpha(isQuantityAvailable ? 0.6f : 1f);
		holder.productSize.setPaintFlags(isQuantityAvailable ? Paint.ANTI_ALIAS_FLAG : holder.productSize.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		holder.outOfStockText.setVisibility(isQuantityAvailable ? View.GONE : View.VISIBLE);
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int position = holder.getAdapterPosition();
				if (otherSkuses.get(position).quantity > 0) {
					onAvailableSizeSelection.onAvailableSizeSelected(otherSkuses.get(position));
				} else {
					onAvailableSizeSelection.onFindInStoreForNotAvailableProducts(otherSkuses.get(position));
				}
			}
		});
	}

	@Override
	public AvailableSizePickerAdapter.SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.size_item, parent, false);
		return new AvailableSizePickerAdapter.SimpleViewHolder(v);
	}

	@Override
	public int getItemCount() {
		return otherSkuses.size();
	}
}