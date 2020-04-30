package za.co.woolworths.financial.services.android.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.crashlytics.android.Crashlytics
import kotlinx.android.synthetic.main.wtransactions_activity.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.TransactionHistoryResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService.getAccountTransactionHistory
import za.co.woolworths.financial.services.android.ui.adapters.WTransactionAdapter
import za.co.woolworths.financial.services.android.ui.extension.cancelRetrofitRequest
import za.co.woolworths.financial.services.android.ui.views.actionsheet.AccountsErrorHandlerFragment
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getListOfTransaction
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension


class WTransactionsActivity : AppCompatActivity(), View.OnClickListener {

    var productOfferingId: String? = null
    private var mErrorHandlerView: ErrorHandlerView? = null
    private var mExecuteTransactionRequest: Call<TransactionHistoryResponse>? = null
    private var accountNumber: String? = null
    private var lastPosition = -1
    private var cardType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.updateStatusBarBackground(this)
        setContentView(R.layout.wtransactions_activity)

        intent?.extras?.apply {
            productOfferingId = getString("productOfferingId")
            accountNumber = getString("accountNumber")
            cardType = getString("cardType")

        }

        val woolworthApplication = this@WTransactionsActivity.application as WoolworthsApplication

        mErrorHandlerView = ErrorHandlerView(this, woolworthApplication,
                findViewById(R.id.relEmptyStateHandler),
                findViewById(R.id.imgEmpyStateIcon),
                findViewById(R.id.txtEmptyStateTitle),
                findViewById(R.id.txtEmptyStateDesc),
                findViewById(R.id.no_connection_layout))


        closeTransactionImageButton?.let { closeIcon ->
            AnimationUtilExtension.animateViewPushDown(closeIcon)
            closeIcon.setOnClickListener(this)
        }

        loadTransactionHistory(productOfferingId)

        findViewById<View>(R.id.btnRetry)?.setOnClickListener { _: View? ->
            if (NetworkManager.getInstance().isConnectedToNetwork(this@WTransactionsActivity)) {
                loadTransactionHistory(productOfferingId)
            }
        }
        initInAppChat()
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.TRANSACTIONS)
    }

    private fun loadTransactionHistory(prOfferId: String?) {
        pbTransaction?.visibility = View.VISIBLE
        transactionAsyncAPI(prOfferId)
    }

    private fun transactionAsyncAPI(productOfferingId: String?) {
        mExecuteTransactionRequest = productOfferingId?.let { id -> getAccountTransactionHistory(id) }
        mExecuteTransactionRequest?.enqueue(CompletionHandler(object : IResponseListener<TransactionHistoryResponse> {
            override fun onSuccess(transactionHistoryResponse: TransactionHistoryResponse) {
                dismissProgress()
                if (this@WTransactionsActivity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    when (transactionHistoryResponse.httpCode) {
                        200 -> {
                            if (transactionHistoryResponse.transactions.size > 0) {
                                mErrorHandlerView?.hideEmpyState()
                                setupTransactionRecyclerview(transactionHistoryResponse)
                            } else {
                                transactionRecyclerview?.visibility = View.GONE
                                mErrorHandlerView?.showEmptyState(3)
                            }
                            showChatBubble()
                        }
                        440 -> if (!this@WTransactionsActivity.isFinishing) {
                            SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, transactionHistoryResponse.response.stsParams, this@WTransactionsActivity)
                        }
                        else -> transactionHistoryResponse.response?.desc?.let { desc ->
                            try {
                                val accountsErrorHandlerFragment = AccountsErrorHandlerFragment.newInstance(desc)
                                accountsErrorHandlerFragment.show(supportFragmentManager, AccountsErrorHandlerFragment::class.java.simpleName)
                            } catch (ex: IllegalStateException) {
                                Crashlytics.logException(ex)
                            }
                        }
                    }
                }
            }

            override fun onFailure(error: Throwable) {
                if (this@WTransactionsActivity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    error.message?.let { errorMessage -> networkFailureHandler(errorMessage) }
                }
            }
        }, TransactionHistoryResponse::class.java))
    }

    private fun setupTransactionRecyclerview(transactionHistoryResponse: TransactionHistoryResponse) {
        val transactionsAdapter = WTransactionAdapter(getListOfTransaction(transactionHistoryResponse.transactions))
        val linearLayoutManager: LinearLayoutManager? = LinearLayoutManager(this@WTransactionsActivity)
        linearLayoutManager?.orientation = LinearLayoutManager.VERTICAL
        transactionRecyclerview?.apply {
            layoutManager = linearLayoutManager
            adapter = transactionsAdapter
            visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    private fun dismissProgress() {
        pbTransaction?.visibility = View.GONE
    }

    fun networkFailureHandler(errorMessage: String?) {
        runOnUiThread {
            errorMessage?.let { messageLabel -> mErrorHandlerView?.networkFailureHandler(messageLabel) }
            dismissProgress()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelRetrofitRequest(mExecuteTransactionRequest)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.closeTransactionImageButton -> onBackPressed()
            R.id.chatIcon -> {
                Utils.triggerFireBaseEvents(if (Utils.isOperatingHoursForInAppChat()) FirebaseManagerAnalyticsProperties.MY_ACCOUNTS_CHAT_ONLINE else FirebaseManagerAnalyticsProperties.MY_ACCOUNTS_CHAT_OFFLINE)
                val intent = Intent(this, WChatActivity::class.java)
                intent.putExtra("productOfferingId", productOfferingId)
                intent.putExtra("accountNumber", accountNumber)
                startActivity(intent)
            }
        }
    }

    private fun initInAppChat() {
        if (cardType.equals("CC", ignoreCase = true) && chatIsEnabled()) {
            chatIcon?.apply {
                expand(true)
                setStatusIndicatorIcon(if (Utils.isOperatingHoursForInAppChat()) R.drawable.indicator_online else R.drawable.indicator_offline)
                transactionRecyclerview?.apply {
                    addOnScrollListener(object : RecyclerView.OnScrollListener() {

                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            val layoutManager = layoutManager as? LinearLayoutManager
                            val firstVisibleItem: Int = layoutManager?.findFirstVisibleItemPosition() ?: 0
                            if (lastPosition == firstVisibleItem) {
                                return
                            }
                            if (firstVisibleItem > lastPosition) {
                                if (visibility == View.VISIBLE) collapse(true)
                            } else {
                                if (visibility == View.VISIBLE) expand(true)
                            }
                            lastPosition = firstVisibleItem
                        }
                    })
                }

                setOnClickListener(this@WTransactionsActivity)
            }
        }
    }

    private fun showChatBubble() {
        if (cardType.equals("CC", ignoreCase = true) && chatIsEnabled()) {
            Handler().postDelayed({
                chatIcon?.visibility = View.VISIBLE
                chatIcon?.expand(true)
            }, 100)
        }
    }

    private fun chatIsEnabled(): Boolean {
        return try {
           WoolworthsApplication.getPresenceInAppChat()?.isEnabled ?: false
        } catch (npe: NullPointerException) {
            false
        }
    }
}