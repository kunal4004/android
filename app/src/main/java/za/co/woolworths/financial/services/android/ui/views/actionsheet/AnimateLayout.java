package za.co.woolworths.financial.services.android.ui.views.actionsheet;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;

public class AnimateLayout {

	private static final int ANIM_DOWN_DURATION = 300;
	private ResultAnimator mResultAnimator;

	public interface ResultAnimator {
		void onAnimationCompleted(boolean positiveResultSelected);
	}

	public void setAnimation(View view, ResultAnimator resultAnimator) {
		Context context = WoolworthsApplication.getAppContext();
		if (context == null) return;
		this.mResultAnimator = resultAnimator;
		Animation mPopEnterAnimation = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.popup_enter);
		view.startAnimation(mPopEnterAnimation);
	}

	public void animateDismissView(View view, final boolean positiveResultSelected) {
		TranslateAnimation animation = new TranslateAnimation(0, 0, 0, view.getHeight());
		animation.setFillAfter(true);
		animation.setDuration(ANIM_DOWN_DURATION);
		animation.setAnimationListener(new TranslateAnimation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mResultAnimator.onAnimationCompleted(positiveResultSelected);
			}
		});
		view.startAnimation(animation);
	}
}
