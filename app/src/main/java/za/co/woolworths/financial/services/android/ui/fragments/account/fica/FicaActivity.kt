package za.co.woolworths.financial.services.android.ui.fragments.account.fica

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_credit_report_tu.*
import kotlinx.android.synthetic.main.fica_dialog.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

@AndroidEntryPoint
class FicaActivity : AppCompatActivity(), View.OnClickListener {
    private val ficaViewModel: FicaViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fica)
        ficaViewModel.start(intent)
        Utils.updateStatusBarBackground(this, R.color.bg_e6e6e6)
        setUpActionBar()
        setClickListeners()
    }

    private fun setClickListeners() {
        btn_fica_maybe_later.setOnClickListener(this)
        btn_fica_verify.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view) {
            btn_fica_maybe_later -> {
                onBackPressed()
            }
            btn_fica_verify -> {
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.FICA_VERIFY_START,
                    this
                );
                ficaViewModel.handleVerify(this)
            }
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbarCreditReport)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
        KotlinUtils.setTransparentStatusBar(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_search)?.icon?.setTint(resources.getColor(R.color.white))
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.FICA_VERIFY_SKIP,
            this
        );
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            KotlinUtils.RESULT_CODE_CLOSE_VIEW->{
                when(resultCode){
                    RESULT_OK->{
                        finish()
                    }
                }
            }
        }
    }
}