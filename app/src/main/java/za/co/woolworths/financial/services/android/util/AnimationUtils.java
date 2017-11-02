/*
 * Copyright (C) 2016 Frederik Schweiger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.woolworths.financial.services.android.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;

import com.awfs.coordination.R;

public class AnimationUtils {

	public static abstract class AnimationEndListener implements Animator.AnimatorListener {
		@Override
		public void onAnimationStart(Animator animation) {
			// Do nothing
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			// Do nothing
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
			// Do nothing
		}
	}

	public static void startFadeInAnimation(Context context, View view) {
		Animation startAnimation = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.fade_in_anim);
		view.startAnimation(startAnimation);
	}

	public static void startFadeOutAnimation(Context context, View view) {
		Animation startAnimation = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.fade_out);
		view.startAnimation(startAnimation);
	}
}
