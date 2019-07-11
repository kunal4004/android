package za.co.woolworths.financial.services.android.ui.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class DeliveryLocationAdapter extends RecyclerView.Adapter<DeliveryLocationAdapter.DeliveryLocationViewHolder> {

	public interface OnItemClick {
		void onItemClick(ShoppingDeliveryLocation location);
	}

	private OnItemClick onItemClick;
	private List<ShoppingDeliveryLocation> deliveryLocationItems;

	public DeliveryLocationAdapter(List<ShoppingDeliveryLocation> deliveryLocationItems, OnItemClick onItemClick) {
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
		final ShoppingDeliveryLocation location = deliveryLocationItems.get(position);
		holder.tvTitle.setText(location.suburb.name);
		holder.tvDescription.setText(location.province.name);
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
