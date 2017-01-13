package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.awfs.coordination.R;

import java.lang.reflect.Method;

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

    public enum OVERLAY_TYPE {INSOLVENCY_CHECK,INFO,EMAIL,ERROR}

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
            case INSOLVENCY_CHECK:
                popupView = mInflator.inflate(R.layout.cli_insolvency_popup, null);
                mBtnCancel = (WButton) popupView.findViewById(R.id.btnCancel);
                mBtnContinue = (WButton) popupView.findViewById(R.id.btnContinue);
                mBtnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                        public void onClick(View v) {
                        Log.e("OnClickListener","----clicked");
                        ((Activity) mContext).finish();
                        ((Activity) mContext).overridePendingTransition(R.anim.slide_in_left,
                                R.anim.slide_out_right);
                    }
                });
                mBtnContinue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                break;

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
                popupView = mInflator.inflate(R.layout.open_overlay_got_it, null);
                mOverlayBtn = (WButton) popupView.findViewById(R.id.btnOverlay);
                mOverlayTitle = (WTextView)popupView.findViewById(R.id.textApplicationNotProceed);
                mOverlayDescription = (WTextView)popupView.findViewById(R.id.overlayDescription);
                textEmailContent = (WTextView)popupView.findViewById(R.id.textEmailContent);
                mLinEmail =(LinearLayout)popupView.findViewById(R.id.linEmail);
                mLinEmail.setVisibility(View.GONE);
                mOverlayTitle.setVisibility(View.GONE);
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
