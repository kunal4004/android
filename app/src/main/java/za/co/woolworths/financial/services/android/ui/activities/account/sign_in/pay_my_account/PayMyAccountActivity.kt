package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.util.KotlinUtils

class PayMyAccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KotlinUtils.setTransparentStatusBar(this)
        setContentView(R.layout.pay_my_account_activity)
    }

}