package za.co.woolworths.financial.services.android.util.binder.view;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.BankAccountType;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.binder.DataBindAdapter;
import za.co.woolworths.financial.services.android.util.binder.DataBinder;

public class CLIBankAccountTypeBinder extends DataBinder<CLIBankAccountTypeBinder.ViewHolder> {

    private Typeface mFontDefault;
    private Typeface mFontActive;

    public interface OnCheckboxClickListener {
        void onCheckboxViewClick(View v, int position);
    }

    private OnCheckboxClickListener onCheckboxClickListener;
    private List<BankAccountType> mDataSet = new ArrayList<>();
    private int selectedPosition=-1;

    public CLIBankAccountTypeBinder(DataBindAdapter dataBindAdapter, OnCheckboxClickListener onCheckboxClickListener) {
        super(dataBindAdapter);
        this.onCheckboxClickListener = onCheckboxClickListener;
    }

    @Override
    public ViewHolder newViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.cli_check_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void bindViewHolder(final ViewHolder holder, int position) {
        BankAccountType bankAccountType = mDataSet.get(position);
        if (bankAccountType!=null) {
            holder.mTextBankName.setText(bankAccountType.accountType);
        }

        mFontDefault = Typeface.createFromAsset(holder.mImgSelectBank.getContext().getAssets(), "fonts/WFutura-Medium.ttf");
        mFontActive = Typeface.createFromAsset(holder.mImgSelectBank.getContext().getAssets(), "fonts/WFutura-SemiBold.ttf");

        if (selectedPosition==position){
            holder.mImgSelectBank.setBackgroundResource(R.drawable.tick_cli_active);
            holder.mTextBankName.setTypeface(mFontActive);
        }else {
            holder.mImgSelectBank.setBackgroundResource(R.drawable.tick_cli_inactive);
            holder.mTextBankName.setTypeface(mFontDefault);
        }

        holder.linDeaBankContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPosition = holder.getAdapterPosition();
                onCheckboxClickListener.onCheckboxViewClick(view,holder.getAdapterPosition());
                notifyBinderDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void addAll(List<BankAccountType> dataSet) {
        mDataSet.addAll(dataSet);
        notifyBinderDataSetChanged();
    }

    public void clear() {
        mDataSet.clear();
        notifyBinderDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        WTextView mTextBankName;
        ImageView mImgSelectBank;
        LinearLayout linDeaBankContainer;

        public ViewHolder(View view) {
            super(view);
            mTextBankName = (WTextView) view.findViewById(R.id.textBankName);
            mImgSelectBank=(ImageView)view.findViewById(R.id.imgSelectBank);
            linDeaBankContainer=(LinearLayout)view.findViewById(R.id.linDeaBankContainer);
         }
    }
}
