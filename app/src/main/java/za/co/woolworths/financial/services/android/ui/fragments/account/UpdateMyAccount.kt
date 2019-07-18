package za.co.woolworths.financial.services.android.ui.fragments.account

import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService

class UpdateMyAccount(private val swipeRefreshLayout: SwipeRefreshLayout?, private val imRefreshAccount: ImageView?) {

    private var mAccountRequest: Call<AccountsResponse>? = null

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

    fun make(forceNetworkUpdate: Boolean, responseListener: RequestListener<AccountsResponse>?) {
        val oneAppService = OneAppService
        mAccountRequest = oneAppService.getAccounts()
        oneAppService.forceNetworkUpdate = forceNetworkUpdate
        mAccountRequest?.enqueue(CompletionHandler(object : RequestListener<AccountsResponse> {
            override fun onSuccess(accountsResponse: AccountsResponse?) {
                responseListener?.onSuccess(accountsResponse)
            }

            override fun onFailure(error: Throwable?) {
                responseListener?.onFailure(error)
            }

        }, AccountsResponse::class.java))
    }

    fun cancelRequest() {
        mAccountRequest?.apply {
            if (isCanceled)
                cancel()
        }
    }

    fun swipeToRefreshAccount(shouldRefreshMyAccount: Boolean) {
        // prevent swipe to refresh to appear when refresh button is pressed
        when (mRefreshAccountType) {
            RefreshAccountType.CLICK_TO_REFRESH -> {
                swipeRefreshLayout?.isRefreshing = false
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
}