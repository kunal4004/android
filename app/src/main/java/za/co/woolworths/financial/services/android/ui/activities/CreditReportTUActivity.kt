package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_credit_report_tu.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.adapters.CreditReportTUAdapter
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.openBrowserWithUrl
import za.co.woolworths.financial.services.android.util.Utils


class CreditReportTUActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var creditReportAdapter: CreditReportTUAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit_report_tu)
        Utils.updateStatusBarBackground(this, R.color.bg_e6e6e6)
        setUpActionBar()
        setupRecyclerView()
        register_login_now_btn.setOnClickListener(this)
    }

    private fun setupRecyclerView() {
        creditReportAdapter = CreditReportTUAdapter(this)
        recycler_view_credit_report_details.apply {
            adapter = creditReportAdapter
            layoutManager = LinearLayoutManager(this@CreditReportTUActivity, RecyclerView.VERTICAL, false)
            isNestedScrollingEnabled = false
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbarCreditReport)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    override fun onClick(view: View?) {
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.CREDIT_REPORT_CREDITVIEW_COMPLETE)
        openBrowserWithUrl(WoolworthsApplication.getCreditView()?.transUnionLink, this)
    }
}