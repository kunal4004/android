package za.co.woolworths.financial.services.android.ui.adapters;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.awfs.coordination.R;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import za.co.woolworths.financial.services.android.models.dto.CartItemGroup;
import za.co.woolworths.financial.services.android.models.dto.CommerceItem;
import za.co.woolworths.financial.services.android.models.dto.OrderSummary;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.AnimationUtils;
import za.co.woolworths.financial.services.android.util.WFormatter;

import static za.co.woolworths.financial.services.android.models.service.event.ProductState.CANCEL_CALL;

public class CartProductAdapter extends RecyclerSwipeAdapter<RecyclerView.ViewHolder> {

	@Override
	public int getSwipeLayoutResourceId(int position) {
		return R.id.swipe;
	}

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
			return new CartItemViewHolder(LayoutInflater.from(parent.getContext())
					.inflate(R.layout.cart_product_item, parent, false));
		} else {
			return new CartPricesViewHolder(LayoutInflater.from(parent.getContext())
					.inflate(R.layout.cart_product_basket_prices, parent, false));
		}
	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
		final CartProductItemRow itemRow = getItemTypeAtPosition(position);
		switch (itemRow.rowType) {
			case HEADER:
				CartHeaderViewHolder headerHolder = ((CartHeaderViewHolder) holder);
				ArrayList<CommerceItem> productItems = itemRow.productItems;
				headerHolder.tvHeaderTitle.setText(productItems.size() > 1 ? productItems.size() + " " + itemRow.category.toUpperCase() + " ITEMS" : productItems.size() + " " + itemRow.category.toUpperCase() + " ITEM");
				break;

			case PRODUCT:
				final CartItemViewHolder productHolder = ((CartItemViewHolder) holder);
				final CommerceItem commerceItem = itemRow.productItem;
				productHolder.tvTitle.setText(commerceItem.getProductDisplayName());
				productHolder.quantity.setText(String.valueOf(commerceItem.getQuantity()));
				productHolder.price.setText(WFormatter.formatAmount(commerceItem.getPriceInfo().getAmount()));
				productImage(productHolder.productImage, commerceItem.externalImageURL);
				productHolder.btnDeleteRow.setVisibility(this.editMode ? View.VISIBLE : View.GONE);

				// prevent triggering animation on first load
				if (firstLoadWasCompleted())
					animateOnDeleteButtonVisibility(productHolder.llCartItems, this.editMode);

				productHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);

				// Drag From Right
				productHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, productHolder.swipeLayout.findViewById(R.id.bottom_wrapper));

				// Handling different events when swiping
				productHolder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
					@Override
					public void onClose(SwipeLayout layout) {
						//when the SurfaceView totally cover the BottomView.
						Log.e("SwipeLayout", "onClose ");
					}

					@Override
					public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
						//you are swiping.
						Log.e("SwipeLayout", "onUpdate ");

					}

					@Override
					public void onStartOpen(SwipeLayout layout) {
						Log.e("SwipeLayout", "onStartOpen ");

					}

					@Override
					public void onOpen(SwipeLayout layout) {
						//when the BottomView totally show.
						Log.e("SwipeLayout", "onOpen " + layout);
//						mItemManger.removeShownLayouts(layout);
//						cartItems.remove(dataItem);
//						int itemPosition = getItemId(dataItem);
//						notifyItemRemoved(itemPosition);
//						notifyItemRangeChanged(itemPosition, data.size());
//						mItemManger.closeAllItems();
					}

					@Override
					public void onStartClose(SwipeLayout layout) {
						Log.e("SwipeLayout", "onStartClose " + layout);

					}

					@Override
					public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
						//when user's hand released.
					}
				});

				if (commerceItem.getQuantityUploading()) {
					productHolder.pbQuantity.setVisibility(View.VISIBLE);
					productHolder.quantity.setVisibility(View.GONE);
					productHolder.imPrice.setVisibility(View.GONE);
				} else {
					productHolder.pbQuantity.setVisibility(View.GONE);
					productHolder.quantity.setVisibility(View.VISIBLE);
					productHolder.imPrice.setVisibility(View.VISIBLE);
				}
				if (this.editMode) {
					productHolder.btnDeleteRow.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							productHolder.swipeLayout.open(true);
						}
					});

					productHolder.btnDeleteRow.setVisibility(View.VISIBLE);
				}

				productHolder.llDeleteContainer.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						try {
							Animation animation = android.view.animation.AnimationUtils.loadAnimation(view.getContext(), R.anim.shake);
							animation.setAnimationListener(new Animation.AnimationListener() {
								@Override
								public void onAnimationStart(Animation animation) {
								}

								@Override
								public void onAnimationEnd(Animation animation) {
									Iterator<CartItemGroup> cartItemGroupIterator = cartItems.iterator();
									while (cartItemGroupIterator.hasNext()) {
										CartItemGroup cartItemGroup = cartItemGroupIterator.next();
										ArrayList<CommerceItem> commerceItemList = cartItemGroup.commerceItems;
										Iterator<CommerceItem> commerceItemIterator = commerceItemList.iterator();
										while (commerceItemIterator.hasNext()) {
											CommerceItem cm = commerceItemIterator.next();
											if (commerceItem.commerceId.equalsIgnoreCase(cm.commerceId)) {
												orderSummary.basketTotal = orderSummary.basketTotal - cm.getPriceInfo().amount;
												orderSummary.totalItemsCount = orderSummary.totalItemsCount - cm.getQuantity();
												orderSummary.total = orderSummary.basketTotal - orderSummary.estimatedDelivery;
												onItemClick.totalItemInBasket(orderSummary.totalItemsCount);
												mItemManger.removeShownLayouts(productHolder.swipeLayout);
												commerceItemIterator.remove();
												mItemManger.closeAllItems();
												notifyDataSetChanged();
												break;
											}
										}
									}
								}

								@Override
								public void onAnimationRepeat(Animation animation) {

								}
							});
							productHolder.llDeleteContainer.startAnimation(animation);
							//	onItemClick.onItemDeleteClick(commerceItem);
						} catch (Exception ex) {
							Log.e("cartItems", ex.toString());
						}
					}
				});

				// close swipeLayout panel
				if (!commerceItem.deleteSingleItem()) {
					productHolder.swipeLayout.close(true, true);
				} else {
					productHolder.swipeLayout.open(true, true);
				}

				productHolder.llQuantity.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						commerceItem.setQuantityUploading(true);
						setFirstLoadCompleted(false);
						onItemClick.onChangeQuantity(commerceItem);
					}
				});

				// mItemManger is member in RecyclerSwipeAdapter Class
				mItemManger.bindView(productHolder.itemView, position);

				break;

			case PRICES:
				CartPricesViewHolder priceHolder = ((CartPricesViewHolder) holder);
				if (orderSummary != null) {
					priceHolder.orderSummeryLayout.setVisibility(View.VISIBLE);
					if (orderSummary.getTotalItemsCount() > 1)
						priceHolder.txtBasketCount.setText("Basket - " + orderSummary.getTotalItemsCount() + " items");
					else
						priceHolder.txtBasketCount.setText("Basket - " + orderSummary.getTotalItemsCount() + " item");
					setPriceValue(priceHolder.txtPriceBasketItems, orderSummary.getBasketTotal());
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

	private CartProductItemRow getItemTypeAtPosition(int position) {
		int currentPosition = 0;
		for (CartItemGroup entry : cartItems) {
			if (currentPosition == position) {
				return new CartProductItemRow(CartRowType.HEADER, entry.type, null, entry.getCommerceItems());
			}

			// increment position for header
			currentPosition++;

			ArrayList<CommerceItem> productCollection = entry.commerceItems;

			if (position > currentPosition + productCollection.size() - 1) {
				currentPosition += productCollection.size();
			} else {
				return new CartProductItemRow(CartRowType.PRODUCT, entry.type, productCollection.get(position - currentPosition), null);
			}

		}
		// last row is for prices
		return new CartProductItemRow(CartRowType.PRICES, null, null, null);
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

	public void removeItem(ArrayList<CartItemGroup> updatedCartItems, OrderSummary updatedOrderSummer) {
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

	private class CartItemViewHolder extends RecyclerView.ViewHolder {
		private SwipeLayout swipeLayout;
		private WTextView tvTitle, tvDescription, quantity, price;
		private ImageView btnDeleteRow;
		private ImageView imPrice;
		private SimpleDraweeView productImage;
		private LinearLayout llQuantity;
		private LinearLayout llCartItems;
		private LinearLayout llDeleteContainer;
		private ProgressBar pbQuantity;

		public CartItemViewHolder(View view) {
			super(view);
			tvTitle = view.findViewById(R.id.tvTitle);
			tvDescription = view.findViewById(R.id.tvDetails);
			quantity = view.findViewById(R.id.quantity);
			price = view.findViewById(R.id.price);
			btnDeleteRow = view.findViewById(R.id.btnDeleteRow);
			productImage = view.findViewById(R.id.cartProductImage);
			llQuantity = view.findViewById(R.id.llQuantity);
			pbQuantity = view.findViewById(R.id.pbQuantity);
			imPrice = view.findViewById(R.id.imPrice);
			swipeLayout = view.findViewById(R.id.swipe);
			llCartItems = view.findViewById(R.id.llCartItems);
			llDeleteContainer = view.findViewById(R.id.llDeleteContainer);
		}
	}

	private class CartPricesViewHolder extends RecyclerView.ViewHolder {
		private WTextView txtBasketCount, txtPriceBasketItems,
				txtPriceEstimatedDelivery, txtPriceDiscounts,
				txtPriceCompanyDiscount, txtPriceWRewardsSavings,
				txtPriceOtherDiscount, txtPriceTotal;
		private LinearLayout orderSummeryLayout;

		public CartPricesViewHolder(View view) {
			super(view);
			txtBasketCount = view.findViewById(R.id.txtBasketCount);
			txtPriceBasketItems = view.findViewById(R.id.txtPriceBasketItems);
			txtPriceEstimatedDelivery = view.findViewById(R.id.txtPriceEstimatedDelivery);
			/*txtPriceDiscounts = view.findViewById(R.id.txtPriceDiscounts);
			txtPriceCompanyDiscount = view.findViewById(R.id.txtPriceCompanyDiscount);
			txtPriceWRewardsSavings = view.findViewById(R.id.txtPriceWRewardsSavings);
			txtPriceOtherDiscount = view.findViewById(R.id.txtPriceOtherDiscount);*/
			txtPriceTotal = view.findViewById(R.id.txtPriceTotal);
			orderSummeryLayout = view.findViewById(R.id.orderSummeryLayout);
		}
	}

	public class CartProductItemRow {
		private CartRowType rowType;
		private String category;
		private CommerceItem productItem;
		private ArrayList<CommerceItem> productItems;

		CartProductItemRow(CartRowType rowType, String category, CommerceItem productItem, ArrayList<CommerceItem> productItems) {
			this.rowType = rowType;
			this.category = category;
			this.productItem = productItem;
			this.productItems = productItems;
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
}
