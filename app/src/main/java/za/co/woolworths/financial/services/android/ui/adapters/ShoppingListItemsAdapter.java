package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
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

	private List<ShoppingListItem> mShoppingListItem;
	private ShoppingListItemsNavigator navigator;
	private boolean mAdapterIsClickable;


	public ShoppingListItemsAdapter(List<ShoppingListItem> shoppingListItems, ShoppingListItemsNavigator navigator) {
		mShoppingListItem = shoppingListItems;
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
				ShoppingListItem shoppingListItem = getItem(position);
				if (shoppingListItem == null) return;
				if (shoppingListItem.displayName == null) return;
				holder.cartProductImage.setImageURI(Utils.getExternalImageRef() + shoppingListItem.externalImageURL + "?w=" + 85 + "&q=" + 85);
				holder.productName.setText(shoppingListItem.displayName);
				holder.tvQuantity.setText(String.valueOf(shoppingListItem.userQuantity));
				holder.price.setText(WFormatter.formatAmount(shoppingListItem.price));
				holder.select.setChecked(shoppingListItem.isSelected);
				holder.delete.setVisibility(View.VISIBLE);
				holder.progressBar.setVisibility(View.INVISIBLE);

				holder.llShopList.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (!mAdapterIsClickable) return;
						int position = holder.getAdapterPosition();
						ShoppingListItem shoppingListItem = getItem(position);
						ProductList productList = createProductList(shoppingListItem);
						navigator.openProductDetailFragment(shoppingListItem.displayName, productList);
					}
				});
				// Set Color and Size START
				String sizeColor = shoppingListItem.color;
				if (sizeColor == null)
					sizeColor = "";
				if (sizeColor.isEmpty() && !shoppingListItem.size.isEmpty() && !shoppingListItem.size.equalsIgnoreCase("NO SZ"))
					sizeColor = shoppingListItem.size;
				else if (!sizeColor.isEmpty() && !shoppingListItem.size.isEmpty() && !shoppingListItem.size.equalsIgnoreCase("NO SZ"))
					sizeColor = sizeColor + ", " + shoppingListItem.size;

				holder.tvColorSize.setText(sizeColor);
				holder.tvColorSize.setVisibility(View.VISIBLE);
				/****
				 * shoppingListItem.userShouldSetSuburb - is set to true when user did not select any suburb
				 */
				if (shoppingListItem.userShouldSetSuburb) {
					holder.tvProductAvailability.setVisibility(View.GONE);
					holder.llQuantity.setVisibility(View.VISIBLE);
					holder.llQuantity.setAlpha(1.0f);
					holder.select.setEnabled(true);
					holder.llQuantity.setEnabled(true);
				} else {
					if (shoppingListItem != null) {
						boolean productInStock = shoppingListItem.quantityInStock != 0;
						holder.llQuantity.setAlpha(productInStock ? 1.0f : 0.5f);
						holder.tvQuantity.setAlpha(productInStock ? 1.0f : 0.5f);
						holder.select.setEnabled(productInStock);
						holder.imPrice.setAlpha(productInStock ? 1.0f : 0.5f);
						if (shoppingListItem.inventoryCallCompleted) {
							holder.llQuantity.setVisibility((shoppingListItem.quantityInStock == 0) ? View.GONE : View.VISIBLE);
							holder.tvProductAvailability.setVisibility((shoppingListItem.quantityInStock == 0) ? View.VISIBLE : View.GONE);
							holder.select.setAlpha((shoppingListItem.quantityInStock == 0) ? 0f : 1f);
							holder.price.setAlpha((shoppingListItem.quantityInStock == 0) ? 0f : 1f);
							Utils.setBackgroundColor(holder.tvProductAvailability, R.drawable.round_red_corner, R.string.product_unavailable);
						} else {
							holder.llQuantity.setVisibility(View.VISIBLE);
							holder.tvProductAvailability.setVisibility(View.GONE);
						}
					}
				}
				// Set Color and Size END
				holder.select.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (!mAdapterIsClickable) return;
						ShoppingListItem shoppingListItem = getItem(position);
						if (!shoppingListItem.isSelected) {
							if (shoppingListItem.userShouldSetSuburb) {
								shoppingListItem.isSelected = false;
								int currentPosition = position - 1;
								notifyItemRangeChanged(currentPosition, mShoppingListItem.size());
								navigator.openSetSuburbProcess(shoppingListItem);
								return;
							}
						}
						if (shoppingListItem.quantityInStock == 0) return;
						/*
						 1. By default quantity will be ZERO.
						 2. On Selection it will change to ONE.
						 */
						shoppingListItem.userQuantity = shoppingListItem.isSelected ? 0 : 1;
						shoppingListItem.isSelected = !shoppingListItem.isSelected;
						notifyDataSetChanged();
						navigator.onItemSelectionChange(mShoppingListItem);
					}
				});

				holder.delete.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (!mAdapterIsClickable) return;
						holder.delete.setVisibility(View.INVISIBLE);
						holder.progressBar.setVisibility(View.VISIBLE);
						navigator.onItemDeleteClick(getItem(position).Id, getItem(position).productId, getItem(position).catalogRefId);
					}
				});
				holder.llQuantity.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (!mAdapterIsClickable) return;
						ShoppingListItem shoppingListItem = getItem(position);
						if (shoppingListItem.userShouldSetSuburb) {
							navigator.openSetSuburbProcess(shoppingListItem);
							return;
						}
						if (shoppingListItem.quantityInStock == 0) return;
						int index = position;
						index -= 1;
						navigator.onQuantityChangeClick(index, shoppingListItem);
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
		productList.fromPrice = Float.valueOf((TextUtils.isEmpty(shoppingListItem.price) ? "0.0" : shoppingListItem.price));
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
		return mShoppingListItem.size() + 1;
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
		/***
		 * Update old list with new list before refreshing the adapter
		 */
		if (updatedListItems == null) return;
		if (mShoppingListItem == null) return;
		try {
			for (ShoppingListItem shoppinglistItem : mShoppingListItem) {
				for (ShoppingListItem updatedList : updatedListItems) {
					if (shoppinglistItem.catalogRefId.equalsIgnoreCase(updatedList.catalogRefId)) {
						updatedList.inventoryCallCompleted = shoppinglistItem.inventoryCallCompleted;
						updatedList.userQuantity = shoppinglistItem.userQuantity;
						updatedList.quantityInStock = shoppinglistItem.quantityInStock;
						updatedList.delivery_location = shoppinglistItem.delivery_location;
						updatedList.userShouldSetSuburb = shoppinglistItem.userShouldSetSuburb;
						updatedList.isSelected = shoppinglistItem.isSelected;
					}
				}
			}
			this.mShoppingListItem = updatedListItems;
			this.navigator.onItemSelectionChange(mShoppingListItem);
			notifyDataSetChanged();
			closeAllItems();
		} catch (IllegalArgumentException ex) {
			Log.e("updateList", ex.toString());
		}
	}

	@Override
	public int getItemViewType(int position) {
		return isPositionHeader(position) ? ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_BASIC;
	}

	public void adapterClickable(boolean clickable) {
		this.mAdapterIsClickable = clickable;
	}

	private boolean isPositionHeader(int position) {
		return position == 0;
	}

	private ShoppingListItem getItem(int position) {
		return mShoppingListItem.get(position - 1);
	}

	public List<ShoppingListItem> getShoppingListItems() {
		return mShoppingListItem;
	}
}
