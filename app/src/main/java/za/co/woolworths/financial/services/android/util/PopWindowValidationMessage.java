package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.*;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class PopWindowValidationMessage {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private PopupWindow mDarkenScreen;
    private Animation mFadeInAnimation;
    private Animation mPopEnterAnimation;
    private RelativeLayout mRelPopContainer;
    private RelativeLayout mRelRootContainer;

    public enum OVERLAY_TYPE {CONFIDENTIAL, INSOLVENCY, INFO, EMAIL, ERROR, MANDATORY_FIELD, HIGH_LOAN_AMOUNT}

    public PopWindowValidationMessage(Context context) {
        this.mContext = context;
        this.mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public String getString(int id) {
        return mContext.getResources().getString(id);
    }

    public PopupWindow displayValidationMessage(String description, final OVERLAY_TYPE overlay_type) {
        View mView;

        switch (overlay_type) {

            case ERROR:
                mView = mLayoutInflater.inflate(R.layout.error_popup, null);
                popupWindowSetting(mView);
                WButton mOverlayBtn = (WButton) mView.findViewById(R.id.btnOverlay);
                WTextView mOverlayDescription = (WTextView) mView.findViewById(R.id.overlayDescription);
                mOverlayDescription.setText(description);
                setAnimation();
                mRelPopContainer.setAnimation(mFadeInAnimation);
                mRelRootContainer.setAnimation(mPopEnterAnimation);
                touchToDismiss();
                mOverlayBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startExitAnimation();
                    }
                });
                break;

            case INFO:
                mView = mLayoutInflater.inflate(R.layout.open_overlay_got_it, null);
                popupWindowSetting(mView);
                WTextView mOverlayTitle = (WTextView) mView.findViewById(R.id.textApplicationNotProceed);
                mOverlayDescription = (WTextView) mView.findViewById(R.id.overlayDescription);
                mOverlayBtn = (WButton) mView.findViewById(R.id.btnOverlay);
                LinearLayout mLinEmail = (LinearLayout) mView.findViewById(R.id.linEmail);
                mLinEmail.setVisibility(View.GONE);
                mOverlayTitle.setVisibility(View.GONE);
                mOverlayDescription.setText(description);
                mOverlayBtn.setText(getString(R.string.cli_got_it));
                setAnimation();
                mRelPopContainer.setAnimation(mFadeInAnimation);
                mRelRootContainer.setAnimation(mPopEnterAnimation);
                touchToDismiss();
                mOverlayBtn
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startExitAnimation();
                            }
                        });
                break;

            case MANDATORY_FIELD:
                mView = mLayoutInflater.inflate(R.layout.cli_mandatory_error, null);
                popupWindowSetting(mView);
                setAnimation();
                mRelPopContainer.setAnimation(mFadeInAnimation);
                mRelRootContainer.setAnimation(mPopEnterAnimation);
                mView.findViewById(R.id.btnOK)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startExitAnimation();
                            }
                        });
                break;

            case INSOLVENCY:
                mView = mLayoutInflater.inflate(R.layout.cli_insolvency_popup, null);
                popupWindowSetting(mView);
                setAnimation();
                mRelPopContainer.setAnimation(mFadeInAnimation);
                mRelRootContainer.setAnimation(mPopEnterAnimation);
                mView.findViewById(R.id.btnOK)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startExitAnimation();
                            }
                        });
                break;

            case CONFIDENTIAL:
                mView = mLayoutInflater.inflate(R.layout.cli_confidential_popup, null);
                popupWindowSetting(mView);
                WTextView mTextApplicationNotProceed = (WTextView) mView.findViewById(R.id.textApplicationNotProceed);
                mTextApplicationNotProceed.setText(mContext.getResources().getString(R.string.cli_pop_confidential_title));
                setAnimation();
                mRelPopContainer.setAnimation(mFadeInAnimation);
                mRelRootContainer.setAnimation(mPopEnterAnimation);
                mView.findViewById(R.id.btnOK)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startExitAnimation();
                            }
                        });
                break;

            case HIGH_LOAN_AMOUNT:
                mView = mLayoutInflater.inflate(R.layout.lw_too_high_error, null);
                popupWindowSetting(mView);
                setAnimation();
                touchToDismiss();
                mRelPopContainer.setAnimation(mFadeInAnimation);
                mRelRootContainer.setAnimation(mPopEnterAnimation);
                mView.findViewById(R.id.btnOk)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startExitAnimation();
                            }
                        });
                break;

        }

        return mDarkenScreen;
    }

    private void setAnimation() {
        mFadeInAnimation = android.view.animation.AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
        mPopEnterAnimation = android.view.animation.AnimationUtils.loadAnimation(mContext, R.anim.popup_enter);
    }

    private void popupWindowSetting(View view) {
        mDarkenScreen = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mDarkenScreen.setAnimationStyle(R.style.Darken_Screen);
        mDarkenScreen.showAtLocation(view, Gravity.CENTER, 0, 0);
        mDarkenScreen.setOutsideTouchable(true);
        mDarkenScreen.setFocusable(true);
        mDarkenScreen.setAnimationStyle(R.style.Animations_popup);
        mRelPopContainer = (RelativeLayout) view.findViewById(R.id.relPopContainer);
        mRelRootContainer = (RelativeLayout) view.findViewById(R.id.relContainerRootMessage);
    }

    private void startExitAnimation() {
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mRelRootContainer.getHeight());
        animation.setFillAfter(true);
        animation.setDuration(600);
        animation.setAnimationListener(new TranslateAnimation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mDarkenScreen.dismiss();
            }
        });
        mRelRootContainer.startAnimation(animation);
    }

    private void touchToDismiss() {
        mDarkenScreen.setTouchable(true);
        mDarkenScreen.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                startExitAnimation();
                return true;
            }
        });
    }

    public void dismissLayout() {
        if (mDarkenScreen != null) {
            mDarkenScreen.dismiss();
        }
    }
}