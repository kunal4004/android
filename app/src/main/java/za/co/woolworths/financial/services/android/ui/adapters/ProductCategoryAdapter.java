package za.co.woolworths.financial.services.android.ui.adapters;


import android.app.Activity;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.awfs.coordination.R;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.RootCategory;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.SelectedProductView;

public class ProductCategoryAdapter extends RecyclerView.Adapter<ProductCategoryAdapter.ViewHolder> {
    public Activity mContext;
    private List<RootCategory> mCategory;
    private SelectedProductView mSelectedProductView;

    public ProductCategoryAdapter(List<RootCategory> mCategory,
                                  SelectedProductView selectedProductView) {
        this.mCategory = mCategory;
        this.mSelectedProductView = selectedProductView;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView mImageProductCategory;
        WTextView mTextProduct;
        FrameLayout mFrameRootCategory;

        ViewHolder(View view) {
            super(view);
            mImageProductCategory = (SimpleDraweeView) view.findViewById(R.id.imProductCategory);
            mTextProduct = (WTextView) view.findViewById(R.id.textProduct);
            mFrameRootCategory = (FrameLayout) view.findViewById(R.id.frameRootCategory);
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        RootCategory rootCategory = mCategory.get(position);

        holder.mTextProduct.setText(rootCategory.categoryName);
        String imageUrl = rootCategory.imgUrl;

        if (imageUrl != null) {
            Log.e("categoryImageUrl",imageUrl);
            DrawImage drawImage = new DrawImage(holder.mTextProduct.getContext());
            drawImage.widthDisplayImage(holder.mImageProductCategory, Uri.parse(imageUrl));
        }

        holder.mFrameRootCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedProductView.onSelectedProduct(v, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.product_search_root_category_row, parent, false));
    }

    @Override
    public int getItemCount() {
        return mCategory.size();
    }


}