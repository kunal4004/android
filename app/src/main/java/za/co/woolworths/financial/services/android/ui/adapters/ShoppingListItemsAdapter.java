package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ShoppingListItemsNavigator;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView;
import za.co.woolworths.financial.services.android.util.WFormatter;

/**
 * Created by W7099877 on 2018/03/09.
 */

public class ShoppingListItemsAdapter extends RecyclerSwipeAdapter<RecyclerView.ViewHolder> {

	private final int ITEM_VIEW_TYPE_HEADER = 0;
	private final int ITEM_VIEW_TYPE_BASIC = 1;

	private List<ShoppingListItem> listItems;
	private ShoppingListItemsNavigator navigator;


	public ShoppingListItemsAdapter(List<ShoppingListItem> listItems, ShoppingListItemsNavigator navigator) {
		this.listItems = listItems;
		this.navigator = navigator;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		RecyclerView.ViewHolder vh;
		switch (viewType) {
			case ITEM_VIEW_TYPE_HEADER:
				vh = getHeaderViewHolder(parent);
				break;

			case ITEM_VIEW_TYPE_BASIC:
				vh = getSimpleViewHolder(parent);
				break;

			default:
				vh = getSimpleViewHolder(parent);
				break;

		}

		return vh;
	}

	private RecyclerView.ViewHolder getHeaderViewHolder(ViewGroup parent) {
		return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(
				R.layout.search_add_to_list_layout, parent, false));
	}

	private RecyclerView.ViewHolder getSimpleViewHolder(ViewGroup parent) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
				R.layout.shopping_list_commerce_item, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
		switch (getItemViewType(position)) {
			case ITEM_VIEW_TYPE_HEADER:
				HeaderViewHolder headerViewHolder = (HeaderViewHolder) viewHolder;
				headerViewHolder.tvSearchText.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						navigator.onShoppingSearchClick();
					}
				});
				break;

			case ITEM_VIEW_TYPE_BASIC:
				final ViewHolder holder = (ViewHolder) viewHolder;
				holder.cartProductImage.setImageURI("https://images.woolworthsstatic.co.za/" + listItems.get(position).externalImageURL + "?w=" + 85 + "&q=" + 85);
				holder.productName.setText(listItems.get(position).displayName);
				//holder.productDesc.setText(listItems.get(position).description);
				holder.quantity.setText(String.valueOf(listItems.get(position).userQuantity));
				holder.price.setText(WFormatter.formatAmount(listItems.get(position).price));
				holder.select.setChecked(listItems.get(position).isSelected);
				holder.delete.setVisibility(View.VISIBLE);
				holder.progressBar.setVisibility(View.INVISIBLE);

				// Set Color and Size START

					String sizeColor = listItems.get(position).color;
					if (sizeColor == null)
						sizeColor = "";
					if (sizeColor.isEmpty() && !listItems.get(position).size.isEmpty() && !listItems.get(position).size.equalsIgnoreCase("NO SZ"))
						sizeColor = listItems.get(position).size;
					else if (!sizeColor.isEmpty() && !listItems.get(position).size.isEmpty() && !listItems.get(position).size.equalsIgnoreCase("NO SZ"))
						sizeColor = sizeColor + ", " + listItems.get(position).size;

					holder.tvColorSize.setText(sizeColor);
					holder.tvColorSize.setVisibility(View.VISIBLE);

				// Set Color and Size END

				holder.select.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						/*
						 1. By default quantity will be ZERO.
						 2. On Selection it will change to ONE.
						 */
						if (listItems.get(position).isSelected) {
							listItems.get(position).userQuantity = 0;
						} else {
							listItems.get(position).userQuantity = 1;
						}

						listItems.get(position).isSelected = !listItems.get(position).isSelected;
						notifyDataSetChanged();
						// 1st item is header of Recycleview
						navigator.onItemSelectionChange(listItems.subList(1, listItems.size()));
					}
				});

				holder.delete.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						holder.delete.setVisibility(View.INVISIBLE);
						holder.progressBar.setVisibility(View.VISIBLE);
						navigator.onItemDeleteClick(listItems.get(position).Id, listItems.get(position).productId, listItems.get(position).catalogRefId);
					}
				});
				holder.llQuantity.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						navigator.onQuantityChangeClick(position);
					}
				});

				mItemManger.bindView(holder.itemView, position);
				break;
			default:
				break;
		}
	}

	@Override
	public int getItemCount() {
		return listItems.size();
	}

	@Override
	public int getSwipeLayoutResourceId(int position) {
		return R.id.swipe;
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		private WTextView productName;
		private WTextView tvColorSize;
		private WTextView quantity;
		private WTextView price;
		private CheckBox select;
		private WrapContentDraweeView cartProductImage;
		private WTextView delete;
		private ProgressBar progressBar;
		private RelativeLayout llQuantity;
		public ViewHolder(View itemView) {
			super(itemView);
			productName = itemView.findViewById(R.id.tvTitle);
			quantity = itemView.findViewById(R.id.tvQuantity);
			price = itemView.findViewById(R.id.tvPrice);
			select = itemView.findViewById(R.id.btnDeleteRow);
			cartProductImage = itemView.findViewById(R.id.cartProductImage);
			cartProductImage = itemView.findViewById(R.id.cartProductImage);
			delete = itemView.findViewById(R.id.tvDelete);
			progressBar = itemView.findViewById(R.id.pbDeleteIndicator);
			tvColorSize=itemView.findViewById(R.id.tvColorSize);
			llQuantity = itemView.findViewById(R.id.llQuantity);
		}
	}

	public class HeaderViewHolder extends RecyclerView.ViewHolder {
		private WTextView tvSearchText;

		public HeaderViewHolder(View itemView) {
			super(itemView);
			tvSearchText = itemView.findViewById(R.id.textProductSearch);
		}
	}

	public void updateList(List<ShoppingListItem> updatedListItems) {
		this.listItems = updatedListItems;
		this.navigator.onItemSelectionChange(listItems.subList(1, listItems.size()));
		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		return position == 0 ? ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_BASIC;
	}

}
