package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.Utils;

public class TransludentActivity extends AppCompatActivity {

    private RelativeLayout mRelRootContainer;
    private Animation mPopEnterAnimation;
    private RelativeLayout mRelPopContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(this, R.color.black);
        setContentView(R.layout.transparent_activity);

        mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
        mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);

        setAnimation();

        WButton wButton = (WButton) findViewById(R.id.btnOk);
        wButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startExitAnimation();
            }
        });

        mRelPopContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startExitAnimation();
            }
        });
    }

    private void startExitAnimation() {
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mRelRootContainer.getHeight());
        animation.setFillAfter(true);
        animation.setDuration(600);
        animation.setAnimationListener(new TranslateAnimation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                finish();
                overridePendingTransition(0, 0);
            }
        });
        mRelRootContainer.startAnimation(animation);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    private void setAnimation() {
        mPopEnterAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.popup_enter);
        mRelRootContainer.startAnimation(mPopEnterAnimation);

    }

}
