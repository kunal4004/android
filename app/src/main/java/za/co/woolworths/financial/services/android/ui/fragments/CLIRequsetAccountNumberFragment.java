package za.co.woolworths.financial.services.android.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

/**
 * Created by W7099877 on 2017/10/04.
 */

public class CLIRequsetAccountNumberFragment  extends Fragment {
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View view=inflater.inflate(R.layout.cli_documents_post_bank_selection, container, false);
		return view;
	}
}
