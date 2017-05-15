package za.co.woolworths.financial.services.android.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

public class ErrorHandlerView {

    private WTextView mErrorTitle;
    private RelativeLayout mRelativeLayout;
    private Context mContext;

    public ErrorHandlerView(Context context,
                            RelativeLayout relativeLayout,
                            WTextView errorTitle) {
        this.mContext = context;
        this.mRelativeLayout = relativeLayout;
        this.mErrorTitle = errorTitle;
    }

    public void showErrorHandlerLayout() {
        mRelativeLayout.setVisibility(View.VISIBLE);
    }

    public void hideErrorHandlerLayout() {
        this.mRelativeLayout.setVisibility(View.GONE);
    }

    public void setErrorTitle(String errorMessage) {
        mErrorTitle.setText(errorMessage);
    }

    public void diplayErrorMessage(String type) {
        showErrorHandlerLayout();
        Resources res = mContext.getResources();
        if (type.equalsIgnoreCase("SocketTimeoutException")) {
            setErrorTitle(res.getString(R.string.socket_timeout_error));
        } else if (type.equalsIgnoreCase("ConnectException")) {
            setErrorTitle(res.getString(R.string.connection_error));
        } else if (type.startsWith("RuntimeExecutionException")) {
            setErrorTitle(res.getString(R.string.runtime_eror));
        } else {
            setErrorTitle(res.getString(R.string.runtime_eror));
        }
    }

}
