package za.co.woolworths.financial.services.android.ui.fragments.statement;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;

import java.io.File;


import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;


public class PreviewStatement extends Fragment {

    private TextView tvTitle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.statement_preview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);

        tvTitle = view.findViewById(R.id.tvTitle);
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.STATEMENTS_DOCUMENT_PREVIEW);
    }

    private void initView(View view) {
        final FragmentActivity activity = getActivity();
        if (activity == null) return;
        File file = new File(activity.getExternalFilesDir("woolworth") + "/Files/" + "statement.pdf");
        PDFView pdfView = view.findViewById(R.id.pdfView);
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
                        tvTitle.setVisibility(View.VISIBLE);
                    }
                })
                .load();
    }
}

