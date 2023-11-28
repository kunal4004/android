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

        // Pet Insurance event
        const val PET_INSURANCE_AWARENESS_MODEL_LEARN_MORE = "learn_more"
        const val PET_INSURANCE_GET_INSURANCE_PRODUCT = "get_insurance_products"

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
        const val ACTION_PDP_DEEPLINK = "product_id of the item that was viewed in PDP"

        const val CRASH_CAUTION: String = "crash_caution"
        const val SHOP_CATEGORIES: String = "shopcategories"
        const val SHOPMYLISTS: String = "shop_mylists"
        const val SHOPMYORDERS: String = "shop_myorders"
        const val BLOCK_CARD_CONFIRM: String = "myaccounts_blockcard_confirm"
        const val BLOCK_CARD_CANCEL: String = "myaccounts_blockcard_cancel"
        const val ABSA_CC_SET_UP_PASSCODE: String = "absa_cc_setup_passcode"
        const val ABSA_CC_COMPLETE_SETUP: String = "absa_cc_complete_setup"
        const val ABSA_CC_LOGIN_WITH_NEW_PASSCODE: String = "cc_login_with_new_passcode"
        const val ABSA_CC_VIEW_STATEMENTS: String = "absa_cc_view_statement"
        const val ABSA_CC_SHARE_STATEMENT: String = "absa_cc_share_statement"
        const val ABSA_CC_VIEW_INDIVIDUAL_STATEMENT: String = "absa_cc_view_ind_statement"

        const val SHOPQS_ADD_TO_CART = "shopqs_add_to_cart"
        const val SHOP_SCAN_CODE = "shop_scan_code"

        const val MY_ACCOUNTS_VTC_GET = "myaccounts_vtc_get"
        const val MY_ACCOUNTS_VTC_PAY = "myaccounts_vtc_pay"
        const val MY_ACCOUNTS_VTC_VIEWCARDNUMBERS = "vtc_viewcardnumbers"
        const val MY_ACCOUNTS_VTC_HOW_TO = "myaccounts_vtc_howto"

        const val MYACCOUNTS_ICR_GET_CARD = "myaccounts_icr_get_card"
        const val MYACCOUNTS_ICR_STORES = "myaccounts_icr_stores"
        const val MYACCOUNTS_VTC_CARD_REPLACEMENT_START = "vtc_cardreplacement_start"
        const val MYACCOUNTS_SC_REPLACE_CARD_STORE = "sc_replacecard_store"
        const val MYACCOUNTS_SC_REPLACE_CARD_F2F = "sc_replacecard_f2f"
        const val MYACCOUNTS_REPLACE_CARD_STORE_DELIVERY = "replacecard_storedelivery"
        const val MYACCOUNTS_REPLACE_CARD_F2F = "replacecard_f2f"
        const val MYACCOUNTS_ICR_LINK_START = "myaccounts_icr_link_start"
        const val MYACCOUNTS_ICR_LINK_CARD = "myaccounts_icr_link_card"
        const val MYACCOUNTS_ICR_LINK_CONFIRM = "myaccounts_icr_link_confirm"
        const val SHOP_MY_ORDERS_CANCEL_ORDER = "shop_my_orders_cancel_order"
        const val WHATSAPP_PAYMENT_OPTION = "whatsapp_payment_option"
        const val WHATSAPP_CONTACT_US = "whatsapp_contact_us"
        const val WHATSAPP_CHAT_WITH_US = "whattsapp_chat_with_us"
        const val VTSC_CARD_NOT_DELIVERED = "cardnotdelivered"
        const val VTSC_CARD_RECEIVED = "cardreceived"

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
        const val SHOP_PDP_NATIVE_SHARE_DP_LNK = "shop_pdp_nat_shar_dp_lnk"
        const val CART_CLCK_CLLCT_CNFRM_LMT = "cart_clck_cllct_cnfrm_lmt"
        const val BUSINESS_UNIT = "business_unit"

        //Native Checkout
        const val CHANGE_FULFILLMENT_DELIVERY = "chckout_dlvry"
        const val CHANGE_FULFILLMENT_COLLECTION = "chckout_cllct"
        const val CHECKOUT_COLECTION_CHANGE_BTN = "chckout_cllct_chnge"
        const val CHECKOUT_CONFIRM_NEW_STORE = "chckout_cllct_cnfrm_str"
        const val CHANGE_FULFILLMENT_ADD_NEW_ADDRESS = "chckout_dlvry_ad_adrs"
        const val CHANGE_FULFILLMENT_EDIT_ADDRESS = "chckout_dlvry_edt_adrs"
        const val CHANGE_FULFILLMENT_DELETE_ADDRESS = "chckout_dlvry_dlte_adrs"
        const val CHANGE_FULFILLMENT_DELIVERY_CONFIRM_BTN = "chckout_dlvry_cnfrm_adrs"
        const val CHECKOUT_SAVE_ADDRESS = "chckout_dlvry_sve_adrs"
        const val CHECKOUT_DELIVERY_OPTION_ = "chckout_dlvry_"
        const val CHECKOUT_FOOD_SUBSTITUTE_PHONE_ME = "chckout_phne_sub"
        const val CHECKOUT_FOOD_SUBSTITUTE_NO_THANKS = "chckout_nothnks_sub"
        const val CHECKOUT_SPECIAL_COLLECTION_INSTRUCTION = "chckout_ad_spcl_instr"
        const val CHECKOUT_IS_THIS_GIFT = "chckout_is_ths_a_gft"
        const val CHECKOUT_MISSED_WREWARD_SAVINGS = "cnfrm_msd_wrwrds_svgs"
        const val CHECKOUT_WREWARD_SIGN_UP = "cnfrm_wrwrds_sgn_up"
        const val CHECKOUT_ALREADY_HAVE_WREWARD = "cnfrm_alrdy_wrwrds_crd"
        const val CHECKOUT_SHOPPING_BAGS_INFO = "chckout_bgs_info"
        const val CHECKOUT_REMOVE_UNSELLABLE_ITEMS = "chckout_rmve_itms"
        const val CHECKOUT_CANCEL_REMOVE_UNSELLABLE_ITEMS = "chckout_cncl_rmve_itms"
        const val CHECKOUT_CONTINUE_TO_PAYMENT = "chckout_cnt_to_pmnt"
        const val CHECKOUT_COLLECTION_USER_EDIT = "clikcllct_edt_prsn"
        const val CHECKOUT_COLLECTION_VECHILE_SELECT = "clikcllct_veh_det"
        const val CHECKOUT_COLLECTION_TAXI_SELECT = "clikcllct_taxi"
        const val CHECKOUT_COLLECTION_CONFIRM_DETAILS = "clikcllct_confrm"

        // Brand Landing Page
        const val BRAND_LANDING_PAGE_CATEGORY = "shop_brnd_lndng_cat"
        const val BRAND_LANDING_PAGE_SUB_CATEGORY = "shop_categ_pge_lwr_cat"
        const val BRAND_LANDING_PAGE_LOGO_IMAGE = "shop_categ_pge_imge_bnr"

        //PLDD
        const val personalLoanDrawdownStart = "pldd_start"
        const val personalLoanDrawdownComplete = "pldd_complete"
        const val personalLoanDrawdownAmountLow = "pldd_amountlow"
        const val personalLoanDrawdownAmountHigh = "pldd_amounthigh"
        const val personalLoanDrawdownAmountNoFunds = "pldd_nofunds"

        //Black Credit Card Delivery
        const val loginBlackCreditCardDelivery = "login_blk_cc_delivery"
        const val loginBlackCreditCardDeliveryLater = "login_blk_cc_delivery_later"
        const val myAccountBlackCreditCardDelivery = "myacc_blk_cc_delivery"
        const val blackCreditCardDeliveryConfirm = "myacc_blk_cc_delivery_confirm"
        const val blackCreditCardDeliveryScheduled = "blk_cc_delivery_scheduled"
        const val blackCreditCardManageDelivery = "myacc_blk_cc_manage_delivery"
        const val blackCreditCardDeliveryCancel = "blk_cc_manage_delivery_cancel"

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

        const val DEVICESECURITY_LINK_START = "devicesecurity_link_start"
        const val DEVICESECURITY_LINK_SKIP = "devicesecurity_link_skip"
        const val DEVICESECURITY_LINK_CONFIRMED = "devicesecurity_OTP"
        const val DEVICESECURITY_VIEW_LIST = "devicesecurity_view_list"
        const val DEVICESECURITY_DELETE = "devicesecurity_delete"
        const val inAppReviewRequest = "in_app_review_request"

        //view payment plan
        const val VIEW_PAYMENT_PLAN_PERSONAL_LOAN = "pl_viewtreatmentplan"
        const val VIEW_PAYMENT_PLAN_STORE_CARD = "sc_viewtreatmentplan"
        const val VIEW_PAYMENT_PLAN_CREDIT_CARD = "cc_viewtreatmentplan"
        const val VIEW_PAYMENT_PLAN_PERSONAL_LOAN_ACTION = "Personal Loan landing - Arrears popup - View Treatment plan"
        const val VIEW_PAYMENT_PLAN_STORE_CARD_ACTION  = "Store Card landing - Arrears popup - View Treatment plan"
        const val VIEW_PAYMENT_PLAN_CREDIT_CARD_ACTION  = "Credit Card landing - Arrears popup - View Treatment plan"

        //take up treatment plan
        const val TAKE_UP_TREATMENT_PLAN_PL = "pl_takeupplan"
        const val TAKE_UP_TREATMENT_PLAN_SC = "sc_takeupplan"
        const val TAKE_UP_TREATMENT_PLAN_CC = "cc_takeupplan"
        const val TAKE_UP_TREATMENT_PLAN_PL_ACTION = "Personal Loan landing - Arrears popup - SetupPlan"
        const val TAKE_UP_TREATMENT_PLAN_SC_ACTION  = "Store Card landing - Arrears popup - SetupPlan"
        const val TAKE_UP_TREATMENT_PLAN_CC_ACTION  = "Credit Card landing - Arrears popup - SetupPlan"

        // Voice of Customer
        const val VOC_SKIP = "voc_skip"
        const val VOC_SUBMIT = "voc_submit"
        const val VOC_OPTOUT = "voc_optout"

        //Sunsetting and Splash screen
        const val SPLASH_BTN = "splash_"


        //Bpi insurance lead generation
        const val SC_BPI_OPT_IN_START: String = "sc_bpi_optin_start"
        const val SC_BPI_OPT_IN_CONFIRM: String = "sc_bpi_optin_confirm"
        const val SC_BPI_OPT_IN_SEND_EMAIL: String = "sc_bpi_send_email"

        const val PL_BPI_OPT_IN_START: String = "pl_bpi_optin_start"
        const val PL_BPI_OPT_IN_CONFIRM: String = "pl_bpi_optin_confirm"
        const val PL_BPI_OPT_IN_SEND_EMAIL: String = "pl_bpi_send_email"

        const val CC_BPI_OPT_IN_START: String = "cc_bpi_optin_start"
        const val CC_BPI_OPT_IN_CONFIRM: String = "cc_bpi_optin_confirm"
        const val CC_BPI_OPT_IN_SEND_EMAIL: String = "cc_bpi_send_email"

        const val FICA_VERIFY_START: String = "fica_verify_start"
        const val FICA_VERIFY_SKIP: String = "fica_verify_skip"

        const val VIEW_ITEM_LIST: String = "view_item_list"
        const val VIEW_ITEM_EVENT: String = "view_item"
        const val SELECT_ITEM_EVENT: String = "select_item"
        const val ADD_TO_CART_PDP: String = "add_to_cart"
        const val VIEW_CART: String = "view_cart"
        const val ADD_TO_WISHLIST: String = "add_to_wishlist"
        const val IN_APP_POP_UP = "in_app_pop_up"
        const val VIEW_PROMOTION: String = "view_promotion"
        const val SELECT_PROMOTION: String = "select_promotion"
        const val IN_STORE_AVAILABILITY: String ="in_store_availability"
        const val REMOVE_FROM_CART: String = "remove_from_cart"
        const val SEARCH: String = "search"
        const val SHARE: String = "share"
        const val SIGN_UP: String = "sign_up"
        const val ADD_SHIPPING_INFO: String = "add_shipping_info"
        const val SCREEN_VIEW_PLP: String = "screen_view"
        const val ADD_PAYMENT_INFO: String = "add_payment_info"
        const val PURCHASE: String = "purchase"
        const val DASH_PREFIX = "dash_"


        //Geolocation
        const val SHOP_DELIVERY_CLICK_COLLECT = "shop_dlvry_clk_cllct"
        const val SHOP_NEW_ADDRESS = "shop_new_adrs"
        const val SHOP_CONFIRM_LOCATION = "shop_cnfrm_lctn"
        const val SHOP_UPDATE_ADDRESS = "shop_updte_addrss"
        const val SHOP_SAVED_PLACES = "shop_svd_plcs"
        const val SHOP_CONFIRM_ADDRESS = "shop_cnfrm_adrs"
        const val SHOP_CONFIRM_DELIVERY_ADDRESS = "shop_cnfrm_delvry_adrs"
        const val SHOP_EDIT_DELIVERY_ADDRESS = "shop_edit_delvry_adrs"
        const val SHOP_CLICK_COLLECT = "shop_clk_cllct"
        const val SHOP_STANDARD_EDIT = "shop_stnd_edt"
        const val SHOP_STANDARD_CONFIRM = "shop_stnd_cnfrm"
        const val SHOP_DELIVERY = "shop_delvry"
        const val SHOP_CLICK_COLLECT_EDIT = "shop_clck_cllct_edt"
        const val SHOP_STANDARD_CLICK_COLLECT_CONFIRM = "shop_stnd_cnfrm"
        const val SHOP_CONFIRM_STORE = "shop_cnfrm_store"
        const val SHOP_EDIT_LOCATION = "shop_edit_locatn"
        const val SET_Location = "set_location"
        const val DELIVERY_MODE ="delivery_mode"


        //GeoLocation_CHECKOUT
        const val CHECKOUT_ADDRESS_DETAILS_HOME = "chckout_adrs_dtls_hme"
        const val CHECKOUT_ADDRESS_DETAILS_OFFICE = "chckout_adrs_dtls_offce"
        const val CHECKOUT_ADDRESS_DETAILS_COMPLEX = "chckout_adrs_dtls_cmplx"
        const val CHECKOUT_ADDRESS_DETAILS_APARTMENT = "chckout_adrs_dtls_aprtmnt"
        const val CHECKOUT_ADDRESS_SAVE_ADDRESS = "chckout_adrs_sve_adrs"


        const val ADDRESS_HOME = "Home"
        const val ADDRESS_OFFICE = "Office"
        const val ADDRESS_COMPLEX_ESTATE = "Complex / Estate"
        const val ADDRESS_APARTMENT = "Apartment"


         /*Dash*/
         const val DASH_DELIVERY_BROWSE_MODE = "set_delivery_browse_mode"
         const val DASH_SWITCH_DELIVERY_MODE = "switch_delivery_mode"
         const val DASH_SWITCH_BROWSE_MODE = "switch_browse_mode"
         const val REFUND = "refund"
         const val DASH_DRIVER_TIP = "dash_driver_tip"
         const val DASH_SELECT_CONTENT = "select_content"

        //checkout voucher or promocode
        const val CHECKOUT = "Checkout"

        //Address Checkout
        const val FORM_START = "form_start"
        const val FORM_COMPLETE = "form_complete"

        /*enhance substitution*/
        const val SUBSTITUTION: String = "substitution"
        const val ADD_SUBSTITUTION: String = "add_substitute"
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
            const val TOGGLE_SELECTED = "toggle_selected"
            const val TIME_SELECTED = "time_selected"
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
            const val REASON: String = "Reason"
            const val STATUS = "status"
            const val STATUS_URL = "status_url"
            const val TRANSACTION_ID = "transaction_id"
            const val PAYMENT_STATUS = "payment_status"

            const val linkDeviceInitiated = "Link device initiated"
            const val linkDeviceSkipped = "Link device request skipped or canceled"
            const val linkDeviceConfirmed = "OTP entered and confirmed to link device"
            const val linkDeviceViewList = "View linked devices"
            const val linkDeviceDelete = "Device unlinked / deleted"

            const val CURRENCY = "currency"
            const val ITEM_LIST_NAME = "item_list_name"
            const val SHOPPING_LIST_NAME = "shopping_list_name"
            const val ITEM_BRAND = "item_brand"
            const val ORDER_TOTAL_VALUE = "order_total"

            const val ITEM_ID = "item_id"
            const val ITEM_NAME = "item_name"
            const val ITEM_RATING = "item_rating"
            const val COUPON = "coupon"
            const val DISCOUNT = "DISCOUNT"
            const val INDEX = "index"
            const val AFFILIATION = "affiliation"
            const val ITEM_VARIANT = "item_variant"
            const val ITEM_CATEGORY = "item_category"
            const val ITEM_PRICE = "price"
            const val CART_TOTAL_VALUE = "cart_total_value"
            const val ITEM_VALUE = "item_value"
            const val CREATIVE_NAME = "creative_name"
            const val PROMOTION_NAME = "promotion_name"
            const val SEARCH_TERM = "search_term"
            const val SEARCH_TYPE = "search_type"
            const val CONTENT_TYPE = "content_type"
            const val SHIPPING_TIER = "shipping_tier"
            const val DELIVERY_DATE = "delivery_date"
            const val CATEGORY_NAME = "category"
            const val SUB_CATEGORY_NAME = "sub_category"
            const val SUB_SUB_CATEGORY_NAME = "sub_sub_category"
            const val MESSAGE_TYPE = "message_type"

            const val DELIVERY_MODE = "delivery_mode"
            const val BROWSE_MODE = "browse_mode"
            const val DASH_TIP = "dash_tip"
            const val REFUND_TYPE = "refund_type"
            const val CONTENT_NAME = "content_name"
            const val CONTENT_SLOT = "content_slot"
            const val BANNER_ENGAGEMENT = "banner_engagement"
            const val BANNER_POSITION = "banner_position"
            const val BANNER_LIST_NAME = "banner_list_name"
            const val FULFILLMENT_FOOD_STORE_KEY_01 = "food_ffstore"
            const val FULFILLMENT_FBH_STORE_KEY_02 = "other02_ffstore"
            const val FULFILLMENT_FBH_STORE_KEY_04 = "other04_ffstore"
            const val FULFILLMENT_FBH_STORE_KEY_07 = "other07_ffstore"
            const val LIQUOR_DELIVERABLE = "liquor_deliverable"

            //Address Checkout
            const val FORM_TYPE = "form_type"
            const val FORM_NAME = "form_name"
            const val FORM_LOCATION = "form_location"

            //checkout vouchers or promo code
            const val STEP = "step"
            const val OPTION = "option"
            const val DELIVERY_TYPE = "delivery_type"

            const val SEARCH_RESULT_COUNT: String = "search_result_count"
            const val LOCATION_ID: String = "location_id"
            const val PRODUCT_NAME: String = "product_name"
        }
    }

    class PropertyValues {
        companion object {
            const val OUT_OF_STOCK_MESSAGE = "out of stock"
            const val NOT_APPLICABLE: String = "N/A"

            // Chanel
            const val ACTION_BRAND_LANDING_PAGE_CATEGORY = "Customer selects a category which takes them to the relevant category page."
            const val ACTION_BRAND_LANDING_PAGE_SUB_CATEGORY = "Customer selects a lower level category which opens a PLP page with all the related items"
            const val ACTION_BRAND_LANDING_PAGE_LOGO_IMAGE = "Customer selects brand image banner and this links back to the brand landing page"

            const val SC_BPI_OPT_IN_START_VALUE = "Store Card Landing - BPI - Opt-in"
            const val SC_BPI_OPT_IN_CONFIRM_VALUE = "Store Card Landing - BPI - Confirm"
            const val SC_BPI_OPT_IN_SEND_EMAIL_VALUE = "Store Card Landing - BPI - Send Email"

            const val CC_BPI_OPT_IN_START_VALUE = "Credit Card Landing - BPI - Opt-in"
            const val CC_BPI_OPT_IN_CONFIRM_VALUE = "Credit Card Landing - BPI - Confirm"
            const val CC_BPI_OPT_IN_SEND_EMAIL_VALUE = "Credit Card Landing - BPI - Send Email"

            const val PL_BPI_OPT_IN_START_VALUE = "Personal Loan Landing - BPI - Opt-in"
            const val PL_BPI_OPT_IN_CONFIRM_VALUE = "Personal Loan Landing - BPI - Confirm"
            const val PL_BPI_OPT_IN_SEND_EMAIL_VALUE = "Personal Loan Landing - BPI - Send Email"
            // Native Checkout
            const val ACTION_VALUE_NATIVE_CHECKOUT_DELIVERY: String = "Customer selects the Delivery option"
            const val ACTION_VALUE_NATIVE_CHECKOUT_COLLECTION: String = "Customer selects the Collections option"
            const val ACTION_VALUE_NATIVE_CHECKOUT_COLLECTION_CHANGE_SUBURB: String =
                "Customer selects to change their province and suburb to display other stores"
            const val ACTION_VALUE_NATIVE_CHECKOUT_CONFIRM_STORE: String = "Customer selects to their store selected"
            const val ACTION_VALUE_NATIVE_CHECKOUT_ADD_NEW_ADDRESS: String =
                "Customer selects to add an address for their delivery option"
            const val DELIVERY_PAGE = "delivery page"
            const val SELECT_TIMESLOT = "select timeslot"
            const val FOOD_SUBSTITUTION = "food substitutions"
            const val CHAT_WITH_SHOPPER = "Chat with shopper"
            const val PHONE_ME = "Phone me"
            const val SUBSTITUTE = "substitute"
            const val NO_THANKS = "no thanks"
            const val NEED_SHOPPING_BAG = "need shopping bag"
            const val IS_THIS_GIFT = "is this a gift?"
            const val SPECIAL_DELIVERY_INSTRUCTION = "special delivery instructions"
            const val ACTION_VALUE_NATIVE_CHECKOUT_EDIT_ADDRESS: String =
                "Customer selects to edit an already added address for their delivery option"
            const val ACTION_VALUE_NATIVE_CHECKOUT_DELETE_ADDRESS: String =
                "Customer selects to Delete an already added address"
            const val ACTION_VALUE_NATIVE_CHECKOUT_CONFIRM_ADDRESS: String =
                "Customer selects to Confirm after selecting a specific Delivery address"
            const val ACTION_VALUE_NATIVE_CHECKOUT_SAVE_ADDRESS: String =
                "Customer confirms the delivery address that was added"
            const val ACTION_VALUE_NATIVE_CHECKOUT_REMOVE_ITEMS: String =
                "Customer changed Delivery address / Collection"
            const val ACTION_VALUE_NATIVE_CHECKOUT_CANCEL_REMOVE_ITEMS: String =
                "Customer changed Delivery address / Collection and does not want items removed."
            const val ACTION_VALUE_NATIVE_CHECKOUT_BAGS_INFO: String = "Customer selects to view additional information on shopping bags"
            const val ACTION_VALUE_NATIVE_CHECKOUT_WREWARDS_SAVING: String =
                "Customer selects the 'Missed WRewards Savings' information button"
            const val ACTION_VALUE_NATIVE_CHECKOUT_WREWARDS_SIGN_UP: String =
                "Customer selects to sign up for WRewards savings on additional information button"
            const val ACTION_VALUE_NATIVE_CHECKOUT_WREWARDS_ADD_CARD: String =
                "Customer selects that they want to add their WRewards card"
            const val ACTION_VALUE_NATIVE_CHECKOUT_DELIVERY_OPTION_PRE_VALUE1: String =
                "Customer selects "
            const val ACTION_VALUE_NATIVE_CHECKOUT_DELIVERY_OPTION_PRE_VALUE2: String =
                " delivery option"


            const val ACTION_VALUE_NATIVE_CHECKOUT_COLLECTION_EDIT_USER_DETAILS: String =
                "Customer selected to edit (change details) of a collector"
            const val ACTION_VALUE_NATIVE_CHECKOUT_COLLECTION_VEHICLE_SELECT: String =
                "Customer selected to enter Vehicle details"
            const val ACTION_VALUE_NATIVE_CHECKOUT_COLLECTION_TAXI_SELECT: String =
                "Customer selected one of the Ride-along options"
            const val ACTION_VALUE_NATIVE_CHECKOUT_COLLECTION_CONFIRM_DETAILS: String =
                "Customer confirms their added Click & Collect details"

            const val CURRENCY_VALUE: String = "ZAR"
            const val INDEX_VALUE: String = "1"
            const val AFFILIATION_VALUE: String = "WWOneApp"
            const val CREATIVE_NAME_VALUE: String = "Product List"
            const val PAYMENT_TYPE_VALUE: String = "Gift Card"
            const val SHIPPING_TIER_VALUE_DASH: String = "Dash"
            const val SHIPPING_TIER_VALUE_CNC: String = "CNC"
            const val SHIPPING_TIER_VALUE_STD: String = "Std"





            //GeoLOcation_SHOP
            const val ACTION_VALUE_SHOP_DELIVERY_CLICK_COLLECT: String = "Customer selects to choose between Standard Delivery or Click and Collect on the shop default page"
            const val ACTION_VALUE_SHOP_NEW_ADDRESS: String = "Customer selects to enter a new address on the Standard Delivery, confirm address screen"
            const val ACTION_VALUE_SHOP_CONFIRM_LOCATION: String = "Customer selects to confirm their current location on the confirm address screen"
            const val ACTION_VALUE_SHOP_UPDATE_ADDRESS: String = "Customer selects and unverified address and the Call to action changes to ‘Update Address’. Customer selects the Call to action button"
            const val ACTION_VALUE_SHOP_SAVED_PLACES: String = "Customer selects the saved places option on the confirm address screen"
            const val ACTION_VALUE_SHOP_CONFIRM_ADDRESS: String = "Customer selects to confirm their address on the map view screen after selecting their address"
            const val ACTION_VALUE_SHOP_CONFIRM_DELIVERY_ADDRESS: String = "Customer has saved addresses, signs in and pop up is displayed, customer selects to ‘Confirm’ their saved address"
            const val ACTION_VALUE_SHOP_EDIT_DELIVERY_ADDRESS : String = "Customer has saved addresses, signs in and pop up is displayed, customer selects to ‘Edit Location' their saved address"
            const val ACTION_VALUE_SHOP_CLICK_COLLECT: String = "Customer is defaulted to the Standard Delivery tab - selects “Click & Collect' at the top"
            const val ACTION_VALUE_SHOP_STANDARD_EDIT: String = "Customer is defaulted to the Standard Delivery tab - selects to ‘Edit’ their address"
            const val ACTION_VALUE_SHOP_STANDARD_CONFIRM: String = "Customer is defaulted to the Standard Delivery tab - selects to Confirm all the details of their selection"
            const val ACTION_VALUE_SHOP_DELIVERY: String = "Customer has selected the Click and Collect option but chooses to select the Delivery option again at the top"
            const val ACTION_VALUE_SHOP_CLICK_COLLECT_EDIT: String = "Customer selects to ‘Edit’ their province / store for Click and Collect"
            const val ACTION_VALUE_SHOP_STANDARD_CLICK_COLLECT_CONFIRM: String = "Customer selects to Confirm all the details of their Click and Collect selection"
            const val ACTION_VALUE_SHOP_CONFIRM_STORE: String = "Customer selects to change / edit their store, selects a new store and selects to ‘Confirm’ their store"
            const val ACTION_VALUE_SHOP_EDIT_LOCATION: String = "Customer has saved store, signs in and pop up is displayed, customer selects to ‘Edit location' in the pop up that is displayed"



            //GeoLOcation_CHECKOUT
            const val ACTION_VALUE_CHECKOUT_ADDRESS_DETAILS_HOME: String = "Customer has selected Checkout and ‘Where are we delivering to’ page is displayed - Customer selects ‘Home’ under ‘My address details’"
            const val ACTION_VALUE_CHECKOUT_ADDRESS_DETAILS_OFFICE: String = "Customer has selected Checkout and ‘Where are we delivering to’ page is displayed - Customer selects ‘Office’ under ‘My address details’"
            const val ACTION_VALUE_CHECKOUT_ADDRESS_DETAILS_COMPLEX: String = "Customer has selected Checkout and ‘Where are we delivering to’ page is displayed - Customer selects ‘Complex / Estate’ under ‘My address details’"
            const val ACTION_VALUE_CHECKOUT_ADDRESS_DETAILS_APARTMENT: String = "Customer has selected Checkout and ‘Where are we delivering to’ page is displayed - Customer selects ‘Apartment’ under ‘My address details’"
            const val ACTION_VALUE_CHECKOUT_ADDRESS_SAVE_ADDRESS: String = "Customer has entered all the relevant details and selects to save their address"

            /*DASH ANALYTICS VALUES*/
            const val DASH_MENU_CLICK: String = "Menu_Click"
            const val DASH_CATEGORY_NAME: String = "Food"
            const val DASH_CANCELLED_ORDER: String = "Cancelled_Order"

            //Address Checkout
            const val BROWSE = "browse"
            const val TAXI = "Taxi"
            const val MY_VEHICLE = "My Vehicle"

            const val STANDARD = "Standard"
            const val CLICK_AND_COLLECT = "Click and collect"
            const val DASH = "Dash"

            //checkout promo or voucher action
            const val VIEW_VOUCHER = "view voucher"
            const val VIEW_WREWARDS_VOUCHERS = "view wrewards vouchers"
            const val ADD_PROMO_CODE = "add promo code"

            //checkout promo or voucher option
            const val VOUCHERS = "vouchers"
            const val ADD_PROMO = "add promo"

            //checkout promo step
            const val BASKET = "basket"

            const val BACK = "back"


        }
    }

    class ScreenNames {
        companion object {
            const val STARTUP: String = "startup"
            const val STARTUP_API_ERROR: String = "startup_api_error"
            const val DEVICE_ROOTED_AT_STARTUP: String = "startup_jailbreak_error"
            const val DEVICE_SIDELOADED_AT_STARTUP: String = "startup_sideloaded_error"
            const val SPLASH_WITHOUT_CTA: String = "splash_without_cta"
            const val SPLASH_WITH_CTA: String = "splash_with_cta"
            const val ONBOARDING_ONE: String = "onboarding_one"
            const val ONBOARDING_TWO: String = "onboarding_two"
            const val ONBOARDING_THREE: String = "onboarding_three"
            const val ONBOARDING_FOUR: String = "onboarding_four"
            const val WTODAY: String = "wtoday_webview"
            const val SHOP_BARCODE: String = "Shop Barcode"
            const val SHOP_BARCODE_MANUAL: String = "Shop Barcode Manual"
            const val SHOP_MAIN_CATEGORIES: String = "Shop Main Categories"
            const val SHOP_SUB_CATEGORIES: String = "Shop Sub Categories"
            const val PRODUCT_SEARCH: String = "Product Search"
            const val PRODUCT_LISTING_PAGE: String = "Product Listing Page"
            const val PRODUCT_SEARCH_REFINEMENT: String = "Product Search Refinement"
            const val PRODUCT_SEARCH_REFINEMENT_CATEGORY: String = "Product Search Refinement Category"
            const val PRODUCT_DETAIL: String = "Product Detail"
            const val PRODUCT_DETAIL_IMAGE_ZOOM: String = "Product Detail Image Zoom"
            const val DELIVERY_LOCATION_HISTORY: String = "Delivery Location History"
            const val DELIVERY_LOCATION_PROVINCE: String = "Delivery Location Province"
            const val DELIVERY_LOCATION_SUBURB: String = "Delivery Location Suburb"
            const val CART_LIST: String = "Cart List"
            const val CART_CHECKOUT: String = "Cart Checkout"
            const val MY_ACCOUNTS: String = "my_accounts"
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
            const val SSO_FORGOT_PASSWORD: String = "SSO Forgot Password"

        }
    }

    enum class EntryPoint(val value: String) {
        DEEP_LINK("Deep_Link"), MANUAL_SEARCH("Manual_Search"), QR_CODE("QR_Codes")
    }

    class CrashlyticsKeys {
        companion object {
            const val PRODUCT_ID: String = "Product ID"
            const val PRODUCT_NAME: String = "Product Name"
            const val DELIVERY_LOCATION: String = "Delivery Location"
            const val HAS_COLOR: String = "Has Color"
            const val HAS_SIZE: String = "Has Size"
            const val STORE_ID: String = "Store ID"
            const val DELIVERY_TYPE: String = "Delivery Type"
            const val IS_USER_AUTHENTICATED: String = "Is User Authenticated"
            const val FULFILLMENT_ID: String = "Fulfillment ID"
            const val PRODUCT_SKU: String = "Product SKU"
            const val SELECTED_SKU_QUANTITY: String = "Selected SKU Quantity"
            const val LAST_KNOWN_LOCATION: String = "Last Known Location"
            const val URL = "URL"
            const val ExceptionResponse = "ExceptionResponse"
            const val ExceptionMessage = "ExceptionMessage"
        }
    }

    class CrashlyticsExceptionName {
        companion object {
            const val PRODUCT_DETAILS_FIND_IN_STORE: String = "Find In-Store Product Details"
            const val PRODUCT_LIST_FIND_IN_STORE: String = "Find In-Store Product List"
        }
    }
}