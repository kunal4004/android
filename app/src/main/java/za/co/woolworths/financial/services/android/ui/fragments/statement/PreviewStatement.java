package za.co.woolworths.financial.services.android.ui.fragments.statement;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.File;

import za.co.woolworths.financial.services.android.util.Utils;

public class PreviewStatement extends Fragment {

	private File mFile;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.statement_preview, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initView(view);
	}

	private void initView(View view) {
		mFile = new File(getActivity().getExternalFilesDir("woolworth") + "/Files/" + "statement.pdf");
		PDFView pdfView = (PDFView) view.findViewById(R.id.pdfView);
		pdfView.fromFile(mFile)
				.enableDoubletap(true)
				.defaultPage(0)
				.scrollHandle(new DefaultScrollHandle(getActivity()))
				.enableAnnotationRendering(true)
				.enableAntialiasing(true)
				.spacing(0)
				.load();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		try {
			Utils.deleteDirectory(mFile);
		} catch (Exception ex) {
			Log.d("deleteDirectoryErr", ex.toString());
		}
	}
}
