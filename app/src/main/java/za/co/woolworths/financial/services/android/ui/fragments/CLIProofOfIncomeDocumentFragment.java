package za.co.woolworths.financial.services.android.ui.fragments;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.CustomTypefaceSpan;
import za.co.woolworths.financial.services.android.util.Utils;

public class CLIProofOfIncomeDocumentFragment extends Fragment {

	public CLIProofOfIncomeDocumentFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.cli_proof_income_document_layout, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.CLI_POI_DOCUMENTS);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		WTextView tvClearDocumentPhoto = (WTextView) view.findViewById(R.id.tvClearDocumentPhoto);
		WTextView tvProceedDescription = (WTextView) view.findViewById(R.id.tvProceedDescription);
		emailContent(tvClearDocumentPhoto);
		updateFontType(tvProceedDescription);
	}

	private void emailContent(WTextView tvClearDocumentPhoto) {
		String takeClearPhoto = getString(R.string.take_clear_photo);
		int emailStartAt = takeClearPhoto.indexOf("cliproof");
		int emailEndAt = takeClearPhoto.indexOf(".za.") + 4;

		SpannableString takeClearPhotoSpan = new SpannableString(takeClearPhoto);
		tvClearDocumentPhoto.setMovementMethod(LinkMovementMethod.getInstance());
		ClickableSpan clickSpan = new ClickableSpan() {
			@Override
			public void updateDrawState(TextPaint ds) {
				ds.setColor(Color.BLACK);    // you can use custom color
				ds.setUnderlineText(false);    // this remove the underline
			}

			@Override
			public void onClick(View view) {
				Activity activity = getActivity();
				if (activity != null) {
					Utils.sendEmail("cliproofofincome@wfs.co.za");
				}
			}
		};
		takeClearPhotoSpan.setSpan(clickSpan, emailStartAt, emailEndAt, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		tvClearDocumentPhoto.setText(takeClearPhotoSpan);
	}

	private void updateFontType(WTextView tvProceedDescription) {
		String strProceedDescription = getString(R.string.cli_proceed_description);
		Activity activity = getActivity();
		if (activity != null) {
			AssetManager assetManager = activity.getAssets();
			Typeface myriadProRegular = Typeface.createFromAsset(assetManager, "fonts/OpenSans-Regular.ttf");
			Typeface myriadProSemiBold = Typeface.createFromAsset(assetManager, "fonts/OpenSans-SemiBold.ttf");

			TypefaceSpan myriadProRegularSpan = new CustomTypefaceSpan("", myriadProRegular);
			TypefaceSpan myriadProSemiBoldSpan = new CustomTypefaceSpan("", myriadProSemiBold);

			int maxSemiBoldLength = strProceedDescription.indexOf("?") + 1;
			SpannableString updateFontTypeSpan = new SpannableString(strProceedDescription);
			updateFontTypeSpan.setSpan(new ForegroundColorSpan(Color.BLACK), 0, maxSemiBoldLength, 0);
			updateFontTypeSpan.setSpan(myriadProSemiBoldSpan, 0, maxSemiBoldLength, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			updateFontTypeSpan.setSpan(myriadProRegularSpan, maxSemiBoldLength, strProceedDescription.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			tvProceedDescription.setText(updateFontTypeSpan);
		}
	}
}
