package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Context;
import android.icu.lang.UProperty;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class ToastUtils {

	private boolean allCapsUpperCase = true;

	public interface ToastInterface {
		void onToastButtonClicked(String currentState);
	}

	private ToastInterface toastInterface;
	private final int POPUP_DELAY_MILLIS = 3000;
	private Activity activity;
	private String message;
	private boolean viewState = false;
	private int gravity = Gravity.BOTTOM;
	private int pixel;
	private View view;
	private String currentState;
	private String cartText;

	public ToastUtils() {
	}

	public ToastUtils(ToastInterface toastInterface) {
		this.toastInterface = toastInterface;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setMessage(int message) {
		this.message = (getActivity() == null) ? "" : getActivity().getResources().getString(message);
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setViewState(boolean viewState) {
		this.viewState = viewState;
	}

	public boolean getViewState() {
		return this.viewState;
	}

	public void setGravity(int gravity) {
		this.gravity = gravity;
	}

	public int getGravity() {
		return gravity;
	}

	public void setPixel(int pixel) {
		this.pixel = pixel;
	}

	public int getPixel() {
		return pixel;
	}

	public void setView(View view) {
		this.view = view;
	}

	public View getView() {
		return view;
	}

	public void setCurrentState(String currentState) {
		this.currentState = currentState;
	}

	public String getCurrentState() {
		return currentState;
	}

	public String getCartText() {
		return cartText;
	}

	public void setCartText(String cartText) {
		this.cartText = cartText;
	}

	public void setCartText(int cartText) {
		this.cartText = getActivity().getResources().getString(cartText);
	}

	public PopupWindow build() {
		// inflate your xml layout
		if (getActivity() != null) {
			LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.add_to_cart_success, null);
			// set the custom display
			WTextView tvView = layout.findViewById(R.id.tvView);
			WTextView tvCart = layout.findViewById(R.id.tvCart);
			WTextView tvAddToCart = layout.findViewById(R.id.tvAddToCart);
			// initialize your popupWindow and use your custom layout as the view
			final PopupWindow pw = new PopupWindow(layout,
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT, true);

			tvView.setVisibility(getViewState() ? View.VISIBLE : View.GONE);
			tvCart.setVisibility(TextUtils.isEmpty(getCartText()) ? View.GONE : View.VISIBLE);
			tvCart.setText(getCartText());
			tvAddToCart.setText(getMessage());
			tvAddToCart.setAllCaps(allCapsUpperCase);

			// handle popupWindow click event
			tvView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					toastInterface.onToastButtonClicked(getCurrentState());
					pw.dismiss(); // dismiss the window
				}
			});

			// dismiss the popup window after 3sec
			new Handler().postDelayed(new Runnable() {
				public void run() {
					if (pw != null)
						pw.dismiss();
				}
			}, POPUP_DELAY_MILLIS);

			if (getActivity() != null) {
				pw.showAtLocation(view, gravity, 0, getPixel());
			}
			return pw;
		}

		return null;
	}

	public void setAllCapsUpperCase(boolean upperCase){
		this.allCapsUpperCase = upperCase;
	}

	public PopupWindow buildCustomToast(){
		if (getActivity() != null) {

			LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.single_line_toast_layout, null);

			TextView toastMessage = layout.findViewById(R.id.toastMessage);
			toastMessage.setText(HtmlCompat.fromHtml(getMessage(), HtmlCompat.FROM_HTML_MODE_LEGACY));

			// initialize your popupWindow and use your custom layout as the view
			final PopupWindow pw = new PopupWindow(layout,
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT, true);

			// dismiss the popup window after 3sec
			new Handler().postDelayed(new Runnable() {
				public void run() {
					if (pw != null)
						pw.dismiss();
				}
			}, POPUP_DELAY_MILLIS);

			if (getActivity() != null) {
				pw.showAsDropDown(view, 0, (int) -(view.getHeight() * 2.1));
			}
			return pw;
		}

		return null;
	}
}
