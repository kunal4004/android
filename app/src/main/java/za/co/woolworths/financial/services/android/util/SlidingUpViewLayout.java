package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.ContextCompatApi24;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;

import com.awfs.coordination.R;

import java.lang.reflect.Method;
import java.util.Locale;

import za.co.woolworths.financial.services.android.ui.activities.CLIStepIndicatorActivity;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;



/**
 * Created by dimitrij on 2016/12/23.
 */

public class SlidingUpViewLayout {

    private LayoutInflater mInflator;
    public Context mContext;
    private PopupWindow mPopUpWindow;
    private PopupWindow mDarkenScreen;
    private WButton mOverlayBtn;
    private WTextView mOverlayTitle;
    private WTextView mOverlayDescription;
    private WTextView textEmailContent;
    private LinearLayout mLinEmail;
    private PopupWindow pWindow;
    private WButton mBtnCancel;
    private WButton mBtnContinue;

    public enum OVERLAY_TYPE {INSOLVENCY_CHECK,INFO,EMAIL,ERROR,MANDATORY_FIELD}

    public SlidingUpViewLayout(Context context,LayoutInflater inflater){
        this.mContext = context;
        this.mInflator = inflater;
    }

    public void openOverlayView() {
        //darken the current screen
        View view = mInflator.inflate(R.layout.open_nativemaps_layout, null);
        final PopupWindow darkenScreen = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        darkenScreen.setAnimationStyle(R.style.Darken_Screen);
        darkenScreen.showAtLocation(view, Gravity.CENTER, 0, 0);
        darkenScreen.setOutsideTouchable(false);
        //Then popup window appears
        View popupView = mInflator.inflate(R.layout.open_overlay_got_it, null);
        final WButton mOverlayBtn = (WButton) popupView.findViewById(R.id.btnOverlay);
        final WTextView mOverlayTitle = (WTextView)popupView.findViewById(R.id.textApplicationNotProceed);
        final WTextView  mOverlayDescription = (WTextView)popupView.findViewById(R.id.overlayDescription);
        final WTextView textEmailContent = (WTextView)popupView.findViewById(R.id.textEmailContent);
        final LinearLayout mLinEmail =(LinearLayout)popupView.findViewById(R.id.linEmail);
        final PopupWindow pWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        pWindow.setAnimationStyle(R.style.Animations_popup);
        pWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        pWindow.setOutsideTouchable(true);
        //Dismiss popup when touch outside
        pWindow.setTouchable(true);
        pWindow.setBackgroundDrawable(new BitmapDrawable());
        pWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                pWindow.dismiss();
                darkenScreen.dismiss();
                return true;
            }
        });

        mOverlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pWindow.dismiss();
                darkenScreen.dismiss();
            }
        });
    }

    public String getString(int id){
        return  mContext.getResources().getString(id);
    }

    public void setPopupWindowTouchModal(PopupWindow popupWindow, boolean touchModal)
    {
        if (null == popupWindow)
        {
            return;
        }
        Method method;
        try
        {
            method = PopupWindow.class.getDeclaredMethod("setTouchModal", boolean.class);
            method.setAccessible(true);
            method.invoke(popupWindow, touchModal);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public PopupWindow openOverlayView(final String description, final OVERLAY_TYPE overlay_type) {
        //darken the current screen
        View view = mInflator.inflate(R.layout.open_nativemaps_layout, null);
        final PopupWindow darkenScreen = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        darkenScreen.setAnimationStyle(R.style.Darken_Screen);
        darkenScreen.showAtLocation(view, Gravity.CENTER, 0, 0);
        darkenScreen.setOutsideTouchable(false);
        //Then popup window appears
        View popupView = null;
        switch (overlay_type){

            case INFO:
                popupView = mInflator.inflate(R.layout.open_overlay_got_it, null);
                mOverlayBtn = (WButton) popupView.findViewById(R.id.btnOverlay);
                mOverlayTitle = (WTextView)popupView.findViewById(R.id.textApplicationNotProceed);
                mOverlayDescription = (WTextView)popupView.findViewById(R.id.overlayDescription);
                textEmailContent = (WTextView)popupView.findViewById(R.id.textEmailContent);
                mLinEmail =(LinearLayout)popupView.findViewById(R.id.linEmail);
                mLinEmail.setVisibility(View.GONE);
                mOverlayTitle.setVisibility(View.GONE);
                mOverlayDescription.setText(description);
                mOverlayBtn.setText(getString(R.string.cli_got_it));
                mOverlayBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pWindow.dismiss();
                        darkenScreen.dismiss();
                    }
                });

                break;

            case EMAIL:
                popupView = mInflator.inflate(R.layout.open_overlay_got_it, null);
                mOverlayBtn = (WButton) popupView.findViewById(R.id.btnOverlay);
                mOverlayTitle = (WTextView)popupView.findViewById(R.id.textApplicationNotProceed);
                mOverlayDescription = (WTextView)popupView.findViewById(R.id.overlayDescription);
                textEmailContent = (WTextView)popupView.findViewById(R.id.textEmailContent);
                mLinEmail =(LinearLayout)popupView.findViewById(R.id.linEmail);
                mLinEmail.setVisibility(View.VISIBLE);
                mOverlayTitle.setVisibility(View.GONE);
                mOverlayDescription.setText(getString(R.string.cli_process_email_content));
                mOverlayBtn.setText(getString(R.string.ok));
                textEmailContent.setText(description);
                break;

            case ERROR:
                popupView = mInflator.inflate(R.layout.error_popup, null);
                mOverlayBtn = (WButton) popupView.findViewById(R.id.btnOverlay);
                mOverlayDescription = (WTextView)popupView.findViewById(R.id.overlayDescription);
                textEmailContent = (WTextView)popupView.findViewById(R.id.textEmailContent);
                mOverlayDescription.setText(description);
                mOverlayBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pWindow.dismiss();
                        darkenScreen.dismiss();
                    }
                });
                break;


            case MANDATORY_FIELD:
                popupView = mInflator.inflate(R.layout.open_overlay_got_it, null);
                mOverlayBtn = (WButton) popupView.findViewById(R.id.btnOverlay);
                mOverlayTitle = (WTextView)popupView.findViewById(R.id.textApplicationNotProceed);
                mOverlayDescription = (WTextView)popupView.findViewById(R.id.overlayDescription);
                textEmailContent = (WTextView)popupView.findViewById(R.id.textEmailContent);
                mLinEmail =(LinearLayout)popupView.findViewById(R.id.linEmail);
                mOverlayTitle.setText(mContext.getResources().getString(R.string.before_we_get_started));
                mLinEmail.setVisibility(View.GONE);
                mOverlayTitle.setVisibility(View.VISIBLE);
                mOverlayTitle.setAllCaps(true);
                mOverlayDescription.setText(description);
                mOverlayBtn.setText(getString(R.string.ok));
                mOverlayBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pWindow.dismiss();
                        darkenScreen.dismiss();
                    }
                });
                break;
        }

        pWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        pWindow.setAnimationStyle(R.style.Animations_popup);
        pWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        pWindow.setOutsideTouchable(true);
        //Dismiss popup when touch outside
        pWindow.setTouchable(true);
        pWindow.setBackgroundDrawable(new BitmapDrawable());
        pWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                pWindow.dismiss();
                darkenScreen.dismiss();
                return true;
            }
        });

        mPopUpWindow= pWindow;
        mDarkenScreen = darkenScreen;


        return darkenScreen;
    }

    public  void dismissLayout( ){
        if (mPopUpWindow!=null) {
            mPopUpWindow.dismiss();
            mDarkenScreen.dismiss();
        }
    }
}
