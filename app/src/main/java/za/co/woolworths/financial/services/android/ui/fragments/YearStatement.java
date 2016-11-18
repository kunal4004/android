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

import za.co.woolworths.financial.services.android.models.dto.TierInfo;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class YearStatement extends Fragment
{


	public static final String DATA = "DATA";
	private TierInfo mData;

	@Override
	public void setArguments(Bundle args)
	{
		super.setArguments(args);
		String string = args.getString(DATA, "");
		if (!string.isEmpty())
		{
			mData = new Gson().fromJson(string, TierInfo.class);
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.year_statement_fragment, container, false);
		if (mData != null)
		{
			if (mData.currentTier.toLowerCase().equals("valued"))
			{
				((ImageView) view.findViewById(R.id.year_statement_img)).setImageResource(R.drawable.tier1_statement);
			}
			else if (mData.currentTier.toLowerCase().equals("loyal"))
			{
				((ImageView) view.findViewById(R.id.year_statement_img)).setImageResource(R.drawable.tier2_statement);
			}
			else
			{
				((ImageView) view.findViewById(R.id.year_statement_img)).setImageResource(R.drawable.tier3_statement);
			}
			((WTextView) view.findViewById(R.id.year_statement_earn)).setText(WFormatter.formatAmount(mData.earned));
			((WTextView) view.findViewById(R.id.year_statement_spend)).setText(WFormatter.formatAmount(mData.yearToDateSpend));
			((WTextView) view.findViewById(R.id.year_statement_green)).setText(WFormatter.formatAmount(mData.yearToDateGreenValue));
			((WTextView) view.findViewById(R.id.year_loyalty_period)).setText(WFormatter.formatAmount(mData.yearToDateWVouchers));
		}
		return view;
	}
}
