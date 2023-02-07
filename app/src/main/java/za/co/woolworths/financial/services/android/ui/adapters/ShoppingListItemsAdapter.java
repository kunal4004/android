package za.co.woolworths.financial.services.android.ui.adapters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.awfs.coordination.R;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ShoppingListItemsNavigator;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView;
import za.co.woolworths.financial.services.android.util.CurrencyFormatter;
import za.co.woolworths.financial.services.android.util.KotlinUtils;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager;
import za.co.woolworths.financial.services.android.util.wenum.Delivery;

/**
 * Created by W7099877 on 2018/03/09.
 */

public class ShoppingListItemsAdapter extends RecyclerSwipeAdapter<RecyclerView.ViewHolder> {

	private final int ITEM_VIEW_TYPE_HEADER = 0;
	private final int ITEM_VIEW_TYPE_BASIC = 1;

	private ArrayList<ShoppingListItem> mShoppingListItem;
	private final ShoppingListItemsNavigator navigator;
	private boolean mAdapterIsClickable;


	public ShoppingListItemsAdapter(ArrayList<ShoppingListItem> shoppingListItems, ShoppingListItemsNavigator navigator) {
		this.mShoppingListItem = shoppingListItems;
		this.navigator = navigator;

	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		RecyclerView.ViewHolder vh;
		switch (viewType) {
			case ITEM_VIEW_TYPE_HEADER:
				vh = getHeaderViewHolder(parent);
				break;

			case ITEM_VIEW_TYPE_BASIC:
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
				headerViewHolder.tvSearchText.setOnClickListener(view -> navigator.onShoppingSearchClick());
				break;

			case ITEM_VIEW_TYPE_BASIC:
				final ViewHolder holder = (ViewHolder) viewHolder;
				final ShoppingListItem shoppingListItem = getItem(position);
				if (shoppingListItem == null) return;
				if (shoppingListItem.displayName == null) return;
				holder.cartProductImage.setImageURI(shoppingListItem.externalImageRefV2);
				holder.productName.setText(shoppingListItem.displayName);
				holder.tvQuantity.setText(String.valueOf(shoppingListItem.userQuantity));
				holder.price.setText(CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace(shoppingListItem.price));
				holder.cbxSelectShoppingListItem.setChecked(shoppingListItem.isSelected);
				holder.delete.setVisibility(VISIBLE);
				holder.progressBar.setVisibility(View.INVISIBLE);
				holder.tvProductAvailability.setVisibility(GONE);
				holder.swipeLayout.setTopSwipeEnabled(false);

				holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, holder.swipeRight);

				// Set Color and Size START
				String sizeColor = shoppingListItem.color;
				if (sizeColor == null)
					sizeColor = "";
				if (sizeColor.isEmpty() && !shoppingListItem.size.isEmpty() && !shoppingListItem.size.equalsIgnoreCase("NO SZ"))
					sizeColor = shoppingListItem.size;
				else if (!sizeColor.isEmpty() && !shoppingListItem.size.isEmpty() && !shoppingListItem.size.equalsIgnoreCase("NO SZ"))
					sizeColor = sizeColor + ", " + shoppingListItem.size;

				holder.tvColorSize.setText(sizeColor);
				holder.tvColorSize.setVisibility(VISIBLE);
				/****
				 * shoppingListItem.userShouldSetSuburb - is set to true when user did not cbxSelectShoppingListItem any suburb
				 */

				if (userShouldSetSuburb()) {
					holder.llQuantity.setVisibility(VISIBLE);
					holder.llQuantity.setAlpha(1.0f);
					holder.cbxSelectShoppingListItem.setEnabled(true);
					adapterClickable(true);
					holder.cbxSelectShoppingListItem.setAlpha(1.0f);
					holder.llQuantity.setEnabled(true);
				} else {
					boolean productInStock = shoppingListItem.quantityInStock != 0;
					holder.llQuantity.setAlpha(productInStock ? 1.0f : 0.5f);
					holder.tvQuantity.setAlpha(productInStock ? 1.0f : 0.5f);
					holder.cbxSelectShoppingListItem.setEnabled(productInStock);
					holder.pbQuantityLoader.setVisibility(VISIBLE);
					holder.cbxSelectShoppingListItem.setEnabled(false);
					holder.pbQuantityLoader.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
					if (shoppingListItem.inventoryCallCompleted) {
						int inventoryQueryStatus = shoppingListItem.quantityInStock;
						holder.pbQuantityLoader.setVisibility(GONE);
						holder.cbxSelectShoppingListItem.setEnabled(true);
						if (inventoryQueryStatus == -1) {
							holder.llQuantity.setVisibility(GONE);
							holder.cbxSelectShoppingListItem.setVisibility(GONE);
							holder.tvColorSize.setVisibility(GONE);
							holder.tvProductAvailability.setVisibility(VISIBLE);
							holder.price.setAlpha(0f);
							holder.price.setVisibility(GONE);
						} else {
							holder.llQuantity.setVisibility((shoppingListItem.quantityInStock == 0 || !shoppingListItem.isSelected) ? GONE : VISIBLE);
							holder.minusDeleteCountImage.setImageResource(
									shoppingListItem.userQuantity == 1
											&& shoppingListItem.isSelected ?
											R.drawable.delete_24 : R.drawable.ic_minus_black
							);
							int padding = (int) holder.minusDeleteCountImage.getContext()
									.getResources().getDimension(shoppingListItem.userQuantity == 1
											&& shoppingListItem.isSelected ? R.dimen.seven_dp : R.dimen.ten_dp);
							holder.minusDeleteCountImage.setPadding(padding, padding, padding, padding);
							holder.minusDeleteCountImage.setVisibility(shoppingListItem.isSelected ? VISIBLE : GONE);
							holder.addCountImage.setVisibility(
									(shoppingListItem.quantityInStock == 1 ||
											shoppingListItem.userQuantity == shoppingListItem.quantityInStock)
											? GONE : VISIBLE
							);
							holder.tvProductAvailability.setVisibility((shoppingListItem.quantityInStock == 0) ? VISIBLE : GONE);
							holder.cbxSelectShoppingListItem.setVisibility((shoppingListItem.quantityInStock == 0) ? GONE : VISIBLE);
							holder.price.setVisibility((shoppingListItem.quantityInStock == 0) ? GONE : VISIBLE);
							holder.price.setAlpha(1f);
							holder.tvColorSize.setVisibility(VISIBLE);
						}

						int msg = getUnavailableMessage(shoppingListItem.unavailable);
						Utils.setBackgroundColor(
								holder.tvProductAvailability,
								R.drawable.delivery_round_btn_black,
								msg
						);
					} else {
						holder.llQuantity.setVisibility(GONE);
						holder.tvProductAvailability.setVisibility(GONE);
						holder.cbxSelectShoppingListItem.setEnabled(false);
					}
				}

				if (!userShouldSetSuburb())
					if (!shoppingListItem.inventoryCallCompleted) {
						holder.llQuantity.setAlpha(0.5f);
						holder.tvQuantity.setAlpha(0.5f);
					}

				holder.minusDeleteCountImage.setOnClickListener(view -> {
					ShoppingListItem listItem = getItem(position);
					if (enableClickEvent(listItem))
						return;
					if (!mAdapterIsClickable) return;
					if (userShouldSetSuburb()) {
						navigator.openSetSuburbProcess(listItem);
						return;
					}
					if (listItem.quantityInStock == 0) return;
					// One item selected by user in add to list
					if (listItem.userQuantity == 1) {
						deleteItemFromList(listItem, position);
					} else if (listItem.userQuantity > 1) {
						listItem.userQuantity -= 1;
						mShoppingListItem.set((position-1), listItem);
						navigator.onSubstractListItemCount(listItem);
						notifyItemChanged(position, listItem);
					}
				});

				holder.addCountImage.setOnClickListener(view -> {
					ShoppingListItem listItem = getItem(position);
					if (enableClickEvent(listItem))
						return;
					if (!mAdapterIsClickable) return;
					if (userShouldSetSuburb()) {
						navigator.openSetSuburbProcess(listItem);
						return;
					}
					if (listItem.quantityInStock == 0) return;
					// One item selected by user in add to list
					if (listItem.userQuantity < listItem.quantityInStock) {
						listItem.userQuantity += 1;
						mShoppingListItem.set((position - 1), listItem);
						navigator.onAddListItemCount(listItem);
						notifyItemChanged(position, listItem);
					}
				});

                holder.llItemContainer.setOnClickListener(view -> {
                    ShoppingListItem shoppingListItem14 = getItem(position);
                    if (shoppingListItem14.unavailable)
                        navigator.showListBlackToolTip();
                });

				holder.cartProductImage.setOnClickListener(view -> {
					if (!mAdapterIsClickable) return;
					ShoppingListItem shoppingListItem12 = getItem(position);
					ProductList productList = createProductList(shoppingListItem12);
					navigator.openProductDetailFragment(shoppingListItem12.displayName, productList);
				});

				// Set Color and Size END
				holder.cbxSelectShoppingListItem.setOnClickListener(view -> {
					ShoppingListItem item = getItem(position);
					if (enableClickEvent(item))
						return;
					if (!mAdapterIsClickable) return;
					if (!item.isSelected) {
						if (userShouldSetSuburb()) {
							item.isSelected = false;
							notifyItemChanged(position, mShoppingListItem.size());
							navigator.openSetSuburbProcess(item);
							return;
						}
					}
					if (item.quantityInStock == 0) return;
                    /*
                     1. By default quantity will be ZERO.
                     2. On Selection it will change to ONE.
                     */
					item.userQuantity = Math.max(item.userQuantity, 1);
					item.isSelected = !item.isSelected;
					mShoppingListItem.set((position - 1), item);
					navigator.onItemSelectionChange(item.isSelected);
					notifyItemChanged(position, item);
				});

				holder.delete.setOnClickListener(view -> {
					if (!mAdapterIsClickable) return;
					navigator.onItemDeleteClick(getItem(position).Id, getItem(position).productId, getItem(position).catalogRefId, true);
				});
				mItemManger.bindView(holder.itemView, position);
				break;
			default:
				break;
		}
	}

    private int getUnavailableMessage(boolean isUnavailable) {
        int msg = isUnavailable ? R.string.unavailable : R.string.out_of_stock;
        if (isUnavailable) {
            Delivery type = KotlinUtils.Companion.getPreferredDeliveryType();
            if (type == null) {
                return msg;
            }
            switch (type) {
                case DASH:
                    msg = R.string.unavailable_with_dash;
                    break;
                case CNC:
                    msg = R.string.unavailable_with_collection;
                    break;
            }
        }
        return msg;
    }

	private void deleteItemFromList(ShoppingListItem shoppingListItem, int adapterPosition) {
		if (mShoppingListItem == null || mShoppingListItem.size() <= 0) {
			return;
		}
		navigator.onItemDeleteClick(shoppingListItem.Id, shoppingListItem.productId, shoppingListItem.catalogRefId, true);
	}

	private boolean enableClickEvent(ShoppingListItem shoppingListItem) {
		if (!userShouldSetSuburb())
			return shoppingListItem.quantityInStock == -1;
		return false;
	}

	@NonNull
	private ProductList createProductList(ShoppingListItem shoppingListItem) {
		ProductList productList = new ProductList();
		productList.productId = shoppingListItem.productId;
		productList.productName = shoppingListItem.displayName;
		productList.fromPrice = Float.valueOf((TextUtils.isEmpty(shoppingListItem.price) ? "0.0" : shoppingListItem.price));
		productList.sku = shoppingListItem.catalogRefId;
		productList.externalImageRefV2 = shoppingListItem.externalImageRefV2;
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

	public Integer getAddedItemsCount() {
		int count = 0;
		if (mShoppingListItem == null || mShoppingListItem.isEmpty()) return count;
		for (ShoppingListItem item : mShoppingListItem) {
			if (item.isSelected) {
				count += item.userQuantity;
			}
		}
		return count;
	}

	@Override
	public int getSwipeLayoutResourceId(int position) {
		return R.id.swipe;
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		private final WTextView productName;
		private final WTextView tvColorSize;
		private final WTextView tvQuantity;
		private final WTextView price;
		private final CheckBox cbxSelectShoppingListItem;
		private final WrapContentDraweeView cartProductImage;
		private final WTextView delete;
		private final ProgressBar progressBar;
		private final ProgressBar pbQuantityLoader;
		private final LinearLayout llQuantity;
		private final ConstraintLayout llItemContainer;
		private final RelativeLayout rlQuantitySelector;
		private final SwipeLayout swipeLayout;
		private final TextView tvProductAvailability;
		private final RelativeLayout swipeRight;
		private final ImageView minusDeleteCountImage, addCountImage;

		public ViewHolder(View itemView) {
			super(itemView);
			productName = itemView.findViewById(R.id.tvTitle);
			tvQuantity = itemView.findViewById(R.id.tvQuantity);
			price = itemView.findViewById(R.id.tvPrice);
			cbxSelectShoppingListItem = itemView.findViewById(R.id.cbShoppingList);
			pbQuantityLoader = itemView.findViewById(R.id.pbQuantityLoader);
			cartProductImage = itemView.findViewById(R.id.cartProductImage);
			tvProductAvailability = itemView.findViewById(R.id.tvProductAvailability);
			delete = itemView.findViewById(R.id.tvDelete);
			progressBar = itemView.findViewById(R.id.pbDeleteIndicator);
			tvColorSize = itemView.findViewById(R.id.tvColorSize);
			llQuantity = itemView.findViewById(R.id.llQuantity);
			llItemContainer = itemView.findViewById(R.id.llItemContainer);
			minusDeleteCountImage = itemView.findViewById(R.id.minusDeleteCountImage);
			addCountImage = itemView.findViewById(R.id.addCountImage);
			rlQuantitySelector = itemView.findViewById(R.id.rlQuantitySelector);
			swipeLayout = itemView.findViewById(R.id.swipe);
			swipeRight = itemView.findViewById(R.id.swipeRight);
		}
	}

	class HeaderViewHolder extends RecyclerView.ViewHolder {
		private final WTextView tvSearchText;

		public HeaderViewHolder(View itemView) {
			super(itemView);
			tvSearchText = itemView.findViewById(R.id.textProductSearch);
		}
	}

	/***
	 * Update old list with new list before refreshing the adapter
	 */
	public void updateList(ArrayList<ShoppingListItem> updatedListItems) {
		if (updatedListItems == null) return;
		if (mShoppingListItem == null) return;
		try {
			for (ShoppingListItem shoppinglistItem : mShoppingListItem) {
				for (ShoppingListItem updatedList : updatedListItems) {
					if (shoppinglistItem.catalogRefId.equalsIgnoreCase(updatedList.catalogRefId)) {
						int userQuantity = (updatedList.unavailable || updatedList.quantityInStock == 0)
								? 0 : shoppinglistItem.userQuantity;
						userQuantity = userQuantity > updatedList.quantityInStock ? 1 : userQuantity;
						updatedList.userQuantity =  userQuantity;

						updatedList.isSelected = !updatedList.unavailable &&
								updatedList.quantityInStock != 0
								&& shoppinglistItem.isSelected;
					}
				}
			}
			this.mShoppingListItem = updatedListItems;
			navigator.onItemSelectionChange(false); // default value
			notifyDataSetChanged();
			closeAllItems();
		} catch (IllegalArgumentException ex) {
			FirebaseManager.logException(ex);
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

	public ArrayList<ShoppingListItem> getShoppingListItems() {
		return mShoppingListItem;
	}

	public boolean userShouldSetSuburb() {
		return Utils.getPreferredDeliveryLocation() == null;
	}

	public void resetSelection() {
		for (ShoppingListItem shoppinglistItem : mShoppingListItem) {
			shoppinglistItem.userQuantity = 0;
			shoppinglistItem.isSelected = false;
		}
		notifyDataSetChanged();
		navigator.onItemSelectionChange(false);
	}
}