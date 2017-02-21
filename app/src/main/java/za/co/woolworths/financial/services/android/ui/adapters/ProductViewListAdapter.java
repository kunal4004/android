package za.co.woolworths.financial.services.android.ui.adapters;


import android.app.Activity;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.awfs.coordination.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.PromotionImages;
import za.co.woolworths.financial.services.android.ui.fragments.AddToShoppingListFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.SelectedProductView;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class ProductViewListAdapter extends RecyclerSwipeAdapter<ProductViewListAdapter.SimpleViewHolder> {
    public Activity mContext;
    private List<ProductList> mProductList;
    private SelectedProductView mSelectedProductView;
    private ProductList productItem;

    public ProductViewListAdapter(Activity mContext, List<ProductList> mProductList,
                                  SelectedProductView selectedProductView) {
        this.mContext = mContext;
        this.mProductList = mProductList;
        this.mSelectedProductView = selectedProductView;
    }

    class SimpleViewHolder extends RecyclerView.ViewHolder {

        WTextView productName;
        WTextView mTextAmount;
        WTextView mTextWasPrice;
        WTextView mTextLabelAmount;
        ImageView mSimpleDraweeView;
        ImageView imNewImage;
        ImageView mImSave;
        ImageView mImReward;
        ImageView mVitalityView;

        SimpleViewHolder(View view) {
            super(view);
            productName = (WTextView) view.findViewById(R.id.textTitle);
            mTextAmount = (WTextView) view.findViewById(R.id.textAmount);
            mTextWasPrice = (WTextView) view.findViewById(R.id.textWasPrice);
            mTextLabelAmount = (WTextView) view.findViewById(R.id.textLabelAmount);
            mSimpleDraweeView = (ImageView) view.findViewById(R.id.imProduct);
            imNewImage = (ImageView) view.findViewById(R.id.imNewImage);
            mImSave = (ImageView) view.findViewById(R.id.imSave);
            mImReward = (ImageView) view.findViewById(R.id.imReward);
            mVitalityView = (ImageView) view.findViewById(R.id.imVitality);
        }


    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder holder, final int position) {
        productItem = mProductList.get(position);
        if (productItem != null) {
            String productName = productItem.productName;
            String imgUrl = productItem.imagePath;
            String productType = productItem.productType;
            PromotionImages promo = productItem.promotionImages;
            holder.productName.setText(productName);

            productType(holder, productType);
            productImage(holder, imgUrl);
            promoImages(holder, promo);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedProductView.onSelectedProduct(v, holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
              mSelectedProductView.onLongPressState(v,position);
                return false;
            }
        });

    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_view_row, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mProductList.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }


    private void productType(SimpleViewHolder holder, String productType) {
        String price = "";
        if ("clothingProducts".equalsIgnoreCase(productType)) {
            price = String.valueOf(productItem.fromPrice);
        } else {
            price = productItem.otherSkus.get(0).price;
        }

        switch (productType) {
            case "clothingProducts":
                holder.mTextAmount.setText(WFormatter.formatAmount(price));

                if (!TextUtils.isEmpty(productItem.otherSkus.get(0).wasPrice)) {
                    holder.mTextAmount.setPaintFlags(holder.mTextAmount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.mTextAmount.setText("From: " + WFormatter.formatAmount(productItem.otherSkus.get(0).wasPrice));
                    holder.mTextWasPrice.setText(WFormatter.formatAmount(price));
                    holder.mTextLabelAmount.setVisibility(View.GONE);
                } else {
                    holder.mTextAmount.setText("From: " + WFormatter.formatAmount(price));
                    holder.mTextLabelAmount.setVisibility(View.GONE);
                }

                break;
            default:
                holder.mTextAmount.setText(
                        WFormatter.formatAmount(productItem.fromPrice));
                if (!TextUtils.isEmpty(productItem.otherSkus.get(0).wasPrice)) {
                    holder.mTextLabelAmount.setVisibility(View.VISIBLE);
                    holder.mTextAmount.setPaintFlags(holder.mTextAmount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.mTextAmount.setText(WFormatter.formatAmount(productItem.otherSkus.get(0).wasPrice));
                    holder.mTextWasPrice.setText(WFormatter.formatAmount(price));
                    holder.mTextLabelAmount.setVisibility(View.GONE);
                } else {
                    holder.mTextLabelAmount.setVisibility(View.GONE);
                }
                break;
        }
    }

    private void productImage(SimpleViewHolder holder, String imgUrl) {
        if (imgUrl != null) {
            try {
                DrawImage drawImage = new DrawImage(mContext);
                drawImage.displayImage(holder.mSimpleDraweeView, imgUrl);
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
                holder.mImSave.setVisibility(View.VISIBLE);
                drawImage.displayImage(holder.mImSave, wSave);
            } else {
                holder.mImSave.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(wReward)) {
                holder.mImReward.setVisibility(View.VISIBLE);
                drawImage.displayImage(holder.mImReward, wReward);
            } else {
                holder.mImReward.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(wVitality)) {
                holder.mVitalityView.setVisibility(View.VISIBLE);
                drawImage.displayImage(holder.mVitalityView, wVitality);
            } else {
                holder.mVitalityView.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(wNewImage)) {
                holder.imNewImage.setVisibility(View.VISIBLE);
                drawImage.displayImage(holder.imNewImage, wNewImage);

            } else {
                holder.imNewImage.setVisibility(View.GONE);
            }
        }
    }


}