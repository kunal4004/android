package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.ShoppingListNavigator;

/**
 * Created by W7099877 on 2018/03/09.
 */

public class ShoppingListItemsAdapter extends RecyclerView.Adapter<ShoppingListItemsAdapter.ViewHolder> {


	@Override
	public ShoppingListItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ShoppingListItemsAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.shopping_list_commerce_item, parent, false));
	}

	@Override
	public void onBindViewHolder(ShoppingListItemsAdapter.ViewHolder holder, int position) {

	}


	@Override
	public int getItemCount() {
		return 5;
	}

	public class ViewHolder extends RecyclerView.ViewHolder{

		public ViewHolder(View itemView) {
			super(itemView);
		}
	}
}
