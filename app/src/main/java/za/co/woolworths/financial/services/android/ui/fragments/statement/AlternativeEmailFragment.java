package za.co.woolworths.financial.services.android.ui.fragments.statement;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.util.KeyboardUtils;

public class AlternativeEmailFragment extends Fragment {

	private WEditTextView etAlternativeEmailAddress;
	private KeyboardUtils keyboardUtils;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.alternative_email_statement_fragment, container, false);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initView(view);
		keyboardUtils();
	}

	private void initView(View view) {
		etAlternativeEmailAddress = (WEditTextView) view.findViewById(R.id.etAlternativeEmailAddress);
		etAlternativeEmailAddress.requestFocus();
	}

	private void keyboardUtils() {
		keyboardUtils = new KeyboardUtils(getActivity());
		keyboardUtils.showKeyboard(etAlternativeEmailAddress);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		keyboardUtils.hideKeyboard();
	}
}
