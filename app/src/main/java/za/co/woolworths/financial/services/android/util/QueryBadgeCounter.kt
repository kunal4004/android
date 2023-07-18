package za.co.woolworths.financial.services.android.util

import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.CartSummary
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse
import za.co.woolworths.financial.services.android.models.dto.MessageResponse
import za.co.woolworths.financial.services.android.models.dto.VoucherCount
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.extension.cancelRetrofitRequest
import za.co.woolworths.financial.services.android.ui.extension.request
import java.util.Observable

class QueryBadgeCounter : Observable() {
    var cartCount = 0
        private set
    var voucherCount = 0
        private set
    var messageCount = 0
        private set
    var updateAtPosition = 0
        private set

    private var isCartSummaryFailed = true

    private var mGetMessage: Call<MessageResponse>? = null
    private var mGetVoucher: Call<VoucherCount>? = null
    private var mGetCartCount: Call<CartSummaryResponse>? = null

    fun setCartCount(count: Int) {
        cartCount = count
        this.updateAtPosition = BottomNavigationActivity.INDEX_CART
        notifyUpdate()
    }
    fun getCartItemCount(): Int {
        return cartCount
    }

    private fun setVoucherCount(count: Int) {
        voucherCount = count
        this.updateAtPosition = BottomNavigationActivity.INDEX_REWARD
        notifyUpdate()
    }

    private fun setMessageCount(count: Int) {
        messageCount = count
        this.updateAtPosition = BottomNavigationActivity.INDEX_ACCOUNT
        notifyUpdate()
    }

    fun notifyBadgeCounterUpdate(updateAtPosition: Int) {
        this.updateAtPosition = updateAtPosition
        notifyUpdate()
    }

    fun queryMessageCount() {
        if (!isUserAuthenticated) return
        if (!isC2User) return
        mGetMessage = loadMessageCount()
    }

    private val isC2User: Boolean
        get() = SessionUtilities.getInstance().isC2User

    fun queryVoucherCount() {
        if (!isUserAuthenticated) return
        if (!isC2User) return
        mGetVoucher = loadVoucherCount()
    }

    private val isUserAuthenticated: Boolean
        get() = SessionUtilities.getInstance().isUserAuthenticated

    fun queryCartSummaryCount() {
        if (!isUserAuthenticated) return
        mGetCartCount = loadShoppingCartCount()
    }

    fun updateCartSummaryCount() {
        if (!isUserAuthenticated) return
        if(isCartSummaryFailed){
            queryCartSummaryCount()
            return
        }
        this.updateAtPosition = BottomNavigationActivity.INDEX_CART
        notifyUpdate()
    }

    private fun loadVoucherCount(): Call<VoucherCount>? {
        return request(OneAppService().getVouchersCount(), object : IGenericAPILoaderView<Any> {
            override fun onSuccess(response: Any?) {
                (response as? VoucherCount)?.apply {
                        when (httpCode) {
                            200 -> count?.let { count -> setVoucherCount(count) }
                            else -> setVoucherCount(0)
                        }
                }
            }
        })
    }

    private fun loadShoppingCartCount(): Call<CartSummaryResponse>? {

        return GetCartSummary().getCartSummary(object : IResponseListener<CartSummaryResponse> {
            override fun onSuccess(response: CartSummaryResponse?) {
                when (response?.httpCode) {
                    200 -> {
                        response.data.getOrNull(0)?.apply {
                            if (totalItemsCount != null)
                                setCartCount(totalItemsCount)
                        }
                    }
                }
            }
        })
    }

    private fun loadMessageCount(): Call<MessageResponse>? {
        return request(OneAppService().getMessagesResponse(5, 1), object : IGenericAPILoaderView<Any> {
            override fun onSuccess(response: Any?) {
                (response as? MessageResponse)?.unreadCount?.let { unreadCount ->
                    setMessageCount(unreadCount)
                }
            }
        })
    }

    fun clearBadge() {
        setCartCount(0)
        setMessageCount(0)
        setVoucherCount(0)
    }

    fun cancelCounterRequest() {
        cancelRetrofitRequest(mGetMessage)
        cancelRetrofitRequest(mGetVoucher)
        cancelRetrofitRequest(mGetCartCount)
    }

    override fun hasChanged(): Boolean {
        return true
    }

    fun queryBadgeCount() {
        updateAtPosition = 10
        notifyUpdate()
    }

    private fun notifyUpdate() {
        setChanged()
        notifyObservers()
    }

    fun setCartSummaryResponse(cartSummary: CartSummary?, isFailedCartSummary: Boolean = false) {
        cartCount = cartSummary?.totalItemsCount ?: 0
        isCartSummaryFailed = isFailedCartSummary
    }

    companion object {
        private var INSTANCE: QueryBadgeCounter? = null

        // Returns a single instance of this class, creating it if necessary.
        @JvmStatic
        val instance: QueryBadgeCounter
            get() {
                if (INSTANCE == null) {
                    INSTANCE = QueryBadgeCounter()
                }
                return INSTANCE as QueryBadgeCounter
            }
    }
}