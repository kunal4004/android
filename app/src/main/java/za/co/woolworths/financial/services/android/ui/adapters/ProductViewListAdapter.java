package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

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

public class ProductViewListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	public static final int ITEM_VIEW_TYPE_HEADER = 0;
	public static final int ITEM_VIEW_TYPE_BASIC = 1;
	public static final int ITEM_VIEW_TYPE_FOOTER = 2;

	private final DrawImage drawImage;
	public Activity mContext;
	private List<ProductList> mProductList;

	private GridNavigator mGridNavigator;
	private boolean value;

	public ProductViewListAdapter(Activity mContext, List<ProductList> mProductList,
								  GridNavigator gridNavigator) {
		this.mContext = mContext;
		this.mProductList = mProductList;
		this.mGridNavigator = gridNavigator;
		drawImage = new DrawImage(mContext);
	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
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
			String productName = productList.productName;
			String externalImageRef = productList.externalImageRef;
			String saveText = productList.saveText;
			PromotionImages promo = productList.promotionImages;
			vh.tvProductName.setText(Html.fromHtml(productName));

			if (!TextUtils.isEmpty(saveText))
				vh.tvSaveText.setText(saveText);

			ArrayList<Double> priceList = new ArrayList<>();
			for (OtherSkus os : productList.otherSkus) {
				if (!TextUtils.isEmpty(os.wasPrice)) {
					priceList.add(Double.valueOf(os.wasPrice));
				}
			}

			String wasPrice = "";
			if (priceList.size() > 0) {
				wasPrice = String.valueOf(Collections.max(priceList));
			}

			String fromPrice = String.valueOf(productList.fromPrice);
			ProductUtils.gridPriceList(vh.tvAmount, vh.tvWasPrice,
					fromPrice, wasPrice);

			productImage(vh.imProductImage, externalImageRef);
			promoImages(vh, promo);

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
				.inflate(R.layout.item_grid_product, parent, false));
	}

	@NonNull
	private HeaderViewHolder getHeaderViewHolder(ViewGroup parent) {
		return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(
				R.layout.item_found_layout, parent, false));
	}

	@Override
	public int getItemCount() {
		return mProductList.size();
	}

	private void productImage(final SimpleDraweeView image, String imgUrl) {
		if (imgUrl != null) {
			try {
				imgUrl = imgUrl + "?w=" + 300 + "&q=" + 100;
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

		private void setTotalItem(ProductList productList) {
			if (productList.numberOfItems != null)
				tvNumberOfItem.setText(String.valueOf(productList.numberOfItems));
		}
	}

	private class SimpleViewHolder extends RecyclerView.ViewHolder {

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
}