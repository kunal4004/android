package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.activities.CLIStepIndicatorActivity;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;


/**
 * Created by dimitrij on 2016/12/23.
 */

public class SlidingUpViewLayout {

    public Context mContext;

    public enum OVERLAY_TYPE {LOGOUT,INFO,EMAIL}

    public SlidingUpViewLayout(Context context){
        mContext = context;
    }

    public void openOverlayView() {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view =  layoutInflater.inflate(R.layout.open_overlay_view, null);
        WButton mBtnNo = (WButton) view.findViewById(R.id.btnNo);
        WButton mBtnYes = (WButton) view.findViewById(R.id.btnYes);
        final PopupWindow pWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        pWindow.setOutsideTouchable(false);
        mBtnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pWindow.dismiss();
            }
        });
        mBtnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pWindow.dismiss();}
        });
    }

    public PopupWindow openOverlayView(final String description, final OVERLAY_TYPE overlay_type) {

        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View view =  layoutInflater.inflate(R.layout.open_overlay_got_it, null);
        final WButton mOverlayBtn = (WButton) view.findViewById(R.id.btnOverlay);
        final WTextView mOverlayTitle = (WTextView)view.findViewById(R.id.textApplicationNotProceed);
        final WTextView  mOverlayDescription = (WTextView)view.findViewById(R.id.overlayDescription);
        final WTextView textEmailContent = (WTextView)view.findViewById(R.id.textEmailContent);
        final LinearLayout mLinEmail =(LinearLayout)view.findViewById(R.id.linEmail);

        switch(overlay_type){
            case LOGOUT:
                mLinEmail.setVisibility(View.GONE);
                mOverlayTitle.setVisibility(View.VISIBLE);
                mOverlayDescription.setText(getString(R.string.cli_process_error_content));
                mOverlayBtn.setText(getString(R.string.ok));
                break;
            case INFO:
                mLinEmail.setVisibility(View.GONE);
                mOverlayTitle.setVisibility(View.GONE);
                mOverlayDescription.setText(description);
                mOverlayBtn.setText(getString(R.string.cli_got_it));
                break;
            case EMAIL:
                mLinEmail.setVisibility(View.VISIBLE);
                mOverlayTitle.setVisibility(View.GONE);
                mOverlayDescription.setText(getString(R.string.cli_process_email_content));
                mOverlayBtn.setText(getString(R.string.ok));
                break;
            default:
                break;
        }

        final PopupWindow pWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        pWindow.setAnimationStyle(R.style.PopUpWindowAnim);
        pWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        pWindow.setOutsideTouchable(false);
        mOverlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(overlay_type){
                    case LOGOUT:

                        break;
                    case INFO:

                        break;
                    case EMAIL:
                        CLIStepIndicatorActivity cliStepIndicatorActivity = (CLIStepIndicatorActivity)mContext;
                        if (cliStepIndicatorActivity instanceof Activity){
                            cliStepIndicatorActivity.moveToPage(3);
                        }
                        break;
                    default:
                        break;

                }
                pWindow.dismiss();
            }
        });
        return pWindow;
    }

    public String getString(int id){
        return  mContext.getResources().getString(id);
    }
}
