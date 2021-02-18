package za.co.woolworths.financial.services.android.ui.adapters;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

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
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

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
		this.mShoppingListItem = shoppingListItems;
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
				final ShoppingListItem shoppingListItem = getItem(position);
				if (shoppingListItem == null) return;
				if (shoppingListItem.displayName == null) return;
				holder.cartProductImage.setImageURI(Utils.getExternalImageRef() + shoppingListItem.externalImageURL + "?w=" + 85 + "&q=" + 85);
				holder.productName.setText(shoppingListItem.displayName);
				holder.tvQuantity.setText(String.valueOf(shoppingListItem.userQuantity));
				holder.price.setText(CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace(shoppingListItem.price));
				holder.cbxSelectShoppingListItem.setChecked(shoppingListItem.isSelected);
				holder.delete.setVisibility(VISIBLE);
				holder.progressBar.setVisibility(View.INVISIBLE);
				holder.swipeLayout.setTopSwipeEnabled(false);

				holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, holder.swipeRight);

				// show/hide delete button on edit item click from toolbar
				holder.swipeLayout.close(true,!shoppingListItem.editButtonIsEnabled);
				holder.deleteButton.setVisibility(shoppingListItem.editButtonIsEnabled  ? VISIBLE : GONE);
				holder.cbxSelectShoppingListItem.setClickable(!shoppingListItem.editButtonIsEnabled);
				holder.cbxSelectShoppingListItem.setEnabled(!shoppingListItem.editButtonIsEnabled);
				holder.cbxSelectShoppingListItem.setAlpha(shoppingListItem.editButtonIsEnabled ? 0.5f : 1.0f);
				holder.swipeLayout.setSwipeEnabled(!shoppingListItem.editButtonIsEnabled);

				animateOnDeleteButtonVisibility(holder.deleteButton,shoppingListItem.editButtonIsEnabled);

				holder.deleteButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						navigator.onItemDeleteClick(shoppingListItem.Id,shoppingListItem.productId, shoppingListItem.catalogRefId, false);
						mShoppingListItem.remove(shoppingListItem);
						notifyItemRemoved(position);
						notifyItemRangeChanged(position, getItemCount() - position);
					}
				});

				holder.llQuantity.setEnabled(!shoppingListItem.editButtonIsEnabled);

				holder.llShopList.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (!mAdapterIsClickable || shoppingListItem.editButtonIsEnabled) return;
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
				holder.tvColorSize.setVisibility(VISIBLE);
				/****
				 * shoppingListItem.userShouldSetSuburb - is set to true when user did not cbxSelectShoppingListItem any suburb
				 */

				if (userShouldSetSuburb()) {
					holder.tvProductAvailability.setVisibility(GONE);
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
					holder.imPrice.setAlpha(productInStock ? 1.0f : 0.5f);
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
							holder.imPrice.setAlpha(0.5f);
							holder.tvColorSize.setVisibility(GONE);
							holder.tvProductAvailability.setVisibility(VISIBLE);
							holder.price.setAlpha(0f);
holder.price.setVisibility(GONE);
Utils.setBackgroundColor(holder.tvProductAvailability, R.drawable.round_amber_corner, R.string.out_of_stock);
						} else {
							holder.llQuantity.setVisibility((shoppingListItem.quantityInStock == 0) ? GONE : VISIBLE);
							holder.tvProductAvailability.setVisibility((shoppingListItem.quantityInStock == 0) ? VISIBLE : GONE);
							holder.cbxSelectShoppingListItem.setVisibility((shoppingListItem.quantityInStock == 0) ? GONE : VISIBLE);
holder.price.setVisibility((shoppingListItem.quantityInStock == 0) ? GONE : VISIBLE);
holder.price.setAlpha(1f);
							holder.tvColorSize.setVisibility(VISIBLE);
							Utils.setBackgroundColor(holder.tvProductAvailability, R.drawable.round_amber_corner, R.string.out_of_stock);
						}
					} else {
						holder.llQuantity.setVisibility(VISIBLE);
						holder.tvProductAvailability.setVisibility(GONE);
						holder.cbxSelectShoppingListItem.setEnabled(false);
					}
				}

				if (!userShouldSetSuburb())
					if (!shoppingListItem.inventoryCallCompleted) {
						holder.llQuantity.setAlpha(0.5f);
						holder.tvQuantity.setAlpha(0.5f);
						holder.imPrice.setAlpha(0.5f);
					}

				// Set Color and Size END
				holder.cbxSelectShoppingListItem.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						ShoppingListItem shoppingListItem = getItem(position);
						int currentPosition = position - 1;
						if (enableClickEvent(shoppingListItem)|| shoppingListItem.editButtonIsEnabled) return;
						if (!mAdapterIsClickable) return;
						if (!shoppingListItem.isSelected) {
							if (userShouldSetSuburb()) {
								shoppingListItem.isSelected = false;
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
						mShoppingListItem.set(currentPosition,shoppingListItem);
						navigator.onItemSelectionChange(mShoppingListItem);
						notifyDataSetChanged();
					}
				});

				holder.delete.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (!mAdapterIsClickable) return;
						holder.delete.setVisibility(View.INVISIBLE);
						holder.progressBar.setVisibility(VISIBLE);
						navigator.onItemDeleteClick(getItem(position).Id, getItem(position).productId, getItem(position).catalogRefId, true);
					}
				});
				holder.llQuantity.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						ShoppingListItem shoppingListItem = getItem(position);
						if (shoppingListItem.editButtonIsEnabled || enableClickEvent(shoppingListItem)) return;
						if (!mAdapterIsClickable) return;
						if (userShouldSetSuburb()) {
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

	private boolean enableClickEvent(ShoppingListItem shoppingListItem) {
		if (!userShouldSetSuburb())
			if (shoppingListItem.quantityInStock == -1) return true;
		return false;
	}

	@NonNull
	private ProductList createProductList(ShoppingListItem shoppingListItem) {
		ProductList productList = new ProductList();
		productList.productId = shoppingListItem.productId;
		productList.productName = shoppingListItem.displayName;
		productList.fromPrice = Float.valueOf((TextUtils.isEmpty(shoppingListItem.price) ? "0.0" : shoppingListItem.price));
		productList.sku = shoppingListItem.catalogRefId;
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
		private CheckBox cbxSelectShoppingListItem;
		private WrapContentDraweeView cartProductImage;
		private WTextView delete;
		private ProgressBar progressBar;
		private ProgressBar pbQuantityLoader;
		private RelativeLayout llQuantity;
		private SwipeLayout swipeLayout;
		private LinearLayout llShopList;
		private ImageView imPrice;
		private WTextView tvProductAvailability;
		private RelativeLayout swipeRight;
		private final ImageView deleteButton;


		public ViewHolder(View itemView) {
			super(itemView);
			productName = itemView.findViewById(R.id.tvTitle);
			tvQuantity = itemView.findViewById(R.id.tvQuantity);
			price = itemView.findViewById(R.id.tvPrice);
			cbxSelectShoppingListItem = itemView.findViewById(R.id.btnDeleteRow);
			pbQuantityLoader = itemView.findViewById(R.id.pbQuantityLoader);
			cartProductImage = itemView.findViewById(R.id.cartProductImage);
			tvProductAvailability = itemView.findViewById(R.id.tvProductAvailability);
			delete = itemView.findViewById(R.id.tvDelete);
			llShopList = itemView.findViewById(R.id.llShopList);
			progressBar = itemView.findViewById(R.id.pbDeleteIndicator);
			tvColorSize = itemView.findViewById(R.id.tvColorSize);
			llQuantity = itemView.findViewById(R.id.llQuantity);
			imPrice = itemView.findViewById(R.id.imPrice);
			swipeLayout = itemView.findViewById(R.id.swipe);
			swipeRight = itemView.findViewById(R.id.swipeRight);
			deleteButton = itemView.findViewById(R.id.deleteButton);
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

	public boolean userShouldSetSuburb() {
		ShoppingDeliveryLocation shoppingDeliveryLocation = Utils.getPreferredDeliveryLocation();
		if (shoppingDeliveryLocation == null) return true;
		return (shoppingDeliveryLocation.suburb == null && shoppingDeliveryLocation.store == null);
	}

	public void resetSelection() {
		for (ShoppingListItem shoppinglistItem : mShoppingListItem) {
			shoppinglistItem.userQuantity = 0;
			shoppinglistItem.isSelected = false;
		}
		notifyDataSetChanged();
		navigator.onItemSelectionChange(mShoppingListItem);
	}

	public void editButtonEnabled(boolean isEditMode) {
		for (ShoppingListItem shoppinglistItem : mShoppingListItem) {
			shoppinglistItem.editButtonIsEnabled = isEditMode;
		}
		notifyDataSetChanged();
	}

	private void animateOnDeleteButtonVisibility(View view, boolean animate) {
			int width = getWidthAndHeight((Activity) view.getContext());
			ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", animate ? -width : width, 1f);
			animator.setInterpolator(new DecelerateInterpolator());
			animator.setDuration(300);
			animator.start();
	}

	private int getWidthAndHeight(Activity activity) {
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels / 10;
	}
}
