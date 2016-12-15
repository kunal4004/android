package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Transaction;
import za.co.woolworths.financial.services.android.models.dto.TransactionParentObj;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

/**
 * Created by W7099877 on 12/12/2016.
 */

public class WTransactionsAdapter extends BaseExpandableListAdapter {

    Activity mContext;
    List<TransactionParentObj> transactionParentObjList;
    public WTransactionsAdapter(Activity mContext, List<TransactionParentObj> transactionParentObjList)
    {
        this.mContext=mContext;
        this.transactionParentObjList=transactionParentObjList;
    }
    @Override
    public int getGroupCount() {
        return transactionParentObjList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return transactionParentObjList.get(groupPosition).getTransactionList().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return transactionParentObjList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return transactionParentObjList.get(groupPosition).getTransactionList().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.transaction_list_parent_item, null);
        }

        WTextView transactionMonth=(WTextView)convertView.findViewById(R.id.transactionMonth);
        transactionMonth.setText(transactionParentObjList.get(groupPosition).getMonth());

        ExpandableListView eLV = (ExpandableListView) parent;
        eLV.expandGroup(groupPosition);
        convertView.setClickable(false);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Transaction transaction=transactionParentObjList.get(groupPosition).getTransactionList().get(childPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.transaction_list_child_item, null);
        }
        WTextView transactionDate=(WTextView)convertView.findViewById(R.id.transactionDate);
        WTextView transactionAmount=(WTextView)convertView.findViewById(R.id.transactionAmount);
        WTextView transactionDescription=(WTextView)convertView.findViewById(R.id.transactionDescription);
        transactionDate.setText(transaction.date);
        transactionAmount.setText("R"+String.valueOf(transaction.amount));
        transactionDescription.setText(transaction.description);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
