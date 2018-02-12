package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import za.co.woolworths.financial.services.android.models.dto.CartPriceValues;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

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
		void onItemDeleteClick(CartProductItemRow itemRow);
	}

	private OnItemClick onItemClick;
	private HashMap<String, ArrayList<ProductList>> productCategoryItems;
	private CartPriceValues cartPriceValues;
	private boolean editMode = false;

	public CartProductAdapter(HashMap<String, ArrayList<ProductList>> productCategoryItems, CartPriceValues cartPriceValues, OnItemClick onItemClick) {
		this.productCategoryItems = productCategoryItems;
		this.cartPriceValues = cartPriceValues;
		this.onItemClick = onItemClick;
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
			ArrayList<ProductList> productItems = productCategoryItems.get(itemRow.category);
			cartHeaderViewHolder.tvHeaderTitle.setText(productItems.size() + " " + itemRow.category.toUpperCase() + " ITEMS");
		} else if(itemRow.rowType == CartRowType.PRODUCT) {
			CartItemViewHolder cartItemViewHolder = ((CartItemViewHolder) holder);
			final ProductList productItem = itemRow.productItem;
			cartItemViewHolder.tvTitle.setText(productItem.productName);
			cartItemViewHolder.btnDeleteRow.setVisibility(this.editMode ? View.VISIBLE : View.GONE);

			if(this.editMode) {
				cartItemViewHolder.btnDeleteRow.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onItemClick.onItemDeleteClick(itemRow);
					}
				});
			}

			cartItemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onItemClick.onItemClick(v, holder.getAdapterPosition());
				}
			});
		} else if(itemRow.rowType == CartRowType.PRICES) {
			CartPricesViewHolder cartPricesViewHolder = ((CartPricesViewHolder) holder);
			cartPricesViewHolder.txtBasketCount.setText("Basket - " + cartPriceValues.basketItemCount + " items");
			setPriceValue(cartPricesViewHolder.txtPriceBasketItems, cartPriceValues.basketItems);
			setPriceValue(cartPricesViewHolder.txtPriceEstimatedDelivery, cartPriceValues.estimatedDelivery);
			setPriceValue(cartPricesViewHolder.txtPriceDiscounts, cartPriceValues.discounts);
			setPriceValue(cartPricesViewHolder.txtPriceCompanyDiscount, cartPriceValues.companyDiscounts);
			setPriceValue(cartPricesViewHolder.txtPriceWRewardsSavings, cartPriceValues.wRewardsSavings);
			setPriceValue(cartPricesViewHolder.txtPriceOtherDiscount, cartPriceValues.otherDiscount);
			setPriceValue(cartPricesViewHolder.txtPriceTotal, cartPriceValues.total);

		}
	}

	private void setPriceValue(WTextView textView, double value) {
		String valueString = "";
		if (value < 0) {
			valueString += "- ";
		}
		valueString += "R " + String.format("%.2f", Math.abs(value));
		textView.setText(valueString);
	}

	@Override
	public int getItemCount() {
		Integer size = productCategoryItems.keySet().size();
		for (ArrayList<ProductList> collection : productCategoryItems.values()) {
			size += collection.size();
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
		for(Map.Entry<String, ArrayList<ProductList>> entry : productCategoryItems.entrySet()) {
			if(currentPosition == position) {
				return new CartProductItemRow(CartRowType.HEADER, entry.getKey(), null);
			}

			// increment position for header
			currentPosition++;

			ArrayList<ProductList> productCollection = entry.getValue();

			if(position > currentPosition + productCollection.size() - 1) {
				currentPosition += productCollection.size();
			} else {
				return new CartProductItemRow(CartRowType.PRODUCT, entry.getKey(), productCollection.get(position - currentPosition));
			}

		}
		// last row is for prices
		return new CartProductItemRow(CartRowType.PRICES, null, null);
	}

	public boolean toggleEditMode() {
		editMode = !editMode;
		notifyDataSetChanged();
		return editMode;
	}

	public void removeItem(CartProductItemRow itemRow) {
		productCategoryItems.get(itemRow.category).remove(itemRow.productItem);
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
		private WTextView tvTitle, tvDescription;
		private ImageView btnDeleteRow;

		public CartItemViewHolder(View view) {
			super(view);
			tvTitle = view.findViewById(R.id.tvTitle);
			tvDescription = view.findViewById(R.id.tvDescription);
			btnDeleteRow = view.findViewById(R.id.btnDeleteRow);
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
			txtPriceDiscounts = view.findViewById(R.id.txtPriceDiscounts);
			txtPriceCompanyDiscount = view.findViewById(R.id.txtPriceCompanyDiscount);
			txtPriceWRewardsSavings = view.findViewById(R.id.txtPriceWRewardsSavings);
			txtPriceOtherDiscount = view.findViewById(R.id.txtPriceOtherDiscount);
			txtPriceTotal = view.findViewById(R.id.txtPriceTotal);
		}
	}

	public class CartProductItemRow {
		public CartRowType rowType;
		public String category;
		public ProductList productItem;

		CartProductItemRow (CartRowType rowType, String category, ProductList productItem) {
			this.rowType = rowType;
			this.category = category;
			this.productItem = productItem;
		}
	}
}
