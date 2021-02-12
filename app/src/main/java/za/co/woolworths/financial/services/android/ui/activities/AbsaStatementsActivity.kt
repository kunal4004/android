package za.co.woolworths.financial.services.android.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Pair
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.NetworkResponse
import com.android.volley.VolleyError
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_statements_activity.*
import kotlinx.android.synthetic.main.chat_collect_agent_floating_button_layout.*
import kotlinx.android.synthetic.main.empty_state_template.*
import kotlinx.android.synthetic.main.payment_options_activity.*
import kotlinx.android.synthetic.main.store_details_layout_common.*
import za.co.absa.openbankingapi.woolworths.integration.AbsaBalanceEnquiryFacadeGetAllBalances
import za.co.absa.openbankingapi.woolworths.integration.AbsaGetArchivedStatementListRequest
import za.co.absa.openbankingapi.woolworths.integration.AbsaGetIndividualStatementRequest
import za.co.absa.openbankingapi.woolworths.integration.dto.AbsaBalanceEnquiryResponse
import za.co.absa.openbankingapi.woolworths.integration.dto.ArchivedStatement
import za.co.absa.openbankingapi.woolworths.integration.dto.Header
import za.co.absa.openbankingapi.woolworths.integration.dto.StatementListResponse
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.Card
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.adapters.AbsaStatementsAdapter
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatBubbleVisibility
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatExtensionFragment.Companion.ACCOUNTS
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatExtensionFragment.Companion.CARD
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatFloatingActionButtonBubbleView
import za.co.woolworths.financial.services.android.ui.fragments.account.helper.ABSAStatementFirebaseEvent
import za.co.woolworths.financial.services.android.util.*
import java.net.HttpCookie
import java.util.*


class AbsaStatementsActivity : AppCompatActivity(), AbsaStatementsAdapter.ActionListners {

    private var mCreditCardToken: String? = null
    private var chatAccountProductLandingPage: Pair<ApplyNowState, Account>? = null
    private var nonce: String? = null
    private var eSessionId: String? = null
    private var mErrorHandlerView: ErrorHandlerView? = null

    companion object {
        const val NONCE = "NONCE"
        const val E_SESSION_ID = "E_SESSION_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.absa_statements_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()
        if (savedInstanceState == null)
            getBundleArgument()
        initViews()
        showChatBubble()
    }

    private fun actionBar() {
        setSupportActionBar(mToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    private fun getBundleArgument() {
        intent?.extras?.apply {
            nonce = getString(NONCE)
            eSessionId = getString(E_SESSION_ID)
            val accounts: String? = getString(ACCOUNTS, "")
            mCreditCardToken = getString(CARD, "")
            chatAccountProductLandingPage = KotlinUtils.getAccount(accounts)
        }
    }

    fun initViews() {
        mErrorHandlerView = ErrorHandlerView(this, relEmptyStateHandler, imgEmpyStateIcon, txtEmptyStateTitle, txtEmptyStateDesc, btnGoToProduct)
        btnGoToProduct?.setOnClickListener { onActionClick() }
        loadStatements()
    }

    private fun loadStatements() {
        nonce?.let { eSessionId?.let { it1 -> executeGetAllBalances() } }
    }

    fun executeGetArchivedStatement(header: Header, accountNumber: String) {
        KotlinUtils.postOneAppEvent(OneAppEvents.AppScreen.ABSA_GET_ALL_STATEMENTS, OneAppEvents.FeatureName.ABSA)
        AbsaGetArchivedStatementListRequest().make(header, accountNumber, object : AbsaBankingOpenApiResponse.ResponseDelegate<StatementListResponse> {
            override fun onSuccess(response: StatementListResponse?, cookies: MutableList<HttpCookie>?) {
                if (response?.archivedStatementList != null)
                    showStatementsList(response.archivedStatementList)
                else
                    showErrorView()
            }

            override fun onFailure(errorMessage: String?) {
               ABSAStatementFirebaseEvent.network()
                showErrorView()
            }

            override fun onFatalError(error: VolleyError?) {
                showErrorView()
            }

        })
    }

    private fun executeGetAllBalances() {
        showProgress()
        val timestampAsString = Utils.getDate(0);
        AbsaBalanceEnquiryFacadeGetAllBalances().make(nonce, eSessionId, timestampAsString, object : AbsaBankingOpenApiResponse.ResponseDelegate<AbsaBalanceEnquiryResponse> {
            override fun onSuccess(response: AbsaBalanceEnquiryResponse?, cookies: MutableList<HttpCookie>?) {
                response?.apply {
                    executeGetArchivedStatement(this.header, this.accountList[0].number!!)
                }
            }

            override fun onFailure(errorMessage: String?) {
                showErrorView()
            }

            override fun onFatalError(error: VolleyError?) {
                showErrorView()
            }

        })
    }

    fun showStatementsList(archivedStatementList: ArrayList<ArchivedStatement>?) {
        ABSAStatementFirebaseEvent.success()
        archivedStatementList?.let {
            if (it.size > 0) {
                hideProgress()
                rcvStatements.apply {
                    layoutManager = LinearLayoutManager(this@AbsaStatementsActivity, LinearLayoutManager.VERTICAL, false)
                    adapter = AbsaStatementsAdapter(it, this@AbsaStatementsActivity)
                    rcvStatements.visibility = View.VISIBLE
                }
            } else {
                showEmptyView()
            }
        }

    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return false
    }


    private fun showProgress() {
        pbCircular.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        if (this@AbsaStatementsActivity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
            pbCircular.visibility = View.GONE
    }

    fun showErrorView() {
        rcvStatements.visibility = View.GONE
        hideProgress()
        mErrorHandlerView?.setEmptyStateWithAction(8, R.string.retry, ErrorHandlerView.ACTION_TYPE.RETRY)
    }

    private fun showEmptyView() {
        rcvStatements.visibility = View.GONE
        hideProgress()
        mErrorHandlerView?.setEmptyStateWithAction(9, R.string.call_now, ErrorHandlerView.ACTION_TYPE.CALL_NOW)
    }

    private fun onActionClick() {
        when (mErrorHandlerView?.actionType) {
            ErrorHandlerView.ACTION_TYPE.RETRY -> {
                mErrorHandlerView?.hideEmpyState()
                loadStatements()
            }
            ErrorHandlerView.ACTION_TYPE.CALL_NOW -> {

            }
        }
    }

    override fun onViewStatement(item: ArchivedStatement) {
        if (pbCircular.visibility != View.VISIBLE) {
            KotlinUtils.postOneAppEvent(OneAppEvents.AppScreen.ABSA_GET_STATEMENT, OneAppEvents.FeatureName.ABSA)
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.ABSA_CC_VIEW_INDIVIDUAL_STATEMENT)
            getIndividualStatement(item)
        }
    }

    private fun getIndividualStatement(archivedStatement: ArchivedStatement) {
        showProgress()
        AbsaGetIndividualStatementRequest().make(archivedStatement, object : AbsaBankingOpenApiResponse.ResponseDelegate<NetworkResponse> {
            override fun onSuccess(response: NetworkResponse?, cookies: MutableList<HttpCookie>?) {
                if (!this@AbsaStatementsActivity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return
                hideProgress()
                response?.apply {
                    allHeaders?.apply {
                        forEach {
                            //activity is at least partially visible
                            if (it.value.equals("application/pdf", true)) {
                                showTAxInvoice(data, archivedStatement.documentWorkingDate)
                                return
                            }
                        }
                    }
                }
            }

            override fun onFailure(errorMessage: String?) {
                hideProgress()
            }

            override fun onFatalError(error: VolleyError?) {
                hideProgress()
            }

        })
    }

    private fun showTAxInvoice(data: ByteArray?, fileName: String) {
        KotlinUtils.postOneAppEvent(OneAppEvents.AppScreen.ABSA_VIEW_STATEMENT, OneAppEvents.FeatureName.ABSA)
        Intent(this, WPdfViewerActivity::class.java).apply {
            putExtra(WPdfViewerActivity.FILE_NAME, fileName)
            putExtra(WPdfViewerActivity.FILE_VALUE, data)
            putExtra(WPdfViewerActivity.PAGE_TITLE, WFormatter.formatStatementsDate(fileName))
            putExtra(WPdfViewerActivity.GTM_TAG, FirebaseManagerAnalyticsProperties.ABSA_CC_SHARE_STATEMENT)
            startActivity(this)
        }

    }

    private fun showChatBubble() {
        val account = chatAccountProductLandingPage?.second
        val card = Card()
        card.absaCardToken = mCreditCardToken
        card.absaAccountToken = mCreditCardToken
        account?.cards = mutableListOf(card)
        val accountList = account?.let { account -> mutableListOf(account) }
        chatAccountProductLandingPage?.first?.let {
            ChatFloatingActionButtonBubbleView(
                    activity = this@AbsaStatementsActivity,
                    chatBubbleVisibility = ChatBubbleVisibility(accountList, this@AbsaStatementsActivity),
                    floatingActionButton = chatBubbleFloatingButton,
                    applyNowState = it,
                    scrollableView = paymentOptionScrollView)
                    .build()
        }
    }

    private fun chatToCollectionAgent(applyNowState: ApplyNowState, accountList: MutableList<Account>?) {
        ChatFloatingActionButtonBubbleView(
                activity = this@AbsaStatementsActivity,
                chatBubbleVisibility = ChatBubbleVisibility(accountList, this@AbsaStatementsActivity),
                floatingActionButton = chatBubbleFloatingButton,
                applyNowState = applyNowState,
                scrollableView = paymentOptionScrollView)
                .build()
    }
}