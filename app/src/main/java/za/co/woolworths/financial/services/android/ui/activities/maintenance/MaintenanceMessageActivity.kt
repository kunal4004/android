package za.co.woolworths.financial.services.android.ui.activities.maintenance

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.util.Utils

class MaintenanceMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.maintenance_exception_activity)
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.MAINTENANCE_MESSAGE)
    }

    override fun onBackPressed() {

    }
}