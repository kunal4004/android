package za.co.woolworths.financial.services.android.util.binder.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.RootCategory;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.binder.DataBindAdapter;
import za.co.woolworths.financial.services.android.util.binder.DataBinder;

public class RootCategoryBinder extends DataBinder<RootCategoryBinder.ViewHolder> {

    public interface OnClickListener {
        void onClick(View v, int position);
    }

    private OnClickListener mOnClickListener;

    private List<RootCategory> mDataSet = new ArrayList<>();

    public RootCategoryBinder(DataBindAdapter dataBindAdapter, OnClickListener onClickListener) {
        super(dataBindAdapter);
        this.mOnClickListener = onClickListener;
    }

    @Override
    public ViewHolder newViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.product_search_root_category_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void bindViewHolder(final ViewHolder holder, int position) {
        RootCategory rootCategory = mDataSet.get(position);

        holder.mTextProduct.setText(rootCategory.categoryName);
        String imageUrl = rootCategory.imgUrl;

        if (imageUrl != null) {
            DrawImage drawImage = new DrawImage(holder.mTextProduct.getContext());
            drawImage.displayImage(holder.mImageProductCategory, imageUrl);
        }

        holder.mFrameRootCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnClickListener.onClick(view, holder.getAdapterPosition());
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void addAll(List<RootCategory> dataSet) {
        mDataSet.addAll(dataSet);
        notifyBinderDataSetChanged();
    }

    public void clear() {
        mDataSet.clear();
        notifyBinderDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView mImageProductCategory;
        WTextView mTextProduct;
        FrameLayout mFrameRootCategory;

        public ViewHolder(View view) {
            super(view);
            mImageProductCategory = (ImageView) view.findViewById(R.id.imProductCategory);
            mTextProduct = (WTextView) view.findViewById(R.id.textProduct);
            mFrameRootCategory = (FrameLayout) view.findViewById(R.id.frameRootCategory);
        }
    }
}