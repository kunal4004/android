package za.co.woolworths.financial.services.android.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.util.ScreenManager;

public class SingleSignOnExampleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_sign_on_example_avtivity);


        Button btnLogin = ((Button)findViewById(R.id.activity_single_sign_on_example_avtivity_btnLogin));
        btnLogin.setOnClickListener(this.btnLogin_onClick);


    }

    private View.OnClickListener btnLogin_onClick = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            ScreenManager.presentSSOWebView(SingleSignOnExampleActivity.this);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
