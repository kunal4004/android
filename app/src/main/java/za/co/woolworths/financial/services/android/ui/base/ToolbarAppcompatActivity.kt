package za.co.woolworths.financial.services.android.ui.base

import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.app_toolbar.*

open class ToolbarAppcompatActivity : AppCompatActivity() {

    fun actionBar() {
        setSupportActionBar(whatsAppToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

}