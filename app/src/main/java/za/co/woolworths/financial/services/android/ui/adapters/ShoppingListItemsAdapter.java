package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.ShoppingListNavigator;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ShoppingListItemsNavigator;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView;

/**
 * Created by W7099877 on 2018/03/09.
 */

public class ShoppingListItemsAdapter extends RecyclerView.Adapter<ShoppingListItemsAdapter.ViewHolder> {

	private List<ShoppingListItem> listItems;
	private ShoppingListItemsNavigator navigator;

	public ShoppingListItemsAdapter(List<ShoppingListItem> listItems,ShoppingListItemsNavigator navigator) {
		this.listItems=listItems;
		this.navigator=navigator;
		this.navigator.onItemSelectionChange(getButtonStatus());
	}

	@Override
	public ShoppingListItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ShoppingListItemsAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.shopping_list_commerce_item, parent, false));
	}

	@Override
	public void onBindViewHolder(final ShoppingListItemsAdapter.ViewHolder holder, final int position) {
		holder.cartProductImage.setImageURI("https://images.woolworthsstatic.co.za/"+listItems.get(position).externalImageURL+ "?w=" + 85 + "&q=" + 85);
		holder.productName.setText(listItems.get(position).displayName);
		holder.productDesc.setText(listItems.get(position).description);
		holder.quantity.setText(listItems.get(position).quantityDesired);
		//holder.productName.setText(listItems.get(position).displayName);
		holder.select.setChecked(listItems.get(position).isSelected);

		holder.select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				listItems.get(position).isSelected=b;
				notifyDataSetChanged();
				navigator.onItemSelectionChange(getButtonStatus());
			}
		});

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
		private CheckBox select;
		private WrapContentDraweeView cartProductImage;


		public ViewHolder(View itemView) {
			super(itemView);
			productName=itemView.findViewById(R.id.tvTitle);
			productDesc=itemView.findViewById(R.id.tvDetails);
			quantity=itemView.findViewById(R.id.tvQuantity);
			price=itemView.findViewById(R.id.tvWasPrice);
			offerPrice=itemView.findViewById(R.id.tvPrice);
			select=itemView.findViewById(R.id.btnDeleteRow);
			cartProductImage = itemView.findViewById(R.id.cartProductImage);
		}
	}

	public boolean getButtonStatus(){
		for (ShoppingListItem shoppingListItem: listItems) {
			if(shoppingListItem.isSelected)
				return true;
		}
		return false;
	}

}
