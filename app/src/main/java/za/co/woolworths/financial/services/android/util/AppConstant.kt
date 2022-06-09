package za.co.woolworths.financial.services.android.util

/**
 * Created by Nikesh on 2020/09/10.
 *
 * Use this class to add fixed values, delays, types here.
 */

class AppConstant {

    companion object {

        const val WOOLWOORTH_CALL_CENTER_NUMBER: String = "0861 50 20 20"

        /*****************************************************************************************
         * Delays
         *****************************************************************************************/
        const val DELAY_10_MS: Long = 10
        const val DELAY_100_MS: Long = 100
        const val DELAY_200_MS: Long = 200
        const val DELAY_300_MS: Long = 300
        const val DELAY_350_MS: Long = 350
        const val DELAY_900_MS: Long = 900
        const val DELAY_1000_MS: Long = 1000
        const val DELAY_1500_MS: Long = 1500
        const val DELAY_2000_MS: Long = 2000
        const val DELAY_3000_MS: Long = 3000
        const val DELAY_4000_MS: Long = 4000
        const val DELAY_500_MS: Long = 500

        /*****************************************************************************************
         * Durations
         *****************************************************************************************/
        const val DURATION_1000_MS: Long = 1000

        /*****************************************************************************************
         * API Response Code
         *****************************************************************************************/
        const val HTTP_OK: Int = 200
        const val HTTP_OK_201: Int = 201
        const val HTTP_EXPECTATION_FAILED_417: Int = 417
        const val HTTP_SESSION_TIMEOUT_440: Int = 440
        const val HTTP_EXPECTATION_FAILED_502: Int = 502
        const val HTTP_SESSION_TIMEOUT_400: Int = 400

        /*****************************************************************************************
         * Request Codes
         *****************************************************************************************/
        const val REQUEST_CODE = "REQUEST_CODE"
        //Dash
        const val REQUEST_CODE_QUERY_INVENTORY_FOR_STORE = 3343
        //order details
        const val REQUEST_CODE_ORDER_DETAILS_PAGE = 1989

        /*****************************************************************************************
         * Miscellaneous
         *****************************************************************************************/
        const val KEY_DASH_WOOLIES_DOWNLOAD_LINK: String = "download_link"

        const val DP_LINKING_PRODUCT_LISTING = "Product Listing"
        const val DP_LINKING_PRODUCT_DETAIL = "Product Detail"
        const val DP_LINKING_MY_ACCOUNTS = "Accounts Landing"
        const val DP_LINKING_MY_ACCOUNTS_PRODUCT = "Accounts Product"
        const val DP_LINKING_MY_ACCOUNTS_PRODUCT_STATEMENT = "Accounts Product Statement"
        const val DP_LINKING_MY_ACCOUNTS_PRODUCT_PAY_MY_ACCOUNT = "Pay My Account"

        const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id="
        const val VTO_FACE_NOT_DETECT = "face_not_detect"
        const val VTO_INVALID_IMAGE_PATH = "invalid_image_path"
        const val VTO_FAIL_IMAGE_LOAD = "image_load_fail"
        const val VTO_COLOR_NOT_MATCH = "color_not_match"
        const val VTO_COLOR_LIVE_CAMERA = "color_match"
        const val VTO = "Virtual Try On"
        const val SDK_INIT_FAIL = "sdk_init_fail"

        //Absa cookie content
        const val ABSA_COOKIE_WFPT = "wfpt"
        const val ABSA_COOKIE_XFPT = "xfpt"

        const val EMPTY_STRING = ""

        const val FIREBASE_REMOTE_CONFIG_FETCH_INTERVAL:Long = 7200
        const val FIREBASE_REMOTE_CONFIG_TIMEOUT_INTERVAL:Long = 15

        const val BALANCE_PROTECTION_INSURANCE_REQUEST_CODE: Int = 291621
        const val BALANCE_PROTECTION_INSURANCE_OPT_IN_SUCCESS_RESULT_CODE: Int = 20

        const val ALPHA_1F: Float = 1.0F
        const val ALPHA_POINT_3F: Float = 0.3F

        //order details
        const val TAG_ORDER_DETAILS_FRAGMENT: String = "OrderDetailsFragment"
        const val TAG_ORDER_TO_CART_FRAGMENT: String = "OrderToCartFragment"
        const val TAG_TAX_INVOICE_FRAGMENT: String = "TaxInvoiceFragment"
        const val ORDER_ID: String = "ORDER_ID"
        const val NAVIGATED_FROM_MY_ACCOUNTS = "NAVIGATED_FROM_MY_ACCOUNTS"

    }

    class Keys {
        companion object {
            /*****************************************************************************************
             * Extra keys
             *****************************************************************************************/
            const val EXTRA_SEARCH_TYPE = "searchType"
            const val EXTRA_SEARCH_TERM = "searchTerm"
        }
    }

    enum class DashDetailsViewType(val value: Int) { HEADER_TITLE(0), APP_FEATURE_LIST(1), TERMS_AND_CONDITION(2) }
}