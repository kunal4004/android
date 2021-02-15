package za.co.woolworths.financial.services.android.ui.fragments.account

import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse
import za.co.woolworths.financial.services.android.models.dto.account.Products
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.MockOneAppService
import za.co.woolworths.financial.services.android.models.network.OneAppService

class UpdateMyAccount(private val swipeRefreshLayout: SwipeRefreshLayout?, private val imRefreshAccount: ImageView?) {

    private var mAccountRequest: Call<AccountsResponse>? = null
    lateinit var mAccountResponse: AccountsResponse


    internal var mRetryStoreCardAnimation: RotateAnimation? = null// = new RotateAnimation(ROTATE_FROM, ROTATE_TO);
    internal var mRetryCreditCardAnimation: RotateAnimation? = null// = new RotateAnimation(ROTATE_FROM, ROTATE_TO);
    internal var mRetryPersonalLoanAnimation: RotateAnimation? = null// = new RotateAnimation(ROTATE_FROM, ROTATE_TO);


    companion object {
        internal var mRotateAnimation: RotateAnimation? = null// = new RotateAnimation(ROTATE_FROM, ROTATE_TO);
        private const val ROTATE_ACCOUNT_UPDATE_DURATION = (2 * 500).toLong()
        private var mRefreshAccountType: RefreshAccountType? = RefreshAccountType.NONE
    }

    enum class RefreshAccountType {
        CLICK_TO_REFRESH,
        SWIPE_TO_REFRESH,
        NONE
    }

    fun setRefreshType(refreshAccountType: RefreshAccountType) {
        mRefreshAccountType = refreshAccountType
    }

    fun enableSwipeToRefreshAccount(refreshAccount: Boolean?) {
        refreshAccount?.let { swipeRefreshLayout?.isEnabled = it }
    }

    fun accountUpdateActive() = (mRefreshAccountType !== RefreshAccountType.NONE)

    fun make(forceNetworkUpdate: Boolean, result: (HashMap<Products, Account?>?) -> Unit, failure: (Throwable?) -> Unit) {
        val oneAppService = MockOneAppService
        mAccountRequest = oneAppService.getAccounts()
        MockOneAppService.forceNetworkUpdate = forceNetworkUpdate
        mAccountRequest?.enqueue(CompletionHandler(object : IResponseListener<AccountsResponse> {
            override fun onSuccess(accountsResponse: AccountsResponse?) {
                if (accountsResponse != null) {
                    mAccountResponse = accountsResponse
                }
                val hashMap = getProductAccountHashMap(accountsResponse)

                result(hashMap)
            }

            override fun onFailure(error: Throwable?) {
                failure(error)
            }

        }, AccountsResponse::class.java))
    }

    public fun getProductAccountHashMap(accountsResponse: AccountsResponse?): HashMap<Products, Account?> {
        val productsList = accountsResponse?.products
        val accountsList = accountsResponse?.accountList
        val hashMap = HashMap<Products, Account?>()

        productsList?.forEach { product ->
            hashMap[product] = accountsList?.singleOrNull { account -> account.productGroupCode == product.productGroupCode }
        }
        return hashMap
    }

    fun cancelRequest() {
        mAccountRequest?.apply {
            if (isCanceled)
                cancel()
        }
    }

    fun getProductByProductGroupCode(productGroupCode: String): Products? {
        return mAccountResponse.products?.singleOrNull { account -> account.productGroupCode.equals(productGroupCode, ignoreCase = true) }
    }

    fun getAccountsByProductGroupCode(products: Products?, result: (AccountsResponse?) -> Unit, failure: (Throwable?) -> Unit) {
        val productOfferingId = products?.productOfferingId ?: 0
        mAccountRequest = MockOneAppService.getAccountsByProductOfferingId(productOfferingId.toString())
        mAccountRequest?.enqueue(CompletionHandler(object : IResponseListener<AccountsResponse> {
            override fun onSuccess(response: AccountsResponse?) {
                AccountMasterCache.setAccountsProduct(products,response)
                val account = response?.account
                account?.apply {
                    response.accountList = ArrayList()
                    response.accountList.add(account)
                    response.products = ArrayList()
                    response.products.add(Products(account.productGroupCode, account.productOfferingId))
                }
                result(response)
            }

            override fun onFailure(error: Throwable?) = failure(error)
        }, AccountsResponse::class.java))
    }

    fun swipeToRefreshAccount(shouldRefreshMyAccount: Boolean) {
        // prevent swipe to refresh to appear when refresh button is pressed
        when (mRefreshAccountType) {
            RefreshAccountType.CLICK_TO_REFRESH -> {
                swipeRefreshLayout?.isRefreshing = false
                swipeRefreshLayout?.isEnabled = false
                if (shouldRefreshMyAccount) {
                    mRotateAnimation = RotateAnimation(0f, 360f,
                            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                    mRotateAnimation?.apply {
                        duration = ROTATE_ACCOUNT_UPDATE_DURATION
                        repeatCount = 45
                        repeatCount = Animation.INFINITE
                    }
                    imRefreshAccount?.startAnimation(mRotateAnimation)
                } else {
                    swipeRefreshLayout?.isEnabled = true
                    mRefreshAccountType = RefreshAccountType.NONE
                    mRotateAnimation?.cancel()
                }
            }

            RefreshAccountType.SWIPE_TO_REFRESH -> {
                if (mRotateAnimation != null)
                    mRotateAnimation?.cancel()

                if (shouldRefreshMyAccount) {
                    swipeRefreshLayout?.isRefreshing = true
                } else {
                    mRefreshAccountType = RefreshAccountType.NONE
                    swipeRefreshLayout?.isRefreshing = false
                }
            }
            else -> {
            }
        }
    }


    fun cancelAnimation() {
        mRotateAnimation?.cancel()
    }

    private fun rotateViewAnimation(): RotateAnimation {
        val animation = RotateAnimation(0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        with(animation) {
            duration = ROTATE_ACCOUNT_UPDATE_DURATION
            repeatCount = 45
            repeatCount = Animation.INFINITE
        }
        return animation
    }

    fun retryStoreCardAnimationOnTap(view: View?) {
        mRetryStoreCardAnimation = rotateViewAnimation()
        view?.startAnimation(mRetryStoreCardAnimation)
    }

    fun cancelRetryStoreCardAnimation() = mRetryStoreCardAnimation?.cancel()

    fun retryCreditCardAnimationOnTap(view: View?) {
        mRetryCreditCardAnimation = rotateViewAnimation()
        view?.startAnimation(mRetryCreditCardAnimation)
    }

    fun cancelRetryCreditCardAnimation() = mRetryCreditCardAnimation?.cancel()

    fun retryPersonalLoanAnimationOnTap(view: View?) {
        mRetryPersonalLoanAnimation = rotateViewAnimation()
        view?.startAnimation(mRetryPersonalLoanAnimation)
    }

    fun cancelRetryPersonalLoanAnimation() = mRetryPersonalLoanAnimation?.cancel()

}