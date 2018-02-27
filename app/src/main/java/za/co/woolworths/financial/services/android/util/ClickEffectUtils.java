package za.co.woolworths.financial.services.android.util;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;


public class ClickEffectUtils {

	public void addClickEffect(View view) {
		Drawable drawableNormal = view.getBackground();
		Drawable drawablePressed = view.getBackground().getConstantState().newDrawable();
		drawablePressed.mutate();
		drawablePressed.setColorFilter(Color.argb(5, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);

		StateListDrawable listDrawable = new StateListDrawable();
		listDrawable.addState(new int[]{android.R.attr.state_pressed}, drawablePressed);
		listDrawable.addState(new int[]{}, drawableNormal);
		view.setBackground(listDrawable);
	}
}
