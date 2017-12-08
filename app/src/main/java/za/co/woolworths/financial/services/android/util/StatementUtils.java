package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class StatementUtils {

	private Context mContext;

	public StatementUtils(Context context) {
		this.mContext = context;
	}

	public boolean validateEmail(String email) {
		Pattern pattern = Pattern.compile(".+@.+\\.[a-z]+");
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}

	public void showView(View v) {
		v.setVisibility(View.VISIBLE);
	}

	public void hideView(View v) {
		v.setVisibility(View.GONE);
	}

	public void invisibleView(View v) {
		v.setVisibility(View.INVISIBLE);
	}


	public void disableView(View v) {
		v.setEnabled(false);
	}

	public void enableView(View v) {
		v.setEnabled(true);
	}

	public JWTDecodedModel getJWTDecoded() {
		JWTDecodedModel result = new JWTDecodedModel();
		try {
			SessionDao sessionDao = new SessionDao(mContext, SessionDao.KEY.USER_TOKEN).get();
			if (sessionDao.value != null && !sessionDao.value.equals("")) {
				result = JWTHelper.decode(sessionDao.value);
			}
		} catch (Exception ignored) {
		}
		return result;
	}

	public void populateDocument(WTextView textView) {
		JWTDecodedModel userDetail = getJWTDecoded();
		if (userDetail != null) {
			textView.setText(userDetail.email.get(0));
		}
	}

}
