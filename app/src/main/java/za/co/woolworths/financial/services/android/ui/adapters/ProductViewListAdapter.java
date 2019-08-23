package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.PromotionImages;
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.GridNavigator;
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.ProductUtils;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.Utils;

public class ProductViewListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int ITEM_VIEW_TYPE_HEADER = 0;
    private final int ITEM_VIEW_TYPE_BASIC = 1;
    private final int ITEM_VIEW_TYPE_FOOTER = 2;

    public Activity mContext;
    private List<ProductList> mProductList;

    private GridNavigator mGridNavigator;
    private boolean value;

    public ProductViewListAdapter(Activity mContext, List<ProductList> mProductList,
                                  GridNavigator gridNavigator) {
        this.mContext = mContext;
        this.mProductList = mProductList;
        this.mGridNavigator = gridNavigator;
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

            if (!isEmpty(saveText))
                vh.tvSaveText.setText(saveText);
            else {
                final String currentText = vh.tvSaveText.getText().toString();
                if (currentText != null && !currentText.isEmpty()) {
                    vh.tvSaveText.setText("");
                }
            }

            ArrayList<Double> priceList = new ArrayList<>();
            for (OtherSkus os : productList.otherSkus) {
                if (!isEmpty(os.wasPrice)) {
                    priceList.add(Double.valueOf(os.wasPrice));
                }
            }

            String wasPrice = "";
            if (priceList.size() > 0) {
                wasPrice = String.valueOf(Collections.max(priceList));
            }

            String fromPrice = String.valueOf(productList.fromPrice);
            ProductUtils.displayPrice(vh.tvAmount, vh.tvWasPrice,
                    fromPrice, wasPrice);

            productImage(vh.imProductImage, externalImageRef);
            promoImages(vh, promo);

            holder.itemView.setOnClickListener(v -> mGridNavigator.onGridItemSelected(mProductList.get(holder.getAdapterPosition())));
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        switch (viewType) {
            case ITEM_VIEW_TYPE_HEADER:
                vh = getHeaderViewHolder(parent);
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
        return new ProgressViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_progress_bar, parent, false));
    }

    @NonNull
    private SimpleViewHolder getSimpleViewHolder(ViewGroup parent) {
        return new SimpleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_product, parent, false));
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

    private void productImage(WrapContentDraweeView image, String imgUrl) {
        if (!isEmpty(imgUrl)) {
            image.setResizeImage(true);
            image.setImageURI(imgUrl + ((imgUrl.indexOf("?") > 0) ? "w=" + 300 + "&q=" + 85 : "?w=" + 300 + "&q=" + 85));
        }
    }

    private void promoImages(SimpleViewHolder holder, PromotionImages imPromo) {
        setPromotionalImage(holder.imSave, (imPromo == null) ? "" : imPromo.save);
        setPromotionalImage(holder.imReward, (imPromo == null) ? "" : imPromo.wRewards);
        setPromotionalImage(holder.imVitality, (imPromo == null) ? "" : imPromo.vitality);
        setPromotionalImage(holder.imNewImage, (imPromo == null) ? "" : imPromo.newImage);
    }

    private void setPromotionalImage(WrapContentDraweeView image, String url) {
        image.setVisibility(isEmpty(url) ? View.GONE : View.VISIBLE);
        DrawImage drawImage = new DrawImage(mContext);
        drawImage.displayImage(image, isEmpty(url) ? Utils.getExternalImageRef() : url);
    }

    private boolean isEmpty(String text) {
        return TextUtils.isEmpty(text);
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

    private class SimpleViewHolder extends RecyclerView.ViewHolder {

        WTextView tvSaveText;
        WTextView tvProductName;
        WTextView tvAmount;
        WTextView tvWasPrice;
        WrapContentDraweeView imProductImage;
        WrapContentDraweeView imNewImage;
        WrapContentDraweeView imSave;
        WrapContentDraweeView imReward;
        WrapContentDraweeView imVitality;

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
        } else {
            return ITEM_VIEW_TYPE_BASIC;
        }
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

}