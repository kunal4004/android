package za.co.woolworths.financial.services.android.util.binder.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.CreditLimit;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.binder.DataBindAdapter;
import za.co.woolworths.financial.services.android.util.binder.DataBinder;

public class CLICreditLimitContentBinder extends DataBinder<CLICreditLimitContentBinder.ViewHolder> {

    public interface OnClickListener {
         void onClick(View v, int position);
    }

    private OnClickListener onClickListener;
    private List<CreditLimit> mDataSet = new ArrayList<>();
    private int selectedPosition=-1;

    public CLICreditLimitContentBinder(DataBindAdapter dataBindAdapter, OnClickListener listener) {
        super(dataBindAdapter);
        this.onClickListener = listener;
    }

    @Override
    public ViewHolder newViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.cli_info_contents, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void bindViewHolder(final ViewHolder holder, int position) {
        CreditLimit creditLimit = mDataSet.get(position);
          if (creditLimit!=null) {
                holder.mTxtACreditLimit.setText(creditLimit.getTitle());
                holder.mTextAmount.setText(creditLimit.getAmount());
                holder.mTextAmount.setVisibility(View.VISIBLE);
          }

    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void addAll(List<CreditLimit> dataSet) {
        mDataSet.addAll(dataSet);
        notifyBinderDataSetChanged();
    }

    public void clear() {
        mDataSet.clear();
        notifyBinderDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        WTextView mTxtACreditLimit;
        WTextView mTextAmount;
        ImageView mImgInfo;

        public ViewHolder(View view) {
            super(view);
            mTxtACreditLimit = (WTextView) view.findViewById(R.id.textACreditLimit);
            mTextAmount= (WTextView) view.findViewById(R.id.textAmount);
            mImgInfo=(ImageView)view.findViewById(R.id.imgInfo);
         }
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
