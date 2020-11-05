package za.co.woolworths.financial.services.android.util

/**
 * Created by Nikesh on 2020/09/10.
 *
 * Use this class to add fixed values, delays, types here.
 */

class AppConstant {

    companion object {

        const val NAVIGATED_FROM: String = "NAVIGATED_FROM"

        //Delays
        const val DELAY_900_MS: Long = 900
        const val DELAY_3000_MS: Long = 3000
        const val DELAY_300_MS: Long = 300

        //API Response Code
        const val HTTP_OK: Int = 200
        const val HTTP_EXPECTATION_FAILED_417: Int = 417
        const val HTTP_SESSION_TIMEOUT_440: Int = 440

    }
}