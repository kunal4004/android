package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import com.android.volley.VolleyError
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.util.Utils
import kotlinx.android.synthetic.main.absa_statements_activity.*
import kotlinx.android.synthetic.main.empty_state_template.*
import za.co.absa.openbankingapi.woolworths.integration.AbsaBalanceEnquiryFacadeGetAllBalances
import za.co.absa.openbankingapi.woolworths.integration.AbsaGetArchivedStatementListRequest
import za.co.absa.openbankingapi.woolworths.integration.AbsaGetIndividualStatementRequest
import za.co.absa.openbankingapi.woolworths.integration.dto.*
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse
import za.co.woolworths.financial.services.android.ui.adapters.AbsaStatementsAdapter
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import java.net.HttpCookie
import java.util.ArrayList

class AbsaStatementsActivity : AppCompatActivity(), AbsaStatementsAdapter.ActionListners {

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
        AbsaGetArchivedStatementListRequest().make(header, accountNumber, object : AbsaBankingOpenApiResponse.ResponseDelegate<StatementListResponse> {
            override fun onSuccess(response: StatementListResponse?, cookies: MutableList<HttpCookie>?) {
                if (response?.archivedStatementList != null)
                    showStatementsList(response?.archivedStatementList)
                else
                    showErrorView()
            }

            override fun onFailure(errorMessage: String?) {
                showErrorView()
            }

            override fun onFatalError(error: VolleyError?) {
                showErrorView()
            }

        })
    }

    private fun executeGetAllBalances() {
        showProgress()
        AbsaBalanceEnquiryFacadeGetAllBalances().make(nonce, eSessionId, object : AbsaBankingOpenApiResponse.ResponseDelegate<AbsaBalanceEnquiryResponse> {
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
        mErrorHandlerView?.setEmptyStateWithAction(3, R.string.call_now, ErrorHandlerView.ACTION_TYPE.CALL_NOW)
    }

    fun onActionClick() {
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
        getIndivisualStatement(item)
    }

    private fun getIndivisualStatement(archivedStatement: ArchivedStatement) {
        AbsaGetIndividualStatementRequest().make(archivedStatement, object : AbsaBankingOpenApiResponse.ResponseDelegate<AbsaIndividualStatementResponse> {
            override fun onSuccess(response: AbsaIndividualStatementResponse?, cookies: MutableList<HttpCookie>?) {

            }

            override fun onFailure(errorMessage: String?) {
            }

            override fun onFatalError(error: VolleyError?) {
            }

        })
    }
}