package za.co.woolworths.financial.services.android.ui.adapters;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.ProductUtils;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultNavigator;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView;

public class ShoppingListSearchResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final int ITEM_VIEW_TYPE_HEADER = 0;
	private final int ITEM_VIEW_TYPE_BASIC = 1;
	private final int ITEM_VIEW_TYPE_FOOTER = 2;
	private boolean value;

	private List<ProductList> mProductList;

	private SearchResultNavigator mGridNavigator;

	public ShoppingListSearchResultAdapter(List<ProductList> mProductList,
										   SearchResultNavigator gridNavigator) {
		this.mProductList = mProductList;
		this.mGridNavigator = gridNavigator;
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
		ProductList productList = mProductList.get(position);
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
			if (productList != null) {
				vh.setPrice(productList);
				vh.setProductName(productList);
				vh.setCartImage(productList);
			}
			holder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mGridNavigator.onGridItemSelected(mProductList.get(vh.getAdapterPosition()));
				}
			});
			holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					//mSelectedProductView.onLongPressState(v, position);
					return false;
				}
			});
		}
	}

	private class SimpleViewHolder extends RecyclerView.ViewHolder {

		private WTextView tvTitle;
		private WTextView tvPrice;
		private WTextView tvWasPrice;
		private WTextView tvSaveText;
		private WrapContentDraweeView cartProductImage;

		SimpleViewHolder(View view) {
			super(view);
			tvTitle = view.findViewById(R.id.tvTitle);
			tvPrice = view.findViewById(R.id.price);
			tvWasPrice = view.findViewById(R.id.tvWasPrice);
			tvSaveText = view.findViewById(R.id.tvSaveText);
			cartProductImage = view.findViewById(R.id.cartProductImage);
		}

		public void setCartImage(ProductList productItem) {
			cartProductImage.setImageURI(productItem.externalImageRef + "?w=" + 85 + "&q=" + 85);
		}

		public void setProductName(ProductList productItem) {
			tvTitle.setText(Html.fromHtml(productItem.productName));
		}

		public void setPrice(ProductList productItem) {
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
	}

	private class ProgressViewHolder extends RecyclerView.ViewHolder {
		public ProgressBar pbFooterProgress;

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

		public void setTotalItem(ProductList productList) {
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

	@Override
	public int getItemCount() {
		return mProductList == null ? 0 : mProductList.size();
	}

}