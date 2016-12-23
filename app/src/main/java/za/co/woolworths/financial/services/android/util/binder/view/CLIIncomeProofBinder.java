package za.co.woolworths.financial.services.android.util.binder.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.IncomeProof;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.binder.DataBindAdapter;
import za.co.woolworths.financial.services.android.util.binder.DataBinder;

public class CLIIncomeProofBinder extends DataBinder<CLIIncomeProofBinder.ViewHolder> {

    private List<IncomeProof> mDataSet = new ArrayList<>();

    public CLIIncomeProofBinder(DataBindAdapter dataBindAdapter) {
        super(dataBindAdapter);
    }

    @Override
    public ViewHolder newViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.cli_income_proof_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void bindViewHolder(final ViewHolder holder, int position) {
        IncomeProof incomeProof = mDataSet.get(position);
        if (incomeProof!=null) {
            holder.mTextOptionTitle.setText(incomeProof.getTitle());
            holder.mTextOptionDesc.setText(incomeProof.getDescription());
            holder.mImgIcon.setBackgroundResource(incomeProof.getDrawable());
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void addAll(List<IncomeProof> dataSet) {
        mDataSet.addAll(dataSet);
        notifyBinderDataSetChanged();
    }

    public void clear() {
        mDataSet.clear();
        notifyBinderDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        WTextView mTextOptionTitle;
        WTextView mTextOptionDesc;

        ImageView mImgIcon;

        public ViewHolder(View view) {
            super(view);
             mTextOptionTitle = (WTextView) view.findViewById(R.id.textOptionTitle);
             mTextOptionDesc = (WTextView) view.findViewById(R.id.textOptionDesc);
             mImgIcon=(ImageView)view.findViewById(R.id.imgIcon);
        }
    }
}
