package za.co.woolworths.financial.services.android.util

/**
 * Created by Nikesh on 2020/09/10.
 *
 * Use this class to add fixed values, delays, types here.
 */

class AppConstant {

    companion object {

        const val WOOLWOORTH_CALL_CENTER_NUMBER: String = "0861 50 20 20"

        //Delays
        const val DELAY_10_MS: Long = 10
        const val DELAY_100_MS: Long = 100
        const val DELAY_200_MS: Long = 200
        const val DELAY_300_MS: Long = 300
        const val DELAY_350_MS: Long = 350
        const val DELAY_900_MS: Long = 900
        const val DELAY_1000_MS: Long = 1000
        const val DELAY_1500_MS: Long = 1500
        const val DELAY_3000_MS: Long = 3000

        // Durations
        const val DURATION_1000_MS: Long = 1000

        //API Response Code
        const val HTTP_OK: Int = 200
        const val HTTP_OK_201: Int = 201
        const val HTTP_EXPECTATION_FAILED_417: Int = 417
        const val HTTP_SESSION_TIMEOUT_440: Int = 440
        const val HTTP_EXPECTATION_FAILED_502: Int = 502
        const val HTTP_SESSION_TIMEOUT_400: Int = 400

        const val KEY_DASH_WOOLIES_DOWNLOAD_LINK: String = "download_link"

        const val DP_LINKING_PRODUCT_LISTING = "Product Listing"
        const val DP_LINKING_PRODUCT_DETAIL = "Product Detail"
        const val DP_LINKING_MY_ACCOUNTS = "Accounts Landing"
        const val DP_LINKING_MY_ACCOUNTS_PRODUCT = "Accounts Product"
        const val DP_LINKING_MY_ACCOUNTS_PRODUCT_STATEMENT = "Accounts Product Statement"
        const val DP_LINKING_MY_ACCOUNTS_PRODUCT_PAY_MY_ACCOUNT = "Pay My Account"

        const val PLAY_STORE_URL ="https://play.google.com/store/apps/details?id="

        //Absa cookie content
        const val ABSA_COOKIE_WFPT = "wfpt"
        const val ABSA_COOKIE_XFPT = "xfpt"

        const val EMPTY_STRING = ""
        const val FIREBASE_REMOTE_CONFIG_FETCH_INTERVAL:Long = 7200
        const val FIREBASE_REMOTE_CONFIG_TIMEOUT_INTERVAL:Long = 15

    }

    enum class DashDetailsViewType(val value: Int) { HEADER_TITLE(0), APP_FEATURE_LIST(1), TERMS_AND_CONDITION(2) }
}