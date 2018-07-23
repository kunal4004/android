package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.activities.WConnectionHandlerActivity;
import za.co.woolworths.financial.services.android.ui.fragments.cli.OfferCalculationFragment;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.alert.Alerter;

public class ErrorHandlerView {

	private OfferCalculationFragment fragment;
	private RelativeLayout mRelErrorLayout;
	private WTextView mTxtEmptyStateDesc;
	private WTextView mTxtEmptyStateTitle;
	private ImageView mImgEmptyStateIcon;
	private RelativeLayout mRlRootNoConnectionLayout;
	private Context mContext;

	public ErrorHandlerView(Context context) {
		this.mContext = context;
	}

	public ErrorHandlerView(Context context,
							RelativeLayout rel) {
		this.mRelErrorLayout = rel;
		this.mContext = context;
	}

	public ErrorHandlerView(Context context,
							RelativeLayout rootNoConnectionLayout, ImageView imageIcon, WTextView
									textTitle, WTextView textDesc) {
		this.mRlRootNoConnectionLayout = rootNoConnectionLayout;
		this.mContext = context;
		this.mImgEmptyStateIcon = imageIcon;
		this.mTxtEmptyStateTitle = textTitle;
		this.mTxtEmptyStateDesc = textDesc;
	}

	public ErrorHandlerView(Context context, WoolworthsApplication woolworthsApplication,
							RelativeLayout relativeLayout, ImageView imageIcon, WTextView
									textTitle, WTextView textDesc,
							RelativeLayout relative) {
		this.mRlRootNoConnectionLayout = relativeLayout;
		this.mContext = context;
		this.mImgEmptyStateIcon = imageIcon;
		this.mTxtEmptyStateTitle = textTitle;
		this.mTxtEmptyStateDesc = textDesc;
		this.mRelErrorLayout = relative;
	}

	public ErrorHandlerView(Context context, RelativeLayout relConnectionLayout, OfferCalculationFragment fragment) {
		this.mRelErrorLayout = relConnectionLayout;
		this.mContext = context;
		this.fragment = fragment;
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
		mRlRootNoConnectionLayout.setVisibility(View.GONE);
	}

	public void showEmptyState(int position) {
		mRlRootNoConnectionLayout.setVisibility(View.VISIBLE);
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
		Alerter.create((Activity) mContext)
				.setTitle("")
				.setText(mContext.getResources().getString(R.string.no_connection))
				.setContentGravity(Gravity.CENTER)
				.setBackgroundColor(R.color.header_red)
				.setDuration(toastDurationInMilliSeconds)
				.setIcon(null)
				.show();
	}

	public void showToast(String message) {
		int toastDurationInMilliSeconds = 3000;
		Alerter.create((Activity) mContext)
				.setTitle("")
				.setText(message)
				.setContentGravity(Gravity.CENTER)
				.setBackgroundColor(R.color.header_red)
				.setDuration(toastDurationInMilliSeconds)
				.setIcon(null)
				.show();
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

	public void textDescription(String desc) {
		mTxtEmptyStateDesc.setText(desc);
	}

	public void hideDescription() {
		mTxtEmptyStateDesc.setVisibility(View.GONE);
	}

	public void hideIcon() {
		mImgEmptyStateIcon.setVisibility(View.GONE);
	}

	public void hideTitle() {
		mTxtEmptyStateTitle.setVisibility(View.GONE);
	}

	public void responseError(View view, String errorType) {
		showErrorHandler();
		WTextView tvConnectionTitle = (WTextView) view.findViewById(R.id.txtConnectionTitle);
		WTextView tvConnectionSubTitle = (WTextView) view.findViewById(R.id.tvConnectionSubTitle);
		WTextView tvDescription = (WTextView) view.findViewById(R.id.txtWWDescription);
		WButton btnRetry = (WButton) view.findViewById(R.id.btnRetry);
		RelativeLayout relConnectionLayout = (RelativeLayout) view.findViewById(R.id.no_connection_layout);

		btnRetry.setText(getString(R.string.try_again));
		tvConnectionTitle.setText(getString(R.string.no_internet_title));
		tvConnectionSubTitle.setText(getString(R.string.no_internet_subtitle));
		showView(tvConnectionSubTitle);
		if (TextUtils.isEmpty(errorType)) {
			tvDescription.setText(getString(R.string.calculating_offer_error_desc));//response error
			if (fragment.getClass().equals(OfferCalculationFragment.class)) {
				relConnectionLayout.setBackgroundColor(Color.WHITE);
			} else {
				relConnectionLayout.setBackgroundColor(ContextCompat.getColor(tvConnectionSubTitle.getContext(), R.color.recent_search_bg));
			}
		} else {
			tvDescription.setText(getString(R.string.cli_wifi_desc_error));  //network error
			relConnectionLayout.setBackgroundColor(ContextCompat.getColor(tvConnectionSubTitle.getContext(), R.color.recent_search_bg));
		}
	}

	private Resources getResources() {
		return (mContext != null) ? mContext.getResources() : null;
	}

	private String getString(int id) {
		return getResources().getString(id);
	}

	private void showView(View view) {
		view.setVisibility(View.VISIBLE);
	}

	private void hideView(View view) {
		view.setVisibility(View.GONE);
	}

	public void showErrorView(){
		mRlRootNoConnectionLayout.setVisibility(View.VISIBLE);
	}
}
