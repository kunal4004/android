package za.co.woolworths.financial.services.android.ui.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class EditQuantityAdapter extends RecyclerView.Adapter<EditQuantityAdapter.SimpleViewHolder> {

	public interface RecyclerViewClickListener {
		void recyclerViewListClicked(int quantity);
	}

	private RecyclerViewClickListener mRecyclerViewClickListener;
	private List<Integer> mQuantityList;

	public EditQuantityAdapter(List<Integer> quantityList, RecyclerViewClickListener recyclerViewClickListener) {
		this.mQuantityList = quantityList;
		this.mRecyclerViewClickListener = recyclerViewClickListener;
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
		//skipping the filling of the view
		holder.productName.setText(String.valueOf(mQuantityList.get(position)));
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int position = holder.getAdapterPosition();
				mRecyclerViewClickListener.recyclerViewListClicked(mQuantityList.get(position));
			}
		});
	}

	@Override
	public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.quantity_item, parent, false);
		return new SimpleViewHolder(v);
	}

	@Override
	public int getItemCount() {
		if (mQuantityList != null)
			return mQuantityList.size();
		else
			return 0;
	}
}