package za.co.woolworths.financial.services.android.receivers

interface DashOrderReceiverListener {
    fun updateUnreadMessageCount(unreadMsgCount: Int)
    fun updateLastDashOrder()
}
