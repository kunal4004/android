package za.co.woolworths.financial.services.android.ui.adapters;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.OtherSku;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class StockFinderSizeColorAdapter extends RecyclerView.Adapter<StockFinderSizeColorAdapter.SimpleViewHolder> {

	private String mFilterType;
	private int row_index = -1;
	int weight = 1; //number of parts in the recycler view.

	public interface RecyclerViewClickListener {
		void recyclerViewListClicked(View v, int position);
	}

	private RecyclerViewClickListener mRecyclerViewClickListener;
	private final ArrayList<OtherSku> mOtherSKu;

	public StockFinderSizeColorAdapter(ArrayList<OtherSku> otherSkus, RecyclerViewClickListener recyclerViewClickListener, String filterType) {
		this.mOtherSKu = otherSkus;
		this.mRecyclerViewClickListener = recyclerViewClickListener;
		this.mFilterType = filterType;

	}

	class SimpleViewHolder extends RecyclerView.ViewHolder {

		WTextView productName;

		SimpleViewHolder(View view) {
			super(view);
			productName = (WTextView) view.findViewById(R.id.name);
		}
	}

	@Override
	public void onBindViewHolder(final SimpleViewHolder holder, final int position) {
		if (row_index == position) {
			holder.itemView.setBackground(ContextCompat.getDrawable(holder.productName.getContext(),
					R.drawable.pressed_bg));
		} else {
			holder.itemView.setBackgroundColor(Color.WHITE);
		}

		String item;
		if (mFilterType.equalsIgnoreCase("color")) {
			item = mOtherSKu.get(position).colour;
		} else {
			item = mOtherSKu.get(position).size;
		}

		if (TextUtils.isEmpty(item))
			item = "";

		//skipping the filling of the view
		holder.productName.setText(item);
		holder.itemView.setOnClickListener(new View.OnClickListener()

		{
			@Override
			public void onClick(View v) {
				int position = holder.getAdapterPosition();
				setIndex(position);
				mRecyclerViewClickListener.recyclerViewListClicked(v, position);
			}
		});
	}

	@Override
	public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.color_item, parent, false);
		return new SimpleViewHolder(rowView);
	}

	@Override
	public int getItemCount() {
		return mOtherSKu.size();
	}

	public void setIndex(int index) {
		row_index = index;
		notifyDataSetChanged();
	}
}