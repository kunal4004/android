package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.awfs.coordination.R;

import java.text.ParseException;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Voucher;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.WFormatter;

/**
 * Created by W7099877 on 12/01/2017.
 */

public class WRewardsVouchersAdapter extends BaseAdapter{

    public Activity mContext;
    public List<Voucher> vouchers;
    public WRewardsVouchersAdapter(Activity mContext, List<Voucher> vouchers)
    {
        this.mContext=mContext;
        this.vouchers=vouchers;
    }
    @Override
    public int getCount() {
        return vouchers.size();
    }

    @Override
    public Object getItem(int position) {
        return vouchers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mContext.getLayoutInflater().inflate(R.layout.wrewards_voucher_details_item, parent, false);
            holder.beforeDate=(WTextView)convertView.findViewById(R.id.useBefore);
            holder.value=(WTextView)convertView.findViewById(R.id.value);
            holder.message=(WTextView)convertView.findViewById(R.id.message);
            holder.voucherNumber=(WTextView)convertView.findViewById(R.id.voucherNumber);
            holder.minimumSpend=(WTextView)convertView.findViewById(R.id.minSpend);
            holder.barCode=(ImageView) convertView.findViewById(R.id.barcode);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        try {
            holder.beforeDate.setText("USE BEFORE "+WFormatter.formatDateTOddMMMMYYYY(vouchers.get(position).validToDate));
        } catch (ParseException e) {
            holder.beforeDate.setText("USE BEFORE "+String.valueOf(vouchers.get(position).validToDate));
        }

        if ("PERCENTAGE".equals(vouchers.get(position).type))
        {
            holder.value.setText(String.valueOf(WFormatter.formatPercent(vouchers.get(position).amount)));
        }
        else
        {
            holder.value.setText(String.valueOf(WFormatter.formatAmountNoDecimal(vouchers.get(position).amount)));
        }
        holder.message.setText(vouchers.get(position).description);
        holder.voucherNumber.setText(vouchers.get(position).voucherNumber);
        holder.beforeDate.setText(String.valueOf(WFormatter.formatAmount(vouchers.get(position).minimumSpend)));

        return convertView;
    }
    public class ViewHolder
    {
        WTextView beforeDate;
        WTextView value;
        WTextView message;
        WTextView voucherNumber;
        WTextView minimumSpend;
        ImageView barCode;
    }
}
