package za.co.woolworths.financial.services.android.ui.adapters;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.HashMap;

import za.co.woolworths.financial.services.android.models.dto.CartItemGroup;
import za.co.woolworths.financial.services.android.models.dto.CommerceItem;
import za.co.woolworths.financial.services.android.models.dto.OrderSummary;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

import static za.co.woolworths.financial.services.android.models.service.event.ProductState.CANCEL_CALL;

public class CartProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private enum CartRowType {
		HEADER(0), PRODUCT(1), PRICES(2);

		public final int value;

		CartRowType(int value) {
			this.value = value;
		}
	}

	public interface OnItemClick {
		void onItemDeleteClick(CommerceItem commerceId);

		void onChangeQuantity(CommerceItem commerceId);

		void totalItemInBasket(int total);
	}

	private OnItemClick onItemClick;
	private HashMap<String, ArrayList<ProductList>> productCategoryItems;
	private boolean editMode = false;
	private boolean firstLoadCompleted = false;
	private ArrayList<CartItemGroup> cartItems;
	private OrderSummary orderSummary;

	public CartProductAdapter(ArrayList<CartItemGroup> cartItems, OnItemClick onItemClick, OrderSummary orderSummary, Activity mContext) {
		this.cartItems = cartItems;
		this.onItemClick = onItemClick;
		this.orderSummary = orderSummary;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == CartRowType.HEADER.value) {
			return new CartHeaderViewHolder(LayoutInflater.from(parent.getContext())
					.inflate(R.layout.cart_product_header_item, parent, false));
		} else if (viewType == CartRowType.PRODUCT.value) {
			return new productHolder(LayoutInflater.from(parent.getContext())
					.inflate(R.layout.cart_product_item, parent, false));
		} else {
			return new CartPricesViewHolder(LayoutInflater.from(parent.getContext())
					.inflate(R.layout.cart_product_basket_prices, parent, false));
		}
	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
		final CartcommerceItemRow itemRow = getItemTypeAtPosition(position);
		switch (itemRow.rowType) {
			case HEADER:
				CartHeaderViewHolder headerHolder = ((CartHeaderViewHolder) holder);
				ArrayList<CommerceItem> commerceItems = itemRow.commerceItems;
				headerHolder.tvHeaderTitle.setText(commerceItems.size() > 1 ? commerceItems.size() + " " + itemRow.category.toUpperCase() + " ITEMS" : commerceItems.size() + " " + itemRow.category.toUpperCase() + " ITEM");
				break;

			case PRODUCT:
				final productHolder productHolder = ((productHolder) holder);
				final CommerceItem commerceItem = itemRow.commerceItem;
				productHolder.tvTitle.setText(commerceItem.getProductDisplayName());
				productHolder.quantity.setText(String.valueOf(commerceItem.getQuantity()));
				productHolder.price.setText(WFormatter.formatAmount(commerceItem.getPriceInfo().getAmount()));
				productImage(productHolder.productImage, commerceItem.externalImageURL);
				productHolder.btnDeleteRow.setVisibility(this.editMode ? View.VISIBLE : View.GONE);
				onRemoveSingleItem(productHolder, commerceItem);
				//enable/disable change quantity click
				productHolder.llQuantity.setEnabled(!this.editMode);
				Utils.fadeInFadeOutAnimation(productHolder.llQuantity, this.editMode);

				// prevent triggering animation on first load
				if (firstLoadWasCompleted())
					animateOnDeleteButtonVisibility(productHolder.llCartItems, this.editMode);

				if (commerceItem.getQuantityUploading()) {
					productHolder.pbQuantity.setVisibility(View.VISIBLE);
					productHolder.quantity.setVisibility(View.GONE);
					productHolder.imPrice.setVisibility(View.GONE);
				} else {
					productHolder.pbQuantity.setVisibility(View.GONE);
					productHolder.quantity.setVisibility(View.VISIBLE);
					productHolder.imPrice.setVisibility(View.VISIBLE);
				}

				//Set Promotion Text START
				if(commerceItem.getPriceInfo().getDiscountedAmount()>0){
					productHolder.promotionalText.setText(" "+WFormatter.formatAmount(commerceItem.getPriceInfo().getDiscountedAmount()));
					productHolder.llPromotionalText.setVisibility(View.VISIBLE);
				}else {
					productHolder.llPromotionalText.setVisibility(View.GONE);
				}
				//Set Promotion Text END

				// Set Color and Size START
				if(itemRow.category.equalsIgnoreCase("FOOD"))
				{
					productHolder.tvColorSize.setVisibility(View.INVISIBLE);
				}
				else {
					String sizeColor=commerceItem.getColor();
					if(sizeColor == null)
						sizeColor = "";
					if(sizeColor.isEmpty()&& !commerceItem.getSize().isEmpty() && !commerceItem.getSize().equalsIgnoreCase("NO SZ") )
						sizeColor=commerceItem.getSize();
					else if(!sizeColor.isEmpty()&& !commerceItem.getSize().isEmpty() && !commerceItem.getSize().equalsIgnoreCase("NO SZ"))
						sizeColor=sizeColor+", "+commerceItem.getSize();

					productHolder.tvColorSize.setText(sizeColor);
					productHolder.tvColorSize.setVisibility(View.VISIBLE);
				}
				// Set Color and Size END



				productHolder.btnDeleteRow.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						setFirstLoadCompleted(false);
						commerceItem.commerceItemDeletedId(commerceItem);
						commerceItem.setDeleteIconWasPressed(true);
						notifyItemRangeChanged(productHolder.getAdapterPosition(), cartItems.size());
					}
				});


				productHolder.llQuantity.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						commerceItem.setQuantityUploading(true);
						setFirstLoadCompleted(false);
						onItemClick.onChangeQuantity(commerceItem);
					}
				});
				break;

			case PRICES:
				CartPricesViewHolder priceHolder = ((CartPricesViewHolder) holder);
				if (orderSummary != null) {
					priceHolder.orderSummeryLayout.setVisibility(View.VISIBLE);
					setPriceValue(priceHolder.txtPriceEstimatedDelivery, orderSummary.getEstimatedDelivery());
			/*setPriceValue(cartPricesViewHolder.txtPriceDiscounts, cartPriceValues.discounts);
			setPriceValue(cartPricesViewHolder.txtPriceCompanyDiscount, cartPriceValues.companyDiscounts);
			setPriceValue(cartPricesViewHolder.txtPriceWRewardsSavings, cartPriceValues.wRewardsSavings);
			setPriceValue(cartPricesViewHolder.txtPriceOtherDiscount, cartPriceValues.otherDiscount);*/
					setPriceValue(priceHolder.txtPriceTotal, orderSummary.getTotal());
				} else {
					priceHolder.orderSummeryLayout.setVisibility(View.GONE);
				}

				break;

			default:
				break;
		}
	}

	private void onRemoveSingleItem(final productHolder productHolder, final CommerceItem commerceItem) {
		if (this.editMode) {
			if (commerceItem.deleteIconWasPressed()) {
				Animation animateRowToDelete = android.view.animation.AnimationUtils.loadAnimation(productHolder.llCartItems.getContext(), R.anim.animate_layout_delete);
				animateRowToDelete.setAnimationListener(new Animation.AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						productHolder.pbDeleteProgress.setVisibility(commerceItem.deleteIconWasPressed() ? View.VISIBLE : View.GONE);
						productHolder.btnDeleteRow.setVisibility(commerceItem.deleteIconWasPressed() ? View.GONE : View.VISIBLE);
						onItemClick.onItemDeleteClick(commerceItem.getDeletedCommerceItemId());
					}

					@Override
					public void onAnimationRepeat(Animation animation) {

					}
				});
				productHolder.llCartItems.startAnimation(animateRowToDelete);
			} else {
				productHolder.pbDeleteProgress.setVisibility(View.GONE);
			}
		} else {
			productHolder.pbDeleteProgress.setVisibility(View.GONE);
		}
	}

	private void setPriceValue(WTextView textView, double value) {
		textView.setText(WFormatter.formatAmount(value));
	}

	@Override
	public int getItemCount() {
		Integer size = cartItems.size();
		for (CartItemGroup collection : cartItems) {
			size += collection.getCommerceItems().size();
		}
		if (editMode) {
			// returns sum of headers + product items
			return size;
		} else {
			// returns sum of headers + product items + last row for prices
			return size + 1;
		}
	}

	@Override
	public int getItemViewType(int position) {
		return getItemTypeAtPosition(position).rowType.value;
	}

	private CartcommerceItemRow getItemTypeAtPosition(int position) {
		int currentPosition = 0;
		for (CartItemGroup entry : cartItems) {
			if (currentPosition == position) {
				return new CartcommerceItemRow(CartRowType.HEADER, entry.type, null, entry.getCommerceItems());
			}

			// increment position for header
			currentPosition++;

			ArrayList<CommerceItem> productCollection = entry.commerceItems;

			if (position > currentPosition + productCollection.size() - 1) {
				currentPosition += productCollection.size();
			} else {
				return new CartcommerceItemRow(CartRowType.PRODUCT, entry.type, productCollection.get(position - currentPosition), null);
			}

		}
		// last row is for prices
		return new CartcommerceItemRow(CartRowType.PRICES, null, null, null);
	}

	public boolean toggleEditMode() {
		editMode = !editMode;
		toggleFirstLoad();
		notifyDataSetChanged();
		return editMode;
	}

	public boolean toggleFirstLoad() {
		setFirstLoadCompleted(true);
		return firstLoadCompleted;
	}

	public void removeItem(ArrayList<CartItemGroup> updatedCartItems, OrderSummary updatedOrderSummer, CommerceItem commerceItem) {
		ArrayList<CommerceItem> newCommerceItemList = new ArrayList<>();
		for (CartItemGroup cartItemGroup : cartItems) {
			ArrayList<CommerceItem> commerceItemList = cartItemGroup.commerceItems;
			for (CommerceItem cm : commerceItemList) {
				if (commerceItem.commerceId.equalsIgnoreCase(cm.commerceId)) {
					cartItems.remove(commerceItem);
				} else {
					if (cm.deleteIconWasPressed()) {
						newCommerceItemList.add(cm);
					}
				}
			}
		}
		for (CartItemGroup cartItemGroup : updatedCartItems) {
			ArrayList<CommerceItem> commerceItemList = cartItemGroup.commerceItems;
			for (CommerceItem cm : commerceItemList) {
				for (CommerceItem oldCommerceItem : newCommerceItemList) {
					if (cm.commerceId.equals(oldCommerceItem.commerceId)) {
						cm.setDeleteIconWasPressed(true);
					}
				}
			}
		}
		this.cartItems = updatedCartItems;
		this.orderSummary = updatedOrderSummer;
		notifyDataSetChanged();
	}

	public void toggleDeleteSingleItem(CommerceItem commerceItem) {
		for (CartItemGroup cartItemGroup : this.cartItems) {
			ArrayList<CommerceItem> commerceItemList = cartItemGroup.commerceItems;
			if (commerceItemList != null) {
				for (CommerceItem cm : commerceItemList) {
					if (cm.getCommerceId().equalsIgnoreCase(commerceItem.getCommerceId())) {
						boolean deleteSingleItem = !commerceItem.deleteSingleItem();
						commerceItem.setDeleteSingleItem(deleteSingleItem);
						notifyDataSetChanged();
					}
				}
			}
		}
	}

	public void clear() {
		this.cartItems.clear();
		this.orderSummary = null;
		notifyDataSetChanged();
	}

	private class CartHeaderViewHolder extends RecyclerView.ViewHolder {
		private WTextView tvHeaderTitle;

		public CartHeaderViewHolder(View view) {
			super(view);
			tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle);
		}
	}

	public class productHolder extends RecyclerView.ViewHolder {
		private WTextView tvTitle, tvColorSize, quantity, price, promotionalText;
		private ImageView btnDeleteRow;
		private ImageView imPrice;
		private SimpleDraweeView productImage;
		private LinearLayout llQuantity;
		private LinearLayout llCartItems,llPromotionalText;
		private WTextView tvDelete;
		private ProgressBar pbQuantity;
		private ProgressBar pbDeleteProgress;
		public RelativeLayout viewForeground, viewBackground;


		public productHolder(View view) {
			super(view);
			tvTitle = view.findViewById(R.id.tvTitle);
			tvColorSize = view.findViewById(R.id.tvSize);
			quantity = view.findViewById(R.id.quantity);
			price = view.findViewById(R.id.price);
			btnDeleteRow = view.findViewById(R.id.btnDeleteRow);
			productImage = view.findViewById(R.id.cartProductImage);
			llQuantity = view.findViewById(R.id.llQuantity);
			pbQuantity = view.findViewById(R.id.pbQuantity);
			pbDeleteProgress = view.findViewById(R.id.pbDeleteProgress);
			imPrice = view.findViewById(R.id.imPrice);
			llCartItems = view.findViewById(R.id.llCartItems);
			tvDelete = view.findViewById(R.id.tvDelete);
			viewBackground = view.findViewById(R.id.view_background);
			viewForeground = view.findViewById(R.id.view_foreground);
			promotionalText = view.findViewById(R.id.promotionalText);
			llPromotionalText = view.findViewById(R.id.promotionalTextLayout);
		}
	}

	private class CartPricesViewHolder extends RecyclerView.ViewHolder {
		private WTextView
				txtPriceEstimatedDelivery, txtPriceDiscounts,
				txtPriceCompanyDiscount, txtPriceWRewardsSavings,
				txtPriceOtherDiscount, txtPriceTotal;
		private LinearLayout orderSummeryLayout;

		public CartPricesViewHolder(View view) {
			super(view);
			txtPriceEstimatedDelivery = view.findViewById(R.id.txtPriceEstimatedDelivery);
			/*txtPriceDiscounts = view.findViewById(R.id.txtPriceDiscounts);
			txtPriceCompanyDiscount = view.findViewById(R.id.txtPriceCompanyDiscount);
			txtPriceWRewardsSavings = view.findViewById(R.id.txtPriceWRewardsSavings);
			txtPriceOtherDiscount = view.findViewById(R.id.txtPriceOtherDiscount);*/
			txtPriceTotal = view.findViewById(R.id.txtPriceTotal);
			orderSummeryLayout = view.findViewById(R.id.orderSummeryLayout);
		}
	}

	public class CartcommerceItemRow {
		private CartRowType rowType;
		private String category;
		private CommerceItem commerceItem;
		private ArrayList<CommerceItem> commerceItems;

		CartcommerceItemRow(CartRowType rowType, String category, CommerceItem commerceItem, ArrayList<CommerceItem> commerceItems) {
			this.rowType = rowType;
			this.category = category;
			this.commerceItem = commerceItem;
			this.commerceItems = commerceItems;
		}
	}

	private void productImage(final SimpleDraweeView image, String imgUrl) {
		if (imgUrl != null) {
			try {
				//TODO:: get domain name dynamically
				imgUrl = "https://images.woolworthsstatic.co.za/" + imgUrl + "?w=" + 85 + "&q=" + 85;
				image.setImageURI(imgUrl);
			} catch (IllegalArgumentException ignored) {
			}
		}
	}

	public void changeQuantity(ArrayList<CartItemGroup> cartItems,
							   OrderSummary orderSummary) {
		this.cartItems = cartItems;
		this.orderSummary = orderSummary;
		resetQuantityState(false);
		notifyDataSetChanged();
	}

	public void onChangeQuantityComplete() {
		resetQuantityState(false);
		notifyDataSetChanged();
	}

	public void onChangeQuantityLoad(CommerceItem mCommerceItem) {
		for (CartItemGroup cartItemGroup : this.cartItems) {
			ArrayList<CommerceItem> commerceItemList = cartItemGroup.commerceItems;
			if (commerceItemList != null) {
				for (CommerceItem cm : commerceItemList) {
					if (cm == mCommerceItem) {
						cm.setQuantityUploading(true);
					}
				}
			}
		}
		notifyDataSetChanged();
	}

	public void onChangeQuantityError() {
		resetQuantityState(true);
		notifyDataSetChanged();
	}

	public void onChangeQuantityLoad() {
		notifyDataSetChanged();
	}

	private void animateOnDeleteButtonVisibility(View view, boolean animate) {
		Context context = view.getContext();
		if (context != null) {
			int width = getWidthAndHeight((Activity) context);
			ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", animate ? -width : width, 1f);
			animator.setInterpolator(new DecelerateInterpolator());
			animator.setDuration(300);
			animator.start();
		}
	}

	private int getWidthAndHeight(Activity activity) {
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels / 10;
	}

	public void onPopUpCancel(String status) {
		switch (status) {
			case CANCEL_CALL:
				resetQuantityState(true);
				notifyDataSetChanged();
				break;

			default:
				break;
		}
	}

	private void resetQuantityState(boolean refreshQuantity) {
		for (CartItemGroup cartItemGroup : this.cartItems) {
			ArrayList<CommerceItem> commerceItemList = cartItemGroup.commerceItems;
			if (commerceItemList != null) {
				for (CommerceItem cm : commerceItemList) {
					if (refreshQuantity)
						cm.setQuantityUploading(false);
					setFirstLoadCompleted(false);
				}
			}
		}
	}

	private void setFirstLoadCompleted(boolean firstLoadCompleted) {
		this.firstLoadCompleted = firstLoadCompleted;
	}

	private boolean firstLoadWasCompleted() {
		return firstLoadCompleted;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
		if (cartItems != null)
			notifyItemRangeChanged(0, cartItems.size());
	}
}