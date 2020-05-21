package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp

import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.app_toolbar.*
import za.co.woolworths.financial.services.android.util.Utils


class WhatsAppChatDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.updateStatusBarBackground(this)
        setContentView(R.layout.whatsapp_chat_activity)
        actionBar()

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        when (displayMetrics.densityDpi) {
            DisplayMetrics.DENSITY_LOW -> {
                Toast.makeText(this,  DisplayMetrics.DENSITY_LOW, Toast.LENGTH_LONG).show()
            }
            DisplayMetrics.DENSITY_MEDIUM -> {
                Toast.makeText(this,  DisplayMetrics.DENSITY_MEDIUM, Toast.LENGTH_LONG).show()

            }
            DisplayMetrics.DENSITY_HIGH -> {
                Toast.makeText(this,  DisplayMetrics.DENSITY_HIGH, Toast.LENGTH_LONG).show()

            }
            DisplayMetrics.DENSITY_XHIGH -> {
                Toast.makeText(this,  DisplayMetrics.DENSITY_XHIGH, Toast.LENGTH_LONG).show()

            }
            DisplayMetrics.DENSITY_XXHIGH -> {
                Toast.makeText(this,  DisplayMetrics.DENSITY_XXHIGH, Toast.LENGTH_LONG).show()

            }
            DisplayMetrics.DENSITY_XXXHIGH -> {
                Toast.makeText(this,  DisplayMetrics.DENSITY_XXXHIGH, Toast.LENGTH_LONG).show()
            }

            DisplayMetrics.DENSITY_440 -> {
                Toast.makeText(this,  DisplayMetrics.DENSITY_440, Toast.LENGTH_LONG).show()
            }
        }
    }

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