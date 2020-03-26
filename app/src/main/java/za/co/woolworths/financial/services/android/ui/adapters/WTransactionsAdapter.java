package za.co.woolworths.financial.services.android.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.awfs.coordination.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Transaction;
import za.co.woolworths.financial.services.android.models.dto.TransactionParentObj;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.KotlinUtils;
import za.co.woolworths.financial.services.android.util.WFormatter;

/**
 * Created by W7099877 on 12/12/2016.
 */

public class WTransactionsAdapter extends BaseExpandableListAdapter {

	Activity mContext;
	List<TransactionParentObj> transactionParentObjList;

	public WTransactionsAdapter(Activity mContext, List<TransactionParentObj> transactionParentObjList) {
		this.mContext = mContext;
		this.transactionParentObjList = transactionParentObjList;
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

		TextView transactionMonth = (TextView) convertView.findViewById(R.id.transactionMonth);
		TransactionParentObj transaction = transactionParentObjList.get(groupPosition);

		if (transaction.getTransactionList().size() > 0) {
			String date = KotlinUtils.Companion.convertFromDateToDate(transaction.getTransactionList().get(0).date);
			transactionMonth.setText(date);
		} else {
			transactionMonth.setText(transaction.getMonth());
		}

		ExpandableListView eLV = (ExpandableListView) parent;
		eLV.expandGroup(groupPosition);
		convertView.setClickable(false);
		return convertView;
	}

	public String formatTransactionAmount(float transactionAmount) {
		//convert amount to int
		int amount = Math.round(transactionAmount);
		String formatedAmount;
		//Convert amount to +ve
		if (amount < 0)
			formatedAmount = WFormatter.formatAmount(-amount);
		else
			formatedAmount = WFormatter.formatAmount(amount);
		if (transactionAmount < 0)
			return formatedAmount.replace("R", "R-");
		else
			return formatedAmount;
	}

	public String addNegativeSymbolInFront(SpannableString amount) {
		String currentAmount = amount.toString();
		if (currentAmount.contains("R-")) {
			currentAmount = currentAmount.replace("R-", "- R");
		}
		return currentAmount;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		Transaction transaction = transactionParentObjList.get(groupPosition).getTransactionList().get(childPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this.mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.transaction_list_child_item, null);
		}
		TextView transactionDate = (TextView) convertView.findViewById(R.id.transactionDate);
		TextView transactionAmount = (TextView) convertView.findViewById(R.id.transactionAmount);
		TextView transactionDescription = (TextView) convertView.findViewById(R.id.transactionDescription);

		//Setting Date to the format dd/MM/yyyy
		String actualDate = transaction.date;
		String oldFormat = "yyyy-MM-dd";
		String newFormat = "dd / MM / yyyy";

		String formatedDate = "";
		SimpleDateFormat dateFormat = new SimpleDateFormat(oldFormat);
		Date myDate = null;
		try {
			myDate = dateFormat.parse(actualDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		SimpleDateFormat timeFormat = new SimpleDateFormat(newFormat);
		formatedDate = timeFormat.format(myDate);
		transactionDate.setText(formatedDate);


		transactionAmount.setText(addNegativeSymbolInFront(FontHyperTextParser.getSpannable(formatTransactionAmount(transaction.amount), 1, mContext)));
		transactionDescription.setText(transaction.description);

		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
}
