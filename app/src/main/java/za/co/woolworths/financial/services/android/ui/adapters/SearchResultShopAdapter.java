package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.awfs.coordination.R;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.ProductUtils;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultNavigator;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView;

import static za.co.woolworths.financial.services.android.ui.fragments.product.detail.ProductDetailViewModel.CLOTHING_PRODUCT;

public class SearchResultShopAdapter extends RecyclerSwipeAdapter<RecyclerView.ViewHolder> {

	private final int ITEM_VIEW_TYPE_HEADER = 0;
	private final int ITEM_VIEW_TYPE_BASIC = 1;
	private final int ITEM_VIEW_TYPE_FOOTER = 2;
	private boolean value;

	private List<ProductList> mProductList;

	private SearchResultNavigator mSearchResultNavigator;

	public SearchResultShopAdapter(List<ProductList> mProductList,
								   SearchResultNavigator searchResultNavigator) {
		this.mProductList = mProductList;
		this.mSearchResultNavigator = searchResultNavigator;
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

			case ITEM_VIEW_TYPE_FOOTER:
				vh = getProgressViewHolder(parent);
				break;

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
				.inflate(R.layout.shopping_list_commerce_item, parent, false));
	}

	@NonNull
	private HeaderViewHolder getHeaderViewHolder(ViewGroup parent) {
		return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(
				R.layout.item_found_layout, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		final ProductList productList = mProductList.get(position);
		if (productList.viewTypeHeader) {
			HeaderViewHolder hvh = (HeaderViewHolder) holder;
			hvh.setTotalItem(productList);
		} else if (productList.viewTypeFooter) {
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
			vh.cbxItem.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					/**
					 * Disable clothing type selection when product detail api is loading
					 * food item type can still be selected.
					 */
					ProductList productList = mProductList.get(vh.getAdapterPosition());
					String productType = productList.productType;
					List<OtherSkus> otherSkusList = productList.otherSkus;
					int otherSkuSize = (otherSkusList == null) ? 0 : otherSkusList.size();
					if (productType.equalsIgnoreCase(CLOTHING_PRODUCT) || otherSkuSize > 1) {
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
		// Product of type clothing or OtherSkus > 0
		if (clothingTypeProduct(selectedProduct, otherSkuSize)) {
			selectedProduct.viewIsLoading = !selectedProduct.viewIsLoading;
			if (selectedProduct.itemWasChecked) selectedProduct.viewIsLoading = false;
			selectedProduct.itemWasChecked = productWasChecked(selectedProduct);
			mSearchResultNavigator.onCheckedItem(selectedProduct, selectedProduct.viewIsLoading);
			mSearchResultNavigator.minOneItemSelected(mProductList);
			notifyItemChanged(position);
		} else {
			selectedProduct.itemWasChecked = productWasChecked(selectedProduct);
			mSearchResultNavigator.onFoodTypeChecked(selectedProduct);
			mSearchResultNavigator.minOneItemSelected(mProductList);
			notifyItemChanged(position);
		}
	}

	boolean clothingTypeProduct(ProductList selectedProduct, int otherSkuSize) {
		return selectedProduct.productType.equalsIgnoreCase(CLOTHING_PRODUCT) || otherSkuSize > 1;
	}

	private void onItemClick(SimpleViewHolder vh) {
		int position = vh.getAdapterPosition();
		ProductList selectedProduct = mProductList.get(position);
		int otherSkuSize = getOtherSkuSize(selectedProduct);
		// Product of type clothing or OtherSkus > 0
		if (clothingTypeProduct(selectedProduct, otherSkuSize)) {
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
		private WTextView tvPrice;
		private WTextView tvWasPrice;
		private WTextView tvSaveText;
		private WTextView tvQuantity;
		private WTextView tvColorSize;
		private WrapContentDraweeView cartProductImage;
		private LinearLayout llItemContainer;
		private CheckBox cbxItem;
		private ProgressBar pbLoadProduct;
		private SwipeLayout swipeLayout;

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
		}

		private void setDefaultQuantity() {
			tvQuantity.setText("1");
		}

		private void setCartImage(ProductList productItem) {
			cartProductImage.setImageURI(productItem.externalImageRef + "?w=" + 85 + "&q=" + 85);
		}

		public void setProductName(ProductList productItem) {
			tvTitle.setText(Html.fromHtml(productItem.productName));
		}

		private void setPrice(ProductList productItem) {
			ArrayList<Double> priceList = new ArrayList<>();
			for (OtherSkus os : productItem.otherSkus) {
				if (!TextUtils.isEmpty(os.wasPrice)) {
					priceList.add(Double.valueOf(os.wasPrice));
				}
			}

			String wasPrice = "";
			if (priceList.size() > 0) {
				wasPrice = String.valueOf(Collections.max(priceList));
			}

			String fromPrice = String.valueOf(productItem.fromPrice);
			ProductUtils.gridPriceList(tvPrice, tvWasPrice,
					fromPrice, wasPrice);
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
	}

	private class ProgressViewHolder extends RecyclerView.ViewHolder {
		private ProgressBar pbFooterProgress;

		private ProgressViewHolder(View v) {
			super(v);
			pbFooterProgress = v.findViewById(R.id.pbFooterProgress);
		}
	}

	private class HeaderViewHolder extends RecyclerView.ViewHolder {
		private WTextView tvNumberOfItem;

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
		if (mProductList.get(position).viewTypeHeader) {
			return ITEM_VIEW_TYPE_HEADER;
		} else if (mProductList.get(position).viewTypeFooter) {
			return ITEM_VIEW_TYPE_FOOTER;
		} else if (!mProductList.get(position).viewTypeFooter && !mProductList.get(position).viewTypeHeader) {
			return ITEM_VIEW_TYPE_BASIC;
		} else {
			return ITEM_VIEW_TYPE_BASIC;
		}
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
				}
			}
			notifyDataSetChanged();
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
}