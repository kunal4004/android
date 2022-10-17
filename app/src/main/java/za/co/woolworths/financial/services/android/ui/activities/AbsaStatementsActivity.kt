package za.co.woolworths.financial.services.android.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_statements_activity.*
import kotlinx.android.synthetic.main.chat_collect_agent_floating_button_layout.*
import kotlinx.android.synthetic.main.empty_state_template.*
import kotlinx.android.synthetic.main.payment_options_activity.*
import za.co.absa.openbankingapi.woolworths.integration.dto.ArchivedStatement
import za.co.absa.openbankingapi.woolworths.integration.dto.Header
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.Card
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.adapters.AbsaStatementsAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatBubbleVisibility
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFragment.Companion.ACCOUNTS
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFragment.Companion.CARD
import za.co.woolworths.financial.services.android.ui.fragments.account.helper.FirebaseEventDetailManager
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.AbsaApiFailureHandler
import za.co.woolworths.financial.services.android.ui.fragments.integration.viewmodel.AbsaIntegrationViewModel
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.linkDeviceIfNecessary
import za.co.woolworths.financial.services.android.util.wenum.VocTriggerEvent

class AbsaStatementsActivity : AppCompatActivity(), AbsaStatementsAdapter.ActionListners {

    private lateinit var mViewArchivedStatement: ArchivedStatement
    private var mCreditCardToken: String? = null
    private var chatAccountProductLandingPage: Pair<ApplyNowState, Account>? = null
    private var nonce: String? = null
    private var eSessionId: String? = null
    private var mErrorHandlerView: ErrorHandlerView? = null

    private val mViewModel: AbsaIntegrationViewModel by viewModels()

    companion object {
        const val NONCE = "NONCE"
        const val E_SESSION_ID = "E_SESSION_ID"
        var SHOW_VIEW_ABSA_CC_STATEMENT_SCREEN = false
        var VIEW_ABSA_CC_STATEMENT_DETAIL = false
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
        absaResultObserver()
    }

    private fun absaResultObserver() {
        with(mViewModel){
            isLoading.observe(this@AbsaStatementsActivity, { isLoading ->
                when(isLoading){
                    true ->  showProgress()
                    else -> hideProgress()
                }
            })

            absaBalanceEnquiryResponseProperty.observe(this@AbsaStatementsActivity,{ response ->
                val header = response?.header
                val number = response?.accountList?.first()?.number
                executeGetArchivedStatement(header, number)
            })

            archivedStatementResponse.observe(this@AbsaStatementsActivity, { response ->
                FirebaseEventDetailManager.success(FirebaseManagerAnalyticsProperties.ABSA_CC_VIEW_STATEMENTS, this@AbsaStatementsActivity)
                when(response?.archivedStatementList?.isNotEmpty()){
                    true -> {
                        response.archivedStatementList.let { archivedStatementResponse ->
                            rcvStatements?.apply {
                                layoutManager = LinearLayoutManager(
                                    this@AbsaStatementsActivity,
                                    LinearLayoutManager.VERTICAL,
                                    false
                                )
                                adapter = AbsaStatementsAdapter(archivedStatementResponse, this@AbsaStatementsActivity)
                                visibility = View.VISIBLE
                            }
                        }
                    }
                    else -> {
                        rcvStatements?.visibility = View.GONE
                        mErrorHandlerView?.setEmptyStateWithAction(9, R.string.call_now, ErrorHandlerView.ACTION_TYPE.CALL_NOW)
                    }
                }
            })


            failureHandler.observe(this@AbsaStatementsActivity, { failure ->
                when(failure){
                    is AbsaApiFailureHandler.ListStatement.FacadeStatusCodeInvalid,
                    is AbsaApiFailureHandler.ListStatement.ArchiveStatusCodeInvalid -> showErrorView()
                    is AbsaApiFailureHandler.FeatureValidateCardAndPin.LoadPdfError -> KotlinUtils.showGeneralInfoDialog(supportFragmentManager, description = bindString(R.string.absa_statement_error_try_again_later))
                    else -> showErrorView()
                }
            })

            individualStatementResponseProperty.observe(this@AbsaStatementsActivity,{ result ->
                when(result){
                    is ByteArray ->  showTAxInvoice(result, mViewArchivedStatement.documentWorkingDate)
                }
                inProgress(false)
            })
        }

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

    private fun executeGetArchivedStatement(header: Header?, accountNumber: String?) {
        KotlinUtils.postOneAppEvent(OneAppEvents.AppScreen.ABSA_GET_ALL_STATEMENTS, OneAppEvents.FeatureName.ABSA)
        mViewModel.fetchArchivedStatement(header, accountNumber)
    }

    private fun executeGetAllBalances() {
        val timestampAsString = Utils.getDate(0);
        mViewModel.fetchBalanceEnquiryFacadeGetAllBalances(nonce,eSessionId,timestampAsString)
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

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

    private fun showErrorView() {
        rcvStatements?.visibility = View.GONE
        hideProgress()
        mErrorHandlerView?.setEmptyStateWithAction(8, R.string.retry, ErrorHandlerView.ACTION_TYPE.RETRY)
    }

    private fun onActionClick() {
        when (mErrorHandlerView?.actionType) {
            ErrorHandlerView.ACTION_TYPE.RETRY -> {
                mErrorHandlerView?.hideEmpyState()
                loadStatements()
            }
            ErrorHandlerView.ACTION_TYPE.CALL_NOW -> {

            }
            else -> {
                // Nothing
            }
        }
    }

    override fun onViewStatement(item: ArchivedStatement) {
        if (pbCircular.visibility != View.VISIBLE) {
            mViewArchivedStatement = item
            linkDeviceIfNecessary(this, ApplyNowState.GOLD_CREDIT_CARD,
                {
                    VIEW_ABSA_CC_STATEMENT_DETAIL = true
                }
            ) {
                getIndividualStatement(item)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (SHOW_VIEW_ABSA_CC_STATEMENT_SCREEN) {
            SHOW_VIEW_ABSA_CC_STATEMENT_SCREEN = false
            getIndividualStatement(mViewArchivedStatement)
        }
    }

    private fun getIndividualStatement(archivedStatement: ArchivedStatement) {
        KotlinUtils.postOneAppEvent(OneAppEvents.AppScreen.ABSA_GET_STATEMENT, OneAppEvents.FeatureName.ABSA)
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.ABSA_CC_VIEW_INDIVIDUAL_STATEMENT, this)
        mViewModel.fetchIndividualStatement(archivedStatement)
    }

    private fun showTAxInvoice(data: ByteArray?, fileName: String) {
        KotlinUtils.postOneAppEvent(OneAppEvents.AppScreen.ABSA_VIEW_STATEMENT, OneAppEvents.FeatureName.ABSA)
        Intent(this, WPdfViewerActivity::class.java).apply {
            putExtra(WPdfViewerActivity.FILE_NAME, fileName)
            putExtra(WPdfViewerActivity.FILE_VALUE, data)
            putExtra(WPdfViewerActivity.PAGE_TITLE,WFormatter.formatStatementsDate(fileName))
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

        var vocTriggerEvent: VocTriggerEvent? = null
        account?.let {
            vocTriggerEvent = when {
                it.productGroupCode.equals(AccountsProductGroupCode.STORE_CARD.groupCode, ignoreCase = true) -> {
                    VocTriggerEvent.CHAT_SC_STATEMENT
                }
                it.productGroupCode.equals(AccountsProductGroupCode.PERSONAL_LOAN.groupCode, ignoreCase = true) -> {
                    VocTriggerEvent.CHAT_PL_STATEMENT
                }
                else -> {
                    VocTriggerEvent.CHAT_CC_STATEMENT
                }
            }
        }

        chatAccountProductLandingPage?.first?.let {
            ChatFloatingActionButtonBubbleView(
                activity = this@AbsaStatementsActivity,
                chatBubbleVisibility = ChatBubbleVisibility(
                    accountList,
                    this@AbsaStatementsActivity
                ),
                floatingActionButton = chatBubbleFloatingButton,
                applyNowState = it,
                scrollableView = paymentOptionScrollView,
                notificationBadge = badge,
                onlineChatImageViewIndicator = onlineIndicatorImageView,
                vocTriggerEvent = vocTriggerEvent
            )
                .build()
        }
    }

    private fun chatToCollectionAgent(
        applyNowState: ApplyNowState,
        accountList: MutableList<Account>?
    ) {
        ChatFloatingActionButtonBubbleView(
            activity = this@AbsaStatementsActivity,
            chatBubbleVisibility = ChatBubbleVisibility(accountList, this@AbsaStatementsActivity),
            floatingActionButton = chatBubbleFloatingButton,
            applyNowState = applyNowState,
            scrollableView = paymentOptionScrollView,
            notificationBadge = badge,
            onlineChatImageViewIndicator = onlineIndicatorImageView
        ).build()
    }
}