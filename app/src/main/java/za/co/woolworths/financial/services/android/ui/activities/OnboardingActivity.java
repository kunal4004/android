package za.co.woolworths.financial.services.android.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.util.ScreenManager;

public class OnboardingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        findViewById(R.id.txtSkip).setOnClickListener(this.txtSkip_onClick);
        findViewById(R.id.btnOnboardingLogin).setOnClickListener(this.btnSignin_onClick);
        findViewById(R.id.btnOnboardingRegister).setOnClickListener(this.btnRegister_onClick);
    }

    private View.OnClickListener txtSkip_onClick =  new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            OnboardingActivity.this.navigateToMain();
        }
    };

    private View.OnClickListener btnSignin_onClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ScreenManager.presentSSOSignin(OnboardingActivity.this);
        }
    };

    private View.OnClickListener btnRegister_onClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ScreenManager.presentSSORegister(OnboardingActivity.this);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()){
            this.navigateToMain();
        }
    }

    private void navigateToMain(){
        startActivity(new Intent(OnboardingActivity.this, WOneAppBaseActivity.class));
        finish();
    }
}
