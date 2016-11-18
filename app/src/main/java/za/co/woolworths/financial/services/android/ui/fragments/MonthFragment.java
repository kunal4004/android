package za.co.woolworths.financial.services.android.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import za.co.woolworths.financial.services.android.models.dto.TierHistory;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class MonthFragment extends Fragment
{

	public static final String DATA = "data";
	private TierHistory mData;


	@Override
	public void setArguments(Bundle args)
	{
		super.setArguments(args);
		String string = args.getString(DATA, "");
		if (!string.isEmpty())
		{
			mData = new Gson().fromJson(string, TierHistory.class);
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.month_statement_fragment, container, false);
		if (mData != null)
		{
			if (mData.tier.toLowerCase().equals("valued"))
			{
				((ImageView) view.findViewById(R.id.month_statement_img)).setImageResource(R.drawable.tier1_statement);
			}
			else if (mData.tier.toLowerCase().equals("loyal"))
			{
				((ImageView) view.findViewById(R.id.month_statement_img)).setImageResource(R.drawable.tier2_statement);
			}
			else
			{
				((ImageView) view.findViewById(R.id.month_statement_img)).setImageResource(R.drawable.tier3_statement);
			}
			((WTextView) view.findViewById(R.id.month_statement_earn)).setText(WFormatter.formatAmount(mData.monthlySavings));
			((WTextView) view.findViewById(R.id.month_statement_spend)).setText(WFormatter.formatAmount(mData.monthlySpend));
			((WTextView) view.findViewById(R.id.month_statement_green)).setText(WFormatter.formatAmount(mData.monthlyGreenValueEarned));
			((WTextView) view.findViewById(R.id.month_statement_period)).setText(WFormatter.formatAmount(mData.wVouchers));
			//Fixes WFS-109
			((WTextView) view.findViewById(R.id.month_statement_month)).setText(mData.finMonthDescription.toUpperCase());
			((WTextView) view.findViewById(R.id.month_statement_description)).setText(this.getStatementDescriptionByReplacingMonthKeyWithString(mData.finMonthDescription));
		}
		return view;
	}

	private String getStatementDescriptionByReplacingMonthKeyWithString(String replaceString){
		final String replaceKey = "THE MONTH";
		String statementDescription = getString(R.string.month_description);

		if(statementDescription.indexOf(replaceKey) >-1){

			return statementDescription.replace(replaceKey, replaceString.toUpperCase());
		}

		return statementDescription;
	}
}
