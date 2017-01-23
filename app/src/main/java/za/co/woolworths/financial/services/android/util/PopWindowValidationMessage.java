package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.*;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.Locale;

import za.co.woolworths.financial.services.android.ui.activities.CLIStepIndicatorActivity;
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
    private double mLatitude;
    private double mLongiude;

    public enum OVERLAY_TYPE {
        CONFIDENTIAL, INSOLVENCY, INFO, EMAIL, ERROR, MANDATORY_FIELD,
        HIGH_LOAN_AMOUNT, STORE_LOCATOR_DIRECTION, SIGN_OUT
    }

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
                touchToDismiss(overlay_type);
                mOverlayBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startExitAnimation(overlay_type);
                    }
                });
                break;

            case INFO:
                mView = mLayoutInflater.inflate(R.layout.open_overlay_got_it, null);
                popupWindowSetting(mView);
                WTextView mOverlayTitle = (WTextView) mView.findViewById(R.id.textSignOut);
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
                touchToDismiss(overlay_type);
                mOverlayBtn
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startExitAnimation(overlay_type);
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
                                startExitAnimation(overlay_type);
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
                                startExitAnimation(overlay_type);
                            }
                        });
                break;

            case CONFIDENTIAL:
                mView = mLayoutInflater.inflate(R.layout.cli_confidential_popup, null);
                popupWindowSetting(mView);
                WTextView mTextApplicationNotProceed = (WTextView) mView.findViewById(R.id.textSignOut);
                mTextApplicationNotProceed.setText(mContext.getResources().getString(R.string.cli_pop_confidential_title));
                setAnimation();
                mRelPopContainer.setAnimation(mFadeInAnimation);
                mRelRootContainer.setAnimation(mPopEnterAnimation);
                mView.findViewById(R.id.btnOK)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startExitAnimation(overlay_type);
                            }
                        });
                break;

            case STORE_LOCATOR_DIRECTION:
                mView = mLayoutInflater.inflate(R.layout.popup_view, null);
                popupWindowSetting(mView);
                WTextView nativeMap = (WTextView) mView.findViewById(R.id.nativeGoogleMap);
                WTextView cancel = (WTextView) mView.findViewById(R.id.cancel);
                setAnimation();
                mRelPopContainer.setAnimation(mFadeInAnimation);
                mRelRootContainer.setAnimation(mPopEnterAnimation);
                //touchToDismiss();
                mRelPopContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startExitAnimation(overlay_type);
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startExitAnimation(overlay_type);
                    }
                });
                nativeMap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String uri = String.format(Locale.ENGLISH, "", "http://maps.google.com/maps?daddr=%f,%f (%s)", getmLatitude(), getmLongiude(), "");
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                        mContext.startActivity(intent);
                        dismissLayout();
                    }
                });
                break;
            case HIGH_LOAN_AMOUNT:
                mView = mLayoutInflater.inflate(R.layout.lw_too_high_error, null);
                popupWindowSetting(mView);
                setAnimation();
                touchToDismiss(overlay_type);
                mRelPopContainer.setAnimation(mFadeInAnimation);
                mRelRootContainer.setAnimation(mPopEnterAnimation);
                mView.findViewById(R.id.btnOk)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startExitAnimation(overlay_type);
                            }
                        });
                break;

            case SIGN_OUT:
                mView = mLayoutInflater.inflate(R.layout.sign_out, null);
                popupWindowSetting(mView);
                setAnimation();
                mRelPopContainer.setAnimation(mFadeInAnimation);
                mRelRootContainer.setAnimation(mPopEnterAnimation);
                mView.findViewById(R.id.btnOK)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startExitAnimation(overlay_type);
                            }
                        });

                mView.findViewById(R.id.btCancel)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startExitAnimation(overlay_type.ERROR);
                            }
                        });

                mRelPopContainer
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startExitAnimation(overlay_type.ERROR);
                            }
                        });

                break;

            case EMAIL:
                mView = mLayoutInflater.inflate(R.layout.cli_email_layout, null);
                popupWindowSetting(mView);
                setAnimation();
                WTextView textEmailContent = (WTextView) mView.findViewById(R.id.textEmailAddress);
                textEmailContent.setText(description);
                mRelPopContainer.setAnimation(mFadeInAnimation);
                mRelRootContainer.setAnimation(mPopEnterAnimation);

                mRelPopContainer
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CLIStepIndicatorActivity cliStepIndicatorActivity = (CLIStepIndicatorActivity) mContext;
                                if (cliStepIndicatorActivity instanceof Activity) {
                                    cliStepIndicatorActivity.moveToPage(3);
                                }
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
        hideStatusBar((Activity) mContext);
        mDarkenScreen = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        mDarkenScreen.setAnimationStyle(R.style.Darken_Screen);
        mDarkenScreen.showAtLocation(view, Gravity.CENTER, 0, 0);
        mDarkenScreen.setOutsideTouchable(true);
        mDarkenScreen.setFocusable(true);
        mDarkenScreen.setAnimationStyle(R.style.Animations_popup);
        mRelPopContainer = (RelativeLayout) view.findViewById(R.id.relPopContainer);
        mRelRootContainer = (RelativeLayout) view.findViewById(R.id.relContainerRootMessage);
    }

    private void startExitAnimation(final OVERLAY_TYPE type) {
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
                dismissLayout();
                showStatusBar((Activity) mContext);
                switch (type) {
                    case SIGN_OUT:
                        ScreenManager.presentSSOLogout((Activity) mContext);
                        break;
                }
            }
        });
        mRelRootContainer.startAnimation(animation);
    }

    public void touchToDismiss(final OVERLAY_TYPE overlay_type) {
        mDarkenScreen.setTouchable(true);
        mDarkenScreen.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                startExitAnimation(overlay_type);
                return true;
            }
        });
    }

    public void dismissLayout() {
        if (mDarkenScreen != null) {
            mDarkenScreen.dismiss();
        }
    }

    public double getmLatitude() {
        return mLatitude;
    }

    public void setmLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getmLongiude() {
        return mLongiude;
    }

    public void setmLongiude(double mLongiude) {
        this.mLongiude = mLongiude;
    }

    public void hideStatusBar(Activity activity) {
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        int visibility = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(visibility);
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.clearFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    }

    public void showStatusBar(Activity activity) {
        Window window = activity.getWindow();
        View decorView = activity.getWindow().getDecorView();
        int visibility = View.SYSTEM_UI_FLAG_VISIBLE;
        decorView.setSystemUiVisibility(visibility);
        window.addFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

}