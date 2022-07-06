package za.co.woolworths.financial.services.android.getstream

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.awfs.coordination.R

@Suppress("JoinDeclarationAndAssignment")
class OCChatActivity : AppCompatActivity(R.layout.activity_one_cart_chat_activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null && lastNonConfigurationInstance == null) {
            // the application process was killed by the OS
            startActivity(packageManager.getLaunchIntentForPackage(packageName))
            finishAffinity()
        }
    }
}