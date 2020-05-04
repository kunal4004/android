package za.co.woolworths.financial.services.android.ui.activities.account

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountsFragment
import za.co.woolworths.financial.services.android.util.Utils

class MyAccountActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_OPEN_STATEMENT = 3334
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_account_activity)
        Utils.updateStatusBarBackground(this)
        if (savedInstanceState == null) {
            addFragment(
                    fragment = MyAccountsFragment(),
                    tag = MyAccountsFragment::class.java.simpleName,
                    containerViewId = R.id.accountContainerFrameLayout)
        }

        intent?.extras?.apply {
            val accounts: String? = getString("accounts", "")
            accounts?.apply {
                val accountResponse = Gson().fromJson(this, AccountsResponse::class.java)
                accountResponse?.accountList?.get(0)?.apply {
                    if (productGroupCode.toLowerCase() != "cc") {
                        WoolworthsApplication.getInstance().setProductOfferingId(productOfferingId)
                        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDSTATEMENTS)
                        val openStatement =
                                Intent(this@MyAccountActivity, StatementActivity::class.java)
                        startActivityForResult(openStatement, REQUEST_CODE_OPEN_STATEMENT)
                        overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_CODE_OPEN_STATEMENT -> {
                finish()
                overridePendingTransition(0,0)
                return
            }
        }
        val fragment = supportFragmentManager.findFragmentById(R.id.accountContainerFrameLayout)
        fragment?.onActivityResult(requestCode, resultCode, data)
    }
}