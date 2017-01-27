package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.util.Utils;

/**
 * Created by dimitrij on 2017/01/13.
 */

public class EnterBarcodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(this,R.color.black);
        setContentView(R.layout.product_search_manual_code_activity);


    }
}
