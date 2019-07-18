package za.co.woolworths.financial.services.android.ui.activities

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.error_handler_activity.*
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.ErrorHandlerFragment
import za.co.woolworths.financial.services.android.util.Utils

class ErrorHandlerActivity : AppCompatActivity() {

    var errorType: Int = 0

    companion object {
        // Error Types
        const val COMMON: Int = 0
        const val ATM_PIN_LOCKED: Int = 1
        const val PASSCODE_LOCKED: Int = 2
        const val WITH_NO_ACTION: Int = 3

        //RESULT_CODES
        const val RESULT_RETRY: Int = 153
        const val RESULT_RESET_PASSCODE: Int = 155
        //REQUEST_CODES
        const val ERROR_PAGE_REQUEST_CODE: Int = 190
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.error_handler_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()
        if (savedInstanceState == null) {
            getBundleArgument()
            addFragment(
                    fragment = ErrorHandlerFragment.newInstance(errorType),
                    tag = ErrorHandlerFragment::class.java.simpleName,
                    containerViewId = R.id.container)
        }
    }

    private fun actionBar() {
        setSupportActionBar(tbErrorHandler)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
    }

    private fun getBundleArgument() {
        intent?.extras?.apply {
            errorType = getInt("errorType", 0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.error_handler_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.itmIconClose -> {
                onBackPressed();
                return true
            }

        }
        return false
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

}