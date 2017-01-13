package za.co.woolworths.financial.services.android.util.binder.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.awfs.coordination.R;
import java.util.ArrayList;
import java.util.List;
import za.co.woolworths.financial.services.android.models.dto.SubCategory;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.binder.DataBindAdapter;
import za.co.woolworths.financial.services.android.util.binder.DataBinder;

public class SubCategoryBinder extends DataBinder<SubCategoryBinder.ViewHolder> {

    private int lastPosition=-1;

    public interface OnClickListener {
        void onClick(View v, int position);
    }

    private OnClickListener mOnClickListener;

    private Context mContext;
    private List<SubCategory> mDataSet = new ArrayList<>();
    private SubCategory subCategory;

    public SubCategoryBinder(DataBindAdapter dataBindAdapter,OnClickListener onClickListener) {
        super(dataBindAdapter);
        this.mOnClickListener = onClickListener;
    }

    @Override
    public ViewHolder newViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.ps_sub_categories_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void bindViewHolder(final ViewHolder holder, final int position) {
        subCategory = mDataSet.get(position);
        holder.mSubCategoryName.setText(subCategory.categoryName);

        holder.mSubCategoryName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnClickListener.onClick(v,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void addAll(List<SubCategory> dataSet) {
        mDataSet.addAll(dataSet);
        notifyBinderDataSetChanged();
    }

    public void clear() {
        mDataSet.clear();
        notifyBinderDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private WTextView mSubCategoryName;
        public ViewHolder(View view) {
            super(view);
            mContext = view.getContext();
            mSubCategoryName = (WTextView) view.findViewById(R.id.subCategoryName);
        }
    }

}
