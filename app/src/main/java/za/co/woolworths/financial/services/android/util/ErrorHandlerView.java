package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.activities.WConnectionHandlerActivity;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class ErrorHandlerView {

	private WoolworthsApplication mWoolWorthsApp;
	private RelativeLayout mRelErrorLayout;
	private WTextView mTxtEmptyStateDesc;
	private WTextView mTxtEmptyStateTitle;
	private ImageView mImgEmptyStateIcon;
	private RelativeLayout mRelativeLayout;
	private Context mContext;

	public ErrorHandlerView(Context context) {
		this.mContext = context;
	}

	public ErrorHandlerView(Context context,
	                        RelativeLayout rel) {
		this.mRelErrorLayout = rel;
		this.mContext = context;
	}


	public ErrorHandlerView(Context context, WoolworthsApplication woolworthsApplication,
	                        RelativeLayout rel) {
		this.mRelErrorLayout = rel;
		this.mWoolWorthsApp = woolworthsApplication;
		this.mContext = context;
	}

	public ErrorHandlerView(Context context,
	                        RelativeLayout relativeLayout, ImageView imageIcon, WTextView
			                        textTitle, WTextView textDesc) {
		this.mRelativeLayout = relativeLayout;
		this.mContext = context;
		this.mImgEmptyStateIcon = imageIcon;
		this.mTxtEmptyStateTitle = textTitle;
		this.mTxtEmptyStateDesc = textDesc;
	}

	public ErrorHandlerView(Context context, WoolworthsApplication woolworthsApplication,
	                        RelativeLayout relativeLayout, ImageView imageIcon, WTextView
			                        textTitle, WTextView textDesc,
	                        RelativeLayout relative) {
		this.mRelativeLayout = relativeLayout;
		this.mContext = context;
		this.mImgEmptyStateIcon = imageIcon;
		this.mTxtEmptyStateTitle = textTitle;
		this.mTxtEmptyStateDesc = textDesc;
		this.mRelErrorLayout = relative;
	}

	public void hideErrorHandlerLayout() {
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
		mRelErrorLayout.setVisibility(View.VISIBLE);
	}

	public void hideErrorHandler() {
		mRelErrorLayout.setVisibility(View.GONE);
	}

	public void showToast() {
		int toastDurationInMilliSeconds = 3000;
		Resources resources = mContext.getResources();
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context
				.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.custom_toast, null);
		view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.header_red));
		WTextView text = (WTextView) view.findViewById(R.id.textWhite);
		text.setGravity(Gravity.CENTER);
		text.setText(resources.getString(R.string.no_connection));
		text.setTextColor(Color.WHITE);
		final Toast toast = new Toast(mContext.getApplicationContext());
		toast.setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		// Set the countdown to display the toast
		CountDownTimer toastCountDown;
		toastCountDown = new CountDownTimer(toastDurationInMilliSeconds, 1000 /*Tick duration*/) {
			public void onTick(long millisUntilFinished) {
				toast.show();
			}

			public void onFinish() {
				toast.cancel();
			}
		};

		toast.setView(view);
		// Show the toast and starts the countdown
		toast.show();
		toastCountDown.start();
	}

	public void webViewBlankPage(WebView view) {
		view.loadUrl("about:blank");
	}

	public void networkFailureHandler(final String errorMessage) {
		((Activity) mContext).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showErrorHandler();
			}
		});
	}

	public void setMargin(View v, int left, int top, int right, int bottom) {
		ViewGroup.MarginLayoutParams params =
				(ViewGroup.MarginLayoutParams) v.getLayoutParams();
		params.setMargins(left, top,
				right, bottom);
	}

	public void enableCardPageScrollGesture() {
		mWoolWorthsApp.getWGlobalState().setCardGestureIsEnabled(false);
	}

	public void disableCardPageScrollGesture() {
		mWoolWorthsApp.getWGlobalState().setCardGestureIsEnabled(true);
	}
}
