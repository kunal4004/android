package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.awfs.coordination.R;
import java.util.List;

import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager;

public class PopWindowValidationMessage {

	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private PopupWindow mDarkenScreen;
	private Animation mFadeInAnimation;
	private Animation mPopEnterAnimation;
	private RelativeLayout mRelPopContainer;
	private RelativeLayout mRelRootContainer;
	private String mName;
	private double mLatitude;
	private double mLongiude;
	final String HUAWEI_MAP_PACKAGE = "com.huawei.maps.app";
	final String GOOGLE_MAP_PACKAGE = "com.google.android.apps.maps";

	public enum OVERLAY_TYPE {
		CONFIDENTIAL, INSOLVENCY, INFO, EMAIL, ERROR, MANDATORY_FIELD,
		HIGH_LOAN_AMOUNT, LOW_LOAN_AMOUNT, STORE_LOCATOR_DIRECTION, BARCODE_ERROR,
		SHOPPING_LIST_INFO
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
				TextView mOverlayDescription = (TextView) mView.findViewById(R.id.overlayDescription);
				if (description != null)
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
				WTextView mOverlayTitle = (WTextView) mView.findViewById(R.id.textApplicationNotProceed);
				TextView overlayDescription = mView.findViewById(R.id.overlayDescription);
				mOverlayBtn = (WButton) mView.findViewById(R.id.btnOverlay);
				LinearLayout mLinEmail = (LinearLayout) mView.findViewById(R.id.linEmail);
				mLinEmail.setVisibility(View.GONE);
				mOverlayTitle.setVisibility(View.GONE);
				overlayDescription.setText(description);
				mOverlayBtn.setText(getString(R.string.got_it));
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
				WTextView mTextProceed = (WTextView) mView.findViewById(R.id.textApplicationNotProceed);
				mTextProceed.setText(description);
				setAnimation();
				mRelPopContainer.setAnimation(mFadeInAnimation);
				mRelRootContainer.setAnimation(mPopEnterAnimation);
//                mView.findViewById(R.id.btnOK)
//                        .setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                startExitAnimation(overlay_type);
//                            }
//                        });
				break;

			case INSOLVENCY:
				mView = mLayoutInflater.inflate(R.layout.cli_insolvency_popup, null);
				popupWindowSetting(mView);
				setAnimation();
				mRelPopContainer.setAnimation(mFadeInAnimation);
				mRelRootContainer.setAnimation(mPopEnterAnimation);
//                mView.findViewById(R.id.btnOK)
//                        .setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                startExitAnimation(overlay_type);
//                            }
//                        });
				break;

			case CONFIDENTIAL:
				mView = mLayoutInflater.inflate(R.layout.cli_confidential_popup, null);
				popupWindowSetting(mView);
				WTextView mTextApplicationNotProceed = (WTextView) mView.findViewById(R.id.textApplicationNotProceed);
				mTextApplicationNotProceed.setText(mContext.getResources().getString(R.string.cli_pop_confidential_title));
				setAnimation();
				mRelPopContainer.setAnimation(mFadeInAnimation);
				mRelRootContainer.setAnimation(mPopEnterAnimation);
//                mView.findViewById(R.id.btnOK)
//                        .setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                startExitAnimation(overlay_type);
//                            }
//                        });
				break;

			case STORE_LOCATOR_DIRECTION:
				mView = mLayoutInflater.inflate(R.layout.popup_view, null);
				popupWindowSetting(mView);
				WTextView googleNativeMap =  mView.findViewById(R.id.nativeGoogleMap);
				WTextView petalNativeMap =  mView.findViewById(R.id.nativePetalMap);
				WTextView cancel =  mView.findViewById(R.id.cancel);
				setAnimation();
				mRelPopContainer.setAnimation(mFadeInAnimation);
				mRelRootContainer.setAnimation(mPopEnterAnimation);
				//touchToDismiss();
				mRelPopContainer.setOnClickListener(v -> startExitAnimation(overlay_type));
				cancel.setOnClickListener(v -> startExitAnimation(overlay_type));

				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="));
				List<ResolveInfo> list = mContext.getPackageManager().queryIntentActivities(intent,
						PackageManager.MATCH_DEFAULT_ONLY);
				try {
					if (list != null) {
						if (list.size() == 0 && Utils.isGooglePlayServicesAvailable() && !Utils.isHuaweiMobileServicesAvailable()) {
							googleNativeMap.setVisibility(View.VISIBLE);
							mView.findViewById(R.id.nativeGoogleMapDivider).setVisibility(View.VISIBLE);
						} else if (list.size() == 0 && !Utils.isGooglePlayServicesAvailable() && Utils.isHuaweiMobileServicesAvailable()) {

							petalNativeMap.setVisibility(View.VISIBLE);
							mView.findViewById(R.id.nativePetalMapDivider).setVisibility(View.VISIBLE);

						} else {
							for (ResolveInfo resolveInfo : list) {
								ActivityInfo activityInfo = resolveInfo.activityInfo;
								switch (activityInfo.packageName) {
									case HUAWEI_MAP_PACKAGE:
										petalNativeMap.setVisibility(View.VISIBLE);
										mView.findViewById(R.id.nativePetalMapDivider).setVisibility(View.VISIBLE);
										break;
									case GOOGLE_MAP_PACKAGE:
										googleNativeMap.setVisibility(View.VISIBLE);
										mView.findViewById(R.id.nativeGoogleMapDivider).setVisibility(View.VISIBLE);
										break;
									default:
										break;
								}
							}
						}
					}
				} catch (Exception e) {
					FirebaseManager.logException(e);
				}
				View.OnClickListener onClickListener= v -> {
					Location location = Utils.getLastSavedLocation();
					String uri = null;
					switch (v.getId()){
						case R.id.nativeGoogleMap:
							if (location != null) {
								uri = "http://maps.google.com/maps?f=d&saddr=" + location.getLatitude() + "," + location.getLongitude() + "&daddr=" + getmLatitude() + "," + getmLongiude();
							} else {
								uri = "http://maps.google.com/maps?q=loc:" + getmLatitude() + "," + getmLongiude();
							}
							Intent intent12 = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
							intent12.setComponent(new ComponentName("com.google.android.apps.maps",
									"com.google.android.maps.MapsActivity"));
							mContext.startActivity(intent12);
							dismissLayout();
							break;

						case R.id.nativePetalMap:
							if(location!=null) {
								uri = "petalmaps://navigation?saddr=" + location.getLatitude() + "," + location.getLongitude() + "&daddr=" + getmLatitude() + "," + getmLongiude();
								Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
								if (intent1.resolveActivity(mContext.getPackageManager()) != null) {
								mContext.startActivity(intent1);
								}
								dismissLayout();
							}
							break;
					}
				};
				googleNativeMap.setOnClickListener(onClickListener);
				petalNativeMap.setOnClickListener(onClickListener);
				break;
			case HIGH_LOAN_AMOUNT:
				mView = mLayoutInflater.inflate(R.layout.error_title_desc_layout, null);
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


			case EMAIL:
				mView = mLayoutInflater.inflate(R.layout.cli_email_layout, null);
				popupWindowSetting(mView);
				setAnimation();
				WTextView textEmailContent = (WTextView) mView.findViewById(R.id.textEmailAddress);
				textEmailContent.setText(description);
				mRelPopContainer.setAnimation(mFadeInAnimation);
				mRelRootContainer.setAnimation(mPopEnterAnimation);
				break;

			case BARCODE_ERROR:
				mView = mLayoutInflater.inflate(R.layout.barcode_error, null);
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

		}

		return mDarkenScreen;
	}

	private void setAnimation() {
		mFadeInAnimation = android.view.animation.AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
		mPopEnterAnimation = android.view.animation.AnimationUtils.loadAnimation(mContext, R.anim.popup_enter);
	}

	private void popupWindowSetting(View view) {
		view.bringToFront();
		hideStatusBar((Activity) mContext);
		mDarkenScreen = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
		mDarkenScreen.setAnimationStyle(R.style.Darken_Screen);
		mDarkenScreen.showAtLocation(view, Gravity.AXIS_PULL_BEFORE, 0, 0);
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

	public String getmName() {
		return mName;
	}

	public void setmName(String mName) {
		this.mName = mName;
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
		window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR, WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR);
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	public void showStatusBar(Activity activity) {
		Window window = activity.getWindow();
		View decorView = activity.getWindow().getDecorView();
		int visibility = View.SYSTEM_UI_FLAG_VISIBLE;
		decorView.setSystemUiVisibility(visibility);
		window.addFlags(
				WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//Utils.updateStatusBarBackground(activity);
		View decor = activity.getWindow().getDecorView();
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
			decor.setSystemUiVisibility(0);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
		}
	}
}