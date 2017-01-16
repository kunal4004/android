package za.co.woolworths.financial.services.android.util.binder.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.awfs.coordination.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.holder.AnimateViewHolder;
import za.co.woolworths.financial.services.android.models.dto.RootCategory;
import za.co.woolworths.financial.services.android.ui.views.DynamicHeightImageView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.BitmapTransform;
import za.co.woolworths.financial.services.android.util.binder.DataBindAdapter;
import za.co.woolworths.financial.services.android.util.binder.DataBinder;

public class RootCategoryBinder extends DataBinder<RootCategoryBinder.ViewHolder> {


    public interface OnClickListener {
        void onClick(View v, int position);
    }

    private OnClickListener mOnClickListener;

    private Context mContext;
    private List<RootCategory> mDataSet = new ArrayList<>();
    private RootCategory rootCategory;

    public RootCategoryBinder(DataBindAdapter dataBindAdapter,OnClickListener onClickListener) {
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
        rootCategory = mDataSet.get(position);

       // holder.mImageProductCategory.setHeightRatio(((double)photo.getHeight())/photo.getWidth());

        Transformation transformation = new Transformation() {

            @Override
            public Bitmap transform(Bitmap source) {
                int targetWidth = holder.mImageProductCategory.getWidth();

                double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
                int targetHeight = (int) (targetWidth * aspectRatio);
                Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
                if (result != source) {
                    // Same bitmap is returned if sizes are the same
                    source.recycle();
                }

                return result;
            }

            @Override
            public String key() {
                return "transformation" + " desiredWidth";
            }
        };


        holder.mTextProduct.setText(rootCategory.categoryName);
        String imageUrl = rootCategory.imgUrl;
        if (imageUrl!=null) {
            Picasso.with(mContext)
                    .load(rootCategory.imgUrl)
                    .error(android.R.color.darker_gray)
                    .transform(transformation)
                    .into(holder.mImageProductCategory, new Callback() {
                        @Override
                        public void onSuccess() {}

                        @Override
                        public void onError() {}
                    });
        }

        holder.mFrameRootCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnClickListener.onClick(view,holder.getAdapterPosition());
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

        DynamicHeightImageView mImageProductCategory;
        WTextView mTextProduct;
        FrameLayout mFrameRootCategory;

        public ViewHolder(View view) {
            super(view);
            mContext = view.getContext();

//            Animation animation = AnimationUtils.loadAnimation(mContext,  R.anim.down_from_top);
//            view.startAnimation(animation);

            mImageProductCategory = (DynamicHeightImageView) view.findViewById(R.id.imProductCategory);
            mTextProduct = (WTextView) view.findViewById(R.id.textProduct);
            mFrameRootCategory = (FrameLayout) view.findViewById(R.id.frameRootCategory);


        }
    }
}
