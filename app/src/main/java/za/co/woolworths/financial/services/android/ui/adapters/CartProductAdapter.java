package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.awfs.coordination.R;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.HashMap;

import za.co.woolworths.financial.services.android.models.dto.CartItemGroup;
import za.co.woolworths.financial.services.android.models.dto.CartPriceValues;
import za.co.woolworths.financial.services.android.models.dto.CartProduct;
import za.co.woolworths.financial.services.android.models.dto.OrderSummary;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CartFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class CartProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private enum CartRowType {
		HEADER(0), PRODUCT(1), PRICES(2);

		public final int value;

		CartRowType(int value) {
			this.value = value;
		}
	}

	public interface OnItemClick {
		void onItemClick(View view, int position);
		void onItemDeleteClick(String productId);
	}

	private OnItemClick onItemClick;
	private HashMap<String, ArrayList<ProductList>> productCategoryItems;
	private CartPriceValues cartPriceValues;
	private boolean editMode = false;
	private ArrayList<CartItemGroup> cartItems;
	private OrderSummary orderSummary;
	private final DrawImage drawImage;

	/*public CartProductAdapter(ArrayList<CartItemGroup> productCategoryItems, CartFragment cartPriceValues, CartPriceValues onItemClick) {
		this.productCategoryItems = productCategoryItems;
		this.cartPriceValues = cartPriceValues;
		this.onItemClick = onItemClick;
	}*/

	public CartProductAdapter(ArrayList<CartItemGroup> cartItems, OnItemClick onItemClick, OrderSummary orderSummary, Activity mContext)
	{
		this.cartItems=cartItems;
		this.onItemClick=onItemClick;
		this.orderSummary = orderSummary;
		drawImage = new DrawImage(mContext);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if(viewType == CartRowType.HEADER.value) {
			return new CartHeaderViewHolder(LayoutInflater.from(parent.getContext())
					.inflate(R.layout.cart_product_header_item, parent, false));
		} else if(viewType == CartRowType.PRODUCT.value) {
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

		if(itemRow.rowType == CartRowType.HEADER) {
			CartHeaderViewHolder cartHeaderViewHolder = ((CartHeaderViewHolder) holder);
			ArrayList<CartProduct> productItems = itemRow.productItems;
			cartHeaderViewHolder.tvHeaderTitle.setText(productItems.size() + " " + itemRow.category.toUpperCase() + " ITEMS");
		} else if(itemRow.rowType == CartRowType.PRODUCT) {
			CartItemViewHolder cartItemViewHolder = ((CartItemViewHolder) holder);
			final CartProduct productItem = itemRow.productItem;
			cartItemViewHolder.tvTitle.setText(productItem.getProductDisplayName());
			cartItemViewHolder.quantity.setText(String.valueOf(productItem.getQuantity()));
			cartItemViewHolder.price.setText(WFormatter.formatAmount(productItem.getPriceInfo().getAmount()));
			productImage(cartItemViewHolder.productImage,productItem.internalImageURL);
			cartItemViewHolder.btnDeleteRow.setVisibility(this.editMode ? View.VISIBLE : View.GONE);

			if(this.editMode) {
				cartItemViewHolder.btnDeleteRow.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onItemClick.onItemDeleteClick(itemRow.productItem.getProductId());
					}
				});
			}

			cartItemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//onItemClick.onItemClick(v, holder.getAdapterPosition());
				}
			});
		} else if(itemRow.rowType == CartRowType.PRICES) {
			CartPricesViewHolder cartPricesViewHolder = ((CartPricesViewHolder) holder);
			cartPricesViewHolder.txtBasketCount.setText("Basket - " + orderSummary.getTotalItemsCount() + " items");
			setPriceValue(cartPricesViewHolder.txtPriceBasketItems, orderSummary.getBasketTotal());
			setPriceValue(cartPricesViewHolder.txtPriceEstimatedDelivery, orderSummary.getEstimatedDelivery());
			/*setPriceValue(cartPricesViewHolder.txtPriceDiscounts, cartPriceValues.discounts);
			setPriceValue(cartPricesViewHolder.txtPriceCompanyDiscount, cartPriceValues.companyDiscounts);
			setPriceValue(cartPricesViewHolder.txtPriceWRewardsSavings, cartPriceValues.wRewardsSavings);
			setPriceValue(cartPricesViewHolder.txtPriceOtherDiscount, cartPriceValues.otherDiscount);*/
			setPriceValue(cartPricesViewHolder.txtPriceTotal, orderSummary.getTotal());

		}
	}

	private void setPriceValue(WTextView textView, double value) {
		textView.setText(WFormatter.formatAmount(value));
	}

	@Override
	public int getItemCount() {
		Integer size = cartItems.size();
		for (CartItemGroup collection : cartItems) {
			size += collection.getCartProducts().size();
		}
		if(editMode) {
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
		for(CartItemGroup entry : cartItems) {
			if(currentPosition == position) {
				return new CartProductItemRow(CartRowType.HEADER, entry.type, null,entry.getCartProducts());
			}

			// increment position for header
			currentPosition++;

			ArrayList<CartProduct> productCollection = entry.cartProducts;

			if(position > currentPosition + productCollection.size() - 1) {
				currentPosition += productCollection.size();
			} else {
				return new CartProductItemRow(CartRowType.PRODUCT, entry.type, productCollection.get(position - currentPosition),null);
			}

		}
		// last row is for prices
		return new CartProductItemRow(CartRowType.PRICES, null, null,null);
	}

	public boolean toggleEditMode() {
		editMode = !editMode;
		notifyDataSetChanged();
		return editMode;
	}

	public void removeItem(ArrayList<CartItemGroup> updatedCartItems,OrderSummary updatedOrderSummer) {
		this.cartItems=updatedCartItems;
		this.orderSummary=updatedOrderSummer;
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
		private WTextView tvTitle, tvDescription,quantity,price;
		private ImageView btnDeleteRow;
		private SimpleDraweeView productImage;

		public CartItemViewHolder(View view) {
			super(view);
			tvTitle = view.findViewById(R.id.tvTitle);
			tvDescription = view.findViewById(R.id.tvDetails);
			quantity = view.findViewById(R.id.quantity);
			price = view.findViewById(R.id.price);
			btnDeleteRow = view.findViewById(R.id.btnDeleteRow);
			productImage=view.findViewById(R.id.cartProductImage);
		}
	}

	private class CartPricesViewHolder extends RecyclerView.ViewHolder {
		private WTextView txtBasketCount, txtPriceBasketItems,
				txtPriceEstimatedDelivery, txtPriceDiscounts,
				txtPriceCompanyDiscount, txtPriceWRewardsSavings,
				txtPriceOtherDiscount, txtPriceTotal;

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
		}
	}

	public class CartProductItemRow {
		public CartRowType rowType;
		public String category;
		public CartProduct productItem;
		public ArrayList<CartProduct> productItems;

		CartProductItemRow (CartRowType rowType, String category, CartProduct productItem,ArrayList<CartProduct> productItems) {
			this.rowType = rowType;
			this.category = category;
			this.productItem = productItem;
			this.productItems = productItems;
		}
	}

	private void productImage(final SimpleDraweeView image, String imgUrl) {
		if (imgUrl != null) {
			try {
				imgUrl = imgUrl + "?w=" + 85 + "&q=" + 85;
				drawImage.displayImage(image, "http://www-win-qa.woolworths.co.za/"+imgUrl);
			} catch (IllegalArgumentException ignored) {
			}
		}
	}
}
