package za.co.woolworths.financial.services.android.util.binder.view;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.awfs.coordination.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import za.co.woolworths.financial.services.android.models.dto.CreditLimit;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.binder.DataBindAdapter;
import za.co.woolworths.financial.services.android.util.binder.DataBinder;

public class CLICreditLimitContentBinder extends DataBinder<CLICreditLimitContentBinder.ViewHolder> {

    public interface OnClickListener {
         void onClick(View v, int position);
    }

    private OnClickListener onClickListener;
    private List<CreditLimit> mDataSet = new ArrayList<>();

    public CLICreditLimitContentBinder(DataBindAdapter dataBindAdapter, OnClickListener listener) {
        super(dataBindAdapter);
        this.onClickListener = listener;
    }

    @Override
    public ViewHolder newViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.cli_info_content_limit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void bindViewHolder(final ViewHolder holder, final int position) {
        CreditLimit creditLimit = mDataSet.get(position);
          if (creditLimit!=null) {
                holder.mTxtACreditLimit.setText(creditLimit.getTitle());
                holder.mTextAmount.setTag(position);
                holder.mTextAmount.setHint(creditLimit.getAmount());
                holder.mTextAmount.setVisibility(View.VISIBLE);
          }
        holder.mImgInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onClick(view,position);
            }
        });
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
        WEditTextView mTextAmount;
        ImageView mImgInfo;
        LinearLayout mLinRootView;
        private String current;

        public ViewHolder(View view) {
            super(view);
            mTxtACreditLimit = (WTextView) view.findViewById(R.id.textACreditLimit);
            mTextAmount = (WEditTextView) view.findViewById(R.id.textAmount);
            mImgInfo = (ImageView) view.findViewById(R.id.imgInfo);
            mLinRootView = (LinearLayout) view.findViewById(R.id.linRootView);
            mTextAmount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if(!s.toString().equals(current))
                    {
                        if(mTextAmount.getText().toString().trim().length()>0)
                        {
                            mTextAmount.removeTextChangedListener(this);
                            String formated = mTextAmount.getText().toString().trim().replace("R", "");
                            current = formated;
                            mTextAmount.setText("R"+formated);
                            mTextAmount.setSelection(formated.length()+1);
                        }
                    }

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    int position = (int) mTextAmount.getTag();

                    if (!s.toString().equals(current)) {
                        mTextAmount.removeTextChangedListener(this);

                        String replaceable = String.format("[%s,.\\s]", NumberFormat.getCurrencyInstance().getCurrency().getSymbol());
                        String cleanString = s.toString().replaceAll(replaceable, "");

                        double parsed;
                        try {
                            parsed = Double.parseDouble(cleanString);
                        } catch (NumberFormatException e) {
                            parsed = 0.00;
                        }
                        NumberFormat formatter = NumberFormat.getCurrencyInstance();
                        formatter.setMaximumFractionDigits(0);
                        String formatted = formatter.format((parsed));

                        current = formatted;
                        mTextAmount.setText(formatted);
                        mTextAmount.setSelection(formatted.length());

                        if (mDataSet != null) {
                            mDataSet.get(position).setAmount(newAmount(String.valueOf(s)));
                        }
                        // Do whatever you want with position
                        mTextAmount.addTextChangedListener(this);

                    }
                }

            });

        }
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public String newAmount(String amount){
        if(amount.length()>0) {
            return amount.replace(" ", "").replace("R", "").replace(",","");
        }else {
            return "0";
        }
    }

}
