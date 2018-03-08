package za.co.woolworths.financial.services.android.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.ShoppingListNavigator;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ShoppingListItemsNavigator;

/**
 * Created by W7099877 on 2018/03/08.
 */

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

	private ShoppingListNavigator shoppingListNavigator;
	public ShoppingListAdapter(ShoppingListNavigator shoppingListNavigator) {
		this.shoppingListNavigator= shoppingListNavigator;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.shopping_list_item, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				shoppingListNavigator.onListItemSelected();
			}
		});

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
