package za.co.woolworths.financial.services.android.util.wenum

enum class VocTriggerEvent(val value: String) {
    CHAT_PL_MYACCOUNTS("pl_myaccounts_chat_online"),
    CHAT_PL_TRANSACTION("pl_transaction_chat_online"),
    CHAT_PL_STATEMENT("pl_statement_chat_online"),
    CHAT_PL_PAYMENTOPTIONS("pl_paymentoptions_chat_online"),

    CHAT_SC_MYACCOUNTS("sc_myaccounts_chat_online"),
    CHAT_SC_TRANSACTION("sc_transaction_chat_online"),
    CHAT_SC_STATEMENT("sc_statement_chat_online"),
    CHAT_SC_PAYMENTOPTIONS("sc_paymentoptions_chat_online"),

    CHAT_CC_MYACCOUNTS("cc_myaccounts_chat_online"),
    CHAT_CC_TRANSACTION("cc_transaction_chat_online"),
    CHAT_CC_STATEMENT("cc_statement_chat_online"),
    CHAT_CC_PAYMENTOPTIONS("cc_paymentoptions_chat_online"),

    MYACCOUNTS_ICR_LINK_CONFIRM("myaccounts_icr_link_confirm"),
    MYACCOUNTS_BLOCKCARD_CONFIRM("myaccounts_blockcard_confirm"),

    SHOP_CLICK_COLLECT_CONFIRM("shop_click_collect_confirm"),
    CHCKOUT_CNT_TO_PMNT("chckout_cnt_to_pmnt");
}