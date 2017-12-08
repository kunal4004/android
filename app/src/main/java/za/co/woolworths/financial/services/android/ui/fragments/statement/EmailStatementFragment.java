package za.co.woolworths.financial.services.android.ui.fragments.statement;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.StatementUtils;

public class EmailStatementFragment extends Fragment implements View.OnClickListener {

	private WButton btnBackToMyAccount;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.estatement_confirmation_layout, container, false);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initView(view);
	}

	private void initView(View view) {
		btnBackToMyAccount = (WButton) view.findViewById(R.id.btnBackToMyAccount);
		WTextView tvUserEmailAddress = (WTextView) view.findViewById(R.id.tvUserEmailAddress);
		StatementUtils statementUtils = new StatementUtils(getActivity());
		statementUtils.populateDocument(tvUserEmailAddress);
		btnBackToMyAccount.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnBackToMyAccount:
				Activity activity = getActivity();
				if (activity != null) {
					activity.finish();
					activity.overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
				}
				break;
		}
	}
}
