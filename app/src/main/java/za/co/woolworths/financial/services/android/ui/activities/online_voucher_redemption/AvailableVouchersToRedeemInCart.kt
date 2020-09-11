package za.co.woolworths.financial.services.android.ui.activities.online_voucher_redemption

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.cart_available_vouchers_to_redeem.*
import kotlinx.android.synthetic.main.credit_card_activation_activity.toolbar
import za.co.woolworths.financial.services.android.ui.adapters.AvailableVouchersToRedeemListAdapter
import za.co.woolworths.financial.services.android.util.Utils

class AvailableVouchersToRedeemInCart : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cart_available_vouchers_to_redeem)
        Utils.updateStatusBarBackground(this)
        actionBar()
        loadVouchersList()
    }

    private fun loadVouchersList() {
        rcvVoucherList?.apply {
            layoutManager = LinearLayoutManager(this@AvailableVouchersToRedeemInCart)
            adapter = AvailableVouchersToRedeemListAdapter()
        }
    }

    private fun actionBar() {
        setSupportActionBar(toolbar)
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
        setResult(Activity.RESULT_CANCELED)
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }
}