package za.co.woolworths.financial.services.android.ui.views;

/**
 * Created by dimitrij on 2017/02/23.
 */

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.util.Utils;

public class ProductProgressDialogFrag extends DialogFragment {

	private static final String ARG_MESSAGE = "message";
	private static final String ARG_INDETERMINATE = "indeterminate";

	public static boolean DIALOG_INDETERMINATE = true;
	public static boolean DIALOG_NOT_INDETERMINATE;
	public static boolean DIALOG_CANCELABLE = true;

	public static ProductProgressDialogFrag newInstance() {
		return newInstance(R.string.loading);
	}

	public static ProductProgressDialogFrag newInstance(int message) {
		return newInstance(message, DIALOG_INDETERMINATE, DIALOG_CANCELABLE);
	}

	public static ProductProgressDialogFrag newInstance(int message, boolean indeterminate, boolean cancelable) {
		Bundle args = new Bundle();
		args.putInt(ARG_MESSAGE, message);
		args.putBoolean(ARG_INDETERMINATE, indeterminate);

		ProductProgressDialogFrag progressDialogFragment = new ProductProgressDialogFrag();
		progressDialogFragment.setArguments(args);
		progressDialogFragment.setCancelable(cancelable);
		return progressDialogFragment;
	}

	public static ProgressDialogFragment newInstance(String message, boolean indeterminate, boolean cancelable) {
		Bundle args = new Bundle();
		args.putString(ARG_MESSAGE, message);
		args.putBoolean(ARG_INDETERMINATE, indeterminate);

		ProgressDialogFragment progressDialogFragment = new ProgressDialogFragment();
		progressDialogFragment.setArguments(args);
		progressDialogFragment.setCancelable(cancelable);
		return progressDialogFragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle arguments = getArguments();
		try {
			String message = arguments.getString(ARG_MESSAGE, null);
			if (message == null) {
				message = getString(arguments.getInt(ARG_MESSAGE));
				if (message == null) {
					message = getString(R.string.loading);
				}
			}
		} catch (ClassCastException ex) {
		}
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		setCancelable(false);
		return dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		getDialog().getWindow().setBackgroundDrawableResource(
				android.R.color.transparent);
		setStatusBarColorIfPossible(R.color.white);

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
			// Marshmallow+
		} else {
			//below Marshmallow
			// remove dialog divider
			int divierId = getDialog().getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
			View divider = getDialog().findViewById(divierId);
			if (divider != null) {
				divider.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
			}
		}
		return inflater.inflate(R.layout.product_dialog, container);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		//If showing as a dialog, add some padding at the top to ensure it doesn't overlap status bar
		if (getDialog() != null) {
			getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
			view.setPadding(0, (int) convertDpToPixel(25f, getContext()), 0, 0);
			view.requestLayout();
		}

		ProgressBar mProgressBar = (ProgressBar) view.findViewById(R.id.mWoolworthsProgressBar);
		mProgressBar.getIndeterminateDrawable().setColorFilter(null);
		mProgressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
	}

	@Override
	public void onStart() {
		super.onStart();
		Utils.updateStatusBarBackground(getActivity());
		Dialog dialog = getDialog();
		if (dialog != null) {
			int width = ViewGroup.LayoutParams.MATCH_PARENT;
			int height = ViewGroup.LayoutParams.MATCH_PARENT;
			dialog.getWindow().setLayout(width, height);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Utils.updateStatusBarBackground(getActivity());
	}

	/**
	 * This method converts dp unit to equivalent pixels, depending on device density.
	 *
	 * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent px equivalent to dp depending on device density
	 */
	public static float convertDpToPixel(float dp, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
		return px;
	}

	public void setStatusBarColorIfPossible(int color) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			try {
				getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				getDialog().getWindow().setStatusBarColor(color);
			} catch (Exception ex) {
			}
		}
	}

	@Override
	public void show(FragmentManager manager, String tag) {
		FragmentTransaction fragmentTransaction = manager.beginTransaction();
		fragmentTransaction.add(this, tag);
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commitAllowingStateLoss();
		FragmentManager fragmentManager = getFragmentManager();
		if (fragmentManager != null)
			fragmentManager.executePendingTransactions();
	}
}