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
import za.co.woolworths.financial.services.android.models.dto.PromotionImages;
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.GridNavigator;
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.ProductUtils;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.DrawImage;

public class ProductViewListAdapter extends RecyclerView.Adapter<ProductViewListAdapter.SimpleViewHolder> {

	private final DrawImage drawImage;
	public Activity mContext;
	private List<ProductList> mProductList;

	private GridNavigator mGridNavigator;

	public ProductViewListAdapter(Activity mContext, List<ProductList> mProductList,
								  GridNavigator gridNavigator) {
		this.mContext = mContext;
		this.mProductList = mProductList;
		this.mGridNavigator = gridNavigator;
		drawImage = new DrawImage(mContext);
	}

	class SimpleViewHolder extends RecyclerView.ViewHolder {

		WTextView tvSaveText;
		WTextView tvProductName;
		WTextView tvAmount;
		WTextView tvWasPrice;
		SimpleDraweeView imProductImage;
		SimpleDraweeView imNewImage;
		SimpleDraweeView imSave;
		SimpleDraweeView imReward;
		SimpleDraweeView imVitality;

		SimpleViewHolder(View view) {
			super(view);
			tvProductName = view.findViewById(R.id.tvProductName);
			tvSaveText = view.findViewById(R.id.tvSaveText);
			tvAmount = view.findViewById(R.id.textAmount);
			tvWasPrice = view.findViewById(R.id.textWasPrice);
			imProductImage = view.findViewById(R.id.imProduct);
			imNewImage = view.findViewById(R.id.imNewImage);
			imSave = view.findViewById(R.id.imSave);
			imReward = view.findViewById(R.id.imReward);
			imVitality = view.findViewById(R.id.imVitality);
		}
	}

	@Override
	public void onBindViewHolder(final SimpleViewHolder holder, final int position) {
		ProductList productItem = mProductList.get(position);
		if (productItem != null) {
			String productName = productItem.productName;
			String externalImageRef = productItem.externalImageRef;
			String saveText = productItem.saveText;
			PromotionImages promo = productItem.promotionImages;
			holder.tvProductName.setText(Html.fromHtml(productName));

			if (!TextUtils.isEmpty(saveText))
				holder.tvSaveText.setText(saveText);

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
			ProductUtils.gridPriceList(holder.tvAmount, holder.tvWasPrice,
					fromPrice, wasPrice);

			productImage(holder.imProductImage, externalImageRef);
			promoImages(holder, promo);
		}

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
	}

	@Override
	public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new SimpleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_product, parent, false));
	}

	@Override
	public int getItemCount() {
		return mProductList.size();
	}

	private void productImage(final SimpleDraweeView image, String imgUrl) {
		if (imgUrl != null) {
			try {
				imgUrl = imgUrl + "?w=" + 300 + "&q=" + 85;
				drawImage.displayImage(image, imgUrl);
			} catch (IllegalArgumentException ignored) {
			}
		}
	}

	private void promoImages(SimpleViewHolder holder, PromotionImages imPromo) {

		DrawImage drawImage = new DrawImage(mContext);
		if (imPromo != null) {
			String wSave = imPromo.save;
			String wReward = imPromo.wRewards;
			String wVitality = imPromo.vitality;
			String wNewImage = imPromo.newImage;

			if (!TextUtils.isEmpty(wSave)) {
				holder.imSave.setVisibility(View.VISIBLE);
				drawImage.displaySmallImage(holder.imSave, wSave);
			} else {
				holder.imSave.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(wReward)) {
				holder.imReward.setVisibility(View.VISIBLE);
				drawImage.displaySmallImage(holder.imReward, wReward);
			} else {
				holder.imReward.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(wVitality)) {
				holder.imVitality.setVisibility(View.VISIBLE);
				drawImage.displaySmallImage(holder.imVitality, wVitality);
			} else {
				holder.imVitality.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(wNewImage)) {
				holder.imNewImage.setVisibility(View.VISIBLE);
				drawImage.displaySmallImage(holder.imNewImage, wNewImage);

			} else {
				holder.imNewImage.setVisibility(View.GONE);
			}
		}
	}
}