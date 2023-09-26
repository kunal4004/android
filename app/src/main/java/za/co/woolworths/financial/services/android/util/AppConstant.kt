package za.co.woolworths.financial.services.android.util

/**
 * Created by Nikesh on 2020/09/10.
 *
 * Use this class to add fixed values, delays, types here.
 *
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
        const val DELAY_500_MS: Long = 500
        const val DELAY_900_MS: Long = 900
        const val DELAY_1000_MS: Long = 1000
        const val DELAY_1500_MS: Long = 1500
        const val DELAY_2000_MS: Long = 2000
        const val DELAY_3000_MS: Long = 3000
        const val DELAY_4000_MS: Long = 4000
        const val DELAY_20000_MS: Long = 20000

        /*****************************************************************************************
         * Durations
         *****************************************************************************************/
        const val DURATION_1000_MS: Long = 1000
        const val DURATION_120000_MS:Long=120000
        const val DURATION_0_MS:Long=0L

        const val FIREBASE_REMOTE_CONFIG_TIMEOUT_INTERVAL:Long = 15
        const val FIREBASE_REMOTE_CONFIG_FETCH_INTERVAL:Long = 7200

        /*****************************************************************************************
         * API Response Code
         *****************************************************************************************/
        const val HTTP_OK: Int = 200
        const val HTTP_OK_201: Int = 201
        const val HTTP_EXPECTATION_FAILED_417: Int = 417
        const val HTTP_SESSION_TIMEOUT_440: Int = 440
        const val HTTP_EXPECTATION_FAILED_502: Int = 502
        const val HTTP_SESSION_TIMEOUT_400: Int = 400
        const val HTTP_NOT_FOUND_404: Int = 404
        const val HTTP_SERVICE_UNAVAILABLE_503: Int = 503
        const val RESPONSE_ERROR_CODE_1235: String = "1235"

        /*****************************************************************************************
         * Request/Result Codes
         *****************************************************************************************/
        const val REQUEST_CODE = "REQUEST_CODE"
        const val RESULT_CODE = "RESULT_CODE"

        const val RESULT_FAILED = 9000

        //Dash
        const val REQUEST_CODE_QUERY_INVENTORY_FOR_STORE = 3343
        const val SET_DELIVERY_LOCATION_REQUEST_CODE = 3346
        const val REQUEST_CODE_QUERY_STORE_FINDER = 3344
        //order details
        const val REQUEST_CODE_ORDER_DETAILS_PAGE = 1989
        //Barcode scanning
        const val REQUEST_CODE_BARCODE_ACTIVITY = 1947

        const val REQUEST_CODE_CREATE_LIST = 9001

        const val BALANCE_PROTECTION_INSURANCE_REQUEST_CODE: Int = 291621
        const val BALANCE_PROTECTION_INSURANCE_OPT_IN_SUCCESS_RESULT_CODE: Int = 20

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
        const val DP_LINKING_MY_ACCOUNTS_ORDER_DETAILS = "Order Details"
        const val DP_LINKING_STREAM_CHAT_CHANNEL_ID = "Stream Chat Channel Id"

        const val DP_LINKING_PARAM_STREAM_ORDER_ID = "orderId"
        const val DP_LINKING_PARAM_STREAM_CHANNEL_ID = "channelId"

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
        const val ABSA_COOKIE_JSESSIONID = "jsessionid"

        const val EMPTY_STRING = ""

        const val ALPHA_1F: Float = 1.0F
        const val ALPHA_POINT_3F: Float = 0.3F

        //order details
        const val TAG_ORDER_DETAILS_FRAGMENT: String = "OrderDetailsFragment"
        const val TAG_ORDER_TO_CART_FRAGMENT: String = "OrderToCartFragment"
        const val TAG_TAX_INVOICE_FRAGMENT: String = "TaxInvoiceFragment"
        const val ORDER_ID: String = "ORDER_ID"
        const val NAVIGATED_FROM_MY_ACCOUNTS = "NAVIGATED_FROM_MY_ACCOUNTS"
        const val ORDER_ITEM_LIST = "Order_Item_List"
        const val ORDER_ITEM_TOTAL = "Order_Item_Total"
        const val ORDER_SHIPPING_TOTAL = "Order_Shipping_Total"
        const val QUICK_LINK = "Quick Link - Slot "

        const val RED_HEX_COLOR ="#ff0000"
        const val DEFAULT_TAG_HEX_COLOR="#b2b2b2"
        // delete my profile
        const val DELETE_ACCOUNT = "DELETE_ACCOUNT"
        const val DELETE_ACCOUNT_CONFIRMATION = "DELETE_ACCOUNT_CONFIRMATION"
        const val RESULT_CODE_DELETE_ACCOUNT = 444
        const val FIFTY=50
        const val TEN=10

        // pargo store
        const val TAG_CHANGEFULLFILMENT_COLLECTION_STORE_FRAGMENT: String = "ChangeFullfilmentCollectionStoreFragment"
        const val TAG_FBH_CNC_FRAGMENT: String = "FBHCNC"

        // My List
        const val CONST_NO_SIZE = "NO SZ"
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
        const val TAG_ADD_TO_LIST_PLP: String = "ProductListingFragment"

        //Payflex info html
        const val PAYFLEX_POP_UP_URL = "https://widgets.payflex.co.za/how_to.html?"

        //connect online
        const val PRODUCT_TYPE_DIGITAL = "DIGITAL"
        const val SA_MOBILE_NUMBER_PATTERN="^0\\d{9}$"
    }

    class Keys {
        companion object {

            /*****************************************************************************************
             * Extra Keys
             *****************************************************************************************/
            const val EXTRA_NOTIFICATION_FEATURE = "feature"
            const val EXTRA_NOTIFICATION_PARAMETERS = "parameters"

            const val EXTRA_SEND_DELIVERY_DETAILS_PARAMS = "EXTRA_SEND_DELIVERY_DETAILS_PARAMS"
            const val EXTRA_PRODUCT_NAME = "PRODUCT_NAME"
            const val EXTRA_SEARCH_TYPE = "searchType"
            const val EXTRA_SEARCH_TERM = "searchTerm"
            /*****************************************************************************************
             * Bundle Keys
             * const val BUNDLE_EXAMPLE_NAME = "BUNDLE_KEY_NAME"
             *****************************************************************************************/
            const val KEY_LIST_DETAILS = "listDetails"
            const val KEY_COUNT = "count"
            const val KEY_HAS_GIFT_PRODUCT = "KEY_HAS_GIFT_PRODUCT"

            const val BUNDLE_WISHLIST_EVENT_DATA = "BUNDLE_WISHLIST_EVENT_DATA"

            /*****************************************************************************************
             * Args Keys
             * const val ARGS_EXAMPLE_NAME = "ARGS_NAME"
             *****************************************************************************************/
            const val ARG_NOTIFICATION_PARAMETERS: String = "parameters"
            const val ARG_ORDER: String = "order"
            const val ARG_SEND_DELIVERY_DETAILS = "sendDeliveryDetails"
            const val ARG_FROM_NOTIFICATION = "fromNotification"

            /*****************************************************************************************
             * Parameters Keys
             * const val PARAM_EXAMPLE_NAME = "PARAM_NAME"
             *****************************************************************************************/
        }
    }

    enum class DashDetailsViewType(val value: Int) { HEADER_TITLE(0), APP_FEATURE_LIST(1), TERMS_AND_CONDITION(2) }
}