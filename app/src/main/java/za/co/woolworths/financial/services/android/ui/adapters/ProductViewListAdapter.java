package za.co.woolworths.financial.services.android.ui.adapters;


import android.app.Activity;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.awfs.coordination.R;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.PromotionImages;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.SelectedProductView;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class ProductViewListAdapter extends RecyclerSwipeAdapter<ProductViewListAdapter.SimpleViewHolder> {
    public Activity mContext;
    private List<ProductList> mProductList;
    private SelectedProductView mSelectedProductView;

    public ProductViewListAdapter(Activity mContext, List<ProductList> mProductList,
                                  SelectedProductView selectedProductView) {
        this.mContext = mContext;
        this.mProductList = mProductList;
        this.mSelectedProductView = selectedProductView;
    }

    class SimpleViewHolder extends RecyclerView.ViewHolder {

        WTextView productName;
        WTextView mTextAmount;
        SimpleDraweeView mSimpleDraweeView;
        SimpleDraweeView imNewImage;
        SimpleDraweeView mImSave;
        SimpleDraweeView mImReward;
        SimpleDraweeView mVitalityView;

        SimpleViewHolder(View view) {
            super(view);
            productName = (WTextView) view.findViewById(R.id.textTitle);
            mTextAmount = (WTextView) view.findViewById(R.id.textAmount);
            mSimpleDraweeView = (SimpleDraweeView) view.findViewById(R.id.imProduct);
            imNewImage = (SimpleDraweeView) view.findViewById(R.id.imNewImage);
            mImSave = (SimpleDraweeView) view.findViewById(R.id.imSave);
            mImReward = (SimpleDraweeView) view.findViewById(R.id.imReward);
            mVitalityView = (SimpleDraweeView) view.findViewById(R.id.imVitality);
        }
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder holder, final int position) {
        ProductList productItem = mProductList.get(position);
        if (productItem != null) {
            String productName = productItem.productName;
            double fromPrice = productItem.fromPrice;
            String imgUrl = productItem.imagePath;
            String productType = productItem.productType;
            PromotionImages promo = productItem.promotionImages;
            holder.productName.setText(productName);

            productType(holder, productType, fromPrice);
            productImage(holder, imgUrl);
            promoImages(holder, promo);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedProductView.onSelectedProduct(v, holder.getAdapterPosition());
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

    @Override
    public void onViewRecycled(SimpleViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.mSimpleDraweeView.getController() != null) {
            holder.mSimpleDraweeView.getController().onDetach();
        }
        if (holder.mSimpleDraweeView.getTopLevelDrawable() != null) {
            holder.mSimpleDraweeView.getTopLevelDrawable().setCallback(null);
        }
    }

    private void productType(SimpleViewHolder holder, String productType, double fromPrice) {
        switch (productType) {
            case "clothingProducts":
                holder.mTextAmount.setText(holder.mTextAmount.getContext().getString(R.string.product_from) + " : "
                        + WFormatter.formatAmount(fromPrice));

                //  holder.mSimpleDraweeView.getLayoutParams().height = 200;

                break;
            default:
                holder.mTextAmount.setText(
                        WFormatter.formatAmount(fromPrice));
                // holder.mSimpleDraweeView.getLayoutParams().height = 150;
                break;
        }
    }

    private void productImage(SimpleViewHolder holder, String imgUrl) {
        if (imgUrl != null) {
            holder.mSimpleDraweeView.getHierarchy().setPlaceholderImage(ContextCompat
                            .getDrawable(holder.mSimpleDraweeView.getContext(), R.drawable.rectangle),
                    ScalingUtils.ScaleType.CENTER_INSIDE);
            try {
                setupImage(holder.mSimpleDraweeView, imgUrl);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void promoImages(SimpleViewHolder holder, PromotionImages imPromo) {
        if (imPromo != null) {
            String wSave = imPromo.save;
            String wReward = imPromo.wRewards;
            String wVitality = imPromo.vitality;
            String wNewImage = imPromo.newImage;

            if (!TextUtils.isEmpty(wSave)) {
                holder.mImSave.setVisibility(View.VISIBLE);
                setupImage(holder.mImSave, wSave);
            } else {
                holder.mImSave.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(wReward)) {
                holder.mImReward.setVisibility(View.VISIBLE);
                setupImage(holder.mImSave, wReward);
            } else {
                holder.mImReward.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(wVitality)) {
                holder.mVitalityView.setVisibility(View.VISIBLE);
                setupImage(holder.mImSave, wVitality);
            } else {
                holder.mVitalityView.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(wNewImage)) {
                holder.imNewImage.setVisibility(View.VISIBLE);
                setupImage(holder.mImSave, wNewImage);

            } else {
                holder.imNewImage.setVisibility(View.GONE);
            }
        }
    }

    private void setupImage(final SimpleDraweeView simpleDraweeView, final String uri) {

        if (TextUtils.isEmpty(uri))
            return;

        simpleDraweeView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                simpleDraweeView.getViewTreeObserver().removeOnPreDrawListener(this);
                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri))
                        //.setResizeOptions(new ResizeOptions(simpleDraweeView.getWidth(), simpleDraweeView.getHeight()))
                        .build();
                PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                        .setOldController(simpleDraweeView.getController())
                        .setImageRequest(request)
                        .build();

                simpleDraweeView.setController(controller);
                simpleDraweeView.setImageURI(uri);
                return true;

            }
        });
    }
}