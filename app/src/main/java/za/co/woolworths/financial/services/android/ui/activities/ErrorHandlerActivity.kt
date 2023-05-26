package za.co.woolworths.financial.services.android.ui.activities

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ErrorHandlerActivityBinding
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.ErrorHandlerFragment
import za.co.woolworths.financial.services.android.util.Utils

class ErrorHandlerActivity : AppCompatActivity() {

    companion object {
        private var errorType: Int = 0
        private lateinit var errorMessage: String

        const val ERROR_TYPE = "errorType"
        const val ERROR_MESSAGE = "errorMessage"
        const val ERROR_TITLE = "errorTitle"

        // Error Types
        const val COMMON: Int = 0
        const val ATM_PIN_LOCKED: Int = 1
        const val PASSCODE_LOCKED: Int = 2
        const val WITH_NO_ACTION: Int = 3
        const val LINK_DEVICE_FAILED: Int = 4
        const val ERROR_STORE_CARD_EMAIL_CONFIRMATION: Int = 5
        const val ERROR_STORE_CARD_DUPLICATE_CARD_REPLACEMENT: Int = 6
        const val ERROR_TYPE_SUBMITTED_ORDER: Int = 7
        const val ERROR_TYPE_EMPTY_CART: Int = 8
        const val ERROR_TYPE_ADD_SUBSTITUTION: Int = 9

        const val COMMON_WITH_BACK_BUTTON: Int = 5

        //RESULT_CODES
        const val RESULT_RETRY: Int = 153
        const val RESULT_RESET_PASSCODE: Int = 155
        const val RESULT_CALL_CENTER: Int = 156

        //REQUEST_CODES
        const val ERROR_PAGE_REQUEST_CODE: Int = 190
        const val ERROR_EMPTY_REQUEST_CODE: Int = 191
    }

    private lateinit var binding: ErrorHandlerActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ErrorHandlerActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils.updateStatusBarBackground(this)
        if (savedInstanceState == null) {
            getBundleArgument()
            addFragment(
                fragment = ErrorHandlerFragment.newInstance(errorMessage, errorType),
                tag = ErrorHandlerFragment::class.java.simpleName,
                containerViewId = R.id.container
            )
        }
        actionBar()
    }

    private fun actionBar() {
        setSupportActionBar(binding.tbErrorHandler)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            when (errorType) {
                ERROR_TYPE_SUBMITTED_ORDER, COMMON_WITH_BACK_BUTTON -> {
                    setDisplayHomeAsUpEnabled(true)
                    setHomeAsUpIndicator(R.drawable.back24)
                }
                else -> {
                    setDisplayHomeAsUpEnabled(false)
                    setHomeAsUpIndicator(null)
                }
            }
        }
    }

    private fun getBundleArgument() {
        intent?.extras?.apply {
            errorType = getInt("errorType", 0)
            errorMessage = getString("errorMessage", "")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.error_handler_activity_menu, menu)
        when (errorType) {
            ERROR_TYPE_SUBMITTED_ORDER, COMMON_WITH_BACK_BUTTON -> {
                menu?.clear()
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            android.R.id.home, R.id.itmIconClose -> {
                onBackPressed()
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