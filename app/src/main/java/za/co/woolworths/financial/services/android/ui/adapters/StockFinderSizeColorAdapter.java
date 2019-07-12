package za.co.woolworths.financial.services.android.ui.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class StockFinderSizeColorAdapter extends RecyclerView.Adapter<StockFinderSizeColorAdapter.SimpleViewHolder> {

	private String mFilterType;

	public interface RecyclerViewClickListener {
		void recyclerViewListClicked(View v, int position);
	}

	private RecyclerViewClickListener mRecyclerViewClickListener;
	private final ArrayList<OtherSkus> mOtherSKu;

	public StockFinderSizeColorAdapter(ArrayList<OtherSkus> otherSkus, RecyclerViewClickListener recyclerViewClickListener, String filterType) {
		this.mOtherSKu = otherSkus;
		this.mRecyclerViewClickListener = recyclerViewClickListener;
		this.mFilterType = filterType;

	}

	static class SimpleViewHolder extends RecyclerView.ViewHolder {

		WTextView productName;

		SimpleViewHolder(View view) {
			super(view);
			productName = view.findViewById(R.id.name);
		}
	}

	@Override
	public void onBindViewHolder(final SimpleViewHolder holder, final int position) {
		String item;
		if (mFilterType.equalsIgnoreCase("color")) {
			item = mOtherSKu.get(position).colour;
		} else {
			item = mOtherSKu.get(position).size;
		}

		if (TextUtils.isEmpty(item)) {
			item = "";
		} else {
			item = item.trim();
		}

		//skipping the filling of the view
		holder.productName.setText(item);
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int position = holder.getAdapterPosition();
				mRecyclerViewClickListener.recyclerViewListClicked(v, position);
			}
		});
	}

	@Override
	public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.color_item, parent, false);
		return new SimpleViewHolder(v);
	}

	@Override
	public int getItemCount() {
		if (mOtherSKu != null)
			return mOtherSKu.size();
		else
			return 0;
	}
}