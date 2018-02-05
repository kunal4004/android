package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import za.co.woolworths.financial.services.android.models.dto.CartPriceValues;
import za.co.woolworths.financial.services.android.models.dto.DeliveryLocation;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class DeliveryLocationAdapter extends RecyclerView.Adapter<DeliveryLocationAdapter.DeliveryLocationViewHolder> {

	public interface OnItemClick {
		void onItemClick(DeliveryLocation location);
	}

	private OnItemClick onItemClick;
	private ArrayList<DeliveryLocation> deliveryLocationItems;

	public DeliveryLocationAdapter(ArrayList<DeliveryLocation> deliveryLocationItems, OnItemClick onItemClick) {
		this.deliveryLocationItems = deliveryLocationItems;
		this.onItemClick = onItemClick;
	}

	@Override
	public DeliveryLocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new DeliveryLocationViewHolder(LayoutInflater.from(parent.getContext())
					.inflate(R.layout.delivery_location_item, parent, false));
	}

	@Override
	public void onBindViewHolder(final DeliveryLocationViewHolder holder, final int position) {
		final DeliveryLocation location = deliveryLocationItems.get(position);
		holder.tvTitle.setText(location.title);
		holder.tvDescription.setText(location.description);
		holder.deliveryLocationItemLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onItemClick.onItemClick(location);
			}
		});
	}

	@Override
	public int getItemCount() {
		return deliveryLocationItems.size();
	}

	public class DeliveryLocationViewHolder extends RecyclerView.ViewHolder {
		private RelativeLayout deliveryLocationItemLayout;
		private WTextView tvTitle, tvDescription;

		public DeliveryLocationViewHolder(View view) {
			super(view);
			deliveryLocationItemLayout = view.findViewById(R.id.deliveryLocationItemLayout);
			tvTitle = view.findViewById(R.id.tvTitle);
			tvDescription = view.findViewById(R.id.tvDescription);
		}
	}
}
