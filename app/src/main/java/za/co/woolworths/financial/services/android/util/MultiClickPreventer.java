package za.co.woolworths.financial.services.android.util;

import android.view.View;

public class MultiClickPreventer {
	private static final long DELAY_IN_MS = 500;

	public static void preventMultiClick(final View view) {
		if (!view.isClickable()) {
			return;
		}
		view.setClickable(false);
		view.postDelayed(new Runnable() {
			@Override
			public void run() {
				view.setClickable(true);
			}
		}, DELAY_IN_MS);
	}
}