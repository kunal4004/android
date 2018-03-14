package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.ShoppingListNavigator;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

/**
 * Created by W7099877 on 2018/03/09.
 */

public class ShoppingListItemsAdapter extends RecyclerView.Adapter<ShoppingListItemsAdapter.ViewHolder> {

	private List<ShoppingListItem> listItems;

	public ShoppingListItemsAdapter(List<ShoppingListItem> listItems) {
		this.listItems=listItems;
	}

	@Override
	public ShoppingListItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ShoppingListItemsAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.shopping_list_commerce_item, parent, false));
	}

	@Override
	public void onBindViewHolder(ShoppingListItemsAdapter.ViewHolder holder, int position) {
		holder.productName.setText(listItems.get(position).displayName);
		holder.productDesc.setText(listItems.get(position).description);
		holder.quantity.setText(listItems.get(position).quantityDesired);
		//holder.productName.setText(listItems.get(position).displayName);

	}


	@Override
	public int getItemCount() {
		return listItems.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder{
		private WTextView productName;
		private WTextView productDesc;
		private WTextView quantity;
		private WTextView price;
		private WTextView offerPrice;


		public ViewHolder(View itemView) {
			super(itemView);
			productName=itemView.findViewById(R.id.tvTitle);
			productDesc=itemView.findViewById(R.id.tvDetails);
			quantity=itemView.findViewById(R.id.tvQuantity);
			price=itemView.findViewById(R.id.tvWasPrice);
			offerPrice=itemView.findViewById(R.id.tvPrice);
		}
	}
}
