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
import com.github.barteksc.pdfviewer.listener.OnErrorListener;

import java.io.File;


public class PreviewStatement extends Fragment {

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
        File file = new File(getActivity().getExternalFilesDir("woolworth") + "/Files/" + "statement.pdf");
        PDFView pdfView = (PDFView) view.findViewById(R.id.pdfView);
        pdfView.fromFile(file)
                .enableDoubletap(true)
                .defaultPage(0)
                .scrollHandle(null)
                .enableAnnotationRendering(false)
                .enableAntialiasing(false)
                .spacing(0)
                .onError(new OnErrorListener() {
                    @Override
                    public void onError(Throwable t) {
                        Log.e("onError", t.toString());
                    }
                })
                .load();
    }
}

