package za.co.woolworths.financial.services.android.ui.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.ui.adapters.holder.ProductListingViewType;
import za.co.woolworths.financial.services.android.ui.adapters.holder.SearchResultPriceItem;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultNavigator;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView;

public class SearchResultShopAdapter extends RecyclerSwipeAdapter<RecyclerView.ViewHolder> {

	private boolean value;

	private List<ProductList> mProductList;

	private SearchResultNavigator mSearchResultNavigator;

	private static final String FOOD_PRODUCT = "foodProducts";

	public SearchResultShopAdapter(List<ProductList> mProductList,
								   SearchResultNavigator searchResultNavigator) {
		this.mProductList = mProductList;
		this.mSearchResultNavigator = searchResultNavigator;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		RecyclerView.ViewHolder vh;
		switch (ProductListingViewType.values()[viewType]) {
			case HEADER:
				vh = getHeaderViewHolder(parent);
				break;

			case FOOTER:
				vh = getProgressViewHolder(parent);
				break;

			case PRODUCT:
			default:
				vh = getSimpleViewHolder(parent);
				break;
		}
		return vh;
	}

	@NonNull
	private ProgressViewHolder getProgressViewHolder(ViewGroup parent) {
		return new ProgressViewHolder(LayoutInflater.from(parent.getContext()).inflate(
				R.layout.bottom_progress_bar, parent, false));
	}

	@NonNull
	private SimpleViewHolder getSimpleViewHolder(ViewGroup parent) {
		return new SimpleViewHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.shop_search_product_item, parent, false));
	}

	@NonNull
	private HeaderViewHolder getHeaderViewHolder(ViewGroup parent) {
		return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(
				R.layout.item_found_layout, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		final ProductList productList = mProductList.get(position);
		if (productList.rowType == ProductListingViewType.HEADER) {
			HeaderViewHolder hvh = (HeaderViewHolder) holder;
			hvh.setTotalItem(productList);
		} else if (productList.rowType == ProductListingViewType.FOOTER) {
			ProgressViewHolder pvh = ((ProgressViewHolder) holder);
			if (!value) {
				pvh.pbFooterProgress.setVisibility(View.VISIBLE);
				pvh.pbFooterProgress.setIndeterminate(true);
			} else pvh.pbFooterProgress.setVisibility(View.GONE);
		} else {
			final SimpleViewHolder vh = (SimpleViewHolder) holder;
			vh.setPrice(productList);
			vh.setProductName(productList);
			vh.setCartImage(productList);
			vh.setChecked(productList);
			vh.setDefaultQuantity();
			vh.showProgressBar(productList.viewIsLoading);
			vh.disableSwipeToDelete(false);
			vh.setTvColorSize(productList);
			vh.hideDropdownIcon();
			vh.cbxItem.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					/**
					 * Disable clothing type selection when product detail api is loading
					 * food item type can still be selected.
					 */
					ProductList productList = mProductList.get(vh.getAdapterPosition());
					String productType = productList.productType;
					if (!productType.equalsIgnoreCase(FOOD_PRODUCT)) {
						boolean unlockSelection = !viewIsLoading();
						vh.cbxItem.setChecked(unlockSelection);
						if (unlockSelection) {
							onCheckItemClick(vh);
						}
					} else {
						onCheckItemClick(vh);
					}
				}
			});

			vh.llItemContainer.setOnClickListener(new View.OnClickListener()

			{
				@Override
				public void onClick(View v) {
					if (!viewIsLoading()) {
						onItemClick(vh);
					}
				}
			});

			mItemManger.bindView(holder.itemView, position);
		}
	}

	private void onCheckItemClick(SimpleViewHolder vh) {
		int position = vh.getAdapterPosition();
		ProductList selectedProduct = mProductList.get(position);
		int otherSkuSize = getOtherSkuSize(selectedProduct);
		// ProductDetails of type clothing or OtherSkus > 0
		if (clothingTypeProduct(selectedProduct)) {
			selectedProduct.viewIsLoading = !selectedProduct.viewIsLoading;
			if (selectedProduct.itemWasChecked) selectedProduct.viewIsLoading = false;
			selectedProduct.itemWasChecked = productWasChecked(selectedProduct);
			mSearchResultNavigator.onCheckedItem(mProductList, selectedProduct, selectedProduct.viewIsLoading);
			notifyItemChanged(position);
		} else {
			selectedProduct.itemWasChecked = productWasChecked(selectedProduct);
			mSearchResultNavigator.onFoodTypeChecked(mProductList, selectedProduct);
			mSearchResultNavigator.minOneItemSelected(mProductList);
			notifyItemChanged(position);
		}
	}

	private boolean clothingTypeProduct(ProductList selectedProduct) {
		return !selectedProduct.productType.equalsIgnoreCase(FOOD_PRODUCT);
	}

	private void onItemClick(SimpleViewHolder vh) {
		int position = vh.getAdapterPosition();
		ProductList selectedProduct = mProductList.get(position);
		int otherSkuSize = getOtherSkuSize(selectedProduct);
		// ProductDetails of type clothing or OtherSkus > 0
		if (clothingTypeProduct(selectedProduct)) {
			mSearchResultNavigator.onClothingTypeSelect(selectedProduct);
		} else {
			mSearchResultNavigator.onFoodTypeSelect(selectedProduct);
		}
	}

	private int getOtherSkuSize(ProductList selectedProduct) {
		List<OtherSkus> otherSkuList = selectedProduct.otherSkus;
		int otherSkuSize = 0;
		if (otherSkuList != null) {
			otherSkuSize = otherSkuList.size();
		}
		return otherSkuSize;
	}

	private boolean productWasChecked(ProductList prodList) {
		return !prodList.itemWasChecked;
	}

	@Override
	public int getSwipeLayoutResourceId(int position) {
		return R.id.swipe;
	}


	private class SimpleViewHolder extends RecyclerView.ViewHolder {

		private WTextView tvTitle;
		private TextView tvPrice;
		private TextView tvWasPrice;
		private WTextView tvSaveText;
		private WTextView tvQuantity;
		private WTextView tvColorSize;
		private WrapContentDraweeView cartProductImage;
		private LinearLayout llItemContainer;
		private CheckBox cbxItem;
		private ProgressBar pbLoadProduct;
		private SwipeLayout swipeLayout;
		private ImageView imPrice;

		private SimpleViewHolder(View view) {
			super(view);
			tvTitle = view.findViewById(R.id.tvTitle);
			tvPrice = view.findViewById(R.id.tvPrice);
			tvWasPrice = view.findViewById(R.id.tvWasPrice);
			tvSaveText = view.findViewById(R.id.tvSaveText);
			tvColorSize = view.findViewById(R.id.tvColorSize);
			cartProductImage = view.findViewById(R.id.cartProductImage);
			llItemContainer = view.findViewById(R.id.llItemContainer);
			cbxItem = view.findViewById(R.id.btnDeleteRow);
			pbLoadProduct = view.findViewById(R.id.pbLoadProduct);
			tvQuantity = view.findViewById(R.id.tvQuantity);
			swipeLayout = view.findViewById(R.id.swipe);
			imPrice = view.findViewById(R.id.imPrice);
		}

		private void setDefaultQuantity() {
			tvQuantity.setText("1");
		}

		private void setCartImage(ProductList productItem) {
			cartProductImage.setImageURI(productItem.externalImageRef + ((productItem.externalImageRef.indexOf("?") > 0) ? "w=" + 85 + "&q=" + 85 : "?w=" + 85 + "&q=" + 85));
		}

		public void setProductName(ProductList productItem) {
			tvTitle.setText(Html.fromHtml(productItem.productName));
		}

		private void setPrice(ProductList productItem) {
			SearchResultPriceItem priceItem =new  SearchResultPriceItem();
			priceItem.setPrice(productItem, itemView, true);
		}

		private void setSaveText(ProductList productItem) {
			tvSaveText.setVisibility(TextUtils.isEmpty(productItem.saveText) ? View.GONE : View.VISIBLE);
			tvSaveText.setText(!TextUtils.isEmpty(productItem.saveText) ? productItem.saveText : "");
		}

		public void setChecked(ProductList productList) {
			cbxItem.setChecked(productList.itemWasChecked);
		}

		public void showProgressBar(boolean visible) {
			pbLoadProduct.setVisibility(visible ? View.VISIBLE : View.GONE);
			cbxItem.setVisibility(visible ? View.GONE : View.VISIBLE);
		}

		public void disableSwipeToDelete(boolean enable) {
			swipeLayout.setRightSwipeEnabled(enable);
		}

		public void setTvColorSize(ProductList productlist) {
			tvColorSize.setText(TextUtils.isEmpty(productlist.displayColorSizeText) ? "" : productlist.displayColorSizeText);
		}

		public void hideDropdownIcon() {
			imPrice.setVisibility(View.GONE);
		}
	}

	private class ProgressViewHolder extends RecyclerView.ViewHolder {
		private ProgressBar pbFooterProgress;

		private ProgressViewHolder(View v) {
			super(v);
			pbFooterProgress = v.findViewById(R.id.pbFooterProgress);
		}
	}

	private class HeaderViewHolder extends RecyclerView.ViewHolder {
		private TextView tvNumberOfItem;

		private HeaderViewHolder(View v) {
			super(v);
			tvNumberOfItem = v.findViewById(R.id.tvNumberOfItem);
		}

		private void setTotalItem(ProductList productList) {
			if (productList.numberOfItems != null)
				tvNumberOfItem.setText(String.valueOf(productList.numberOfItems));
		}
	}

	@Override
	public int getItemViewType(int position) {
		return mProductList.get(position).rowType.ordinal();
	}

	public void refreshAdapter(boolean value, List<ProductList> tempProductList) {
		this.value = value;
		this.mProductList = tempProductList;
		notifyDataSetChanged();
	}

	public void setCheckedProgressBar(ProductList productList) {
		if (mProductList != null) {
			for (ProductList pList : mProductList) {
				if (pList == productList) {
					pList.viewIsLoading = !pList.viewIsLoading;
				}
			}
			notifyDataSetChanged();
		}
	}

	public void setSelectedSku(ProductList selectedProduct, OtherSkus selectedSKU) {
		if (mProductList != null) {
			for (ProductList pList : mProductList) {
				if (pList == selectedProduct) {
					pList.sku = selectedSKU.sku;
					String colour = TextUtils.isEmpty(selectedSKU.colour) ? "" : selectedSKU.colour;
					String size = TextUtils.isEmpty(selectedSKU.size) ? "" : selectedSKU.size;
					boolean colourSize = TextUtils.isEmpty(colour) || TextUtils.isEmpty(size);
					pList.displayColorSizeText = colourSize ? (colour + "" + size) : (colour + ", " + size);
				}
			}
			notifyDataSetChanged();
		}
	}

	public void onDeselectSKU(ProductList selectedProduct, OtherSkus selectedSKU) {
		if (mProductList != null) {
			for (ProductList pList : mProductList) {
				if (pList == selectedProduct) {
					pList.itemWasChecked = false;
					pList.viewIsLoading = false;
					pList.displayColorSizeText = "";
					if (mSearchResultNavigator != null)
						mSearchResultNavigator.minOneItemSelected(mProductList);
					notifyDataSetChanged();
					return;
				}
			}
		}
	}

	@Override
	public int getItemCount() {
		return mProductList == null ? 0 : mProductList.size();
	}

	public boolean viewIsLoading() {
		if (mProductList != null) {
			for (ProductList pList : mProductList) {
				if (pList.viewIsLoading) {
					return true;
				}
			}
		}
		return false;
	}

	public List<ProductList> getProductList() {
		return mProductList;
	}
}
