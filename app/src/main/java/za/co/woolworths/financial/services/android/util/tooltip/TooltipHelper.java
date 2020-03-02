package za.co.woolworths.financial.services.android.util.tooltip;


import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class TooltipHelper {

	private Context mContext;
	private final int AUTO_HIDE_DURATION = 3000;
	private final int FADE_TOOLTIP_DURATION = 500;

	public TooltipHelper(Context context) {
		this.mContext = context;
	}

	public ViewTooltip showToolTipView(View anchorView, String text, int backgroundColor) {
		Context context = anchorView.getContext();
		return ViewTooltip
				.on(anchorView)
				.color(backgroundColor)
				// .customView(customView)
				.position(ViewTooltip.Position.TOP)
				.text(text)
				.textTypeFace(Typeface.createFromAsset(context.getAssets(), "fonts/WFutura-Medium.ttf"))
				.clickToHide(true)
				.textSize(TypedValue.COMPLEX_UNIT_SP, context.getResources().getInteger(R.integer.twelve))
				.setTextGravity(Gravity.CENTER)
				.autoHide(true, AUTO_HIDE_DURATION)
				.animation(new ViewTooltip.FadeTooltipAnimation(FADE_TOOLTIP_DURATION))
				.onDisplay(new ViewTooltip.ListenerDisplay() {
					@Override
					public void onDisplay(View view) {
					}
				})
				.onHide(new ViewTooltip.ListenerHide() {
					@Override
					public void onHide(View view) {
					}
				});
	}


	private void fadeOutAndHideTooltip(final WTextView textView) {
		Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new AccelerateInterpolator());
		fadeOut.setDuration(AUTO_HIDE_DURATION);

		fadeOut.setAnimationListener(new Animation.AnimationListener() {
			public void onAnimationEnd(Animation animation) {
				textView.setVisibility(View.INVISIBLE);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}
		});

		textView.startAnimation(fadeOut);
	}
}

