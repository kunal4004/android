package za.co.woolworths.financial.services.android.util.binder.view;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.awfs.coordination.R;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.CreditLimit;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.binder.DataBindAdapter;
import za.co.woolworths.financial.services.android.util.binder.DataBinder;

public class CLICreditLimitContentBinder extends DataBinder<CLICreditLimitContentBinder.ViewHolder> {

    public interface OnClickListener {
         void onClick(View v, int position);
         void scrollToBottom();
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
                holder.mTextAmount.setVisibility(View.VISIBLE);
          }

        holder.mImgInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onClick(view,position);
            }
        });

        if (position==7){
            holder.mTextAmount.setImeOptions(EditorInfo.IME_ACTION_DONE);

            holder.mTextAmount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        onClickListener.scrollToBottom();
                        return true;
                    }
                    return false;
                }
            });
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
            mTextAmount.addTextChangedListener(new NumberTextWatcher(mTextAmount));
        }
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public String newAmount(String amount){
        if(amount.length()>0) {
            return amount.replaceAll("[^0-9.]", "");
        }else {
            return "0";
        }
    }

    public class NumberTextWatcher implements TextWatcher {

        private DecimalFormat df;
        private DecimalFormat dfnd;
        private boolean hasFractionalPart;

        private EditText et;

        public NumberTextWatcher(EditText et)
        {
            df = new DecimalFormat("#,###.##");
            df.setDecimalSeparatorAlwaysShown(true);
            dfnd = new DecimalFormat("#,###");
            this.et = et;
            hasFractionalPart = false;
        }

        @SuppressWarnings("unused")
        private static final String TAG = "NumberTextWatcher";

        @Override
        public void afterTextChanged(Editable s)
        {
            int position = (int) et.getTag();
            et.removeTextChangedListener(this);

            try {
                int inilen, endlen;
                inilen = et.getText().length();
                String v = s.toString().replace(String.valueOf(df.getDecimalFormatSymbols().getGroupingSeparator()), "").replace(" ", "").replace("R ", "").replace("R", "");
                Number n = null;
                if (TextUtils.isEmpty(v)) {
                    et.setText("");
                } else {
                    try {
                        n = df.parse(v);
                    } catch (ParseException e) {
                    }
                    int cp = et.getSelectionStart();
                    String finalAmount="";
                    if (hasFractionalPart) {
                        finalAmount = "R " + df.format(n).replace(".", " ").replace(","," ");
                    } else {
                        finalAmount = "R " + dfnd.format(n).replace(".", " ").replace(","," ");
                    }
                    et.setText(finalAmount);
                    if (mDataSet != null) {
                            mDataSet.get(position).setAmount(newAmount(String.valueOf(s)));
                    }
                    endlen = et.getText().length();
                    int sel = (cp + (endlen - inilen));
                    if (sel > 0 && sel <= et.getText().length()) {
                        et.setSelection(sel);
                    } else {
                        // place cursor at the end?
                        et.setSelection(et.getText().length());
                    }
                }
            }catch(NumberFormatException nfe){
            }
            et.addTextChangedListener(this);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            if (s.toString().contains(String.valueOf(df.getDecimalFormatSymbols().getDecimalSeparator())))
            {
                hasFractionalPart = true;
            } else {
                hasFractionalPart = false;
            }
        }

    }

}
