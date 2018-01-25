package za.co.woolworths.financial.services.android.ui.fragments.statement;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.StatementUtils;

public class EmailStatementFragment extends Fragment implements View.OnClickListener {

	private String mAlternativeEmail;


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		if (bundle != null) {
			mAlternativeEmail = bundle.getString("alternativeEmail");
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.estatement_confirmation_layout, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initView(view);
	}

	private void initView(View view) {
		WButton btnBackToMyAccount = (WButton) view.findViewById(R.id.btnBackToMyAccount);
		WTextView tvUserEmailAddress = (WTextView) view.findViewById(R.id.tvUserEmailAddress);
		StatementUtils statementUtils = new StatementUtils(getActivity());
		if (TextUtils.isEmpty(mAlternativeEmail)) {
			statementUtils.populateDocument(tvUserEmailAddress);
		} else {
			tvUserEmailAddress.setText(mAlternativeEmail);
		}
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
