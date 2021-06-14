package za.co.woolworths.financial.services.android.util.voc

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerActivity

class VoiceOfCustomerManager {
    companion object {
        fun showVocSurvey(context: Context?) {
            context?.apply {
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(this, VoiceOfCustomerActivity::class.java))
                }, 1500)
            }
        }
    }
}