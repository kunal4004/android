package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ShoppingListItemsNavigator;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

/**
 * Created by W7099877 on 2018/03/09.
 */

public class ShoppingListItemsAdapter extends RecyclerSwipeAdapter<RecyclerView.ViewHolder> {

	private final int ITEM_VIEW_TYPE_HEADER = 0;
	private final int ITEM_VIEW_TYPE_BASIC = 1;

	private List<ShoppingListItem> listItems;
	private ShoppingListItemsNavigator navigator;
	private boolean mShoppingListIsLoading;


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
				holder.cartProductImage.setImageURI(Utils.getExternalImageRef() + listItems.get(position).externalImageURL + "?w=" + 85 + "&q=" + 85);
				ShoppingListItem shoppingListItem = listItems.get(position);
				holder.productName.setText(shoppingListItem.displayName);
				//holder.productDesc.setText(listItems.get(position).description);
				holder.tvQuantity.setText(String.valueOf(shoppingListItem.userQuantity));
				holder.price.setText(WFormatter.formatAmount(shoppingListItem.price));
				holder.select.setChecked(shoppingListItem.isSelected);
				holder.delete.setVisibility(View.VISIBLE);
				holder.progressBar.setVisibility(View.INVISIBLE);

				holder.llShopList.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						int position = holder.getAdapterPosition();
						ShoppingListItem shoppingListItem = listItems.get(position);
						ProductList productList = createProductList(shoppingListItem);
						navigator.openProductDetailFragment(shoppingListItem.displayName, productList);
					}
				});
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
				if (shoppingListItem != null) {
					boolean productInStock = shoppingListItem.quantityInStock != 0;
					holder.llQuantity.setAlpha(productInStock ? 1.0f : 0.5f);
					holder.tvQuantity.setAlpha(productInStock ? 1.0f : 0.5f);
					holder.select.setEnabled(productInStock);
					holder.imPrice.setAlpha(productInStock ? 1.0f : 0.5f);
					if (shoppingListItem.inventoryCallCompleted) {
						holder.llQuantity.setVisibility((shoppingListItem.quantityInStock == 0) ? View.GONE : View.VISIBLE);
						holder.tvProductAvailability.setVisibility((shoppingListItem.quantityInStock == 0) ? View.VISIBLE : View.GONE);
						Utils.setBackgroundColor(holder.tvProductAvailability, R.drawable.round_red_corner, R.string.product_unavailable);
					}
				}
				// Set Color and Size END
				holder.select.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						ShoppingListItem shoppingListItem = listItems.get(position);
						if (shoppingListItem.quantityInStock == 0) return;
						/*
						 1. By default quantity will be ZERO.
						 2. On Selection it will change to ONE.
						 */
						shoppingListItem.userQuantity = shoppingListItem.isSelected ? 0 : 1;
						shoppingListItem.isSelected = !listItems.get(position).isSelected;
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
						ShoppingListItem shoppingListItem = listItems.get(position);
						if (shoppingListItem.quantityInStock == 0) return;
						navigator.onQuantityChangeClick(position, shoppingListItem);
					}
				});
				mItemManger.bindView(holder.itemView, position);
				break;
			default:
				break;
		}
	}

	@NonNull
	private ProductList createProductList(ShoppingListItem shoppingListItem) {
		ProductList productList = new ProductList();
		productList.productId = shoppingListItem.productId;
		productList.productName = shoppingListItem.displayName;
		productList.fromPrice = TextUtils.isEmpty(shoppingListItem.price) ? (float) 0.0 : Float.valueOf(shoppingListItem.price);
		productList.sku = shoppingListItem.Id;
		productList.externalImageRef = Utils.getExternalImageRef() + shoppingListItem.externalImageURL;
		OtherSkus otherSku = new OtherSkus();
		otherSku.price = String.valueOf(shoppingListItem.price);
		otherSku.size = "";
		List<OtherSkus> otherSkuList = new ArrayList<>();
		productList.otherSkus = new ArrayList<>();
		otherSkuList.add(otherSku);
		productList.otherSkus = otherSkuList;
		return productList;
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
		private WTextView tvQuantity;
		private WTextView price;
		private CheckBox select;
		private WrapContentDraweeView cartProductImage;
		private WTextView delete;
		private ProgressBar progressBar;
		private RelativeLayout llQuantity;
		private LinearLayout llShopList;
		private ImageView imPrice;
		private WTextView tvProductAvailability;

		public ViewHolder(View itemView) {
			super(itemView);
			productName = itemView.findViewById(R.id.tvTitle);
			tvQuantity = itemView.findViewById(R.id.tvQuantity);
			price = itemView.findViewById(R.id.tvPrice);
			select = itemView.findViewById(R.id.btnDeleteRow);
			cartProductImage = itemView.findViewById(R.id.cartProductImage);
			tvProductAvailability = itemView.findViewById(R.id.tvProductAvailability);
			delete = itemView.findViewById(R.id.tvDelete);
			llShopList = itemView.findViewById(R.id.llShopList);
			progressBar = itemView.findViewById(R.id.pbDeleteIndicator);
			tvColorSize = itemView.findViewById(R.id.tvColorSize);
			llQuantity = itemView.findViewById(R.id.llQuantity);
			imPrice = itemView.findViewById(R.id.imPrice);
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
		closeAllItems();
	}

	@Override
	public int getItemViewType(int position) {
		return position == 0 ? ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_BASIC;
	}
}
