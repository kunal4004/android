package za.co.woolworths.financial.services.android.ui.activities.account

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountsFragment

class MyAccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_account_activity)

        if (savedInstanceState == null) {
            addFragment(
                    fragment = MyAccountsFragment(),
                    tag = MyAccountsFragment::class.java.simpleName,
                    containerViewId = R.id.accountContainerFrameLayout
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment = supportFragmentManager.findFragmentById(R.id.accountContainerFrameLayout)
        fragment?.onActivityResult(requestCode, resultCode, data)
    }
}