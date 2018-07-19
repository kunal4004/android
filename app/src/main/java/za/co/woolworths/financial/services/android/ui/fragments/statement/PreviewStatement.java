package za.co.woolworths.financial.services.android.ui.fragments.statement;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.awfs.coordination.R;

import java.io.File;

import es.voghdev.pdfviewpager.library.PDFViewPager;
import es.voghdev.pdfviewpager.library.adapter.BasePDFPagerAdapter;
import za.co.woolworths.financial.services.android.util.Utils;

public class PreviewStatement extends Fragment {

	private File mFile;
	private PDFViewPager pdfViewPager;
	private View view;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		view = inflater.inflate(R.layout.statement_preview, container, false);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initView(view);
	}

	private void initView(View view) {
		mFile = new File(getActivity().getExternalFilesDir("woolworth") + "/Files/" + "statement.pdf");
		Log.e("mFile", String.valueOf(mFile.exists()));
		String path = mFile.getAbsolutePath();
		pdfViewPager = new PDFViewPager(getActivity(), path);
		LinearLayout llPDFLine = (LinearLayout) view.findViewById(R.id.llPDFLine);
		llPDFLine.addView(pdfViewPager);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		try {
			if (pdfViewPager != null) {
				((BasePDFPagerAdapter) pdfViewPager.getAdapter()).close();
			}
			Utils.deleteDirectory(mFile);
		} catch (Exception ex) {
			Log.d("deleteDirectoryErr", ex.toString());
		}
	}
}
