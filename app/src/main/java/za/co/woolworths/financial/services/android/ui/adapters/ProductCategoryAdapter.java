package za.co.woolworths.financial.services.android.ui.adapters;


import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.awfs.coordination.R;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.RootCategory;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.SelectedProductView;

public class ProductCategoryAdapter extends RecyclerSwipeAdapter<ProductCategoryAdapter.SimpleViewHolder> {
    public Activity mContext;
    private List<RootCategory> mCategory;
    private SelectedProductView mSelectedProductView;

    public ProductCategoryAdapter(List<RootCategory> mCategory,
                                  SelectedProductView selectedProductView) {
        this.mCategory = mCategory;
        this.mSelectedProductView = selectedProductView;
    }

    class SimpleViewHolder extends RecyclerView.ViewHolder {

        ImageView mImageProductCategory;
        WTextView mTextProduct;
        FrameLayout mFrameRootCategory;

        SimpleViewHolder(View view) {
            super(view);
            mImageProductCategory = (ImageView) view.findViewById(R.id.imProductCategory);
            mTextProduct = (WTextView) view.findViewById(R.id.textProduct);
            mFrameRootCategory = (FrameLayout) view.findViewById(R.id.frameRootCategory);
        }
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder holder, final int position) {
        RootCategory rootCategory = mCategory.get(position);

        holder.mTextProduct.setText(rootCategory.categoryName);
        String imageUrl = rootCategory.imgUrl;

        if (imageUrl != null) {
            DrawImage drawImage = new DrawImage(holder.mTextProduct.getContext());
            drawImage.displayImage(holder.mImageProductCategory, imageUrl);
        }

        holder.mFrameRootCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedProductView.onSelectedProduct(v, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SimpleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.product_search_root_category_row, parent, false));
    }

    @Override
    public int getItemCount() {
        return mCategory.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

}