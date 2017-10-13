package za.co.woolworths.financial.services.android.ui.activities;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.Utils;


public class SelectFromDriveActivity extends AppCompatActivity implements View.OnClickListener {

	public RelativeLayout mRelRootContainer;
	public Animation mPopEnterAnimation;
	public RelativeLayout mRelPopContainer;
	public static final int ANIM_DOWN_DURATION = 700;
	public WoolworthsApplication woolworthsApplication;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(this, android.R.color.transparent);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		woolworthsApplication = (WoolworthsApplication) SelectFromDriveActivity.this.getApplication();
		setContentView(R.layout.cli_select_file);
		mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
		mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);

		WTextView tvSelectFromGallery = (WTextView) findViewById(R.id.tvSelectFromGallery);
		WTextView tvSelectFromDrive = (WTextView) findViewById(R.id.tvSelectFromDrive);

		WButton btnLoanHighOk = (WButton) findViewById(R.id.btnLoanHighOk);

		tvSelectFromGallery.setOnClickListener(this);
		tvSelectFromDrive.setOnClickListener(this);
		btnLoanHighOk.setOnClickListener(this);

		setAnimation();
		mRelPopContainer.setOnClickListener(this);
	}

	private void setAnimation() {
		mPopEnterAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.popup_enter);
		mRelRootContainer.startAnimation(mPopEnterAnimation);
	}

	@Override
	public void onClick(View v) {
		MultiClickPreventer.preventMultiClick(v);
		switch (v.getId()) {
			case R.id.btnLoanHighOk:
			case R.id.relPopContainer:
				startExitAnimation();
				break;
			case R.id.tvSelectFromGallery:
				Toast.makeText(this, "SELECT FROM GALLERY", Toast.LENGTH_LONG).show();
				break;
			case R.id.tvSelectFromDrive:
				Toast.makeText(this, "IMPORT FROM DRIVE", Toast.LENGTH_LONG).show();
				break;
			default:
				break;
		}
	}

	private void startExitAnimation() {
		TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mRelRootContainer.getHeight());
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
				dismissLayout();
			}
		});
		mRelRootContainer.startAnimation(animation);
	}

	private void dismissLayout() {
		finish();
		overridePendingTransition(0, 0);
	}
}
