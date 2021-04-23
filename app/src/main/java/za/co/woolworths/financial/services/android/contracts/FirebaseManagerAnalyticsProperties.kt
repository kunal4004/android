package za.co.woolworths.financial.services.android.contracts

open class FirebaseManagerAnalyticsProperties {
    companion object {

        const val WTODAYMENU = "wtodaymenu"
        const val SHOPMENU: String = "shopmenu"
        const val MYCARTMENU: String = "mycartmenu"
        const val WREWARDSMENU: String = "wrewardsmenu"
        const val MYACCOUNTSMENU: String = "myaccountsmenu"
        const val MYACCOUNTSSIGNIN: String = "myaccounts_signin"
        const val WREWARDSSIGNIN: String = "wrewards_signin"
        const val MYACCOUNTSREGISTER: String = "myaccounts_register"
        const val WREWARDSREGISTER: String = "wrewardsregister"
        const val SHOPSEARCHBAR: String = "shop_searchbar"
        const val SHOPBARCODE: String = "shop_barcode"
        const val MYCARTEXIT: String = "mycart_exit"
        const val MYCARTDELIVERY: String = "mycart_delivery"
        const val SHOPADDTOLIST: String = "shop_addtolist"
        const val SHOPNEWLIST: String = "shop_new_list"
        const val SHOP_PDP_SELECT_QUANTITY = "shop_pdp_select_quantity"
        const val SHOP_PDP_ADD_TO_CART = "shop_pdp_add_to_cart"
        const val SHOP_MY_LIST_ADD_TO_CART = "shop_my_list_add_to_cart"
        const val SHOP_MY_LIST_NEW_LIST = "shop_my_list_new_list"

        const val MYACCOUNTSSHOPPINGLIST: String = "myaccounts_shoppinglist"
        const val MYCARTADDTOLIST: String = "mycart_add_to_list"
        const val MYCARTCHECKOUT: String = "mycart_checkout"
        const val MYCARTREMOVEALL: String = "mycart_remove_all"
        const val WREWARDSOVERVIEW: String = "wrewardsoverview"
        const val WREWARDSVOUCHERS: String = "wrewards_vouchers"
        const val WREWARDSSAVINGS: String = "wrewardssavings"
        const val WREWARDSFLIP: String = "wrewardsflip"
        const val WREWARDSDESCRIPTION_VOUCHERDESCRIPTION: String = "wrewards_description"
        const val MYACCOUNTSSTORECARDAPPLYNOW: String = "myaccounts_sc_apply_now"
        const val MYACCOUNTSCREDITCARDAPPLYNOW: String = "myaccounts_cc_apply_now"
        const val MYACCOUNTSPERSONALLOANAPPLYNOW: String = "myaccounts_pl_apply_now"
        const val MYACCOUNTSPERSONALLOANINCREASE: String = "myaccounts_pl_increase"
        const val MYACCOUNTSPERSONALLOANMORE: String = "myaccounts_pl_more"
        const val MYACCOUNTSSTORECARDTRANSACTIONS: String = "myaccounts_sc_transactions"
        const val MYACCOUNTSCREDITCARDTRANSACTIONS: String = "myaccounts_cc_transactions"
        const val MYACCOUNTSSTORECARDSTATEMENTS: String = "myaccounts_sc_statements"
        const val MYACCOUNTSPERSONALLOANTRANSACTIONS: String = "myaccounts_pl_transactions"
        const val MYACCOUNTSPERSONALLOANSTATEMENTS: String = "myaccounts_pl_statements"
        const val MYACCOUNTSCREDITCARDBPI: String = "myaccounts_cc_bpi"
        const val MYACCOUNTSSTORECARDBPI: String = "myaccounts_sc_bpi"
        const val MYACCOUNTSPERSONALLOANBPI: String = "myaccounts_pl_bpi"
        const val ACCOUNTSEVENTSAPPEARED: String = "accounts_event_appeared"
        const val LOGIN: String = "login"
        const val SORTBY_EVENT_APPEARED: String = "sortsfilters_but"
        const val SORTBY_EVENT_APPLIED: String = "sortsfilters_option"
        const val REFINE_EVENT_APPEARED: String = "sortsfilters_refine"
        const val REFINE_EVENT_PROMO_ON: String = "sortsfilters_promo_on"
        const val REFINE_EVENT_PROMO_OFF: String = "sortsfilters_promo_off"
        const val REFINE_EVENT_SEE_RESULT: String = "sortsfilters_seersb"
        const val REFINE_EVENT_BACK_BUTTON: String = "sortsfilters_seersb"
        const val SHOP_PRODUCTDETAIL_NUTRITIONAL_INFORMATION: String = "shop_pdp_ni"
        const val SHOP_PRODUCTDETAILS_INFORMATION: String = "shop_pdp_prod_det"
        const val SHOP_PRODUCTDETAIL_INGREDIENTS_INFORMATION: String = "shop_pdp_ingr"
        const val SHOP_PRODUCTDETAIL_ALLERGEN_INFORMATION: String = "shop_pdp_allerg"
        const val SHOP_PRODUCTDETAIL_DIETARY_INFORMATION: String = "shop_pdp_diet"
        const val ACTION_NUTRITIONAL_INFORMATION = "Selects Accordion to view Nutritional Information"
        const val ACTION_PRODUCTDETAILS_INFORMATION = "Selects Accordion to view Product Detail"
        const val ACTION_INGREDIENTS_INFORMATION = "Selects Accordion to view Ingredients"
        const val ACTION_ALLERGEN_INFORMATION = "Selects Accordion to view Allergens"
        const val ACTION_DIETARY_INFORMATION = "Selects Accordion to view Dietary Information"
        const val SHOP_PRODUCTDETAIL_SIZE_GUIDE: String = "shop_pdp_sg"
        const val ACTION_SIZE_GUIDE = "Product id for product that customer has selected the size guide for"


        const val CRASH_CAUTION: String = "crash_caution"
        const val SHOP_CATEGORIES: String = "shopcategories"
        const val SHOPMYLISTS: String = "shop_mylists"
        const val SHOPMYORDERS: String = "shop_myorders"
        const val BLOCK_CARD_CONFIRM: String = "myaccounts_blockcard_confirm"
        const val BLOCK_CARD_CANCEL: String = "myaccounts_blockcard_cancel"
        const val ABSA_CC_SET_UP_PASSCODE: String = "absa_cc_setup_passcode"
        const val ABSA_CC_COMPLETE_SETUP: String = "absa_cc_complete_setup"
        const val ABSA_CC_LOGIN_WITH_NEW_PASSCODE: String = "cc_login_with_new_passcode"
        const val ABSA_CC_VIEW_STATEMENTS: String = "absa_cc_view_statements"
        const val ABSA_CC_SHARE_STATEMENT: String = "absa_cc_share_statement"
        const val ABSA_CC_VIEW_INDIVIDUAL_STATEMENT: String = "absa_cc_view_ind_statements"

        const val SHOPQS_ADD_TO_CART = "shopqs_add_to_cart"
        const val SHOP_SCAN_CODE = "shop_scan_code"

        const val MY_ACCOUNTS_VTC_GET = "MYACCOUNTS_VTC_GET"
        const val MY_ACCOUNTS_VTC_PAY = "MYACCOUNTS_VTC_PAY"
        const val MY_ACCOUNTS_VTC_HOW_TO = "MYACCOUNTS_VTC_HOWTO"

        const val MYACCOUNTS_ICR_GET_CARD = "MYACCOUNTS_ICR_GET_CARD"
        const val MYACCOUNTS_ICR_STORES = "MYACCOUNTS_ICR_STORES"
        const val MYACCOUNTS_ICR_LINK_START = "MYACCOUNTS_ICR_LINK_START"
        const val MYACCOUNTS_ICR_LINK_CARD = "MYACCOUNTS_ICR_LINK_CARD"
        const val MYACCOUNTS_ICR_LINK_CONFIRM = "MYACCOUNTS_ICR_LINK_CONFIRM"
        const val SHOP_MY_ORDERS_CANCEL_ORDER = "SHOP_MY_ORDERS_CANCEL_ORDER"
        const val MYACCOUNTS_BLK_CC_DELIVERY = "MyAcc_Blk_CC_Delivery"
        const val MYACCOUNTS_BLK_CC_DELIVERY_CONFIRM = "MyAcc_Blk_CC_Delivery_Confirm"
        const val MYACCOUNTS_BLK_CC_MANAGE_DELIVERY = "MyAcc_Blk_CC_Manage_Delivery"
        const val MYACCOUNTS_BLK_CC_MANAGE_DELIVERY_CANCEL = "MyAcc_Blk_CC_Manage_Delivery_Cancel"
        const val WHATSAPP_PAYMENT_OPTION = "WHATSAPP_PAYMENT_OPTION"
        const val WHATSAPP_CONTACT_US = "WHATSAPP_CONTACT_US"
        const val WHATSAPP_CHAT_WITH_US = "WHATSAPP_CHAT_WITH_US"

        const val SHOP_SEARCH = "SHOP_Search"

        const val CC_ACTIVATE_NEW_CARD = "cc_activate_new_card"
        const val CC_ACTIVATE_MY_CARD = "cc_activate_my_card"
        const val CC_ACTIVATION_COMPLETE = "cc_activation_complete"

        const val SHOP_Click_Collect = "shop_click_collect"
        const val SHOP_Click_Collect_Prov = "shop_click_collect_prov"
        const val SHOP_Click_Collect_Stor = "shop_click_collect_stor"
        const val SHOP_Click_Collect_CConfirm = "shop_click_collect_confirm"
        const val CART_BEGIN_CHECKOUT = "begin_checkout"
        const val CART_CHECKOUT_ABANDON = "ecommerce_abandon"
        const val CART_CHECKOUT_COMPLETE = "ecommerce_complete"
        const val CART_ORDER_CONFIRMATION = "ecommerce_purchase"

        const val Acc_My_Orders = "acc_my_orders"
        const val Acc_My_Orders_Cancel_Order = "acc_my_orders_cancel_order"
        const val Acc_My_Orders_DT = "acc_my_orders_ddp"
        const val Shop_My_Orders_DT = "shop_my_orders_ddp"

        const val SC_UNFREEZE_CARD = "sc_unfreeze_card"
        const val SC_FREEZE_CARD = "sc_freeze_card"

        const val Cart_ovr_popup_view = "cart_ovr_popup_view"
        const val Cart_ovr_voucher_redeem = "cart_ovr_voucher_redeem"
        const val Cart_ovr_view = "cart_ovr_view"
        const val Cart_ovr_edit = "cart_ovr_edit"

        const val Cart_promo_enter = "cart_promo_enter"
        const val Cart_promo_clear = "cart_promo_clear"
        const val Cart_promo_apply = "cart_promo_apply"
        const val Cart_promo_remove = "cart_promo_remove"

        const val MYACCOUNTS_PMA_CC = "myaccounts_pma_cc"
        const val MYACCOUNTS_PMA_SC = "myaccounts_pma_sc"
        const val MYACCOUNTS_PMA_PL = "myaccounts_pma_pl"

        const val PMA_CC_PAY = "pma_cc_pay"
        const val PMA_SC_PAY = "pma_sc_pay"
        const val PMA_PL_PAY = "pma_pl_pay"

        const val PMA_CC_AMTEDIT = "pma_cc_amtedit"
        const val PMA_SC_AMTEDIT = "pma_sc_amtedit"
        const val PMA_PL_AMTEDIT = "pma_pl_amtedit"
        const val PMA_CC_PAY_CMPLT = "pma_cc_pay_cmplt"
        const val PMA_SC_PAY_CMPLT = "pma_sc_pay_cmplt"
        const val PMA_PL_PAY_CMPLT = "pma_pl_pay_cmplt"
        const val Myaccounts_creditview = "myaccounts_creditview"
        const val CREDIT_REPORT_CREDITVIEW_COMPLETE = "creditview_complete"

        const val MY_ACCOUNT_INBOX = "myaccount_inbox"
        const val CC_MYACCOUNTS_CHAT_ONLINE = "cc_myaccounts_chat_online"
        const val SC_MYACCOUNTS_CHAT_ONLINE = "sc_myaccounts_chat_online"
        const val PL_MYACCOUNTS_CHAT_ONLINE = "pl_myaccounts_chat_online"
        const val CC_PAYMENT_OPTIONS_CHAT_ONLINE = "cc_paymentoptions_chat_online"
        const val SC_PAYMENT_OPTIONS_CHAT_ONLINE = "sc_paymentoptions_chat_online"
        const val PL_PAYMENT_OPTIONS_CHAT_ONLINE = "pl_paymentoptions_chat_online"
        const val CC_TRANSACTION_CHAT_ONLINE = "cc_transaction_chat_online"
        const val SC_TRANSACTION_CHAT_ONLINE = "sc_transaction_chat_online"
        const val PL_TRANSACTION_CHAT_ONLINE = "pl_transaction_chat_online"
        const val CC_STATEMENT_CHAT_ONLINE = "cc_statement_chat_online"
        const val SC_STATEMENT_CHAT_ONLINE = "sc_statement_chat_online"
        const val PL_STATEMENT_CHAT_ONLINE = "pl_statement_chat_online"

        const val CC_MYACCOUNTS_CHAT_OFFLINE = "cc_myaccounts_chat_offline"
        const val SC_MYACCOUNTS_CHAT_OFFLINE = "sc_myaccounts_chat_offline"
        const val PL_MYACCOUNTS_CHAT_OFFLINE = "pl_myaccounts_chat_offline"
        const val CC_PAYMENT_OPTIONS_CHAT_OFFLINE = "cc_paymentoptions_chat_online"
        const val SC_PAYMENT_OPTIONS_CHAT_OFFLINE = "sc_paymentoptions_chat_offline"
        const val PL_PAYMENT_OPTIONS_CHAT_OFFLINE = "pl_paymentoptions_chat_offline"
        const val CC_TRANSACTION_CHAT_OFFLINE = "cc_transaction_chat_offline"
        const val SC_TRANSACTION_CHAT_OFFLINE = "sc_transaction_chat_offline"
        const val PL_TRANSACTION_CHAT_OFFLINE = "pl_transaction_chat_offline"
        const val CC_STATEMENT_CHAT_OFFLINE = "cc_statement_chat_offline"
        const val SC_STATEMENT_CHAT_OFFLINE = "sc_statement_chat_offline"
        const val PL_STATEMENT_CHAT_OFFLINE = "pl_statement_chat_offline"

        const val CC_MYACCOUNTS_CHAT_BREAK = "cc_myaccounts_chat_break"
        const val SC_MYACCOUNTS_CHAT_BREAK = "sc_myaccounts_chat_break"
        const val PL_MYACCOUNTS_CHAT_BREAK = "pl_myaccounts_chat_break"
        const val CC_PAYMENT_OPTIONS_CHAT_BREAK = "cc_paymentoptions_chat_break"
        const val SC_PAYMENT_OPTIONS_CHAT_BREAK = "sc_paymentoptions_chat_break"
        const val PL_PAYMENT_OPTIONS_CHAT_BREAK = "pl_paymentoptions_chat_break"
        const val CC_TRANSACTION_CHAT_BREAK = "cc_transaction_chat_break"
        const val SC_TRANSACTION_CHAT_BREAK = "sc_transaction_chat_break"
        const val PL_TRANSACTION_CHAT_BREAK = "pl_transaction_chat_break"
        const val CC_STATEMENTS_CHAT_BREAK = "cc_statements_chat_break"
        const val SC_STATEMENTS_CHAT_BREAK = "sc_statements_chat_break"
        const val PL_STATEMENTS_CHAT_BREAK = "pl_statements_chat_break"

        const val CC_MYACCOUNTS_CHAT_END = "cc_myaccounts_chat_end"
        const val SC_MYACCOUNTS_CHAT_END = "sc_myaccounts_chat_end"
        const val PL_MYACCOUNTS_CHAT_END = "pl_myaccounts_chat_end"
        const val CC_PAYMENT_OPTIONS_CHAT_END = "cc_paymentoptions_chat_end"
        const val SC_PAYMENT_OPTIONS_CHAT_END = "sc_paymentoptions_chat_end"
        const val PL_PAYMENT_OPTIONS_CHAT_END = "pl_paymentoptions_chat_end"
        const val CC_TRANSACTIONS_CHAT_END = "cc_transaction_chat_end"
        const val SC_TRANSACTIONS_CHAT_END = "sc_transaction_chat_end"
        const val PL_TRANSACTIONS_CHAT_END = "pl_transaction_chat_end"
        const val CC_STATEMENTS_CHAT_END = "cc_statements_chat_end"
        const val SC_STATEMENTS_CHAT_END = "sc_statements_chat_end"
        const val PL_STATEMENTS_CHAT_END = "pl_statements_chat_end"

        const val SHOP_PDP_NATIVE_SHARE = "shop_pdp_nat_shar"
        const val CART_CLCK_CLLCT_CNFRM_LMT = "cart_clck_cllct_cnfrm_lmt"


        //PLDD
        const val personalLoanDrawdownStart = "pldd_start"
        const val personalLoanDrawdownComplete = "pldd_complete"
        const val personalLoanDrawdownAmountLow = "pldd_amountlow"
        const val personalLoanDrawdownAmountHigh = "pldd_amounthigh"
        const val personalLoanDrawdownAmountNoFunds = "pldd_nofunds"

        //Gold Credit Card Delivery
        const val loginGoldCreditCardDelivery = "login_gold_cc_delivery"
        const val loginGoldCreditCardDeliveryLater = "login_gold_cc_delivery_later"
        const val myAccountGoldCreditCardDelivery = "myacc_gold_cc_delivery"
        const val goldCreditCardDeliveryConfirm = "gold_cc_delivery_confirm"
        const val goldCreditCardDeliveryScheduled = "gold_cc_delivery_scheduled"
        const val goldCreditCardManageDelivery = "gold_cc_manage_delivery"
        const val goldCreditCardDeliveryCancel = "gold__cc_delivery_cancel"

        //silver Credit Card Delivery
        const val loginSilverCreditCardDelivery = "login_slvr_cc_delivery"
        const val loginSilverCreditCardDeliveryLater = "login_slvr_cc_delivery_later"
        const val myAccountSilverCreditCardDelivery = "myacc_slvr_cc_delivery"
        const val silverCreditCardDeliveryConfirm = "slvr_cc_delivery_confirm"
        const val silverCreditCardDeliveryScheduled = "slvr_cc_delivery_scheduled"
        const val silverCreditCardManageDelivery = "slvr_cc_manage_delivery"
        const val silverCreditCardDeliveryCancel = "slvr_cc_delivery_cancel"

        //Credit Limit Increase
        //start
        const val storeCardCreditLimitIncreaseStart = "sc_cli_start"
        const val personalLoanCreditLimitIncreaseStart = "pl_cli_start"
        const val blackCreditCardCreditLimitIncreaseStart = "blkcc_cli_start"
        const val goldCreditCardCreditLimitIncreaseStart = "goldcc_cli_start"
        const val silverCreditCardCreditLimitIncreaseStart = "slvrcc_cli_start"

        //Maritalstatus
        const val storeCardCreditLimitIncreaseMaritalstatus = "sc_cli_maritalstatus"
        const val personalLoanCreditLimitIncreaseMaritalstatus = "pl_cli_maritalstatus"
        const val blackCreditCardCreditLimitIncreaseMaritalstatus = "blkcc_cli_maritalstatus"
        const val goldCreditCardCreditLimitIncreaseMaritalstatus = "goldcc_cli_maritalstatus"
        const val silverCreditCardCreditLimitIncreaseMaritalstatus = "slvrcc_cli_maritalstatus"

        //income expense
        const val storeCardCreditLimitIncreaseIncomeExpense = "sc_cli_income_expense"
        const val personalLoanCreditLimitIncreaseIncomeExpense = "pl_cli_income_expense"
        const val blackCreditCardCreditLimitIncreaseIncomeExpense = "blkcc_cli_income_expense"
        const val goldCreditCardCreditLimitIncreaseIncomeExpense = "goldcc_cli_income_expense"
        const val silverCreditCardCreditLimitIncreaseIncomeExpense = "slvrcc_cli_income_expense"

        //accept offer
        const val storeCardCreditLimitIncreaseAcceptOffer = "sc_cli_accept_offer"
        const val personalLoanCreditLimitIncreaseAcceptOffer = "pl_cli_accept_offer"
        const val blackCreditCardCreditLimitIncreaseAcceptOffer = "blkcc_cli_accept_offer"
        const val goldCreditCardCreditLimitIncreaseAcceptOffer = "goldcc_cli_accept_offer"
        const val silverCreditCardCreditLimitIncreaseAcceptOffer = "slvrcc_cli_accept_offer"

        //dea option
        const val storeCardCreditLimitIncreaseDeaOption = "sc_cli_dea_optin"
        const val personalLoanCreditLimitIncreaseDeaOption = "pl_cli_dea_optin"
        const val blackCreditCardCreditLimitIncreaseDeaOption = "blkcc_cli_dea_optin"
        const val goldCreditCardCreditLimitIncreaseDeaOption = "goldcc_cli_dea_optin"
        const val silverCreditCardCreditLimitIncreaseDeaOption = "slvrcc_cli_dea_optin"

        //poi confirm
        const val storeCardCreditLimitIncreasePoiConfirm = "sc_cli_poi_confirm"
        const val personalLoanCreditLimitIncreasePoiConfirm = "pl_cli_poi_confirm"
        const val blackCreditCardCreditLimitIncreasePoiConfirm = "blkcc_cli_poi_confirm"
        const val goldCreditCardCreditLimitIncreasePoiConfirm = "goldcc_cli_poi_confirm"
        const val silverCreditCardCreditLimitIncreasePoiConfirm = "slvrcc_cli_poi_confirm"
    }

    class PropertyNames {
        companion object {
            const val SUBURBNAME: String = "SUBURBNAME"
            const val VOUCHERDESCRIPTION: String = "VOUCHERDESCRIPTION"
            const val C2ID: String = "C2Id"
            const val ATGId: String = "ATGId"
            const val SORT_OPTION_NAME: String = "SORTBY_OPTION_NAME"
            const val DESCRIPTION: String = "DESCRIPTION"
            const val CANCEL_ORDER_TAP = "Cancel Order Tap"
            const val CONFIRM_CANCEL = "Confirm Cancel"
            const val CANCEL_CANCEL = "Cancel Cancel"
            const val CANCEL_API_SUCCESS = "Cancel API Success"
            const val CANCEL_API_FAILURE = "Cancel API Failure"
            const val CLOSE_FAILURE_CANCEL = "Close Failure Cancel"
            const val CANCEL_FAILURE_CALL_CENTRE = "Cancel Failure Call Centre"
            const val CANCEL_FAILURE_RETRY = "Cancel Failure Retry"
            const val ACTION = "ACTION"
            const val ACTION_LOWER_CASE = "action"
            const val NUTRITIONAL_INFORMATION_PRODUCT_ID = "NUTRITIONAL_PRODUCT_ID"
            const val PRODUCT_DETAILS_INFORMATION_PRODUCT_ID = "PRODUCT_DETAILS_PRODUCT_ID"
            const val INGREDIENTS_INFORMATION_PRODUCT_ID = "INGREDIENTS_PRODUCT_ID"
            const val ALLERGEN_INFORMATION_PRODUCT_ID = "ALLERGEN_PRODUCT_ID"
            const val DIETARY_INFORMATION_PRODUCT_ID = "DIETARY_PRODUCT_ID"
            const val NUTRITIONAL_INFORMATION_FILTER_OPTION = "Filter_Option"
            const val ENTRY_POINT = "Entry_Point"
            const val DEEP_LINK_URL = "deepLinkUrl"
            const val provinceName: String = "province_name"
            const val storeName: String = "store_name"
            const val PERSONAL_LOAN_PRODUCT_OFFERING = "personal_loan"
            const val STORE_CARD_PRODUCT_OFFERING = "store_card"
            const val GOLD_CREDIT_CARD_PRODUCT_OFFERING = "gold_credit_card"
            const val SILVER_CREDIT_CARD_PRODUCT_OFFERING = "silver_credit_card"
            const val BLACK_CREDIT_CARD_PRODUCT_OFFERING = "black_credit_card"
            const val PERSONAL_LOAN_PRODUCT_STATE: String = "pl_account_dc_state"
            const val CREDIT_CARD_PRODUCT_STATE: String = "cc_account_dc_state"
            const val STORE_CARD_PRODUCT_STATE: String = "sc_account_dc_state"

            const val activationInitiated = "Initiate Credit Card Activation"
            const val activationRequested = "Request Credit Card Activation"
            const val activationConfirmed = "Confirm Credit Card Activation"
            const val PRODUCT_ID: String = "product_id"
            const val SC_ACCOUNT_STATE = "sc_account_dc_state"
            const val CC_ACCOUNT_STATE = "cc_account_dc_state"
            const val PL_ACCOUNT_STATE = "pl_account_dc_state"

            const val SC_PAYMENT_DUE_DATE = "sc_payment_due_date"
            const val CC_PAYMENT_DUE_DATE = "cc_payment_due_date"
            const val PL_PAYMENT_DUE_DATE = "pl_payment_due_date"
            const val SC_DEBIT_ORDER = "sc_debit_order"
            const val CC_DEBIT_ORDER = "cc_debit_order"
            const val PL_DEBIT_ORDER = "pl_debit_order"

            const val TAPPED: String = "tapped"
            const val PASSCODE: String = "passcode"
            const val NETWORK: String = "network"
            const val OTHER_HTTP_CODE: String = "other http code"
            const val PIN: String = "pin"
            const val TIMEOUT: String = "timeout"
            const val UNDEFINED: String = "undefined"
            const val FAILED: String = "failed"
            const val SUCCESSFUL: String = "successful"
        }
    }

    class ScreenNames {
        companion object {
            const val STARTUP: String = "Startup"
            const val WTODAY: String = "WToday"
            const val SHOP_BARCODE: String = "Shop Barcode"
            const val SHOP_BARCODE_MANUAL: String = "Shop Barcode Manual"
            const val SHOP_MAIN_CATEGORIES: String = "Shop Main Categories"
            const val SHOP_SUB_CATEGORIES: String = "Shop Sub Categories"
            const val PRODUCT_SEARCH: String = "Product Search"
            const val PRODUCT_SEARCH_RESULTS: String = "Product Search Result"
            const val PRODUCT_SEARCH_REFINEMENT: String = "Product Search Refinement"
            const val PRODUCT_SEARCH_REFINEMENT_CATEGORY: String = "Product Search Refinement Category"
            const val PRODUCT_DETAIL: String = "Product Detail"
            const val PRODUCT_DETAIL_IMAGE_ZOOM: String = "Product Detail Image Zoom"
            const val DELIVERY_LOCATION_HISTORY: String = "Delivery Location History"
            const val DELIVERY_LOCATION_PROVINCE: String = "Delivery Location Province"
            const val DELIVERY_LOCATION_SUBURB: String = "Delivery Location Suburb"
            const val CART_LIST: String = "Cart List"
            const val CART_CHECKOUT: String = "Cart Checkout"
            const val MY_ACCOUNTS: String = "My Accounts"
            const val PREFERENCES: String = "Preferences"
            const val HELP_SECTION: String = "Help Section"
            const val FAQ_LIST: String = "FAQ List"
            const val FAQ_DETAIL: String = "FAQ Detail"
            const val TIPS_AND_TRICKS_LIST: String = "Tips and Tricks List"
            const val TIPS_AND_TRICKS_DETAILS: String = "Tips and Tricks Details"
            const val STATEMENTS_LIST: String = "Statements List"
            const val STATEMENTS_EMAIL_CONFIRMED: String = "Statements Email Confirmed"
            const val STATEMENTS_ALTERNATIVE_EMAIL: String = "Statements Alternative Email"
            const val STATEMENTS_DOCUMENT_PREVIEW: String = "Statements Document Preview"
            const val SHOPPING_LISTS: String = "Shopping Lists"
            const val SHOPPING_LIST_ITEMS: String = "Shopping List Items"
            const val CREATE_SHOPPING_LIST: String = "Create Shopping List"
            const val SHOPPING_LIST_SEARCH_RESULTS: String = "Shopping List Search Results"
            const val SSO_SIGN_IN: String = "SSO Sign In"
            const val SSO_REGISTER: String = "SSO Register"
            const val SSO_LOGOUT: String = "SSO Logout"
            const val SSO_PASSWORD_CHANGE: String = "SSO Password Change"
            const val SSO_PROFILE_INFO: String = "SSO Profile Info"
            const val STORES_NEARBY: String = "Stores Nearby"
            const val STORES_SEARCH: String = "Stores Search"
            const val STORE_DETAILS: String = "Store Details"
            const val MESSAGES: String = "Messages"
            const val FINANCIAL_SERVICES: String = "Financial Services"
            const val FINANCIAL_SERVICES_STORE_CARD: String = "Financial Services Store Card"
            const val FINANCIAL_SERVICES_CREDIT_CARD: String = "Financial Services Credit Card"
            const val FINANCIAL_SERVICES_PERSONAL_LOAN: String = "Financial Services Personal Loan"
            const val HOW_TO_PAY: String = "Financial Services - How To Pay"
            const val TRANSACTIONS: String = "Transactions"
            const val BPI_OVERVIEW: String = "BPI Overview"
            const val BPI_DETAILS: String = "BPI Details"
            const val BPI_CLAIM_REASONS: String = "BPI Claim Reasons"
            const val BPI_DOCUMENTS_PROCESSING: String = "BPI Documents Processing"
            const val WITHDRAW_CASH: String = "Withdraw Cash"
            const val WITHDRAW_CASH_CONFIRMATION: String = "Withdraw Cash Confirmation"
            const val WITHDRAW_CASH_SUCCESSFUL: String = "Withdraw Cash Successful"
            const val DEBIT_ORDERS: String = "Financial Services - Debit Orders"
            const val CLI_WALKTHROUGH: String = "CLI Walkthrough"
            const val CLI_INSOLVENCY_CHECK: String = "CLI Insolvency Check"
            const val CLI_CONSENT: String = "CLI Consent"
            const val CLI_INCOME: String = "CLI Income"
            const val CLI_EXPENSES: String = "CLI Expenses"
            const val CLI_OFFER: String = "CLI Offer"
            const val CLI_POI_BANKS: String = "CLI POI Banks"
            const val CLI_POI_BANKS_ACCOUNT_TYPE: String = "CLI POI Banks Account Type"
            const val CLI_POI_BANK_ACC_NUMBER: String = "CLI POI Bank Account Number"
            const val CLI_POI_BANKS_AUTHORIZATION: String = "CLI POI Banks Authorization"
            const val CLI_POI_DOCUMENTS: String = "CLI POI Documents"
            const val CLI_POI_DOCUMENTS_UPLOAD: String = "CLI POI Documents Upload"
            const val CLI_POI_ERROR: String = "CLI POI Error"
            const val CLI_PROCESS_COMPLETE: String = "CLI Process Complete"
            const val CLI_PROCESS_COMPLETE_NO_POI: String = "CLI Process Complete No POI"
            const val WREWARDS_TIER_INFO: String = "WRewards Tier Info"
            const val WREWARDS_SIGNED_OUT: String = "WRewards Signed Out"
            const val WREWARDS_OVERVIEW: String = "WRewards Overview"
            const val WREWARDS_SIGNED_IN_LINKED: String = "WRewards Signed In Linked"
            const val WREWARDS_SIGNED_IN_NOT_LINKED: String = "WRewards Signed In Not Linked"
            const val WREWARDS_SAVINGS: String = "WRewards Savings"
            const val WREWARDS_VOUCHERS: String = "WRewards Vouchers"
            const val WREWARDS_VOUCHERS_BARCODE: String = "WRewards Vouchers Barcode"
            const val WREWARDS_TERMS_CONDITIONS: String = "WRewards Terms and Conditions"
            const val MAINTENANCE_MESSAGE: String = "Maintenance Message"
            const val DEVICE_ROOTED_AT_STARTUP: String = "Block Rooted Device"
        }
    }

    enum class EntryPoint(val value: String) {
        DEEP_LINK("Deep_Link"), MANUAL_SEARCH("Manual_Search"), QR_CODE("QR_Codes")
    }
}