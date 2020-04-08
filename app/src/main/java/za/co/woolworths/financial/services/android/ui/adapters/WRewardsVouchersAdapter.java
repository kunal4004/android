package za.co.woolworths.financial.services.android.ui.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.core.content.ContextCompat;

import com.awfs.coordination.R;
import com.crashlytics.android.Crashlytics;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.text.ParseException;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Voucher;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

/**
 * Created by W7099877 on 12/01/2017.
 */

public class WRewardsVouchersAdapter extends BaseAdapter {

    public Activity mContext;
    public List<Voucher> vouchers;

    public WRewardsVouchersAdapter(Activity mContext, List<Voucher> vouchers) {
        this.mContext = mContext;
        this.vouchers = vouchers;
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

    @SuppressLint("CutPasteId")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mContext.getLayoutInflater().inflate(R.layout.wrewards_voucher_details_item, parent, false);
            holder.validFromDate = convertView.findViewById(R.id.validFromTextTextView);
            holder.validUntilDate = convertView.findViewById(R.id.validFromTextTextView);
            holder.voucherValue = convertView.findViewById(R.id.voucherValueTextView);
            holder.message = convertView.findViewById(R.id.message);
            holder.voucherNumber = convertView.findViewById(R.id.voucherNumber);
            holder.minimumSpend = convertView.findViewById(R.id.minSpend);
            holder.barCode = convertView.findViewById(R.id.barcode);
            holder.validFromContainer = convertView.findViewById(R.id.validFromContainer);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        int fromUntilContainerColor = (position == 0) ? R.color.status_green : R.color.status_dark_green;
        holder.validFromContainer.setBackgroundColor(ContextCompat.getColor(mContext, fromUntilContainerColor));

        try {
            holder.validFromDate.setText(WFormatter.formatDate(vouchers.get(position).validFromDate));
            holder.validUntilDate.setText(WFormatter.formatDate(vouchers.get(position).validToDate));
        } catch (ParseException e) {
            holder.validFromDate.setText(String.valueOf(vouchers.get(position).validFromDate));
            holder.validUntilDate.setText(String.valueOf(vouchers.get(position).validToDate));
        }

        if ("PERCENTAGE".equals(vouchers.get(position).type)) {
            holder.voucherValue.setText(WFormatter.formatPercent(vouchers.get(position).amount));
        } else {
            holder.voucherValue.setText(WFormatter.formatAmountNoDecimal(vouchers.get(position).amount));
        }
        holder.message.setText(vouchers.get(position).description);
        holder.voucherNumber.setText(WFormatter.formatVoucher(vouchers.get(position).voucherNumber));
        holder.minimumSpend.setText(WFormatter.formatAmount(vouchers.get(position).minimumSpend));
        try {
            holder.barCode.setImageBitmap(Utils.encodeAsBitmap(vouchers.get(position).voucherNumber, BarcodeFormat.CODE_128, convertView.getWidth(), 60));
        } catch (WriterException e) {
            Crashlytics.logException(e);
        }
        return convertView;
    }

    public static class ViewHolder {
        TextView validFromDate;
        TextView validUntilDate;
        TextView voucherValue;
        TextView message;
        TextView voucherNumber;
        TextView minimumSpend;
        ImageView barCode;
        View validFromContainer;
    }
}
