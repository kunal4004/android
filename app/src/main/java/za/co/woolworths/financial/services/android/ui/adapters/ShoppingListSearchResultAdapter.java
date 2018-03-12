package za.co.woolworths.financial.services.android.ui.adapters;


import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.ProductUtils;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultNavigator;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class ShoppingListSearchResultAdapter extends RecyclerView.Adapter<ShoppingListSearchResultAdapter.SimpleViewHolder> {

	public Activity mContext;
	private List<ProductList> mProductList;

	private SearchResultNavigator mGridNavigator;

	public ShoppingListSearchResultAdapter(Activity mContext, List<ProductList> mProductList,
										   SearchResultNavigator gridNavigator) {
		this.mContext = mContext;
		this.mProductList = mProductList;
		this.mGridNavigator = gridNavigator;
	}

	@Override
	public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new SimpleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_list_commerce_item, parent, false));
	}


	public class SimpleViewHolder extends RecyclerView.ViewHolder {

		WTextView tvTitle;
		WTextView tvPrice;
		WTextView tvWasPrice;
		SimpleDraweeView cartProductImage;

		SimpleViewHolder(View view) {
			super(view);
			tvTitle = view.findViewById(R.id.tvTitle);
			tvPrice = view.findViewById(R.id.price);
			tvWasPrice = view.findViewById(R.id.tvWasPrice);
			cartProductImage = view.findViewById(R.id.cartProductImage);
		}

		private void setCartImage(ProductList productItem) {
			cartProductImage.setImageURI(productItem.externalImageRef + "?w=" + 85 + "&q=" + 85);
		}

		private void setProductName(ProductList productItem) {
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

		}
	}

	@Override
	public void onBindViewHolder(final SimpleViewHolder holder, final int position) {
		ProductList productItem = mProductList.get(position);

		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mGridNavigator.onGridItemSelected(mProductList.get(holder.getAdapterPosition()));
			}
		});

		holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				//mSelectedProductView.onLongPressState(v, position);
				return false;
			}
		});

		if (productItem != null) {
			holder.setSaveText(productItem);
			holder.setPrice(productItem);
			holder.setProductName(productItem);
			holder.setCartImage(productItem);
		}
	}


	@Override
	public int getItemCount() {
		return mProductList.size();
	}
}