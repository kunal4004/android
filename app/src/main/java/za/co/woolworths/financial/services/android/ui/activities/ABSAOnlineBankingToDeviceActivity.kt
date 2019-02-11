package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.absa.ABSABiometricFragment

class ABSAOnlineBankingToDeviceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.absa_online_banking_to_device_activity)

        if (savedInstanceState == null) {
            addFragment(
                    fragment = ABSABiometricFragment.newInstance(),
                    tag = ABSABiometricFragment::class.java.simpleName,
                    containerViewId = R.id.flAbsaOnlineBankingToDevice
            )
        }
    }
}