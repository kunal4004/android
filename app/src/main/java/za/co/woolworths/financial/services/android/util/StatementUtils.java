package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
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

	public void populateDocument(WTextView textView) {
		JWTDecodedModel userDetail = SessionUtilities.getInstance().getJwt();
		if (userDetail != null) {
			textView.setText(userDetail.email.get(0));
		}
	}

	public void savePDF(InputStream is) {
		try {
			File folderDir = null;
			folderDir = new File(mContext.getExternalFilesDir("woolworth") + "/Files");
			File file = new File(folderDir, "statement.pdf");
			if (file.exists()) {
				file.delete();
			}
			if ((folderDir.mkdirs() || folderDir.isDirectory())) {
				BufferedInputStream bufferedInputStream = null;

				bufferedInputStream = new BufferedInputStream(is,
						1024 * 5);

				FileOutputStream fileOutputStream = new FileOutputStream(
						folderDir + "/" + "statement.pdf");
				byte[] buffer = new byte[1024];
				int len1 = 0;
				while ((len1 = is.read(buffer)) != -1) {
					fileOutputStream.write(buffer, 0, len1);
				}
				bufferedInputStream.close();
				fileOutputStream.close();
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void cancelRequest(Call call) {
		if (call != null && !call.isCanceled()) {
			call.cancel();
		}
	}
}
