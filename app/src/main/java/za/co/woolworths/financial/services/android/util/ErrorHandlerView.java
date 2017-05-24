package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.activities.WConnectionHandlerActivity;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class ErrorHandlerView {

    private WTextView mTxtEmptyStateDesc;
    private WTextView mTxtEmptyStateTitle;
    private ImageView mImgEmptyStateIcon;
    private RelativeLayout mRelativeLayout;
    private WoolworthsApplication mWoolworthApp;
    private Context mContext;


    public ErrorHandlerView(WoolworthsApplication woolworthsApplication) {
        this.mWoolworthApp = woolworthsApplication;
    }

    public ErrorHandlerView(Context context, WoolworthsApplication woolworthsApplication,
                            RelativeLayout rel) {
        this.mWoolworthApp = woolworthsApplication;
        this.mRelativeLayout = rel;
        this.mContext = context;
    }

    public ErrorHandlerView(Context context, WoolworthsApplication woolworthsApplication,
                            RelativeLayout relativeLayout, ImageView imageIcon, WTextView
                                    textTitle, WTextView textDesc) {
        this.mWoolworthApp = woolworthsApplication;
        this.mRelativeLayout = relativeLayout;
        this.mContext = context;
        this.mImgEmptyStateIcon = imageIcon;
        this.mTxtEmptyStateTitle = textTitle;
        this.mTxtEmptyStateDesc = textDesc;
    }

    public void hideErrorHandlerLayout() {
        mWoolworthApp.setTriggerErrorHandler(false);
        try {
            hideErrorHandler();
        } catch (Exception ex) {
        }
    }

    public void startActivity(Activity currentActivity) {
        Intent currentIntent = new Intent(currentActivity, WConnectionHandlerActivity.class);
        currentActivity.startActivity(currentIntent);
        currentActivity.overridePendingTransition(0, 0);
    }

    public void hideEmpyState() {
        mRelativeLayout.setVisibility(View.GONE);
    }

    public void showEmptyState(int position) {
        mRelativeLayout.setVisibility(View.VISIBLE);
        setEmptyState(position);
    }

    // message(5) 4. 3. 2. 1.
    public void setEmptyState(int position) {
        Resources resources = mContext.getResources();
        TypedArray emptyStateIcon = resources.obtainTypedArray(R.array.empty_state_icon);
        String[] emptyStateTitle = resources.getStringArray(R.array.empty_state_title);
        String[] emptyStateDesc = resources.getStringArray(R.array.empty_state_desc);
        mImgEmptyStateIcon.setImageResource(emptyStateIcon.getResourceId(position, -1));
        mTxtEmptyStateTitle.setText(emptyStateTitle[position]);
        mTxtEmptyStateDesc.setText(emptyStateDesc[position]);
    }

    public void showErrorHandler() {
        mRelativeLayout.setVisibility(View.VISIBLE);
    }

    public void hideErrorHandler() {
        mRelativeLayout.setVisibility(View.GONE);
    }

    public void showToast() {
        Resources resources = mContext.getResources();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.custom_toast, null);
        view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.header_red));
        WTextView text = (WTextView) view.findViewById(R.id.textWhite);
        text.setGravity(Gravity.CENTER);
        text.setText(resources.getString(R.string.no_connection));
        text.setTextColor(Color.WHITE);
        Toast toast = new Toast(mContext.getApplicationContext());
        toast.setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        toast.show();
    }

    public void networkFailureHandler(final String errorMessage) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (errorMessage.contains("Connect")) {
                    showToast();
                }

                showErrorHandler();
            }
        });
    }
}
