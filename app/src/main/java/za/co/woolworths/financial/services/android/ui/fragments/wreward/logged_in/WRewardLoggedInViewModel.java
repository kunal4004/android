package za.co.woolworths.financial.services.android.ui.fragments.wreward.logged_in;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.ScriptIntrinsicBlur;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class WRewardLoggedInViewModel extends BaseViewModel<WRewardLoggedInViewModel> {

	float blurRadius = (float) 25.0;
	public WRewardLoggedInViewModel() {
		super();
	}

	public WRewardLoggedInViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}

	public void setScreenBlurEnabled(Fragment fragment) {
		View view = fragment.getView();
		View viewToBlur = view.findViewById(R.id.tabs);
		ImageView backgroundImage = view.findViewById(R.id.blurEffectImage);

		Bitmap contentBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(contentBitmap);
		viewToBlur.draw(canvas);

		// Apply a blur effect to the captured bitmap
		Bitmap blurredBitmap = applyBlur(fragment.getActivity(), contentBitmap);

		backgroundImage.setImageBitmap(blurredBitmap);
		backgroundImage.setVisibility(View.VISIBLE);
		backgroundImage.invalidate();
		view.invalidate();
		viewToBlur.invalidate();
	}


	private Bitmap applyBlur(FragmentActivity fragmentActivity, Bitmap inputBitmap) {
		RenderScript renderScript = RenderScript.create(fragmentActivity);
		Allocation input = Allocation.createFromBitmap(renderScript, inputBitmap);
		Allocation output = Allocation.createTyped(renderScript, input.getType());

		ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
		blurScript.setInput(input);
		blurScript.setRadius(blurRadius);
		blurScript.forEach(output);
		output.copyTo(inputBitmap);

		input.destroy();
		output.destroy();
		renderScript.destroy();

		return inputBitmap;
	}

	public void setScreenBlurDisabled(Fragment fragment) {
		View view = fragment.getView();
		View viewToBlur = view.findViewById(R.id.tabs);
		ImageView backgroundImage = view.findViewById(R.id.blurEffectImage);

		backgroundImage.setVisibility(View.GONE);
		backgroundImage.setImageBitmap(null);
		backgroundImage.invalidate();

		viewToBlur.invalidate();
		view.invalidate();
	}

}
