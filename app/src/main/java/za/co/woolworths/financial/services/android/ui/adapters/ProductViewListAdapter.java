package za.co.woolworths.financial.services.android.ui.adapters;


import android.app.Activity;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.awfs.coordination.R;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.PromotionImages;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.SelectedProductView;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class ProductViewListAdapter extends RecyclerSwipeAdapter<ProductViewListAdapter.SimpleViewHolder> {
    private final DrawImage drawImage;
    public Activity mContext;
    private List<ProductList> mProductList;
    private SelectedProductView mSelectedProductView;
    private ProductList productItem;

    public ProductViewListAdapter(Activity mContext, List<ProductList> mProductList,
                                  SelectedProductView selectedProductView) {
        this.mContext = mContext;
        this.mProductList = mProductList;
        this.mSelectedProductView = selectedProductView;
        drawImage = new DrawImage(mContext);
    }

    class SimpleViewHolder extends RecyclerView.ViewHolder {

        WTextView productName;
        WTextView mTextAmount;
        WTextView mTextWasPrice;
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
            mSimpleDraweeView = (ImageView) view.findViewById(R.id.imProduct);
            imNewImage = (ImageView) view.findViewById(R.id.imNewImage);
            mImSave = (ImageView) view.findViewById(R.id.imSave);
            mImReward = (ImageView) view.findViewById(R.id.imReward);
            mVitalityView = (ImageView) view.findViewById(R.id.imVitality);
        }
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder holder, final int position) {
        this.productItem = mProductList.get(position);
        if (productItem != null) {
            String productName = productItem.productName;
            String imgUrl = productItem.externalImageRef;
            String productType = productItem.productType;
            PromotionImages promo = productItem.promotionImages;
            holder.productName.setText(productName);

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
            productPriceList(holder.mTextAmount, holder.mTextWasPrice,
                    fromPrice, wasPrice, productType);

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
                mSelectedProductView.onLongPressState(v, position);
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

    private void productPriceList(WTextView wPrice, WTextView WwasPrice,
                                  String price, String wasPrice, String productType) {

        switch (productType) {
            case "clothingProducts":
                if (TextUtils.isEmpty(wasPrice)) {
                    wPrice.setText("From: " + WFormatter.formatAmount(price));
                    WwasPrice.setText("");
                } else {
                    if (wasPrice.equalsIgnoreCase(price)) {
                        //wasPrice equals currentPrice
                        wPrice.setText("From: " + WFormatter.formatAmount(price));
                        WwasPrice.setText("");
                        return;
                    }
                    wPrice.setText("From: " + WFormatter.formatAmount(wasPrice));
                    wPrice.setPaintFlags(wPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    WwasPrice.setText(WFormatter.formatAmount(price));
                }
                break;

            default:
                if (TextUtils.isEmpty(wasPrice)) {
                    wPrice.setText(WFormatter.formatAmount(price));
                    WwasPrice.setText("");
                } else {
                    if (wasPrice.equalsIgnoreCase(price)) { //wasPrice equals currentPrice
                        wPrice.setText(WFormatter.formatAmount(price));
                        WwasPrice.setText("");
                    } else {
                        wPrice.setText(WFormatter.formatAmount(wasPrice));
                        wPrice.setPaintFlags(wPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        WwasPrice.setText(WFormatter.formatAmount(price));
                    }
                }
                break;
        }
    }

    private void productImage(final SimpleViewHolder holder, String imgUrl) {
        if (imgUrl != null) {
            try {
                imgUrl = imgUrl + "?w=" + 300;
                drawImage.displayThumbnailImage(holder.mSimpleDraweeView, imgUrl);
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