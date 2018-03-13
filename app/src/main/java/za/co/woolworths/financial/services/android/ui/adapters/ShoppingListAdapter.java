package za.co.woolworths.financial.services.android.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.ShoppingListNavigator;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ShoppingListItemsNavigator;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

/**
 * Created by W7099877 on 2018/03/08.
 */

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

	private ShoppingListNavigator shoppingListNavigator;
	private List<ShoppingList> lists;
	public ShoppingListAdapter(ShoppingListNavigator shoppingListNavigator, List<ShoppingList> lists) {
		this.shoppingListNavigator= shoppingListNavigator;
		this.lists=lists;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.shopping_list_item, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, final int position) {
		holder.listName.setText(lists.get(position).listName);
		holder.listCount.setText(lists.get(position).listCount);
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				shoppingListNavigator.onListItemSelected(lists.get(position).listName,lists.get(position).listId);
			}
		});

	}


	@Override
	public int getItemCount() {
		return lists.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder{
		public WTextView listName;
		public WTextView listCount;
		public WTextView lastModified;
		public ViewHolder(View itemView) {
			super(itemView);
			listName=itemView.findViewById(R.id.listName);
			listCount=itemView.findViewById(R.id.listItemCount);
			lastModified=itemView.findViewById(R.id.listLastModified);
		}
	}
}
